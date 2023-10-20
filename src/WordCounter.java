import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.*;
import java.util.*;

/**
 * Scans the target file identified by {@link MainView} and counts keywords.
 */
public class WordCounter extends BufferedReader {

    // for holding keywords and their counts as the scan is taking place
    final HashMap<String, Integer> wordMap = new HashMap<>();

    // holds the keywords and their counts post-scan, for the purpose of sorting
    ArrayList<Map.Entry<String, Integer>> wordList;

    static WordCounter wordCounter; // stores the total number of words
    int maxWordLen = -1, // stores the length of the longest word
            maxWordCountDigits; // stores the number of digits of the largest word-count
    int characterCount = 0; // stores the total number of characters in the file scanned
    int totalWords = 0; // stores the total number of words in this file, including both keywords and stop words
    int totalKeywords = 0; // stores the total number of keywords identified
    int totalStopWords = 0; // stores the total number of stop words encountered

    /**
     * Creates a new instance of WordCounter.
     * @param target file for this to scan
     * @param stopFilter StopFilter instance to use for filtering out stop words
     * @return an instance of WordCounter
     */
    public static WordCounter createWordCounter(File target, StopFilter stopFilter) throws FileNotFoundException {
        wordCounter = new WordCounter(target, stopFilter);
        return wordCounter;
    }

    /**
     * Creates a JPanel displaying the top 10 most frequent keywords along with their counts, to make up part of the
     * larger scan results panel.
     * @return Jpanel
     */
    public JPanel createTop10View() {
        if (wordList == null) return null;

        StringBuilder text = new StringBuilder();
        for (int i = 0; i < wordList.size() && i < 10; i++) {
            text.append(String.format("%" + maxWordLen + "s   %" + maxWordCountDigits + "d%n",
                    wordList.get(i).getKey(), wordList.get(i).getValue()));
        }
        text.deleteCharAt(text.length() - 1);

        JTextArea textArea = new JTextArea(text.toString());
        textArea.setFont(GUI_Util.SMALL_PLAIN);
        textArea.setForeground(Color.WHITE);
        textArea.setOpaque(false);

        JPanel pnl = new JPanel();
        pnl.setOpaque(false);
        TitledBorder top10Border = BorderFactory.createTitledBorder(BorderFactory.createCompoundBorder
                        (BorderFactory.createEmptyBorder(10, 10, 10, 10),
                                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true)),
                "Top 10", TitledBorder.CENTER, TitledBorder.TOP);
        top10Border.setTitleFont(GUI_Util.MED_BOLD);
        top10Border.setTitleColor(Color.WHITE);
        pnl.setBorder(top10Border);

        pnl.add(textArea);
        return pnl;
    }

    /**
     * Creates a JPanel displaying every keyword and their respective counts, to make up part of the larger scan results
     * panel.
     * @return JPanel
     */
    public JScrollPane createFullListView() {
        if (wordList == null) return null;

        StringBuilder text = getKeywordsAsText();

        JTextArea textArea = new JTextArea(text.toString());
        textArea.setFont(GUI_Util.SMALL_PLAIN);
        textArea.setForeground(Color.WHITE);
        textArea.setOpaque(false);

        TitledBorder fullListBorder = BorderFactory.createTitledBorder(BorderFactory.createCompoundBorder
                        (BorderFactory.createEmptyBorder(10, 10, 10, 10),
                                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2, true)),
                "All Keywords", TitledBorder.CENTER, TitledBorder.TOP);
        fullListBorder.setTitleFont(GUI_Util.MED_BOLD);
        fullListBorder.setTitleColor(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(fullListBorder);

        return scrollPane;
    }

    /**
     * Uses a {@link BufferedWriter} instance to write the results of the last scan into a text file.
     * @param writer BufferedWriter object
     */
    public void writeFile(BufferedWriter writer) {
        try {
            writer.write(getKeywordsAsText().toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getTotalChars() {
        return characterCount;
    }

    public int getTotalWords() {
        return totalWords;
    }

    public int getTotalKeywords() {
        return totalKeywords;
    }

    public int getTotalStopWords() {
        return totalStopWords;
    }

    /**
     * Creates an instance of WordCounter that immediately scans the file passed as an argument.
     * @param target file to scan for tags
     * @param stopFilter StopFilter object for filtering out stop words
     */
    private WordCounter(File target, StopFilter stopFilter) throws FileNotFoundException {
        super(new InputStreamReader(new BufferedInputStream(new FileInputStream(target))));

        try {
            // if the target file happens to be Dracula, skips over the beginning part that contains copyright info
            if (Objects.equals(target, MainView.DRACULA) && ready()) {
                //noinspection ResultOfMethodCallIgnored
                skip(2461);
            }

            StringBuilder word = new StringBuilder();
            while (ready()) {
                characterCount++; // increments character count
                char c = (char) read(); // reads character

                // if the character is a letter, converts it to lowercase and adds it to the current word
                if (Character.isAlphabetic(c))
                    word.append(Character.toLowerCase(c));

                // if the character is not a letter, the current word must be over
                else if (!word.isEmpty()) {
                    totalWords++; // increments the total words counter

                    /*
                    Checks if the word is longer than one character, because there should be no keywords that are
                    only one character long. If the character is more than one character long, checks if it qualifies
                    as a stop word.
                     */
                    if (word.length() > 1 && stopFilter.isKeyWord(word)) {
                        totalKeywords++; // increments total keywords count
                        addToWordCount(word); // adds the word to the hashmap
                    } else totalStopWords++; // if the word is a stopword, increments the stop word count

                    // clears the current word
                    word.delete(0, word.length());
                }
            }
            close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // creates an ArrayList of Map entries to effectively sort the map
        wordList = new ArrayList<>(wordMap.entrySet());

        /*
        Sorts them using an anonymous comparator that checks each keyword's count. This comparator technically
        sorts in "reverse" order, so that the list goes from largest to smallest.
         */
        wordList.sort((o1, o2) -> {
            if (o1.getValue().compareTo(o2.getValue()) > 0) return o2.getValue() - o1.getValue();
            return o2.getValue().compareTo(o1.getValue());
        });

        /*
        Takes the value (count) of the first item in the list and determines the number of digits it contains. Does
        this by dividing it by 10, then 100, then 1000, and so on and so forth until the result is zero.
         */
        int highestCount = wordList.get(0).getValue();
        int i = 0;
        while (highestCount > 0) highestCount = wordList.get(0).getValue() / (int) Math.pow(10, ++i);
        maxWordCountDigits = i;
    }

    /**
     * Adds a keyword to {@link #wordMap}.
     * @param word word String to add to map
     */
    private void addToWordCount(StringBuilder word) {
        String s = word.toString();
        if (wordMap.containsKey(s)) {
            wordMap.put(s, wordMap.get(s) + 1);
        }
        else {
            wordMap.put(s, 1);
        }
        if (word.length() > maxWordLen) maxWordLen = word.length();
    }

    /**
     * Iterates through {@link #wordList}, formatting the contents into a StringBuilder object. Used for creating
     * a text file whenever the user wants to save.
     * @return StringBuilder containing every keyword and their counts.
     */
    private StringBuilder getKeywordsAsText() {
        StringBuilder text = new StringBuilder();
        for (Map.Entry<String, Integer> wordCountPair : wordList) {
            text.append(String.format("%" + maxWordLen + "s   %" + maxWordCountDigits + "d%n",
                    wordCountPair.getKey(), wordCountPair.getValue()));
        }
        text.deleteCharAt(text.length() - 1); // deletes the last character, '\n'
        return text;
    }
}
