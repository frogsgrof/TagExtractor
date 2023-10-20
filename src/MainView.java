import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;

public class MainView extends JFrame {

    public static void main(String[] args) {
        MainView view = new MainView();
        view.setVisible(true);
    }

    public static int WIDTH, HEIGHT; // width and height of the frame
    static final String TEXT_VIEW_KEY = ":)"; // "key" for retrieving the panel containing keyword counts using CardLayout
    JPanel mainMenu,
            textView = new JPanel(); // the two JPanels used within the CardLayout
    JButton draculaBtn, chooseTextFileBtn, defaultStopBtn, chooseStopBtn; // buttons on the main menu
    ButtonGroup scanFileBtns, stopFileBtns; // for controlling the buttons above
    public static final File DRACULA = new File(System.getProperty("user.dir") + "//sample//dracula.txt"), // default target file
            STOP_WORDS = new File(System.getProperty("user.dir") + "//stop//stopwords.txt"); // default stop word file
    File targetFile, stopFile; // store the user's choice of target and stop files
    JScrollPane scanFileArea, stopFileArea; // these appear on the main menu, showing the names of the files chosen by the user
    StopFilter stopFilter;
    WordCounter wordCounter;
    TitledBorder frameBorder; // border on the JFrame

    private MainView() {
        super("Tag Extractor");
        GUI_Util.initGUI();
        setIconImage(GUI_Util.FRAME_IMAGE);

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        int screenWidth = toolkit.getScreenSize().width,
                screenHeight = toolkit.getScreenSize().height;
        WIDTH = screenWidth * 3 / 4;
        HEIGHT = screenHeight * 3 / 4;
        setSize(WIDTH, HEIGHT);
        setLocation((screenWidth - WIDTH) / 2, (screenHeight - HEIGHT) / 3);

        // cosmetic changes

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }

        frameBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder
                (Color.WHITE, 3, true), "Tag Extractor");
        frameBorder.setTitleFont(GUI_Util.BIG_BOLD);
        frameBorder.setTitleColor(Color.WHITE);
        getRootPane().setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder
                (50, 50, 50, 50), frameBorder));

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBackground(Color.DARK_GRAY);
        getContentPane().setBackground(Color.DARK_GRAY);

        // adds the two primary elements to the frame
        createMainMenu();
        getContentPane().setLayout(new CardLayout());
        add(mainMenu, "");
        add(textView, TEXT_VIEW_KEY);
    }

    /**
     * Switches from the main menu to the screen for showing keywords.
     */
    private void showKeywords() {
        if (scanFileBtns.getSelection() == null || stopFileBtns.getSelection() == null) return;

        // initializes StopFilter and WordCounter instances, passing in their respective files as parameters
        try {
            stopFilter = StopFilter.createStopFilter(stopFile);
            wordCounter = WordCounter.createWordCounter(targetFile, stopFilter);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        // changes the TitledBorder to show the file name
        frameBorder.setTitle("Extracting tags from " + targetFile.toString());
        frameBorder.setTitlePosition(TitledBorder.BELOW_TOP);
        frameBorder.setTitleColor(Color.LIGHT_GRAY);
        frameBorder.setTitleFont(GUI_Util.TINY_ITAL);
        getRootPane().setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder
                (50, 50, 50, 50), frameBorder));

        // finishes creating the second panel and shows it
        createWordCountView(wordCounter);
        ((CardLayout) getContentPane().getLayout()).show(getContentPane(), TEXT_VIEW_KEY);
    }

    /**
     * Controls the two main menu buttons for selecting the file to scan. Either selects or deselects the button,
     * depending on if the button was already selected. The only reason this exists is because I didn't want to use
     * JRadioButtons, and ButtonGroup doesn't automatically handle JButtons in this way.
     * @param btn menu button that was pressed
     */
    private void toggleScanFileBtns(JButton btn) {
        if (btn.equals(draculaBtn)) {
            if (scanFileBtns.getSelection() == draculaBtn.getModel())
                scanFileBtns.clearSelection();
            else {
                scanFileBtns.clearSelection();
                scanFileBtns.setSelected(draculaBtn.getModel(), true);
                chooseDracula();
            }
        } else {
            scanFileBtns.clearSelection();
            scanFileBtns.setSelected(chooseTextFileBtn.getModel(), true);
            chooseFile();
        }
    }

    /**
     * Controls the two main menu buttons for selecting the stop words file. Either selects or deselects the button,
     * depending on if the button was already selected. The only reason this exists is because I didn't want to use
     * JRadioButtons, and ButtonGroup doesn't automatically handle JButtons in this way.
     *
     * @param btn menu button that was pressed
     */
    private void toggleStopFileBtns(JButton btn) {
        if (btn.equals(defaultStopBtn)) {
            if (stopFileBtns.getSelection() == defaultStopBtn.getModel())
                stopFileBtns.clearSelection();
            else {
                stopFileBtns.clearSelection();
                stopFileBtns.setSelected(defaultStopBtn.getModel(), true);
                useDefaultStopWords();
            }

        } else {
            stopFileBtns.clearSelection();
            stopFileBtns.setSelected(chooseStopBtn.getModel(), true);
            chooseStopWords();
        }
    }

    /**
     * Sets the target file to a static variable holding the Dracula text file, and displays the file name on the
     * main menu.
     */
    private void chooseDracula() {
        targetFile = DRACULA;
        ((JTextArea) scanFileArea.getViewport().getView()).setText(targetFile.toString());
    }

    /**
     * Opens a JFileChooser window for the user to select the file they wish to scan. After the user successfully
     * chooses a file, sets the {@link #targetFile} field to that file.
     */
    private void chooseFile() {
        JFileChooser jfc = new JFileChooser();
        // sets the directory to "documents" by default
        jfc.setCurrentDirectory(new File(System.getProperty("user.home") + "//documents//"));
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jfc.addChoosableFileFilter(new FileNameExtensionFilter("*.txt", "txt"));
        jfc.setAcceptAllFileFilterUsed(false);
        jfc.setDialogTitle("File to scan");

        if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            targetFile = jfc.getSelectedFile();
            ((JTextArea) scanFileArea.getViewport().getView()).setText(jfc.getName(targetFile));
        } else {
            scanFileBtns.clearSelection();
        }
    }

    /**
     * Sets the target file to a static variable holding the text file of default stop words, and displays the
     * file name on the main menu.
     */
    private void useDefaultStopWords() {
        stopFile = STOP_WORDS;
        ((JTextArea) stopFileArea.getViewport().getView()).setText(stopFile.toString());
    }

    /**
     * Opens a JFileChooser window for the user to select the file they wish to use for stop words. After the user
     * successfully chooses a file, sets the {@link #stopFile} field to that file.
     */
    private void chooseStopWords() {
        JFileChooser jfc = new JFileChooser();
        // sets the directory to "documents" by default
        jfc.setCurrentDirectory(new File(System.getProperty("user.home") + "//documents//"));
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jfc.addChoosableFileFilter(new FileNameExtensionFilter(".txt", "txt"));
        jfc.setAcceptAllFileFilterUsed(false);
        jfc.setDialogTitle("Stop words list");

        if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            stopFile = jfc.getSelectedFile();
            ((JTextArea) stopFileArea.getViewport().getView()).setText(jfc.getName(stopFile));
        } else {
            scanFileBtns.clearSelection();
        }
    }

    /**
     * Saves the list of keywords and their respective counts to a text file.
     */
    private void save() {
        boolean isIllegalName;
        String fileName = "tag_extractor";

        // loops until either the user has inputed a valid file name or canceled
        do {
            // shows input dialog asking for a file name
            fileName = (String) JOptionPane.showInputDialog(null, "Save as:", "Save",
                    JOptionPane.PLAIN_MESSAGE, null, null, fileName);
            isIllegalName = false; // resets flag

            // if they left the field blank or an error has occurred, shows a dialog window saying the save is canceled
            if (fileName == null || fileName.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Save canceled.", "Save",
                        JOptionPane.PLAIN_MESSAGE);
                return;
            }

            // if the file name is unreasonably long, shows a message dialog to tell them and then loops
            if (fileName.length() > 50) {
                JOptionPane.showMessageDialog(null,
                        "Error: file names cannot be longer than 50 characters.",
                        "Error", JOptionPane.PLAIN_MESSAGE);
                isIllegalName = true;

            } else {
                // if the file name has passed all of the above criteria, checks the file name for any illegal characters

                for (int i = 0; i < fileName.length() && !isIllegalName; i++) {
                    char c = fileName.charAt(i);
                    if (c == '/' || c == '<' || c == '>' || c == ':' || c == '\"'
                            || c == '\\' || c == '|' || c == '?' || c == '*') {
                        JOptionPane.showMessageDialog(null,
                                """
                                        Error: file names cannot contain the following characters:
                                        /
                                        \\
                                        <
                                        >
                                        :
                                        "
                                        |
                                        ?
                                        *
                                        """,
                                "Save", JOptionPane.PLAIN_MESSAGE);
                        isIllegalName = true;
                    }
                }
            }
        } while (isIllegalName);

        // shows a JFileChooser window for the user to pick a directory to save the file in
        JFileChooser jfc = new JFileChooser();
        jfc.setDialogTitle("Save");
        jfc.setCurrentDirectory(new File(System.getProperty("user.home") + "//documents//"));
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File dir = jfc.getSelectedFile(),
                    data = new File(dir, fileName + ".txt");

            try {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream
                        (new FileOutputStream(data))));

                // writes the file using a method in WordCounter
                wordCounter.writeFile(writer);
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            JOptionPane.showMessageDialog(null, "Save successful.", "Save",
                    JOptionPane.PLAIN_MESSAGE);
        }
    }

    /**
     * Uses {@link #wordCounter} to perform a scan on the target ile and sets up the JPanel used to show scan results.
     * @param wordCounter instance of {@link WordCounter} to get keywords and counts from
     */
    private void createWordCountView(WordCounter wordCounter) {
        textView.setOpaque(false);
        textView.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        textView.setLayout(new BoxLayout(textView, BoxLayout.X_AXIS));

        JPanel top10Pnl = wordCounter.createTop10View();
        JScrollPane fullListPnl = wordCounter.createFullListView();

        JPanel statsPnl = new JPanel();
        statsPnl.setOpaque(false);
        TitledBorder statsPnlBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder
                (Color.LIGHT_GRAY,2, true), "Stats", TitledBorder.RIGHT,
                TitledBorder.TOP);
        statsPnlBorder.setTitleColor(Color.WHITE);
        statsPnlBorder.setTitleFont(GUI_Util.MED_BOLD);
        statsPnl.setBorder(BorderFactory.createCompoundBorder(statsPnlBorder,
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        statsPnl.setLayout(new BoxLayout(statsPnl, BoxLayout.X_AXIS));

        Box leftStats = GUI_Util.createVerticalBox(GUI_Util.createLabel("Total characters: ", GUI_Util.SMALL_BOLD),
                GUI_Util.createLabel("Total words: ", GUI_Util.SMALL_BOLD),
                GUI_Util.createLabel("Total keywords: ", GUI_Util.SMALL_BOLD),
                GUI_Util.createLabel("Total stopwords: ", GUI_Util.SMALL_BOLD));
        Box rightStats = GUI_Util.createVerticalBox(GUI_Util.createLabel(String.valueOf(wordCounter.getTotalChars()),
                        GUI_Util.SMALL_PLAIN),
                GUI_Util.createLabel(String.valueOf(wordCounter.getTotalWords()), GUI_Util.SMALL_PLAIN),
                GUI_Util.createLabel(String.valueOf(wordCounter.getTotalKeywords()), GUI_Util.SMALL_PLAIN),
                GUI_Util.createLabel(String.valueOf(wordCounter.getTotalStopWords()), GUI_Util.SMALL_PLAIN));
        statsPnl.add(leftStats);
        statsPnl.add(rightStats);

        JButton saveBtn = GUI_Util.createMenuButton("Save", e -> save());
        Box btnBox = Box.createHorizontalBox();
        btnBox.add(Box.createHorizontalGlue());
        btnBox.add(saveBtn);

        JPanel rightPnl = new JPanel();
        rightPnl.setLayout(new BoxLayout(rightPnl, BoxLayout.Y_AXIS));
        rightPnl.setOpaque(false);
        rightPnl.add(Box.createVerticalGlue());
        rightPnl.add(statsPnl);
        rightPnl.add(Box.createVerticalStrut(10));
        rightPnl.add(btnBox);

        textView.add(Box.createHorizontalGlue());
        textView.add(GUI_Util.createVerticalBox(GUI_Util.createHorizontalBoxNoGap(top10Pnl, fullListPnl)));
        textView.add(Box.createHorizontalGlue());
        textView.add(rightPnl);
        textView.add(Box.createHorizontalGlue());
    }

    /**
     * Sets up the main menu.
     */
    private void createMainMenu() {
        createMenuButtons();

        JPanel leftBtnPnl = GUI_Util.createTitledPanel("File to scan"),
                rightBtnPnl = GUI_Util.createTitledPanel("List of stop words"),
                goBtnPnl = new JPanel();
        leftBtnPnl.add(GUI_Util.createVerticalBox(GUI_Util.createHorizontalBox(draculaBtn),
                GUI_Util.createHorizontalBox(chooseTextFileBtn)));
        rightBtnPnl.add(GUI_Util.createVerticalBox(GUI_Util.createHorizontalBox(defaultStopBtn),
                GUI_Util.createHorizontalBox(chooseStopBtn)));
        JPanel centerPnl = new JPanel(new GridLayout(1, 2, 40, 0));
        centerPnl.setBorder(BorderFactory.createEmptyBorder(20, 60, 0, 60));
        centerPnl.setOpaque(false);
        centerPnl.add(leftBtnPnl);
        centerPnl.add(rightBtnPnl);

        JPanel fileNamePnl = new JPanel(new GridLayout(1, 2, 40, 0));
        fileNamePnl.setBorder(BorderFactory.createEmptyBorder(0, 60, 20, 60));
        fileNamePnl.setOpaque(false);
        fileNamePnl.add(scanFileArea);
        fileNamePnl.add(stopFileArea);

        JButton goBtn = GUI_Util.createMenuButton("Go!", e -> showKeywords());
        goBtnPnl.setOpaque(false);
        goBtnPnl.add(Box.createHorizontalStrut(WIDTH * 3 / 4));
        goBtnPnl.add(goBtn);

        JPanel bottomPnl = new JPanel(new GridLayout(3, 1));
        bottomPnl.setOpaque(false);
        bottomPnl.add(fileNamePnl);
        bottomPnl.add(Box.createVerticalBox());
        bottomPnl.add(goBtnPnl);

        mainMenu = new JPanel(new GridLayout(2, 1));
        mainMenu.setOpaque(false);
        mainMenu.add(centerPnl);
        mainMenu.add(bottomPnl);
    }

    /**
     * Creates the buttons on the main menu.
     */
    private void createMenuButtons() {
        draculaBtn = GUI_Util.createMenuButton("Dracula", null);
        chooseTextFileBtn = GUI_Util.createMenuButton("Choose a file", null);
        defaultStopBtn = GUI_Util.createMenuButton("Default stop words", null);
        chooseStopBtn = GUI_Util.createMenuButton("Choose a stop file", null);
        draculaBtn.addActionListener(e -> toggleScanFileBtns(draculaBtn));
        chooseTextFileBtn.addActionListener(e -> toggleScanFileBtns(chooseTextFileBtn));
        defaultStopBtn.addActionListener(e -> toggleStopFileBtns(defaultStopBtn));
        chooseStopBtn.addActionListener(e -> toggleStopFileBtns(chooseStopBtn));

        scanFileBtns = new ButtonGroup();
        stopFileBtns = new ButtonGroup();
        scanFileBtns.add(draculaBtn);
        scanFileBtns.add(chooseTextFileBtn);
        stopFileBtns.add(defaultStopBtn);
        stopFileBtns.add(chooseStopBtn);

        scanFileArea = GUI_Util.createGrayedOutTextArea();
        stopFileArea = GUI_Util.createGrayedOutTextArea();
    }
}