
public class RunDatabase {

	public static void main(String[] args) {

		LibraryDatabase db = new LibraryDatabase("./shorterBooks");
//		LibraryDatabase db = new LibraryDatabase("./longerBooks");

		db.queryByWord("fish");
		
		System.out.println();
		
		db.queryByWord("the");
		
		System.out.println();
		
		db.queryByWord("hat");
		
		System.out.println();
		
		String[] words = {"fish", "hat"};
		db.queryByWords(words);
		
		System.out.println();
		
		db.queryByFrequency(40); // for ./shorterBooks
//		db.queryByFrequency(2500); // for ./longerBooks		
		
	}

}
