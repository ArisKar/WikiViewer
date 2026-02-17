package gr.eap.wikiviewer.gui;

import gr.eap.wikiviewer.model.Article;
import gr.eap.wikiviewer.model.Category;
import gr.eap.wikiviewer.service.DBManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Κλάση που υλοποιεί το Panel της Βιβλιοθήκης (Αποθηκευμένα Άρθρα).
 * Επιτρέπει την προβολή, το φιλτράρισμα και την επεξεργασία των άρθρων της ΒΔ.
 */
public class LibraryPanel extends JPanel {
    private final DBManager dbManager;          // Ο κύριος πίνακας προβολής
    private JTable localTable;                  // Το μοντέλο δεδομένων του πίνακα
    private DefaultTableModel localModel;       // Dropdown για φιλτράρισμα ανά κατηγορία
    private JComboBox<Category> categoryFilter; // Σύνδεση με το Service της βάσης

    //COnstructor
    public LibraryPanel(DBManager dbManager) {
        this.dbManager = dbManager;
        setLayout(new BorderLayout());
        initComponents();   // Αρχικοποίηση GUI
    }
    
    //Κατασκευή του οπτικού περιβάλλοντος
    private void initComponents() {
        // Πάνω Panel
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new java.awt.Color(18, 21, 28));

        categoryFilter = new JComboBox < > ();
        //Κουμπί ανανέωσης
        JButton filterBtn = new JButton("↻");
        filterBtn.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 18));
        //Κουμπί προσθήκης κατηγορίας
        JButton addCatBtn = new JButton("Προσθήκη Κατηγορίας");

        // Styling Κουμπιών & ComboBox
        filterBtn.setBackground(new java.awt.Color(18, 21, 28));
        filterBtn.setForeground(new java.awt.Color(51, 102, 204));
        addCatBtn.setBackground(new java.awt.Color(18, 21, 28));
        addCatBtn.setForeground(new java.awt.Color(51, 102 ,204));

        topPanel.add(categoryFilter);
        topPanel.add(filterBtn);
        topPanel.add(addCatBtn);
        
        //Πίνακας αποτελεσμάτων
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
                if (!isRowSelected(row)) { // Εναλλαγή χρωμάτων γραμμών για καλύτερη ανάγνωση (striped effect)
                    c.setBackground(row % 2 == 0 ? new java.awt.Color(32, 37, 48) : new java.awt.Color(25, 30, 40));
                    c.setForeground(new java.awt.Color(230, 230, 230));
                } else {// Χρώμα όταν μια γραμμή είναι επιλεγμένη
                    c.setBackground(new java.awt.Color(51, 102, 204));
                    c.setForeground(java.awt.Color.WHITE);
                }
                return c;
            }
        };
        
        // Styling του Πίνακα για Dark Mode
        localTable.setBackground(new java.awt.Color(18, 21, 28));
        localTable.setForeground(new  java.awt.Color(51, 102, 204));
        localTable.setFillsViewportHeight(true);
        localTable.setRowHeight(25);
        localTable.setShowGrid(false);

        // Header & Στήλες
        javax.swing.table.JTableHeader header = localTable.getTableHeader();
        header.setBackground(new java.awt.Color(18, 21, 28));
        header.setForeground(new java.awt.Color(51, 102, 204));

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
        editBtn.setBackground(new java.awt.Color(18, 21, 28));
        editBtn.setForeground(new java.awt.Color(51, 102, 204));
        bottomPanel.add(editBtn);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Listeners
        filterBtn.addActionListener(e -> loadLocalArticles());
        addCatBtn.addActionListener(e -> addCategory());
        editBtn.addActionListener(e -> editSelectedArticle());

        this.setBackground(new java.awt.Color(18, 21, 28));
        this.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }


    // Φόρτωσε τα σχετικά άρθα απο την βάση. Ανα κατηγορία.
    public void loadLocalArticles() {
        
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
    //Επεξεργασία επιλεγμένης κατηγορίας
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
}