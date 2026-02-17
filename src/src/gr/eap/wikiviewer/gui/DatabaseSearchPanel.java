package gr.eap.wikiviewer.gui;

/**
 *
 * @author Administrator
 */

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import gr.eap.wikiviewer.model.Article;
import gr.eap.wikiviewer.service.DBManager;

/**
 * Κλάση που υλοποιεί το Panel για την αναζήτηση άρθρων 
 * αποκλειστικά μέσα στην τοπική βάση δεδομένων (SQLite).
 */
public class DatabaseSearchPanel extends JPanel {
    private JTextField searchFieldDB;   // Πεδίο εισαγωγής κειμένου για αναζήτηση
    private JTable dbResultTable;       // Πίνακας προβολής αποτελεσμάτων
    private DefaultTableModel dbModel;  // Μοντέλο δεδομένων του πίνακα
    private DBManager dbManager;        // Αναφορά στον διαχειριστή της βάσης δεδομένων

    // Constructor της κλάσης.
    public DatabaseSearchPanel(DBManager dbManager) {
        this.dbManager = dbManager;
        setLayout(new BorderLayout());  // Ορισμός Layout για σωστή τοποθέτηση των στοιχείων
        initComponents();               // Κλήση της μεθόδου αρχικοποίησης των στοιχείων
    }

    private void initComponents() {
        // Wikipedia Dark Background
        this.setBackground(new java.awt.Color(18, 21, 28));

        // Top Search Panel
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new java.awt.Color(18, 21, 28));
        
        // Κουππί αναζήτησης
        JButton searchBtn = new JButton("Αναζήτηση στη ΒΔ:");
        searchBtn.setBackground(new java.awt.Color(18, 21, 28));
        searchBtn.setForeground(new java.awt.Color(51, 102, 204));
        searchBtn.setBorder(BorderFactory.createLineBorder(new java.awt.Color(18, 21, 28)));
        searchBtn.setFocusPainted(false);
        
        // Πεδίο κειμένου αναζήτησης
        searchFieldDB = new JTextField(25);
        searchFieldDB.setBackground(new java.awt.Color(18, 21, 28));
        searchFieldDB.setForeground(Color.WHITE);
        searchFieldDB.setCaretColor(Color.WHITE);
        searchFieldDB.setBorder(BorderFactory.createLineBorder(new java.awt.Color(51, 102, 204)));
        
        //Κουμπί refresh
        JButton refreshBtn = new JButton("↻");
        refreshBtn.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 18));
        refreshBtn.setBackground(new java.awt.Color(18, 21, 28));
        refreshBtn.setForeground(new java.awt.Color(51, 102, 204));
        
        //Κουμπί clear results
        JButton clearSearchBtn = new JButton("X");
        clearSearchBtn.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 18));
        clearSearchBtn.setBackground(new java.awt.Color(18, 21, 28));
        clearSearchBtn.setForeground(new java.awt.Color(204, 0, 0));
        
        topPanel.add(searchBtn);
        topPanel.add(searchFieldDB);
        topPanel.add(refreshBtn);
        topPanel.add(clearSearchBtn);
        
        //Πίνακας αποτελεσμάτων
        dbModel = new DefaultTableModel(new String[]{"ID Σελίδας", "Τίτλος", "Κατηγορία", "Βαθμολογία", "Σχόλια"}, 0);
        dbResultTable = new JTable(dbModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    // Εναλλαγή χρωμάτων γραμμών για καλύτερη ανάγνωση (striped effect)
                    c.setBackground(row % 2 == 0 ? new java.awt.Color(32, 37, 48) : new java.awt.Color(25, 30, 40));
                    c.setForeground(new java.awt.Color(230, 230, 230));
                } else {
                    // Χρώμα όταν μια γραμμή είναι επιλεγμένη
                    c.setBackground(new java.awt.Color(51, 102, 204));
                    c.setForeground(Color.WHITE);
                }
                return c;
            }
        };
        //Styling της Κεφαλίδας
        javax.swing.table.JTableHeader header = dbResultTable.getTableHeader();
        header.setBackground(new java.awt.Color(18, 21, 28));
        header.setForeground(new  java.awt.Color(51, 102, 204));
        header.setFont(header.getFont().deriveFont(Font.BOLD));
        
        // Προσθήκη του πίνακα σε ScrollPane για κύλιση
        JScrollPane scrollPane = new JScrollPane(dbResultTable);
        scrollPane.getViewport().setBackground(new java.awt.Color(18, 21, 28));
        scrollPane.setBorder(BorderFactory.createLineBorder(new java.awt.Color(51, 102, 204)));
        
        // Τοποθέτηση των Panels στο κεντρικό DatabaseSearchPanel
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Listeners
        // Εκτέλεση αναζήτησης με το πάτημα του κουμπιού
        searchBtn.addActionListener(e -> executeDatabaseLookup());
        //Εκτέλεση refresh με το πάτημα του κουμπιού
        refreshBtn.addActionListener(e -> executeDatabaseLookup());
        // Εκτέλεση αναζήτησης με το πάτημα του "Enter" στο πεδίο κειμένου
        searchFieldDB.addActionListener(e -> executeDatabaseLookup());
        //Εκτέλεση clear results με το πάτημα του κουμπιού
        clearSearchBtn.addActionListener(e -> clearSearch());
        
        // Περιθώρια (Padding) γύρω από το Panel
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    /**
     * Εκτελεί την αναζήτηση στη βάση δεδομένων και προβάλλει 
     * τα εμπλουτισμένα δεδομένα (σχόλια/βαθμολογία)
     */
    private void executeDatabaseLookup() {
        String keyword = searchFieldDB.getText().trim();// Λήψη και καθαρισμός κειμένου από το πεδίο

        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Εισάγετε λέξη-κλειδί!", "Προσοχή", JOptionPane.WARNING_MESSAGE);
            return; // Αν το πεδίο είναι κενό, δεν κάνουμε τίποτα
        }

        // Καθαρισμός πίνακα
        dbModel.setRowCount(0);

        try {
            // Αποθήκευσε στην βάση κατάλληλα το keyword που έδωσε ο χρήστης (για στατιστικά κτλ).
            dbManager.trackKeyword(keyword);
            // Κλήση της μεθόδου αναζήτησης από τη DBManager
            List<Article> results = dbManager.searchLocalArticles(keyword);

            if (results.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Δεν βρέθηκαν άρθρα για: " + keyword);
                return;
            }
            // Προσθήκη των αποτελεσμάτων στις γραμμές του πίνακα
            for (Article a : results) {
                dbModel.addRow(new Object[] {
                    a.getPageId(),
                    a.getTitle(),
                    // Προβολή ονόματος κατηγορίας (αν υπάρχει)
                    (a.getCategory() != null) ? a.getCategory().getName() : "Χωρίς Κατηγορία",
                    a.getRating(),
                    // Εμφάνιση των σχολίων από τη ΒΔ (αν υπάρχουν)
                    (a.getComments() != null) ? a.getComments() : ""
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Σφάλμα αναζήτησης: " + ex.getMessage());
        }
    }
	// καθάρισε τα αποτελέσματα
        private void clearSearch() {
        searchFieldDB.setText("");
        // Καθαρισμός πίνακα
        dbModel.setRowCount(0);
    }
} 
