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
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
    private void initComponents() {
        // Πάνω Panel Αναζήτησης
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new java.awt.Color(18, 21, 28));
    
        searchField = new JTextField(30);
        searchField.setBackground(new java.awt.Color(32, 37, 48));
        searchField.setForeground(java.awt.Color.WHITE);
        searchField.setCaretColor(java.awt.Color.WHITE);
        searchField.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 102, 204)));

        JButton searchBtn = new JButton("Αναζήτηση");
        searchBtn.setBackground(new java.awt.Color(51, 102, 204));
        searchBtn.setForeground(java.awt.Color.WHITE);
        searchBtn.setFocusPainted(false);

        JLabel searchLabel = new JLabel("Λέξη κλειδί:");
        searchLabel.setForeground(new java.awt.Color(51, 102, 204));
        searchLabel.setFont(searchLabel.getFont().deriveFont(java.awt.Font.BOLD));

        topPanel.add(searchLabel);
        topPanel.add(searchField);
        topPanel.add(searchBtn);

        // Δημιουργία Μοντέλου και Πίνακα
        searchModel = new DefaultTableModel(new String[]{"Τίτλος", "Απόσπασμα", "ID Σελίδας"}, 0);
        searchTable = new JTable(searchModel) {
            @Override
            public java.awt.Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                java.awt.Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? new java.awt.Color(32, 37, 48) : new java.awt.Color(25, 30, 40));
                    c.setForeground(new java.awt.Color(230, 230, 230));
                } else {
                    c.setBackground(new java.awt.Color(51, 102, 204));
                    c.setForeground(java.awt.Color.WHITE);
                }
                return c;
            }
        };

        // Ρυθμίσεις Εμφάνισης Πίνακα
        searchTable.setBackground(new java.awt.Color(32, 37, 48));
        searchTable.setFillsViewportHeight(true);
        searchTable.setRowHeight(25);
        searchTable.setShowGrid(false);

        // Ρυθμίσεις Header
        javax.swing.table.JTableHeader header = searchTable.getTableHeader();
        header.setBackground(new java.awt.Color(51, 102, 204));
        header.setForeground(java.awt.Color.WHITE);
        header.setFont(header.getFont().deriveFont(java.awt.Font.BOLD));

        // Ρυθμίσεις Στηλών
        javax.swing.table.TableColumnModel cm = searchTable.getColumnModel();
        cm.getColumn(0).setPreferredWidth(200); // Τίτλος
        cm.getColumn(1).setPreferredWidth(500); // Απόσπασμα
        cm.getColumn(2).setPreferredWidth(80);  // ID

        // ScrollPane
        JScrollPane scrollPane = new JScrollPane(searchTable);
        scrollPane.getViewport().setBackground(new java.awt.Color(18, 21, 28));
        scrollPane.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 102, 204)));

        // Κάτω Panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new java.awt.Color(18, 21, 28));
        JButton saveBtn = new JButton("Αποθήκευση Επιλεγμένου");
        saveBtn.setBackground(new java.awt.Color(51, 102, 204));
        saveBtn.setForeground(java.awt.Color.WHITE);
        saveBtn.setFocusPainted(false);
        bottomPanel.add(saveBtn);

        // Προσθήκη στο Panel
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Listeners
        searchBtn.addActionListener(e -> performSearch());
        searchField.addActionListener(e -> performSearch());
        saveBtn.addActionListener(e -> saveSelectedArticle());

        this.setBackground(new java.awt.Color(18, 21, 28));
        this.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
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
