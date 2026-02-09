package gr.eap.wikiviewer.gui;

import gr.eap.wikiviewer.model.Category;
import gr.eap.wikiviewer.model.SearchKeyword;
import gr.eap.wikiviewer.service.DBManager;
import gr.eap.wikiviewer.service.PDFService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class StatisticsPanel extends JPanel {
    private final DBManager dbManager;
    private final PDFService pdfService;
    private JTable statsTable;
    private DefaultTableModel statsModel;

    public StatisticsPanel(DBManager dbManager, PDFService pdfService) {
        this.dbManager = dbManager;
        this.pdfService = pdfService;
        setLayout(new BorderLayout());
        initComponents();
    }

   private void initComponents() {
        // Δημιουργία του πίνακα του αρχικού με τις 2 στήλες.
        statsModel = new DefaultTableModel(new String[]{"Στατιστικό", "Τιμή"}, 0); 

        // Δημιουργία του JTable με το Override του Renderer για τις ρίγες
        statsTable = new JTable(statsModel) { 
            @Override
            public java.awt.Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                java.awt.Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    // Εναλλαγή χρωμάτων για ριγέ εμφάνιση
                    c.setBackground(row % 2 == 0 ? getBackground() : new java.awt.Color(242, 242, 242));
                }
                return c;
            }
    };

    // Ρυθμίσεις εμφάνισης (μετά τη δημιουργία του table)
    statsTable.setRowHeight(28);
    statsTable.setShowGrid(false);
    
    // Κεντράρισμα της δεύτερης στήλης (Τιμή)
    javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
    centerRenderer.setHorizontalAlignment(javax.swing.JLabel.CENTER);
    statsTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

    // Προσθήκη στο ScrollPane
    add(new JScrollPane(statsTable), BorderLayout.CENTER); 

    // Δημιουργία του κάτω Panel και των κουμπιών
    JPanel bottomPanel = new JPanel(); 
    JButton refreshBtn = new JButton("Ανανέωση"); 
    JButton pdfBtn = new JButton("Εξαγωγή σε PDF"); 
    
    // Styling στα κουμπιά
    refreshBtn.setBackground(new java.awt.Color(108, 117, 125));
    refreshBtn.setForeground(java.awt.Color.WHITE);
    pdfBtn.setBackground(new java.awt.Color(220, 53, 69));
    pdfBtn.setForeground(java.awt.Color.WHITE);

    bottomPanel.add(refreshBtn); 
    bottomPanel.add(pdfBtn); 
    add(bottomPanel, BorderLayout.SOUTH); 

    // Ενέργειες Παραθύρου
    refreshBtn.addActionListener(e -> loadStats()); 
    pdfBtn.addActionListener(e -> exportStatsToPdf()); 
    
    // Προσθήκη κενού (padding) σε όλο το Panel
    this.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15));
}
    
    // Φόρτωση στατιστικών από τη βάση δεδομένων.
    public void loadStats() {
        // Καθαρισμός υπάρχοντων γραμμών
        statsModel.setRowCount(0);
        
        List<SearchKeyword> keywords = dbManager.getTopKeywords();
        // Προσθήκη στατιστικών για keywords.
        for (SearchKeyword k : keywords) {
            statsModel.addRow(new Object[]{"Keyword: " + k.getKeyword(), k.getSearchCount()});
        }
        
        /**
        * Προσθήκη στατιστικών για κατηγορίες.
        * EAGER loading: τα άρθρα φορτώνονται αυτόματα μαζί με την κατηγορία (στο ίδιο db session).
        * Το getArticles() επιστρέφει τη λίστα που είναι ήδη φορτωμένη στη μνήμη
        * και με το size() παίρνουμε το πλήθος των άρθρων.
        */
        List<Category> cats = dbManager.getAllCategories();
        for (Category c : cats) {
            statsModel.addRow(new Object[]{"Category: " + c.getName(), c.getArticles().size()});
        }
    }

    //Εξαγωγή στατιστικών σε PDF αρχείο
    private void exportStatsToPdf() {
        try {
            String path = "WikiViewer_Stats.pdf";
            // Δημιουργία PDF με τα στατιστικά.
            pdfService.generateStatisticsReport(path, dbManager.getTopKeywords(), dbManager.getAllCategories());
            // Μήνυμα επιτυχίας με το path του αρχείου.
            JOptionPane.showMessageDialog(this, "Stats exported to " + new File(path).getAbsolutePath());
        } catch (Exception e) {
            //Σφάλμα.
            JOptionPane.showMessageDialog(this, "Error exporting PDF: " + e.getMessage());
        }
    }
}
