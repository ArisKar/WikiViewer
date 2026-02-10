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
    private JComboBox<Category> categoryFilter;

    public LibraryPanel(DBManager dbManager) {
        this.dbManager = dbManager;
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel();
        categoryFilter = new JComboBox<>();
        JButton filterBtn = new JButton("Εμφάνιση Αποθηκευμένων");
        JButton addCatBtn = new JButton("Προσθήκη Κατηγορίας");
        topPanel.add(new JLabel("Κατηγορία:"));
        topPanel.add(categoryFilter);
        topPanel.add(filterBtn);
        topPanel.add(addCatBtn);

        localModel = new DefaultTableModel(new String[]{"ID Σελίδας", "Τίτλος", "Κατηγορία", "Βαθμολογία", "Σχόλια"}, 0);
        localTable = new JTable(localModel) {
        @Override
        public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
            Component c = super.prepareRenderer(renderer, row, column);
            // Αν η γραμμή δεν είναι επιλεγμένη
            if (!isRowSelected(row)) {
                // Χρωμάτισε τις ζυγές γραμμές με ένα ελαφρύ γκρι
                c.setBackground(row % 2 == 0 ? getBackground() : new Color(240, 240, 240));
            }
            return c;
        }
        };

        localTable.setRowHeight(25); // Αυξάνει το ύψος για να φαίνεται πιο καθαρά το κείμενο
        localTable.setShowGrid(false); // Κρύβει τις γραμμές του πλέγματος για πιο clean look
        localTable.setIntercellSpacing(new Dimension(0, 0)); // Μειώνει τα κενά ανάμεσα στα κελιά
        
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(localTable), BorderLayout.CENTER);
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JButton editBtn = new JButton("Επεξεργασία Επιλεγμένου");
        add(editBtn, BorderLayout.SOUTH);

        // Ενέργειες Παραθύρου
        // Πατάμε το κουμπί του Φίλτρο (βάση κατηγορίας επιλογής)
        filterBtn.addActionListener(e -> loadLocalArticles());
        // Προσθήκη νέας κατηγορίας.
        addCatBtn.addActionListener(e -> addCategory());
        // Επεξεργασία Αποθηκευμένου Άρθρου
        editBtn.addActionListener(e -> editSelectedArticle());
    }

    // Φόρτωσε τα σχετικά άρθα απο την βάση
    public void loadLocalArticles() {
        Category selected = (Category) categoryFilter.getSelectedItem();
        List<Article> articles;
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
        for (Article a : articles) {
            localModel.addRow(new Object[]{
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
        categoryFilter.removeAllItems();
        categoryFilter.addItem(new Category("Όλες"));
        List<Category> cats = dbManager.getAllCategories();
        for (Category c : cats) {
            // προσθήκη κάθε κατηγορίας που βρήκαμε στο dropdown menu.
            categoryFilter.addItem(c);
        }
    }

    // Προσθήκη Νέας Κατηγορίας
    private void addCategory() {
        String name = JOptionPane.showInputDialog(this, "Πρόσθεσε Όνομα νέας Κατηγορίας:");
        if (name != null && !name.trim().isEmpty()) {
            int result = dbManager.saveCategory(name.trim());
            if (result == 1) {
                JOptionPane.showMessageDialog(this, "Η κατηγορία προστέθηκε επιτυχώς!");
                loadCategories(); // Ανανέωση του dropdown
            } else {
                JOptionPane.showMessageDialog(this, "Η κατηγορία '" + name + "' υπάρχει ήδη στη βάση!", 
                        "Προσοχή", JOptionPane.WARNING_MESSAGE);
                }
            }
       }

    private void editSelectedArticle() {
        // Επιλογή του άρθρου
        int row = localTable.getSelectedRow();
        if (row == -1) return;

        int pageID = (int) localModel.getValueAt(row, 0);
        
        // Εύρεση του αντίστοιχου άρθρου από τη βάση.
        List<Article> all = dbManager.getAllArticles();
        Article target = null;
        for (Article a : all) {
            if (a.getPageId()== pageID) {
                target = a;
                break;
            }
        }
        // Τερματισμός αν δεν βρεθεί άρθρο.
        if (target == null) return;

        // Δημιουργία GUI επεξεργασίας άρθρου.
        JPanel editPanel = new JPanel(new GridLayout(0, 1));
        
        // ComboBox κατηγοριών
        JComboBox<Category> catBox = new JComboBox<>();
        List<Category> cats = dbManager.getAllCategories();
        for (Category c : cats) catBox.addItem(c);
        
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
            int rating;
            try {
                rating = Integer.parseInt(ratingField.getText().trim());
                } 
            catch (NumberFormatException e) {
                rating = 0;
                }
            target.setRating(rating);
            target.setComments(commentArea.getText());
            dbManager.saveArticle(target);
            loadLocalArticles();
        }
        // Στο CANCEL δεν κάνουμε τίποτα. Το παράθυρο κλείνει.
    }
}
