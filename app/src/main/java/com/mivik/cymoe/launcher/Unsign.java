package com.mivik.cymoe.launcher;

import android.content.pm.PackageInfo;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 一个简单的工具类，用于运行时替换当前应用的签名。
 * 对读取 apk 验证签名的方式无效。
 */
public final class Unsign implements InvocationHandler {
	public static final Signature SIGNATURE;
	private final Object O;

	static {
		SIGNATURE = new Signature(Base64.decode(
				"MIIB6zCCAVSgAwIBAgIEVUTilDANBgkqhkiG9w0BAQUFADA5MTcwNQYDVQQKDC5DaGVuZ2R1IExvbmdZdWFuIE5ldHdvcmsgVGVjaG5vbG9naWVzIENvLiwgTHRkMCAXDTE1MDQxNDAyMjkzMVoYDzIwNjUwNDAxMDIyOTMxWjA5MTcwNQYDVQQKDC5DaGVuZ2R1IExvbmdZdWFuIE5ldHdvcmsgVGVjaG5vbG9naWVzIENvLiwgTHRkMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCVOMxhPrN6auQps9VMpwIUDvM1zZQ8R/xtFOs426z2aMGXpj8oQsJ8tZ50Yntd3gJhkbM+bAr+oJ5p/Ffod+CR3Fil8zRi8fsWr5a7r+CHaH7MCtqEODq4Ahgv9YxqjWC9AXw4FqjEDHOBMoATXD/fJkQQu5Ih2PJVSGnoWsxl0wIDAQABMA0GCSqGSIb3DQEBBQUAA4GBAJS7vn+xd0YxrR1SCff4CiWNpRGNpgYxO+So+Z368GilRv1GyMerCOBsDNRE1GsCYp2aq/ioaBHJC6S+y3S9DsymCLaouCIAv1mU1LRzpPChXdZsnpUkAh7aoFB1G9MiRSmWWwuleY+Q06EUGe5+xsqTRgIwIiH+e3qmuKZabVxm",
				Base64.NO_WRAP
		));
	}

	private Unsign(Object origin) {
		this.O = origin;
	}

	public static void hook() {
		try {
			Field field = Class.forName("android.app.ActivityThread").getDeclaredField("sPackageManager");
			field.setAccessible(true);
			final Class<?> packageManagerClass = Class.forName("android.content.pm.IPackageManager");
			field.set(null, Proxy.newProxyInstance(packageManagerClass.getClassLoader(), new Class<?>[]{packageManagerClass}, new Unsign(field.get(null))));
		} catch (Throwable t) {
			Log.e("Unsign", "Failed to hook", t);
		}
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object ret = method.invoke(O, args);
		if (method.getName().equals("getPackageInfo")) {
			PackageInfo info = (PackageInfo) ret;
			if (info == null) return null;
			if (info.signatures != null) info.signatures[0] = SIGNATURE;
		}
		return ret;
	}
}