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
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        
        // Φόρτωση κατηγοριών κατά την εκκίνηση στο dropdown menu του δεύτερου tab.
        libraryPanel.loadCategories();
    }

    private void initComponents() {
        tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
        
        // Δημιουργία panels.
        searchPanel = new SearchPanel(this, wikiService, dbManager);
        libraryPanel = new LibraryPanel(dbManager);
        statisticsPanel = new StatisticsPanel(dbManager, pdfService);
        
        // Προσθήκη tabs
        tabbedPane.addTab("Αναζήτηση [Live από API]", searchPanel);
        tabbedPane.addTab("Αποθηκευμένα Άρθρα", libraryPanel);
        tabbedPane.addTab("Στατιστικά", statisticsPanel);
        
        // Προσθήκη του tabbed pane στο frame
        add(tabbedPane, BorderLayout.CENTER);
    }

    // Aνανέωση των αποθηκευμένων άρθρων
    public void refreshLocalArticles() {
        libraryPanel.loadLocalArticles();
    }
    // Αρχή της εφαρμογής
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}
