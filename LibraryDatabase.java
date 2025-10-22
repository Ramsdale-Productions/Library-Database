import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A class that builds a data structure of words from books, and provides methods for querying that data
 * 
 * @author asauppe
 * @editor pramsdale
 */
public class LibraryDatabase {
	private ArrayList<Book> books;
	private HashSet<String> filter;
	
	/**
	 * The constructor method accepts a valid subdirectory of the working directory of this project,
	 * and is responsible for reading in and processing each book in the directory, storing information
	 * about the title, author, and the words (and their quantities) in the book.
	 * 
	 * @param folder A String of the subdirectory of the working directory that contains the books to be processed
	 */
	public LibraryDatabase(String folder) {
		// TODO
		books = new ArrayList<>();
		loadBannedWords();
		File dir = new File(folder);
		File[] files = dir.listFiles();
		for (File file : files) {
			Stream<String> bookFirst = null;
			Stream<String> bookRest = null;
			try {
				bookFirst = Files.lines(file.toPath());
				bookRest = Files.lines(file.toPath());
			} catch (IOException e) {
					System.err.println("ERROR: Something went wrong");
					System.exit(1);
			}
					
				String[] firstLine = bookFirst
						.findFirst()					// Only takes the first line
						.get()							// returns the first line as a string
			//			.replaceAll("\"", "")			// gets rid of the quotation marks in the title
						.split(";"); 					// separates the title and author into two different strings
					
				String title = firstLine[0];
				String author = firstLine[1];
					
				List<String> lines = bookRest
						.skip(2)						// skip the first 2 lines
						.map(String::toLowerCase) 		// makes every word lower case
						.collect(Collectors.toList());  // make the stream a list that is iterable
					
				HashMap<String, Integer> wordCount = new HashMap<>(); // HashMap that stores the words and how many times they occur
				for (String line : lines ) {
					if (line.strip().isEmpty()) { 		// skips over blank lines
						continue;
					}
					String[] separate = line.replaceAll("[.,;!?]", "").replaceAll("-", " ").split("\\s+");
					for (String word : separate) {
						if (filter.contains(word)) {	// skips current word if in filter list
							continue;
						}
						wordCount.merge(word, 1, ((currValue, _) -> currValue + 1));
					}
				}
				books.add(new Book(title, author, wordCount));
		}
		
	}
	// helper method to get list of words that need to be filtered
	private void loadBannedWords() {
		Stream<String> list;
		filter = new HashSet<>();
		try {
			list = Files.lines(Paths.get("wordsToFilter.txt"));
			list.forEach(ban -> {filter.add(ban);});
		} catch(IOException e) {
			System.err.println("ERROR: wordsToFilter.txt couldn't be found");
			System.exit(1);
		}
	}
	
	
	
	/**
	 * Accepts a single word as an argument, and prints out the books that word is found in in the following format:
	 * 
	 * Searching for "<word>"
	 * 	   <book>: <frequencyInThatBook>
	 *     <book>: <frequencyInThatBook>
	 *     
	 * Books should be ordered with the book where the word has the highest frequency first, with the lowest frequency last.
	 * 
	 * If a word is not found in any book, or is a word that was filtered, the following should be printed:
	 * 
	 * "<word>" is not found in the database
	 * 
	 * @param word
	 */
	public void queryByWord(String word) {
		// TODO
		ArrayList<Book> sortedBooks = new ArrayList<>();
		if (notInList(word)) {
			System.out.printf("\"%s\" is not found in the database\n", word);
		} else {
			for (Book book : books) {
				if (book.words.containsKey(word)) {
					sortedBooks.add(book);
				}
			}
			sortedBooks.sort((book1, book2) -> book2.wordFrequency(word) - book1.wordFrequency(word)); // sorts the books from most frequency to least
			System.out.printf("Searching for \"%s\"\n", word);
			for (Book book : sortedBooks) {
				System.out.printf("%s: %d\n", book.getFullTitle(), book.wordFrequency(word));
			}
		}	
	}
	
		
	// helper method to determine if given word is in every book
	private boolean notInList(String word) {
		int missingCount = 0;
		for (Book book : books) {
			if (!book.words.containsKey(word)) {
				missingCount++;
				continue;
			} 	/*
				 * else { sortedBooks.add(book); }
				 */
		}
	//	sortedBooks.sort((book1, book2) -> book2.wordFrequency(word) - book1.wordFrequency(word)); // sorts the books with the most frequency of given word to the lowest
		return missingCount == books.size();
		
	}
	
