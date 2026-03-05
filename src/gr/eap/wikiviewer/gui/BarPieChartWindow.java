package gr.eap.wikiviewer.gui;

import gr.eap.wikiviewer.model.SearchKeyword;
import gr.eap.wikiviewer.model.Category;
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

/**
 * Δημιουργία και εμφάνιση του κεντρικού παραθύρου στατιστικών
 */

public class BarPieChartWindow {

    private final DBManager dbManager;

    public BarPieChartWindow(DBManager dbManager) {
        this.dbManager = dbManager;
    }

    // Μέθοδος που ανοίγει το παράθυρο με Bar και Pie
    public void showWindow() {
        JFrame frame = new JFrame("Στατιστικά Αναζητήσεων");
        frame.setSize(1000, 850);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Κύριο panel με 2 γραμμές
        JPanel mainContainer = new JPanel(new GridLayout(2, 1, 15, 15));
        mainContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Ενότητα για τα στατιστικά των λέξεων-κλειδιών
        JPanel topSection = new JPanel(new GridLayout(1, 2, 10, 10));
        // Ο Πρώτος Τίτλος ως Border. Χάραξη του εκάστοτε τίτλου.
        topSection.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Στατιστικά για Λέξεις - Κλειδιά"));

        topSection.add(createBarChart("Keyword", "Κορυφαίες Λέξεις"));
        topSection.add(createPieChart("Keyword", "Κατανομή Λέξεων"));

        // Ενότητα για τα στατιστικά των κατηγοριών
        JPanel bottomSection = new JPanel(new GridLayout(1, 2, 10, 10));

        bottomSection.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Στατιστικά για Κατηγορίες"));

        bottomSection.add(createBarChart("Category", "Κορυφαίες Κατηγορίες"));
        bottomSection.add(createPieChart("Category", "Κατανομή Κατηγοριών"));

        mainContainer.add(topSection);
        mainContainer.add(bottomSection);

        frame.add(mainContainer);
        frame.setVisible(true);
    }

    // Μέθοδος για Bar Chart
    private ChartPanel createBarChart(String choice, String chartTitle) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String x_axis = "";
        String y_axis= "";
        try {
            if (choice.equals("Keyword")) {
                x_axis = "Λέξη-Κλειδί";
                y_axis = "Πλήθος Αναζητήσεων";
                List < SearchKeyword > keywords = dbManager.getTopKeywords();
                for (SearchKeyword k: keywords) {
                    dataset.addValue(k.getSearchCount(), "Αναζητήσεις", k.getKeyword());
                }
            } else {
                x_axis = "Κατηγορία";
                y_axis = "Πλήθος Άρθρων";
                List < Category > cats = dbManager.getAllCategories();
                // Φθίνουσα ταξινόμηση απο αριστερα προς τα δεξιά βάση πλήθους άρθρων ανα κατηγορία.
                cats.sort((c1, c2) -> Integer.compare(c2.getArticles().size(), c1.getArticles().size()));
                for (Category c: cats) {
                    dataset.addValue(c.getArticles().size(), "Αναζητήσεις", c.getName());
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "Σφάλμα Φόρτωσης του Bar Chart:\n" + e.getMessage(),
                "Σφάλμα Βάσης",
                JOptionPane.ERROR_MESSAGE);
        }


        JFreeChart chart = ChartFactory.createBarChart(
            chartTitle,
            x_axis,
            y_axis,
            dataset
        );
        // Παραμετροποίηση εμφάνισης τίτλου και αξόνων
        chart.getTitle().setFont(new Font("Arial", Font.BOLD, 14));
        // περιστροφή 45 μοιρών για καλύτερη εμφάνιση των τιμών στον άξονα x
        org.jfree.chart.axis.CategoryAxis domainAxis = chart.getCategoryPlot().getDomainAxis();
        domainAxis.setCategoryLabelPositions(org.jfree.chart.axis.CategoryLabelPositions.UP_45);

        return new ChartPanel(chart);
    }

    // Μέθοδος για Pie Chart
    private ChartPanel createPieChart(String choice, String chartTitle) {
        DefaultPieDataset dataset = new DefaultPieDataset();

        try {
            if (choice.equals("Keyword")) {
                List < SearchKeyword > keywords = dbManager.getTopKeywords();
                for (SearchKeyword k: keywords) {
                    dataset.setValue(k.getKeyword(), k.getSearchCount());
                }
            } else {
                List < Category > cats = dbManager.getAllCategories();
                for (Category c: cats) {
                    dataset.setValue(c.getName(), c.getArticles().size());
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "Σφάλμα Φόρτωσης του Pie Chart:\n" + e.getMessage(),
                "Σφάλμα Βάσης",
                JOptionPane.ERROR_MESSAGE);
        }

        JFreeChart chart = ChartFactory.createPieChart(
            chartTitle,
            dataset,
            true, // legend
            true, // tooltips
            false // URLs
        );
        // Ρύθμιση γραμματοσειρών και αυτόματης δημιουργίας ετικετών με ποσοστά
        chart.getTitle().setFont(new Font("Arial", Font.BOLD, 14));
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelFont(new Font("Arial", Font.PLAIN, 12));
        plot.setNoDataMessage("No data available");
        plot.setCircular(true);
        plot.setLabelGap(0.02);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {2}"));

        return new ChartPanel(chart);
    }
}