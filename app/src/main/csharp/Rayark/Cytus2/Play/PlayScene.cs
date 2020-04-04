namespace Rayark.Cytus2.Play {
	public class PlayScene : SceneManager {
		private void _ChangeScene(PlayPresenter.PlayResult result) {
			GlobalData.Singleton().ResultInfo = result.ResultInfo;
			PlayPresenter.EndState endState = result.EndState;
			if (Cymoe.autoPlay)
				endState = PlayPresenter.EndState.Skip;
			if (endState == PlayPresenter.EndState.Finish) {
				SceneControlCenter.Instance.ChangeScene("result");
				return;
			}
			if (endState != PlayPresenter.EndState.Skip)
				return;
			SceneControlCenter.Instance.ChangeScene("songSelect");
		}
	}
}