	/**
	 * Accepts an array of words as an argument, and prints out the books those word(s) are found in in the following format:
	 * 
	 * Searching for book that contain all of the following words: "<word0>" "<word1>" ... "<wordN>"
	 * 	   <book>: <frequencyOfAllWordsInThatBook>
	 *     <book>: <frequencyOfAllWordsInThatBook>
	 *     
	 * Books should be ordered with the book where the word has the highest frequency of all words first, with the lowest frequency last.
	 * 
	 * If no books is found that contains any of the words, the following should be printed:
	 * 
	 * No book contains all of the following words: "<word0>" "<word1>" ... "<wordN>"
	 * 
	 * @param word
	 */
	public void queryByWords(String[] words) {
		// TODO
		
		if (arrayInBooks(words)) {
			String noBooks = "No book contains all of the following words:";
			for (String word : words) {
				noBooks = noBooks.concat(" \"" + word + "\"");
			}
			System.out.println(noBooks);
		} else {
			String print = "Searching for books that contain all of the following words:";
			for (String word : words) {
				print = print.concat(" \"" + word + "\"");
			}
			System.out.println(print);
			HashMap<Book, Integer> bookMap = new HashMap<>();
			int totalFrequency;
			boolean containsAll;
			for (Book book : books) {
				totalFrequency = 0;
				containsAll = true;
				for (String word : words) {
					if (book.words.containsKey(word)) {
						totalFrequency += book.wordFrequency(word);
					} else {
						containsAll = false;
					}
				}
				if (containsAll) {
					bookMap.put(book, totalFrequency);
				}
			}
			ArrayList<Map.Entry<Book, Integer>> sortedBooks = new ArrayList<>();
			for (Map.Entry<Book, Integer> map : bookMap.entrySet() ) {
				sortedBooks.add(map);
			}
			sortedBooks.sort((entry1, entry2) -> entry2.getValue() - entry1.getValue());
			for (Map.Entry<Book, Integer> map : sortedBooks) {
				System.out.printf("%s: %d\n", map.getKey().getFullTitle(), map.getValue());
			}
		}
		
		
	}
	
	// helper method to see if given words are in any books
	private boolean arrayInBooks(String[] words) {
		int missingCount = 0;
		for (String word : words) {
			if (notInList(word)) {
				missingCount++;
				continue;
			} else {
				break;
			}
		}
		return missingCount == words.length;
	}
	
	

