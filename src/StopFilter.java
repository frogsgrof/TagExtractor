import java.io.*;
import java.util.HashSet;

/**
 * For scanning text files containing stop words.
 */
public class StopFilter extends BufferedReader {

    HashSet<String> stopWords = new HashSet<>(); // for storing stop words
    static StopFilter stopFilter;

    /**
     * Creates a new instance of StopFilter that immediately scans a file passed as an argument and stores a list of
     * stop words.
     * @param wordFile File containing stop words for this instance to scan.
     * @return an instance of StopFilter
     */
    public static StopFilter createStopFilter(File wordFile) throws FileNotFoundException {
        stopFilter = new StopFilter(wordFile);
        return stopFilter;
    }

    /**
     * Checks whether a word appears in the list of stop words.
     *
     * @param word StringBuilder containing a word
     * @return true if the word is not in the list of stop words
     */
    public boolean isKeyWord(StringBuilder word) {
        return !stopWords.contains(word.toString());
    }

    /**
     * Reads a file and separates it line by line to compile and store a list of stop words.
     * @param wordFile File containing stop words
     */
    private StopFilter(File wordFile) throws FileNotFoundException {
        super(new InputStreamReader(new FileInputStream(wordFile)));

        try {
            StringBuilder word = new StringBuilder();
            while (ready()) {
                char c = (char) read();
                if (Character.isAlphabetic(c))
                    word.append(c);
                else {
                    stopWords.add(word.toString());
                    word.delete(0, word.length());
                }
            }
            close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
