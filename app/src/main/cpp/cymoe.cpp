
#include <dlopen.h>
#include <cymoe.h>

#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

#include <linux/limits.h>

#include <string.h>
#include <stdio.h>
#include <atomic>

char assmbly_replacement[PATH_MAX];
AAsset *asset;
char root_dir_replacement[PATH_MAX];

namespace Cymoe {
	jclass Class;
	jobject INSTANCE;
	jmethodID methodGetFakeApplicationInfo, methodGetFakePackageInfo, methodGetName;
	jfieldID fieldFakeApplicationInfo, fieldFakePackageInfo;
	inline void init(JNIEnv* env) {
		Class = (jclass) env->NewGlobalRef(env->FindClass("com/mivik/cymoe/Cymoe"));
		INSTANCE = env->NewGlobalRef(env->GetStaticObjectField(
			Class,
			env->GetStaticFieldID(Class, "INSTANCE", "Lcom/mivik/cymoe/Cymoe;"
		)));
		methodGetFakeApplicationInfo = env->GetMethodID(Class, "getFakeApplicationInfo", "()Landroid/content/pm/ApplicationInfo;");
		methodGetFakePackageInfo = env->GetMethodID(Class, "getFakePackageInfo", "()Landroid/content/pm/PackageInfo;");
		jclass clsClass = env->FindClass("java/lang/Class");
		methodGetName = env->GetMethodID(clsClass, "getName", "()Ljava/lang/String;");
	}
};

DEFINE_HOOK(void*, mono_image_open_from_data_with_name, (char* data, guint32 data_len, gboolean need_copy, void* status, gboolean refonly, const char* name)) {
	if (endsWith(name, "Assembly-CSharp.dll")) {
		LOGI("Got Assembly-CSharp, replacing it");
		do {
			if (asset==nullptr) {
				LOGE("Asset is null");
				break;
			}
			off_t len = AAsset_getLength(asset);
			if (len<0) {
				LOGE("Length < 0");
				break;
			}
			LOGI("File length: %lu\n", len);
			char *file_data = new char[len];
			if (AAsset_read(asset, file_data, len)<0) {
				LOGE("Failed to read");
				delete[] file_data;
				break;
			}
			name = assmbly_replacement;
			data = file_data;
			data_len = len;
			LOGI("Replaced successfully");
		} while (false);
	}
	return mono_image_open_from_data_with_name_old(data, data_len, need_copy, status, refonly, name);
}

DEFINE_HOOK(void, mono_assembly_setrootdir, (const char* root_dir)) {
	mono_assembly_setrootdir_old(root_dir_replacement);
}

DEFINE_HOOK(void, mono_set_assemblies_path_null_separated, (const char* path)) {
	mono_set_assemblies_path_null_separated_old(root_dir_replacement);
}

DEFINE_HOOK(void*, __loader_dlopen, (const char* filename, int flags, const void* caller_addr)) {
	void *image = __loader_dlopen_old(filename, flags, caller_addr);
	if (endsWith(filename, "libmonobdwgc-2.0.so")) {
		LOGI("Got you! libmono!");
		void* mono_image_open_from_data_with_name = dlsym(image, "mono_image_open_from_data_with_name");
		HOOK_FUNCTION_DYNAMIC(mono_image_open_from_data_with_name);
		void* mono_assembly_setrootdir = dlsym(image, "mono_assembly_setrootdir");
		HOOK_FUNCTION_DYNAMIC(mono_assembly_setrootdir);
		void* mono_set_assemblies_path_null_separated = dlsym(image, "mono_set_assemblies_path_null_separated");
		HOOK_FUNCTION_DYNAMIC(mono_set_assemblies_path_null_separated);
	}
	return image;
}

