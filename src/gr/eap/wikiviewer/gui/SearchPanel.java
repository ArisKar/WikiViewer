package gr.eap.wikiviewer.gui;

import gr.eap.wikiviewer.model.Article;
import gr.eap.wikiviewer.service.DBManager;
import gr.eap.wikiviewer.service.WikipediaService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;


/**
 * Κλάση που υλοποιεί το Panel αναζήτησης άρθρων live από το API της Wikipedia.
 * Περιλαμβάνει τη διεπαφή χρήστη και τη λογική επικοινωνίας με το API και τη ΒΔ.
 */

public class SearchPanel extends JPanel {
    // Στοιχεία Διεπαφής (GUI Components)
    private final WikipediaService wikiService;
    private final DBManager dbManager;
    private final MainFrame parent;

    private JTable searchTable;
    private DefaultTableModel searchModel;
    private JTextField searchField;

    //Constructor
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
       

        searchField = new JTextField(30);
        searchField.setCaretColor(java.awt.Color.WHITE);
        searchField.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 102, 204)));

        // Κουμπί Αναζήτησης
        JButton searchBtn = new JButton("🔍");
        searchBtn.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14));
        searchBtn.setFocusPainted(false);

        //Περισγραφή του search
        JLabel searchLabel = new JLabel("Λέξη κλειδί:");
        
        searchLabel.setFont(searchLabel.getFont().deriveFont(java.awt.Font.BOLD));
        topPanel.add(searchLabel);
        topPanel.add(searchField);
        topPanel.add(searchBtn);

        // Πίνακας αποτελεσμάτων
        searchModel = new DefaultTableModel(new String[] {
            "Τίτλος",
            "Απόσπασμα",
            "ID Σελίδας"
        }, 0) {          // να μην μπορεί να γίνει edit απο τον χρήστη.  
            @Override
            public boolean isCellEditable(int row, int column) {
                // Επιστρέφοντας πάντα false, ο πίνακας γίνεται read-only
                return false;
            }
        };
        searchTable = new JTable(searchModel) {
            @Override
            public java.awt.Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                java.awt.Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) { // Striped effect για ευκολότερη ανάγνωση
                    c.setBackground(row % 2 == 0 ? new java.awt.Color(32, 37, 48) : new java.awt.Color(25, 30, 40));
                    c.setForeground(new java.awt.Color(230, 230, 230));
                } else { // Χρώμα όταν μια γραμμή είναι επιλεγμένη
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
        header.setBackground(new java.awt.Color(18, 21, 28));
        header.setForeground(new java.awt.Color(51, 102, 204));
        header.setFont(header.getFont().deriveFont(java.awt.Font.BOLD));

        // Ρυθμίσεις Στηλών
        javax.swing.table.TableColumnModel cm = searchTable.getColumnModel();
        cm.getColumn(0).setPreferredWidth(200); // Τίτλος
        cm.getColumn(1).setPreferredWidth(500); // Απόσπασμα
        cm.getColumn(2).setPreferredWidth(80); // ID

        // ScrollPane
        JScrollPane scrollPane = new JScrollPane(searchTable);
        scrollPane.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 102, 204)));

        // Κάτω Panel
        JPanel bottomPanel = new JPanel();
        JButton saveBtn = new JButton(" 💾 ");
        saveBtn.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 24));
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

        this.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    private void performSearch() {
        // Αφαίρεση τυχόν κενών απο αρχή και τέλος.
        String query = searchField.getText().trim();
        // Ελέγχουμε αν το πεδίο είναι κενό. Αν είναι δεν κάνουμε τίποτα.
        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Παρακαλώ εισάγετε νέα λέξη-κλειδι!",
                "Κενό Πεδίο",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Αποθήκευσε στην βάση κατάλληλα το keyword που έδωσε ο χρήστης (για στατιστικά κτλ).
            dbManager.trackKeyword(query);
            //Επιστρέφει τη λίστα,  κενή αν δεν βρέθηκαν αποτελέσματα. Δες υλοποίηση στην wikiService.
            List < Article > results = wikiService.searchArticles(query);
            // Αφαιρούμε όλες τις προηγούμενες γραμμές από τον πίνακα, πχ απο κάποια προηγούμενη αναζήτηση. 
            searchModel.setRowCount(0);

            // Δείξε μήνυμα διαλόγου pop-up ότι δεν βρέθηκαν αποτελέσματα.
            if (results.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Δεν βρέθηκαν αποτελέσματα για: \"" + query + "\"", "Καμία Εγγραφή", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Εμφάνιση Αποτελεσμάτων στον πίνακα. Γραμμή -γραμμή μέσα σε loop.
            for (Article a: results) {
                searchModel.addRow(new Object[] {
                    a.getTitle(), a.getSnippet(), a.getPageId()
                });
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

        //refresh τα local articles στο tap του κεντρικού menu "Αποθηκευμένα Άρθρα" 
        //parent.refreshLocalArticles();
        parent.refreshLocalCategories();
    }


}