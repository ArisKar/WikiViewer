package gr.eap.wikiviewer.gui;

import gr.eap.wikiviewer.model.Article;
import gr.eap.wikiviewer.model.Category;
import gr.eap.wikiviewer.service.DBManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class LibraryPanel extends JPanel {
    private final DBManager dbManager;
    private JTable localTable;
    private DefaultTableModel localModel;
    private JComboBox < Category > categoryFilter;
    private JTextField searchFieldLocal;

    public LibraryPanel(DBManager dbManager) {
        this.dbManager = dbManager;
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        // Πάνω Panel
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new java.awt.Color(18, 21, 28));

        categoryFilter = new JComboBox < > ();
        JButton filterBtn = new JButton("Εμφάνιση Αποθηκευμένων");
        JButton addCatBtn = new JButton("Προσθήκη Κατηγορίας");

        // Styling Κουμπιών & ComboBox
        filterBtn.setBackground(new java.awt.Color(51, 102, 204));
        filterBtn.setForeground(java.awt.Color.WHITE);
        addCatBtn.setBackground(new java.awt.Color(51, 102, 204));
        addCatBtn.setForeground(java.awt.Color.WHITE);

        JLabel catLabel = new JLabel("Κατηγορία:");
        catLabel.setForeground(new java.awt.Color(51, 102, 204));
        catLabel.setFont(catLabel.getFont().deriveFont(java.awt.Font.BOLD));

        topPanel.add(catLabel);
        topPanel.add(categoryFilter);
        topPanel.add(filterBtn);
        topPanel.add(addCatBtn);

        // Δεύτερο Panel, αναζήτηση με keyword.
        JPanel searchPanel = new JPanel();
        searchPanel.setBackground(new java.awt.Color(18, 21, 28));

        JLabel searchLabel = new JLabel("🔍 Αναζήτηση στην ΒΔ:");
        searchLabel.setForeground(new java.awt.Color(51, 102, 204));
        searchLabel.setFont(searchLabel.getFont().deriveFont(java.awt.Font.BOLD));

        searchFieldLocal = new JTextField(25);
        searchFieldLocal.setBackground(new java.awt.Color(32, 37, 48));
        searchFieldLocal.setForeground(java.awt.Color.WHITE);
        searchFieldLocal.setCaretColor(java.awt.Color.WHITE);
        searchFieldLocal.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 102, 204)));

        JButton keywordSearchBtn = new JButton("Αναζήτηση Keyword ΒΔ");
        keywordSearchBtn.setBackground(new java.awt.Color(40, 167, 69)); // Πράσινο για διαφοροποίηση
        keywordSearchBtn.setForeground(java.awt.Color.WHITE);
        keywordSearchBtn.setFocusPainted(false);

        //JButton clearSearchBtn = new JButton("Καθαρισμός");
        //clearSearchBtn.setBackground(new java.awt.Color(108, 117, 125)); // Γκρι
        //clearSearchBtn.setForeground(java.awt.Color.WHITE);
        //clearSearchBtn.setFocusPainted(false);

        searchPanel.add(searchLabel);
        searchPanel.add(searchFieldLocal);
        searchPanel.add(keywordSearchBtn);
        //searchPanel.add(clearSearchBtn);

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setBackground(new java.awt.Color(18, 21, 28));
        topContainer.add(topPanel);
        topContainer.add(searchPanel);


        // Πίνακας
        localModel = new DefaultTableModel(new String[] {
            "ID Σελίδα",
            "Τίτλος",
            "Κατηγορία",
            "Βαθμολογία",
            "Σχόλια"
        }, 0);
        localTable = new JTable(localModel) {
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

        localTable.setBackground(new java.awt.Color(32, 37, 48));
        localTable.setFillsViewportHeight(true);
        localTable.setRowHeight(25);
        localTable.setShowGrid(false);

        // Header & Στήλες
        javax.swing.table.JTableHeader header = localTable.getTableHeader();
        header.setBackground(new java.awt.Color(51, 102, 204));
        header.setForeground(java.awt.Color.WHITE);

        javax.swing.table.TableColumnModel cm = localTable.getColumnModel();
        cm.getColumn(0).setPreferredWidth(80); // ID
        cm.getColumn(1).setPreferredWidth(250); // Τίτλος
        cm.getColumn(2).setPreferredWidth(100); // Κατηγορία
        cm.getColumn(3).setPreferredWidth(80); // Βαθμολογία
        cm.getColumn(4).setPreferredWidth(150); // Σχόλια

        // ScrollPane
        JScrollPane scrollPane = new JScrollPane(localTable);
        scrollPane.getViewport().setBackground(new java.awt.Color(18, 21, 28));
        scrollPane.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 102, 204)));

        // Κάτω Panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new java.awt.Color(18, 21, 28));
        JButton editBtn = new JButton("Επεξεργασία Επιλεγμένου");
        editBtn.setBackground(new java.awt.Color(51, 102, 204));
        editBtn.setForeground(java.awt.Color.WHITE);
        bottomPanel.add(editBtn);

        add(topContainer, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Listeners
        filterBtn.addActionListener(e -> loadLocalArticles());
        addCatBtn.addActionListener(e -> addCategory());
        editBtn.addActionListener(e -> editSelectedArticle());

        keywordSearchBtn.addActionListener(e -> performKeywordSearch());
        searchFieldLocal.addActionListener(e -> performKeywordSearch());

        this.setBackground(new java.awt.Color(18, 21, 28));
        this.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }


    // Φόρτωσε τα σχετικά άρθα απο την βάση
    public void loadLocalArticles() {
        // Καθάρισε το textbox αν έχει τιμή. 
        searchFieldLocal.setText("");

        Category selected = (Category) categoryFilter.getSelectedItem();
        List < Article > articles;
        // Αν δεν έχω επιλέξει κάποια κατηγορία ή έχω επιλέξει όλες, φέρε όλα τα άρθα
        if (selected == null || selected.getName().equals("Όλες")) {
            articles = dbManager.getAllArticles();
        } else {
            // αλλιώς, φέρε μου το επιλεγμένο.
            articles = dbManager.getArticlesByCategory(selected);
        }

        // Αφαιρούμε όλες τις προηγούμενες γραμμές από τον πίνακα, πχ απο κάποια προηγούμενη αναζήτηση. 
        localModel.setRowCount(0);
        // Τοποθετούμε γραμμή - γραμμή τα αποτελέσμα της αναζήτησης βάση κατηγορίας επιλογής
        for (Article a: articles) {
            localModel.addRow(new Object[] {
                a.getPageId(),
                    a.getTitle(),
                    a.getCategory() != null ? a.getCategory().getName() : "None",
                    a.getRating(),
                    a.getComments()
            });
        }
    }
    // Κάνε retrive όλες τις κατηγορίες απο την βάση στο dropdown menu.
    public void loadCategories() {
        // Αποθήκευση της τρέχουσας επιλογής (αν υπάρχει)
        Category currentSelection = (Category) categoryFilter.getSelectedItem();

        categoryFilter.removeAllItems();
        categoryFilter.addItem(new Category("Όλες"));

        List < Category > cats = dbManager.getAllCategories();
        for (Category c: cats) {
            // προσθήκη κάθε κατηγορίας που βρήκαμε στο dropdown menu.
            categoryFilter.addItem(c);
        }

        // Επαναφορά της προηγούμενης επιλογής (αν υπήρχε)
        if (currentSelection != null) {
            // Ψάξε να βρεις την κατηγορία με το ίδιο όνομα
            for (int i = 0; i < categoryFilter.getItemCount(); i++) {
                Category item = categoryFilter.getItemAt(i);
                if (item.getName().equals(currentSelection.getName())) {
                    categoryFilter.setSelectedItem(item);
                    return; // Βρέθηκε και επιλέχθηκε
                }
            }
        }

        // Αν δεν υπήρχε προηγούμενη επιλογή ή δεν βρέθηκε, επίλεξε "Όλες"
        categoryFilter.setSelectedIndex(0);
    }

    // Προσθήκη Νέας Κατηγορίας
    private void addCategory() {
        String name = JOptionPane.showInputDialog(this, "Πρόσθεσε Όνομα νέας Κατηγορίας:");
        if (name != null && !name.trim().isEmpty()) {
            int result = dbManager.saveCategory(name.trim());

            switch (result) {
                case 1: // Επιτυχία
                    JOptionPane.showMessageDialog(this,
                        "Η κατηγορία προστέθηκε επιτυχώς!",
                        "Επιτυχία",
                        JOptionPane.INFORMATION_MESSAGE);
                    loadCategories();
                    break;

                case 0: // Υπάρχει ήδη
                    JOptionPane.showMessageDialog(this,
                        "Η κατηγορία '" + name + "' υπάρχει ήδη στη βάση!",
                        "Προσοχή",
                        JOptionPane.WARNING_MESSAGE);
                    break;

                case -1: // Σφάλμα
                    JOptionPane.showMessageDialog(this,
                        "Προέκυψε σφάλμα κατά την αποθήκευση της κατηγορίας.",
                        "Σφάλμα",
                        JOptionPane.ERROR_MESSAGE);
                    break;
            }
        }
    }

    private void editSelectedArticle() {
        // Επιλογή του άρθρου
        int row = localTable.getSelectedRow();
        if (row == -1) return;

        int pageID = (int) localModel.getValueAt(row, 0);

        // Εύρεση του αντίστοιχου άρθρου από τη βάση.
        List < Article > all = dbManager.getAllArticles();
        Article target = null;
        for (Article a: all) {
            if (a.getPageId() == pageID) {
                target = a;
                break;
            }
        }
        // Τερματισμός αν δεν βρεθεί άρθρο.
        if (target == null) return;

        // Δημιουργία GUI επεξεργασίας άρθρου.
        JPanel editPanel = new JPanel(new GridLayout(0, 1));

        // ComboBox κατηγοριών
        JComboBox < Category > catBox = new JComboBox < > ();
        List < Category > cats = dbManager.getAllCategories();
        for (Category c: cats) catBox.addItem(c);

        // Έλεγχος αν το άρθρο έχει ήδη ανατεθεί σε κάποια κατηγορία
        if (target.getCategory() != null) {
            // Διατρέχουμε όλα τα στοιχεία που προστέθηκαν στο ComboBox.
            for (int i = 0; i < catBox.getItemCount(); i++) {
                // Συγκρίνουμε το όνομα της κατηγορίας στη λίστα με το όνομα της κατηγορίας του άρθρου.
                if (catBox.getItemAt(i).getName().equals(target.getCategory().getName())) {
                    // Αν βρεθεί ταύτιση επιλέγουμε το αντικείμενο από τη λίστα του ComboBox για καλύτερη εμπειρία UI.
                    catBox.setSelectedItem(catBox.getItemAt(i));
                    break;
                }
            }
        }


        // Πεδία εισαγωγής βαθμολογίας και σχολίων.
        JTextField ratingField = new JTextField(String.valueOf(target.getRating() != null ? target.getRating() : ""));
        JTextArea commentArea = new JTextArea(target.getComments(), 5, 20);

        // Στοιχεία πάνελ UI
        editPanel.add(new JLabel("Κατηγορία:"));
        editPanel.add(catBox);
        editPanel.add(new JLabel("Βαθμολογία (1-5):"));
        editPanel.add(ratingField);
        editPanel.add(new JLabel("Σχόλια:"));
        editPanel.add(new JScrollPane(commentArea));

        // Εμφάνιση διαλόγου επεξεργασίας.
        int result = JOptionPane.showConfirmDialog(this, editPanel, "Επεξεργασία Άρθρου", JOptionPane.OK_CANCEL_OPTION);
        // OK ή CANCEL επιλογές.
        // Αποθήκευση αλλαγών αν πατηθεί OK.
        if (result == JOptionPane.OK_OPTION) {
            target.setCategory((Category) catBox.getSelectedItem());

            // Έλεγχος βαθμολογίας
            Integer rating;
            try {
                String ratingText = ratingField.getText().trim();
                // Αν είναι κενό, θέτουμε 0 (χωρίς βαθμολογία)
                if (ratingText.isEmpty()) {
                    rating = null;
                } else {
                    rating = Integer.parseInt(ratingText);

                    // Έλεγχος εύρους βαθμολογίας
                    if (rating < 1 || rating > 5) {
                        JOptionPane.showMessageDialog(this,
                            "Η βαθμολογία θα πρέπει να είναι μεταξύ 1 και 5",
                            "Δώσατε λάθος Βαθμολογία",
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                // σφάλμα, δώσαμε όχι αριθμό για βαθμολογία.
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                    "Παρακαλώ εισάγετε έγκυρο αριθμό για τη βαθμολογία [1-5]!",
                    "Μη έγκυρη",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            target.setRating(rating);
            target.setComments(commentArea.getText());
            dbManager.saveArticle(target);
            loadLocalArticles();
        }
        // Στο CANCEL δεν κάνουμε τίποτα. Το παράθυρο κλείνει.
    }

    private void performKeywordSearch() {
        // Λάβε την λέξη κλειδί
        String keyword = searchFieldLocal.getText().trim();

        // Είναι κενό?
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Παρακαλώ εισάγετε νέα λέξη-κλειδι!",
                "Κενό Πεδίο",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        resetCategoryFilterToAll();
        
        dbManager.trackKeyword(keyword);
        // Αναζήτηση στη βάση.
        List < Article > results = dbManager.searchLocalArticles(keyword);

        // Καθαρισμός πίνακα.
        localModel.setRowCount(0);

        // Έλεγχος αποτελεσμάτων.
        if (results.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Δεν βρέθηκαν αποθηκευμένα άρθρα με το keyword: \"" + keyword + "\"",
                "Καμία Εγγραφή",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Προβολή αποτελεσμάτων
        for (Article a: results) {
            localModel.addRow(new Object[] {
                a.getPageId(),
                    a.getTitle(),
                    a.getCategory() != null ? a.getCategory().getName() : "None",
                    a.getRating(),
                    a.getComments()
            });
        }
    }

    /**
     * Επαναφέρει το category filter στην επιλογή "Όλες".
     * Αποφεύγεται η σύγχηση dropdown search και επιλογής με keyword.
     */
    private void resetCategoryFilterToAll() {
        // Αναζήτηση του item "Όλες" στο ComboBox
        for (int i = 0; i < categoryFilter.getItemCount(); i++) {
            Category item = categoryFilter.getItemAt(i);
            if (item.getName().equals("Όλες")) {
                categoryFilter.setSelectedIndex(i);
                return;
            }
        }
    }
}