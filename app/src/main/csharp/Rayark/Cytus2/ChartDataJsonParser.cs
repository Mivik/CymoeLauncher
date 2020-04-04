
namespace Rayark.Cytus2 {
	public class ChartDataJsonParser {
		public static ChartDataJson Decrypt(TextAsset asset) {
			ChartDataJson ret;
			if (!Enumerable.SequenceEqual<byte>(Enumerable.Take<byte>(asset.bytes, 4), ChartDataJsonParser.ENCRYPTED_HEADER)) {
				ret = JsonUtility.FromJson<ChartDataJson>(asset.text);
			} else {
				byte[] bytes = ApplicationContext.KeyStore.Decrypt("ASSET", asset.bytes, 4, asset.bytes.Length - 4);
				ret = JsonUtility.FromJson<ChartDataJson>(Encoding.UTF8.GetString(bytes));
			}
			if (Cymoe.clickToFlick) {
				NoteJson[] noteList = ret.note_list;
				for (int i = 0; i < noteList.Length ; i++)
					if (noteList[i].type==0) noteList[i].type = 5;
			}
			if (Cymoe.dragToClick) {
				NoteJson[] noteList = ret.note_list;
				for (int i = 0; i < noteList.Length ; i++) {
					int type = noteList[i].type;
					if (type==3||type==4||type==6||type==7) noteList[i].type = 0;
				}
			}
			return ret;
		}
	}
}