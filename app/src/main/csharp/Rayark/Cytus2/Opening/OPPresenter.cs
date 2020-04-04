namespace Rayark.Cytus2.Opening {
	public class OPPresenter : IRunner {
		IEnumerator IRunner.Run() {
			if (this._isPlaying) {
				yield break;
			}
			this._skipEnterReq = null;
			if (!Cymoe.disableSplash) {
				this._isPlaying = true;
				this._executor.Add(this._MainProcess());
				yield return this._executor.Join();
				this._isPlaying = false;
			}
			yield return this._GoToNextScene();
			yield break;
		}
	}

	private string _GetCurrentTitleSceneName() {
		if (!StoryUtility.IsStoryEnd("story_ending03_true_ending"))
			return "title";
		if (Cymoe.disableTrueEnding) {
			ApplicationContext.PlayerRecord.StoryEventRecord("story_ending03_true_ending").State = 0;
			return "title";
		}
		return "trueEndTitle";
	}
}