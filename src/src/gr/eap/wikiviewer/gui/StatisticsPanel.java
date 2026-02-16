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
        // Δημιουργία του μοντέλου με τις στήλες
        statsModel = new DefaultTableModel(new String[] {
            "Στατιστικό",
            "Τιμή"
        }, 0);

        // Δημιουργία του JTable με το Override του Renderer για ριγέ γραμμές
        statsTable = new JTable(statsModel) {
            @Override
            public java.awt.Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                java.awt.Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    // Εναλλαγή μεταξύ σκούρων χρωμάτων (Wikipedia Dark)
                    c.setBackground(row % 2 == 0 ? new java.awt.Color(32, 37, 48) : new java.awt.Color(25, 30, 40));
                    c.setForeground(new java.awt.Color(230, 230, 230));
                } else {
                    // Χρώμα επιλεγμένης γραμμής
                    c.setBackground(new java.awt.Color(51, 102, 204));
                    c.setForeground(java.awt.Color.WHITE);
                }
                return c;
            }
        };

        // Ρυθμίσεις εμφάνισης και γεμίσματος του πίνακα
        statsTable.setBackground(new java.awt.Color(18, 21, 28));
        statsTable.setForeground(new java.awt.Color(51, 102, 204));
        statsTable.setFillsViewportHeight(true);
        statsTable.setRowHeight(28);
        statsTable.setShowGrid(false);
        statsTable.setIntercellSpacing(new java.awt.Dimension(0, 0));

        // Ρυθμίσεις Κεφαλίδας (Header)
        javax.swing.table.JTableHeader statsHeader = statsTable.getTableHeader();
        statsHeader.setBackground(new java.awt.Color(18, 21, 28));
        statsHeader.setForeground(new java.awt.Color(51, 102, 204));
        statsHeader.setFont(statsHeader.getFont().deriveFont(java.awt.Font.BOLD, 13f));

        // Ρυθμίσεις Στηλών (Πλάτος και Κεντράρισμα)
        javax.swing.table.TableColumnModel statsColumnModel = statsTable.getColumnModel();
        statsColumnModel.getColumn(0).setPreferredWidth(500); // Στατιστικό
        statsColumnModel.getColumn(1).setPreferredWidth(100); // Τιμή
        statsColumnModel.getColumn(1).setMaxWidth(150);

        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(javax.swing.JLabel.CENTER);
        statsColumnModel.getColumn(1).setCellRenderer(centerRenderer);

        // Δημιουργία και Ρύθμιση του ScrollPane (Viewport)
        JScrollPane scrollPane = new JScrollPane(statsTable);
        scrollPane.getViewport().setBackground(new java.awt.Color(18, 21, 28)); // Σκούρο φόντο περιοχής
        scrollPane.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 102, 204)));
        add(scrollPane, BorderLayout.CENTER);

        // Κάτω Panel με Κουμπιά
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new java.awt.Color(18, 21, 28));

        JButton refreshBtn = new JButton("↻");
        refreshBtn.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 18));
        refreshBtn.setBackground(new java.awt.Color(18, 21, 28));
        refreshBtn.setForeground(new java.awt.Color(51, 102, 204));
        refreshBtn.setFocusPainted(false);
        refreshBtn.setToolTipText("Επικαιροποίηση των στατιστικών στοιχείων από τη βάση δεδομένων");

        JButton pdfBtn = new JButton("Εξαγωγή σε PDF");
        pdfBtn.setBackground(new java.awt.Color(220, 53, 69));
        pdfBtn.setForeground(java.awt.Color.WHITE);
        pdfBtn.setFocusPainted(false);
        pdfBtn.setToolTipText("Δημιουργία αρχείου PDF με τα πιο δημοφιλή keywords και άρθρα ανά κατηγορία");

        bottomPanel.add(refreshBtn);
        bottomPanel.add(pdfBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        // Ενέργειες (Listeners)
        refreshBtn.addActionListener(e -> loadStats());
        pdfBtn.addActionListener(e -> exportStatsToPdf());

        // Γενικό περιθώριο στο Panel
        this.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15));
        this.setBackground(new java.awt.Color(18, 21, 28));
    }

    // Φόρτωση στατιστικών από τη βάση δεδομένων.
    public void loadStats() {
        // Καθαρισμός υπάρχοντων γραμμών
        statsModel.setRowCount(0);

        List < SearchKeyword > keywords = dbManager.getTopKeywords();
        // Προσθήκη στατιστικών για keywords.
        for (SearchKeyword k: keywords) {
            statsModel.addRow(new Object[] {
                "Keyword: " + k.getKeyword(), k.getSearchCount()
            });
        }

        /**
         * Προσθήκη στατιστικών για κατηγορίες.
         * EAGER loading: τα άρθρα φορτώνονται αυτόματα μαζί με την κατηγορία (στο ίδιο db session).
         * Το getArticles() επιστρέφει τη λίστα που είναι ήδη φορτωμένη στη μνήμη
         * και με το size() παίρνουμε το πλήθος των άρθρων.
         */
        List < Category > cats = dbManager.getAllCategories();
        for (Category c: cats) {
            statsModel.addRow(new Object[] {
                "Category: " + c.getName(), c.getArticles().size()
            });
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