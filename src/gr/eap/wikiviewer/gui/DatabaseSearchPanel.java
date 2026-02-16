/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gr.eap.wikiviewer.gui;

/**
 *
 * @author Administrator
 */

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import gr.eap.wikiviewer.model.Article;
import gr.eap.wikiviewer.service.DBManager;

public class DatabaseSearchPanel extends JPanel {
    private JTextField searchFieldDB;
    private JTable dbResultTable;
    private DefaultTableModel dbModel;
    private DBManager dbManager;

    public DatabaseSearchPanel(DBManager dbManager) {
        this.dbManager = dbManager;
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        // Wikipedia Dark Background
        this.setBackground(new java.awt.Color(18, 21, 28));

        // Top Search Panel
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new java.awt.Color(18, 21, 28));
        
        JButton searchBtn = new JButton("Αναζήτηση στη ΒΔ:");
        searchBtn.setBackground(new java.awt.Color(18, 21, 28));
        searchBtn.setForeground(new java.awt.Color(51, 102, 204));
        searchBtn.setBorder(BorderFactory.createLineBorder(new java.awt.Color(18, 21, 28)));
        searchBtn.setFocusPainted(false);
        
        searchFieldDB = new JTextField(25);
        searchFieldDB.setBackground(new java.awt.Color(18, 21, 28));
        searchFieldDB.setForeground(Color.WHITE);
        searchFieldDB.setCaretColor(Color.WHITE);
        searchFieldDB.setBorder(BorderFactory.createLineBorder(new java.awt.Color(51, 102, 204)));

        topPanel.add(searchBtn);
        topPanel.add(searchFieldDB);
        
        dbModel = new DefaultTableModel(new String[]{"ID Σελίδα", "Τίτλος", "Κατηγορία", "Βαθμολογία", "Σχόλια"}, 0);
        dbResultTable = new JTable(dbModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    // Ριγέ εμφάνιση [cite: 1, 23]
                    c.setBackground(row % 2 == 0 ? new java.awt.Color(32, 37, 48) : new java.awt.Color(25, 30, 40));
                    c.setForeground(new java.awt.Color(230, 230, 230));
                } else {
                    c.setBackground(new java.awt.Color(51, 102, 204));
                    c.setForeground(Color.WHITE);
                }
                return c;
            }
        };
        
        javax.swing.table.JTableHeader header = dbResultTable.getTableHeader();
        header.setBackground(new java.awt.Color(18, 21, 28));
        header.setForeground(new  java.awt.Color(51, 102, 204));
        header.setFont(header.getFont().deriveFont(Font.BOLD));

        JScrollPane scrollPane = new JScrollPane(dbResultTable);
        scrollPane.getViewport().setBackground(new java.awt.Color(18, 21, 28));
        scrollPane.setBorder(BorderFactory.createLineBorder(new java.awt.Color(51, 102, 204)));

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Listeners
        searchBtn.addActionListener(e -> executeDatabaseLookup());
        searchFieldDB.addActionListener(e -> executeDatabaseLookup());
        
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    /**
     * Εκτελεί την αναζήτηση στη βάση δεδομένων και προβάλλει 
     * τα εμπλουτισμένα δεδομένα (σχόλια/βαθμολογία)
     */
    private void executeDatabaseLookup() {
        String keyword = searchFieldDB.getText().trim();

        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Εισάγετε λέξη-κλειδί!", "Προσοχή", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Καθαρισμός πίνακα
        dbModel.setRowCount(0);

        try {
            // Ανάκτηση δεδομένων από τη DBManager [cite: 32, 33]
            List<Article> results = dbManager.searchLocalArticles(keyword);

            if (results.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Δεν βρέθηκαν άρθρα για: " + keyword);
                return;
            }

            for (Article a : results) {
                dbModel.addRow(new Object[] {
                    a.getPageId(),
                    a.getTitle(),
                    // Προβολή ονόματος κατηγορίας [cite: 28, 29]
                    (a.getCategory() != null) ? a.getCategory().getName() : "Χωρίς Κατηγορία",
                    a.getRating(),
                    // Εμφάνιση των σχολίων από τη ΒΔ 
                    (a.getComments() != null) ? a.getComments() : ""
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Σφάλμα αναζήτησης: " + ex.getMessage());
        }
    }
} 