	/**
	 * Accepts an int for frequency minimum, and prints out words that appear across all books at least that many times.
	 * Words that meet that minimum across all books should be printed as follows:
	 * 
	 * Searching for words that appear <frequency> or more times across books
	 *     "<word>" appears <count> times across the following books: <title> by <author> (<freqInThatBook>), <title> by <author> (<freqInThatBook>), ..., <title> by <author> (<freqInThatBook>)
	 *     "<word>" appears <count> times across the following books: <title> by <author> (<freqInThatBook>), <title> by <author> (<freqInThatBook>), ..., <title> by <author> (<freqInThatBook>)
	 *     
	 * Words should be ordered in descending order by their number of appearances across books, and the books should be ordered in descending order by their number of appearances in each book.
	 * If no words meet that minimum, the following should be printed:
	 * 
	 * No words appear <frequency> or more times across books.
	 * 
	 * @param frequency The minimum number of times a word must appear across all books to be printed.
	 */
	public void queryByFrequency(int frequency) {
		// TODO
		int count = 0;
		boolean hasElements;
		HashMap<String, Integer> totalFrequency = new HashMap<>();
		HashMap<String,  ArrayList<Book>> bookFrequency = new HashMap<>();
		for (Book book : books) {
			for (Map.Entry<String, Integer> map : book.getWords().entrySet()) {
				totalFrequency.merge(map.getKey(), map.getValue(), (oldV, newV) -> oldV + newV);
				bookFrequency.merge(map.getKey(), new ArrayList<>(List.of(book)), (oldArr, newArr) -> {oldArr.addAll(newArr); return oldArr;});	
			}
		}
		
		HashMap<String, Integer> totalFiltered = new HashMap<>(); 
		HashMap<String, ArrayList<Book>> bookFiltered = new HashMap<>();
		
		for (String word : totalFrequency.keySet()) {
			int total = totalFrequency.get(word);
			if (totalFrequency.get(word) >= frequency) {
				totalFiltered.put(word, totalFrequency.get(word));
				bookFiltered.put(word,  bookFrequency.get(word));
			}
		}
		
		if (totalFiltered.isEmpty()) {
			System.out.printf("No words appear %d or more times across books.", frequency);
		} else {
			System.out.printf("Searching for words that appear %d or more times across books\n", frequency);
			ArrayList<Map.Entry<String, Integer>> totalSorted = new ArrayList<>(totalFiltered.entrySet());
				ArrayList<Map.Entry<String, ArrayList<Book>>> bookSorted = new ArrayList<>(bookFiltered.entrySet());
			totalSorted.sort((map1, map2) -> map2.getValue() - map1.getValue());
			
			// sort the book lists in bookSorted
			for(Map.Entry<String, ArrayList<Book>> map : bookSorted) {
				map.getValue().sort((book1, book2) -> book2.wordFrequency(map.getKey()) - book1.wordFrequency(map.getKey()));
			}
			
			// print
			for (Map.Entry<String, Integer> totalMap : totalSorted) {
				System.out.printf("\"%s\" appears %d times across the following books: ", totalMap.getKey(), totalMap.getValue());
				for (Map.Entry<String, ArrayList<Book>> bookMap : bookSorted) {
					if (totalMap.getKey().equals(bookMap.getKey())) {
						for (Book book : bookMap.getValue()) {
							System.out.printf("%s (%d), ", book.getFullTitle(), book.wordFrequency(bookMap.getKey()));
						}
					}
				}
				System.out.println();
			}
		}
	}
	
	// Inner class to make keeping track of every book easier
	private class Book {
		private String author;
		private String title;
		private HashMap<String, Integer> words;
		private HashMap<Integer, ArrayList<String>> reverseWords;
		
		// Constructor method for Books inner class
		public Book(String title, String author, HashMap<String, Integer> words) {
			this.title = title;
			this.author = author;
			this.words = words;
			this.reverseWords = reverseWords(words);
		}
		
		public String getAuthor() { 
			return this.author;
		}
		
		public String getTitle() {
			return this.title;
		}
		
		public String getFullTitle() {
			String string = getTitle() + " by " + getAuthor();
			return string;
		}
		
		public HashMap<String, Integer> getWords() {
			return this.words;
		}
		
		public HashMap<Integer, ArrayList<String>> getReverse() {
			return this.reverseWords;
		}
		
		// frequency of given word in book
		public int wordFrequency(String word) {
			return words.get(word);
		}
		
		// reverse HashMap of this objects HashMap
		private HashMap<Integer, ArrayList<String>> reverseWords(HashMap<String, Integer> words) {
			HashMap<Integer, ArrayList<String>> reverse = new HashMap<>();
			
			for (String word : words.keySet()) {
				reverse.merge(words.get(word), new ArrayList<>(List.of(word)), (oldArr, newArr) -> {oldArr.addAll(newArr); return oldArr;});
			}
			
			return reverse;
		}
		
		
	}

}
