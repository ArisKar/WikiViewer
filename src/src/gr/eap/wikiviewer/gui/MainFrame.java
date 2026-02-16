package gr.eap.wikiviewer.gui;

import gr.eap.wikiviewer.service.DBManager;
import gr.eap.wikiviewer.service.PDFService;
import gr.eap.wikiviewer.service.WikipediaService;

import javax.swing.*;
import java.awt.*;

/**
 * Κύριο παράθυρο της εφαρμογής WikiViewer.
 * Περιέχει τα tabs: Search, Library και Statistics.
 */

public class MainFrame extends JFrame {
    // Service για επικοινωνία με το Wikipedia API.
    private final WikipediaService wikiService = new WikipediaService();
    // Initialize Singleton Pattern ως διαχειριστή βάσης δεδομένων
    private final DBManager dbManager = DBManager.getInstance();
    // Το service για δημιουργία PDF
    private final PDFService pdfService = new PDFService();

    // Βασικά GUI components
    private JTabbedPane tabbedPane;
    private SearchPanel searchPanel;
    private LibraryPanel libraryPanel;
    private StatisticsPanel statisticsPanel;

    public MainFrame() {
        setTitle("WikiViewer - Wikipedia Information Browser");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();

        // Φόρτωση κατηγοριών κατά την εκκίνηση στο dropdown menu του δεύτερου tab.
        libraryPanel.loadCategories();
    }

    private void initComponents() {
        tabbedPane = new JTabbedPane(JTabbedPane.LEFT);

        //  Ρύθμιση του φόντου του JTabbedPane
        tabbedPane.setBackground(new java.awt.Color(18, 21, 28)); 
        tabbedPane.setForeground(new java.awt.Color(51, 102, 204));
        tabbedPane.setOpaque(true);

        //  Ρύθμιση των UI defaults
        UIManager.put("TabbedPane.contentAreaColor", new java.awt.Color(18, 21, 28));
        UIManager.put("TabbedPane.selected", new java.awt.Color(51, 102, 204));
        UIManager.put("TabbedPane.borderHighlightColor", new java.awt.Color(51, 102, 204));

        //  Ρύθμιση του φόντου του ίδιου του Frame
        this.getContentPane().setBackground(new java.awt.Color(18, 21, 28));
       

        // Αλλαγή γραμματοσειράς για όλα τα tabs
        tabbedPane.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14));

        // Δημιουργία panels.
        searchPanel = new SearchPanel(this, wikiService, dbManager);
        libraryPanel = new LibraryPanel(dbManager);
        statisticsPanel = new StatisticsPanel(dbManager, pdfService);
        DatabaseSearchPanel dbSearchPanel = new DatabaseSearchPanel(dbManager);


        //Εφαρμογή του θέματος σε κάθε panel
        applyWikipediaTheme(searchPanel);
        applyWikipediaTheme(libraryPanel);
        applyWikipediaTheme(statisticsPanel);

        // Προσθήκη tabs
        tabbedPane.addTab("<html><body style='color: rgb(51, 102, 204); font-weight: bold;'>Αναζήτηση [Live από API]</body></html>", searchPanel);
        tabbedPane.addTab("<html><body style='color: rgb(51, 102, 204); font-weight: bold;'>Αποθηκευμένα Άρθρα</body></html>", libraryPanel);
        tabbedPane.addTab("<html><body style='color: rgb(51, 102, 204); font-weight: bold;'>Αναζήτηση στη ΒΔ</body></html>", dbSearchPanel);
        tabbedPane.addTab("<html><body style='color: rgb(51, 102, 204); font-weight: bold;'>Στατιστικά</body></html>", statisticsPanel);
        
        //Μη υλοποιημένα tabs
        JPanel manualEntryPanel = new JPanel(); // Για την Απαίτηση 7
        JPanel editCategoryPanel = new JPanel(); // Για την Απαίτηση 8
        JPanel addCategoryPanel = new JPanel(); // Για την Απαίτηση 9
        tabbedPane.addTab("Χειροκίνητη Εισαγωγή", manualEntryPanel);
        tabbedPane.addTab("Τροποποίηση σε Κατηγορία", editCategoryPanel);
        tabbedPane.addTab("Προσθήκη Κατηγορίας ", addCategoryPanel);

        // Απενεργοποίηση των μη υλοποιημένων
        tabbedPane.setEnabledAt(4, false);
        tabbedPane.setEnabledAt(5, false);
        tabbedPane.setEnabledAt(6, false);

        // Προσθήκη του tabbed pane στο frame
        add(tabbedPane, BorderLayout.CENTER);

        //Αλλαγή χρώματος φόντου
        tabbedPane.setBackground(new Color(32, 37, 48));
        tabbedPane.setForeground(Color.WHITE);
        // Αφαίρεση του κλασικού γκρι περιγράμματος
        UIManager.put("TabbedPane.contentOpaque", false);
    }

    // Aνανέωση των αποθηκευμένων άρθρων
    //public void refreshLocalArticles() {
    //    libraryPanel.loadLocalArticles();
    //}

    public void refreshLocalCategories() {
        libraryPanel.loadCategories();
    }

    // Αρχή της εφαρμογής
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }

    public void applyWikipediaTheme(JPanel panel) {
        Color darkBackground = new Color(18, 21, 28);
        Color wikiBlue = new Color(51, 102, 204);
        Color lightText = new Color(230, 230, 230);

        panel.setBackground(darkBackground);

        for (Component c: panel.getComponents()) {
            // Αλλαγή φόντου σε εσωτερικά panels (π.χ. topPanel, bottomPanel)
            if (c instanceof JPanel) {
                c.setBackground(darkBackground);
                applyWikipediaTheme((JPanel) c); // Αναδρομική κλήση για sub-panels
            }
            // Αλλαγή χρώματος σε Labels (Σκούρο Μπλε)
            if (c instanceof JLabel) {
                c.setForeground(wikiBlue);
                c.setFont(c.getFont().deriveFont(Font.BOLD));
            }
            // Αλλαγή εμφάνισης σε TextFields
            if (c instanceof JTextField) {
                c.setBackground(new Color(32, 37, 48));
                c.setForeground(lightText);

            }
            if (c instanceof JLabel) {
                c.setForeground(new java.awt.Color(51, 102, 204)); // Το έντονο μπλε για τους τίτλους "Λέξη κλειδί:"
                c.setFont(c.getFont().deriveFont(java.awt.Font.BOLD));
            }
            if (c instanceof JTextField) {
                c.setBackground(new java.awt.Color(32, 37, 48));
                c.setForeground(java.awt.Color.WHITE);
            }
        }
    }
}