
using UnityEngine;

public class Cymoe {
	public static bool disableSplash {
		get {
			return instance.Call<bool>("getDisableSplash");
		}
	}

	public static bool clickToFlick {
		get {
			return instance.Call<bool>("getClickToFlick");
		}
	}

	public static bool dragToClick {
		get {
			return instance.Call<bool>("getDragToClick");
		}
	}

	public static bool autoPlay {
		get {
			return instance.Call<bool>("getAutoPlay");
		}
	}

	public static bool disableTrueEnding {
		get {
			return instance.Call<bool>("getDisableTrueEnding");
		}
	}

	private static AndroidJavaObject instance = new AndroidJavaClass("com.mivik.cymoe.CymoePreferences").CallStatic<AndroidJavaObject>("getInstance", null);
}