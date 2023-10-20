import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Contains static methods used in {@link MainView} to create the GUI.
 */
public final class GUI_Util {

    public static Font TINY_ITAL, SMALL_PLAIN, SMALL_BOLD, MED_PLAIN, MED_BOLD, BIG_BOLD;
    public static BufferedImage FRAME_IMAGE; // icon displayed on JFrame

    /**
     * Calls {@link #getFonts()} and {@link #getImages()}.
     */
    public static void initGUI() {
        getFonts();
        getImages();
    }

    /**
     * Used for creating the two central JPanel instances on the main menu.
     * @param title title for the TitledBorder of this panel
     * @return a JPanel for the main menu
     */
    public static JPanel createTitledPanel(String title) {
        JPanel pnl = new JPanel();
        pnl.setOpaque(false);
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY,
                2, true), title, TitledBorder.CENTER, TitledBorder.TOP);
        border.setTitleFont(MED_BOLD);
        border.setTitleColor(Color.WHITE);
        pnl.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder
                (30, 30, 30, 30)));
        return pnl;
    }

    /**
     * Used for creating the buttons on the main menu and scan result panel.
     * @param label text to be displayed on the button
     * @param actionListener ActionListener for the button
     * @return a JButton
     */
    public static JButton createMenuButton(String label, ActionListener actionListener) {
        JButton btn = new JButton(label);
        btn.addActionListener(actionListener);
        btn.setFocusPainted(false);
        btn.setFont(MED_PLAIN);
        btn.setBackground(Color.WHITE);
        return btn;
    }

    /**
     * Used for creating a JLabel instance for the GUI.
     * @param text String for the JLabel to contain
     * @param font Font in which to display the label text
     * @return a JLabel
     */
    public static JLabel createLabel(String text, Font font) {
        JLabel lbl = new JLabel(text);
        lbl.setOpaque(false);
        lbl.setFont(font);
        lbl.setForeground(Color.WHITE);
        return lbl;
    }

    /**
     * Used for creating the two JTextArea instances on the main menu for showing file names. Wraps the text area
     * inside a JScrollPane instance to ensure the text wraps properly, rather than stretching across the screen.
     * @return a JScrollPane containing a JTextArea
     */
    public static JScrollPane createGrayedOutTextArea() {
        JTextArea ta = new JTextArea();
        ta.setOpaque(false);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setFont(TINY_ITAL);
        ta.setForeground(Color.LIGHT_GRAY);
        ta.setRows(3);
        JScrollPane sp = new JScrollPane(ta);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(null);
        sp.getViewport().setBorder(null);
        return sp;
    }

    /**
     * Creates a {@link Box} instance using {@link Box#createHorizontalBox()} and centers a component inside of it.
     *
     * @param component component to wrap inside box
     * @return transparent horizontal Box containing this component
     */
    public static Box createHorizontalBox(Component component) {
        Box box = Box.createHorizontalBox();
        box.setOpaque(false);
        box.add(Box.createHorizontalGlue());
        box.add(component);
        box.add(Box.createHorizontalGlue());
        return box;
    }

    /**
     * Creates a {@link Box} instance using {@link Box#createHorizontalBox()} and centers two components inside of it,
     * with no gap between them.
     *
     * @param comp1 first component to wrap inside box
     * @param comp2 second component to wrap inside box
     * @return transparent horizontal box containing these two components
     */
    public static Box createHorizontalBoxNoGap(Component comp1, Component comp2) {
        Box box = Box.createHorizontalBox();
        box.setOpaque(false);
        box.add(Box.createHorizontalGlue());
        box.add(comp1);
        box.add(comp2);
        box.add(Box.createHorizontalGlue());
        return box;
    }

    /**
     * Creates a {@link Box} instance using {@link Box#createVerticalBox()} and centers a component inside of it.
     * @param component component to center inside box
     * @return transparent vertical box containing this component
     */
    public static Box createVerticalBox(Component component) {
        Box box = Box.createVerticalBox();
        box.setOpaque(false);
        box.add(Box.createVerticalGlue());
        box.add(component);
        box.add(Box.createVerticalGlue());
        return box;
    }

    /**
     * Creates a {@link Box} instance using {@link Box#createVerticalBox()} and centers two components inside of it,
     * keeping a gap between them.
     *
     * @param comp1 first component to wrap inside box
     * @param comp2 second component to wrap inside box
     * @return transparent certical box containing these two components
     */
    public static Box createVerticalBox(Component comp1, Component comp2) {
        Box box = Box.createVerticalBox();
        box.setOpaque(false);
        box.add(Box.createVerticalGlue());
        box.add(comp1);
        box.add(Box.createVerticalGlue());
        box.add(comp2);
        box.add(Box.createVerticalGlue());
        return box;
    }

    /**
     * Creates a {@link Box} instance using {@link Box#createVerticalBox()} and centers four components inside of it,
     * with no gap between them. Does not use {@link Box#createVerticalGlue()}.
     *
     * @param comp1 first component to wrap inside box
     * @param comp2 second component to wrap inside box
     * @param comp3 third component to wrap inside box
     * @param comp4 fourth component to wrap inside box
     * @return transparent vertical box containing these four components
     */
    public static Box createVerticalBox(Component comp1, Component comp2, Component comp3, Component comp4) {
        Box box = Box.createVerticalBox();
        box.setOpaque(false);
        box.add(comp1);
        box.add(comp2);
        box.add(comp3);
        box.add(comp4);
        return box;
    }

    /**
     * Private constructor
     */
    private GUI_Util() {

    }

    /**
     * Initializes the {@link Font} instances used in the GUI.
     */
    private static void getFonts() {
        File directory = new File(System.getProperty("user.dir") + "//fonts//");
        try {
            Font reg = Font.createFont(Font.TRUETYPE_FONT, new File(directory +
                    "//unispace_rg.otf"));
            Font bold = Font.createFont(Font.TRUETYPE_FONT, new File(directory +
                    "//unispace_bd.otf"));
            Font italics = Font.createFont(Font.TRUETYPE_FONT, new File(directory +
                    "//unispace it.otf"));
            TINY_ITAL = italics.deriveFont(Font.PLAIN, 14f);
            SMALL_PLAIN = reg.deriveFont(Font.PLAIN, 18f);
            SMALL_BOLD = bold.deriveFont(Font.PLAIN, 18f);
            MED_PLAIN = reg.deriveFont(Font.PLAIN, 20f);
            MED_BOLD = bold.deriveFont(Font.PLAIN, 20f);
            BIG_BOLD = bold.deriveFont(Font.PLAIN, 26f);
        } catch (FontFormatException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Initializes the icon used on the frame.
     */
    private static void getImages() {
        try {
            FRAME_IMAGE = ImageIO.read(new File(System.getProperty("user.dir") + "//images//search.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
