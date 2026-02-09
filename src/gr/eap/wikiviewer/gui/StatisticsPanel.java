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
        statsModel = new DefaultTableModel(new String[]{"Statistic", "Value"}, 0);
        statsTable = new JTable(statsModel);
        add(new JScrollPane(statsTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        JButton refreshBtn = new JButton("Refresh Stats");
        JButton pdfBtn = new JButton("Export to PDF");
        bottomPanel.add(refreshBtn);
        bottomPanel.add(pdfBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> loadStats());
        pdfBtn.addActionListener(e -> exportStatsToPdf());
    }

    public void loadStats() {
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
}
