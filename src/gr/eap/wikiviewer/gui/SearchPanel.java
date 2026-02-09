package gr.eap.wikiviewer.gui;

import gr.eap.wikiviewer.model.Article;
import gr.eap.wikiviewer.service.DBManager;
import gr.eap.wikiviewer.service.WikipediaService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SearchPanel extends JPanel {
    private final WikipediaService wikiService;
    private final DBManager dbManager;
    private final MainFrame parent;

    private JTable searchTable;
    private DefaultTableModel searchModel;
    private JTextField searchField;

    public SearchPanel(MainFrame parent, WikipediaService wikiService, DBManager dbManager) {
        this.parent = parent;
        this.wikiService = wikiService;
        this.dbManager = dbManager;
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel();
        searchField = new JTextField(30);
        JButton searchBtn = new JButton("Αναζήτηση");
        topPanel.add(new JLabel("Λέξη κλειδί:"));
        topPanel.add(searchField);
        topPanel.add(searchBtn);

        searchModel = new DefaultTableModel(new String[]{"Τίτλος", "Απόσπασμα", "ID Σελίδας"}, 0);
        searchTable = new JTable(searchModel);
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(searchTable), BorderLayout.CENTER);

        JButton saveBtn = new JButton("Αποθήκευση Επιλεγμένου");
        add(saveBtn, BorderLayout.SOUTH);
        
        // Ενέργειες Παραθύρου
        // Λειτουργία Αναζήτησης. Πατάμε το κουμπί Αναζήτηση.
        searchBtn.addActionListener(e -> performSearch());
        // Λειτουργία Αποθήκευσης. Πατάμε αποθήκευση του άρθου που θέλουμε.
        saveBtn.addActionListener(e -> saveSelectedArticle());
    }

    private void performSearch() {
        // Αφαίρεση τυχόν κενών απο αρχή και τέλος.
        String query = searchField.getText().trim();
        // Ελέγχουμε αν το πεδίο είναι κενό. Αν είναι δεν κάνουμε τίποτα.
        if (query.isEmpty()) return;

        try {
            // Αποθήκευσε στην βάση κατάλληλα το keyword που έδωσε ο χρήστης (για στατιστικά κτλ).
            dbManager.trackKeyword(query);
            //Επιστρέφει τη λίστα,  κενή αν δεν βρέθηκαν αποτελέσματα. Δες υλοποίηση στην wikiService.
            List<Article> results = wikiService.searchArticles(query);
            // Αφαιρούμε όλες τις προηγούμενες γραμμές από τον πίνακα, πχ απο κάποια προηγούμενη αναζήτηση. 
            searchModel.setRowCount(0);
            
            // Δείξε μήνυμα διαλόγου pop-up ότι δεν βρέθηκαν αποτελέσματα.
            if (results.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Δεν βρέθηκαν αποτελέσματα για: \"" + query + "\"","Καμία Εγγραφή", JOptionPane.INFORMATION_MESSAGE);
            return;
            }
            
            // Εμφάνιση Αποτελεσμάτων στον πίνακα. Γραμμή -γραμμή μέσα σε loop.
            for (Article a : results) {
                searchModel.addRow(new Object[]{a.getTitle(), a.getSnippet(), a.getPageId()});
            }
         // Catch, εμφανίζουμε τυχόν σφάλματα σχετικά με το search.   
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error searching: " + e.getMessage());
        }
    }
    // Αποθήκευση επιλεγμένου άρθρου.
    private void saveSelectedArticle() {
        int row = searchTable.getSelectedRow();
        if (row == -1) return;

        String title = (String) searchModel.getValueAt(row, 0);
        String snippet = (String) searchModel.getValueAt(row, 1);
        Integer pageId = (Integer) searchModel.getValueAt(row, 2);

        Article existing = dbManager.getArticleByPageId(pageId);
        if (existing != null) {
            // Υπάρχει ήδη το άρθρο στην βάση.
            JOptionPane.showMessageDialog(this, "Το άρθρο υπάρχει ήδη!");
            return;
        }
        // Αν δεν υπάρχει ήδη, δημιούργισε νέο άρθρο και αποθήκευσέ το στην βάση.
        Article article = new Article();
        article.setTitle(title);
        article.setSnippet(snippet);
        article.setPageId(pageId);
        
        dbManager.saveArticle(article);
        JOptionPane.showMessageDialog(this, "Το άρθρο αποθηκεύτηκε!");
        parent.refreshLocalArticles();
    }
    
    
}