// 当 360加固 获取 getApplicationInfo/getPackageInfo 的 methodID 时，我们对其进行替换。但由于参数不一样，我们在下面 Call 的时候还要判断一下
DEFINE_HOOK(jmethodID, GetMethodID, (JNIEnv* env, jclass clz, const char* name, const char* signature)) {
	if (!strcmp(signature, "(Ljava/lang/String;I)Landroid/content/pm/ApplicationInfo;")) return Cymoe::methodGetFakeApplicationInfo;
	if (!strcmp(signature, "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;")) return Cymoe::methodGetFakePackageInfo;
	// LOGI("Not in consideration: %s", signature);
	return GetMethodID_old(env, clz, name, signature);
}

DEFINE_HOOK(jobject, CallObjectMethodV, (JNIEnv* env, jobject obj, jmethodID methodID, va_list args)) {
	// LOGI("CallObjectMethodV: %p", methodID);
	if (methodID==Cymoe::methodGetFakeApplicationInfo||methodID==Cymoe::methodGetFakePackageInfo) {
		// 为什么要这样做呢？首先我们要明确我们没法动态构造一个 va_list（如果可以请告诉我），因此我们只能用到 CallObjectMethod
		// 但实际上 CallObjectMethod 是间接地调用了 CallObjectMethodV，如果直接调用 CallObjectMethod 会死循环。
		// 因此我们 CallObjectMethod 时把 obj 传为 nullptr，这样再做个判断就可以了。
		if (obj==nullptr) return CallObjectMethodV_old(env, Cymoe::INSTANCE, methodID, args);
		return env->CallObjectMethod(nullptr, methodID);
	}
	jobject ret = CallObjectMethodV_old(env, obj, methodID, args);
	return ret;
}

static std::atomic_flag hooked = ATOMIC_FLAG_INIT;

DEFINE_HOOK(jint, GetEnv, (JavaVM* vm, JNIEnv** env, int version)) {
	jint ret = GetEnv_old(vm, env, version);
	// 这些方法都只 hook 一次，否则会死循环
	if (!hooked.test_and_set()) {
		JNINativeInterface* functions = const_cast<JNINativeInterface*>((*env)->functions);
		void* GetMethodID = (void*) functions->GetMethodID;
		HOOK_FUNCTION_DYNAMIC(GetMethodID);
		void* CallObjectMethodV = (void*) functions->CallObjectMethodV;
		HOOK_FUNCTION_DYNAMIC(CallObjectMethodV);
	}
	return ret;
}

CYMOE_EXPORT void Java_com_mivik_cymoe_Cymoe_nativeInitialize(ARG_STATIC, jobject assetManagerObj, jstring apkPath) {
	if (apkPath==nullptr) return;
	AAssetManager *asset_manager = AAssetManager_fromJava(env, assetManagerObj);
	asset = AAssetManager_open(AAssetManager_fromJava(env, assetManagerObj), "Assembly-CSharp.dll", AASSET_MODE_STREAMING);
	const char* chars  = env->GetStringUTFChars(apkPath, nullptr);
	strncpy(assmbly_replacement, chars, PATH_MAX);
	snprintf(assmbly_replacement, PATH_MAX, "%s/assets/Assembly-CSharp.dll", chars);
	snprintf(root_dir_replacement, PATH_MAX, "%s/assets/bin/Data/Managed", chars);
	env->ReleaseStringUTFChars(apkPath, chars);
}

CYMOE_EXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	LOGV("Cymoe native loaded");
	JNIEnv *env;
	if (vm->GetEnv((void**)&env, JNI_VERSION_1_6)!=JNI_OK) return -1;
	if (env==nullptr) return -1;

	Cymoe::init(env);
	ndk_init(env);
	void* image = ndk_dlopen("libdl.so", RTLD_LAZY);
	void* __loader_dlopen = ndk_dlsym(image, "__loader_dlopen");
	HOOK_FUNCTION_DYNAMIC(__loader_dlopen);
	image = ndk_dlopen("libart.so", RTLD_LAZY);
	void* GetEnv = ndk_dlsym(image, "_ZN3art9JavaVMExt12HandleGetEnvEPPvi");
	HOOK_FUNCTION_DYNAMIC(GetEnv);
	return JNI_VERSION_1_6;
}