package gr.eap.wikiviewer.gui;

import gr.eap.wikiviewer.model.SearchKeyword;
import gr.eap.wikiviewer.service.DBManager;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class BarPieChartWindow {

    private final DBManager dbManager;

    public BarPieChartWindow(DBManager dbManager) {
        this.dbManager = dbManager;
    }

    // Μέθοδος που ανοίγει το παράθυρο με Bar και Pie
    public void showWindow() {
        JFrame frame = new JFrame("Top Searches Charts");
        frame.setSize(900, 500);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // GridLayout για να βάλουμε τα δύο charts δίπλα-δίπλα
        JPanel panel = new JPanel(new GridLayout(1, 2));

        panel.add(createBarChart());
        panel.add(createPieChart());

        frame.add(panel);
        frame.setVisible(true);
    }

    // Μέθοδος για Bar Chart
    private ChartPanel createBarChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        try {
            List<SearchKeyword> keywords = dbManager.getTopKeywords();
            for (SearchKeyword k : keywords) {
                dataset.addValue(k.getSearchCount(), "Searches", k.getKeyword());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
            "Error loading keywords for Bar Chart:\n" + e.getMessage(),
            "Database Error",
            JOptionPane.ERROR_MESSAGE);
    }


        JFreeChart chart = ChartFactory.createBarChart(
                "Top Searches (Bar Chart)",
                "Keyword",
                "Count",
                dataset
        );

        return new ChartPanel(chart);
    }

    // Μέθοδος για Pie Chart
    private ChartPanel createPieChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();

        try {
            List<SearchKeyword> keywords = dbManager.getTopKeywords();
            for (SearchKeyword k : keywords) {
            dataset.setValue(k.getKeyword(), k.getSearchCount());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
            "Error loading keywords for Pie Chart:\n" + e.getMessage(),
            "Database Error",
            JOptionPane.ERROR_MESSAGE);
        }

        JFreeChart chart = ChartFactory.createPieChart(
                "Top Searches (Pie Chart)",
                dataset,
                true,   // legend
                true,   // tooltips
                false   // URLs
        );

        // Προσαρμογή εμφάνισης
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelFont(new Font("Arial", Font.PLAIN, 12));
        plot.setNoDataMessage("No data available");
        plot.setCircular(true);
        plot.setLabelGap(0.02);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {1}"));

        return new ChartPanel(chart);
    }
}