package gr.eap.wikiviewer.gui;

import gr.eap.wikiviewer.model.Article;
import gr.eap.wikiviewer.model.Category;
import gr.eap.wikiviewer.model.SearchKeyword;
import gr.eap.wikiviewer.service.DBManager;
import gr.eap.wikiviewer.service.PDFService;
import gr.eap.wikiviewer.service.WikipediaService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class MainFrame extends JFrame {
    private final WikipediaService wikiService = new WikipediaService();
    private final DBManager dbManager = DBManager.getInstance();
    private final PDFService pdfService = new PDFService();

    private JTabbedPane tabbedPane;
    private JTable searchTable, localTable, statsTable;
    private DefaultTableModel searchModel, localModel, statsModel;
    private JTextField searchField;
    private JComboBox<Category> categoryFilter;

    public MainFrame() {
        setTitle("WikiViewer - Wikipedia Information Browser");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        loadCategories();
    }

    private void initComponents() {
        tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Search (Live API)", createSearchPanel());
        tabbedPane.addTab("My Saved Articles", createLocalArticlesPanel());
        tabbedPane.addTab("Statistics", createStatsPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel topPanel = new JPanel();
        searchField = new JTextField(30);
        JButton searchBtn = new JButton("Search");
        topPanel.add(new JLabel("Keyword:"));
        topPanel.add(searchField);
        topPanel.add(searchBtn);

        searchModel = new DefaultTableModel(new String[]{"Title", "Snippet", "PageID"}, 0);
        searchTable = new JTable(searchModel);
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(searchTable), BorderLayout.CENTER);

        JButton saveBtn = new JButton("Save Selected Article");
        panel.add(saveBtn, BorderLayout.SOUTH);

        searchBtn.addActionListener(e -> performSearch());
        saveBtn.addActionListener(e -> saveSelectedArticle());

        return panel;
    }

    private JPanel createLocalArticlesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel topPanel = new JPanel();
        categoryFilter = new JComboBox<>();
        JButton filterBtn = new JButton("Filter");
        JButton addCatBtn = new JButton("Add Category");
        topPanel.add(new JLabel("Category:"));
        topPanel.add(categoryFilter);
        topPanel.add(filterBtn);
        topPanel.add(addCatBtn);

        localModel = new DefaultTableModel(new String[]{"Title", "Category", "Rating", "Comments"}, 0);
        localTable = new JTable(localModel);
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(localTable), BorderLayout.CENTER);

        JButton editBtn = new JButton("Edit Selected");
        panel.add(editBtn, BorderLayout.SOUTH);

        filterBtn.addActionListener(e -> loadLocalArticles());
        addCatBtn.addActionListener(e -> addCategory());
        editBtn.addActionListener(e -> editSelectedArticle());

        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        statsModel = new DefaultTableModel(new String[]{"Statistic", "Value"}, 0);
        statsTable = new JTable(statsModel);
        panel.add(new JScrollPane(statsTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        JButton refreshBtn = new JButton("Refresh Stats");
        JButton pdfBtn = new JButton("Export to PDF");
        bottomPanel.add(refreshBtn);
        bottomPanel.add(pdfBtn);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> loadStats());
        pdfBtn.addActionListener(e -> exportStatsToPdf());

        return panel;
    }

    // --- Logic Methods ---

    private void performSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) return;

        try {
            dbManager.trackKeyword(query);
            List<Article> results = wikiService.searchArticles(query);
            searchModel.setRowCount(0);
            for (Article a : results) {
                searchModel.addRow(new Object[]{a.getTitle(), a.getSnippet(), a.getPageId()});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error searching: " + e.getMessage());
        }
    }

    private void saveSelectedArticle() {
        int row = searchTable.getSelectedRow();
        if (row == -1) return;

        String title = (String) searchModel.getValueAt(row, 0);
        String snippet = (String) searchModel.getValueAt(row, 1);
        Integer pageId = (Integer) searchModel.getValueAt(row, 2);

        Article existing = dbManager.getArticleByPageId(pageId);
        if (existing != null) {
            JOptionPane.showMessageDialog(this, "Article already saved!");
            return;
        }

        Article article = new Article();
        article.setTitle(title);
        article.setSnippet(snippet);
        article.setPageId(pageId);
        
        dbManager.saveArticle(article);
        JOptionPane.showMessageDialog(this, "Article saved!");
        loadLocalArticles();
    }

    private void loadLocalArticles() {
        Category selected = (Category) categoryFilter.getSelectedItem();
        List<Article> articles;
        if (selected == null || selected.getName().equals("All")) {
            articles = dbManager.getAllArticles();
        } else {
            articles = dbManager.getArticlesByCategory(selected);
        }

        localModel.setRowCount(0);
        for (Article a : articles) {
            localModel.addRow(new Object[]{
                a.getTitle(), 
                a.getCategory() != null ? a.getCategory().getName() : "None", 
                a.getRating(), 
                a.getComments()
            });
        }
    }

    private void loadCategories() {
        categoryFilter.removeAllItems();
        categoryFilter.addItem(new Category("All"));
        List<Category> cats = dbManager.getAllCategories();
        for (Category c : cats) {
            categoryFilter.addItem(c);
        }
    }

    private void addCategory() {
        String name = JOptionPane.showInputDialog(this, "Enter category name:");
        if (name != null && !name.trim().isEmpty()) {
            dbManager.saveCategory(new Category(name.trim()));
            loadCategories();
        }
    }

    private void editSelectedArticle() {
        int row = localTable.getSelectedRow();
        if (row == -1) return;

        String title = (String) localModel.getValueAt(row, 0);
        List<Article> all = dbManager.getAllArticles();
        Article target = null;
        for (Article a : all) {
            if (a.getTitle().equals(title)) {
                target = a;
                break;
            }
        }

        if (target == null) return;

        // Simple edit dialog
        JPanel editPanel = new JPanel(new GridLayout(0, 1));
        JComboBox<Category> catBox = new JComboBox<>();
        List<Category> cats = dbManager.getAllCategories();
        for (Category c : cats) catBox.addItem(c);
        if (target.getCategory() != null) catBox.setSelectedItem(target.getCategory());

        JTextField ratingField = new JTextField(String.valueOf(target.getRating() != null ? target.getRating() : ""));
        JTextArea commentArea = new JTextArea(target.getComments(), 5, 20);

        editPanel.add(new JLabel("Category:"));
        editPanel.add(catBox);
        editPanel.add(new JLabel("Rating (1-5):"));
        editPanel.add(ratingField);
        editPanel.add(new JLabel("Comments:"));
        editPanel.add(new JScrollPane(commentArea));

        int result = JOptionPane.showConfirmDialog(this, editPanel, "Edit Article", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            target.setCategory((Category) catBox.getSelectedItem());
            try {
                target.setRating(Integer.parseInt(ratingField.getText()));
            } catch (NumberFormatException e) { target.setRating(0); }
            target.setComments(commentArea.getText());
            dbManager.saveArticle(target);
            loadLocalArticles();
        }
    }

    private void loadStats() {
        statsModel.setRowCount(0);
        List<SearchKeyword> keywords = dbManager.getTopKeywords();
        for (SearchKeyword k : keywords) {
            statsModel.addRow(new Object[]{"Keyword: " + k.getKeyword(), k.getSearchCount()});
        }
        List<Category> cats = dbManager.getAllCategories();
        for (Category c : cats) {
            statsModel.addRow(new Object[]{"Category: " + c.getName(), c.getArticles().size()});
        }
    }

    private void exportStatsToPdf() {
        try {
            String path = "WikiViewer_Stats.pdf";
            pdfService.generateStatisticsReport(path, dbManager.getTopKeywords(), dbManager.getAllCategories());
            JOptionPane.showMessageDialog(this, "Stats exported to " + new File(path).getAbsolutePath());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error exporting PDF: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}
