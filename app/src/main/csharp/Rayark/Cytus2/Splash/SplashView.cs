
namespace Rayark.Cytus2.Splash {
	public class SplashView : MonoBehaviour, ISplashView {
		public IEnumerator Enter(){
			yield return null;
			SDKMain.instance.initBugly();
			GameEvent.AddEvent(GameEvent.EVENT_START_1);
			if (!Cymoe.disableSplash) {
				while (SDKMain.instance.CallIsSplash() == 1) {
					yield return this.waitOrSkip();
				}
			}
			SDKMain.instance.CallcloseSplash();
			GameEvent.AddEvent(GameEvent.EVENT_SPLASH_1);
			yield break;
		}
	}
}