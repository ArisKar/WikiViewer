package gr.eap.wikiviewer.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import gr.eap.wikiviewer.model.Category;
import gr.eap.wikiviewer.model.SearchKeyword;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.PdfEncodings;
import java.util.List;

/**
 * Service για τη δημιουργία PDF αναφορών
 */

public class PDFService {
    // Διαδρομή προς μια γραμματοσειρά που υποστηρίζει Ελληνικά σε περιβαλλον windows.
    private static final String FONT_PATH = "C:/Windows/Fonts/arial.ttf"; 

    public void generateStatisticsReport(String dest, List<SearchKeyword> topKeywords, List<Category> categories) {
        try {
            // Δημιουργία PDF writer και document
            PdfWriter writer = new PdfWriter(dest);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Φόρτωση γραμματοσειράς με υποστήριξη Ελληνικών
            PdfFont font = PdfFontFactory.createFont(FONT_PATH, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            document.setFont(font);
            // Τίτλος αναφοράς
            document.add(new Paragraph("WikiViewer - Στατιστικά").setBold().setFontSize(18));
            
            // Τμήμα Λέξεων-Κλειδιών
            document.add(new Paragraph("\nΔημοφιλείς Αναζητήσεις:"));
            
            // Δημιουργία πίνακα με 2 στήλες ,70-30
            Table keywordTable = new Table(UnitValue.createPercentArray(new float[]{70, 30})).useAllAvailableWidth();
            keywordTable.addHeaderCell("Λέξη-Κλειδί");
            keywordTable.addHeaderCell("Πλήθος");
            
             // Προσθήκη γραμμών για κάθε keyword
            for (SearchKeyword sk : topKeywords) {
                keywordTable.addCell(sk.getKeyword());
                keywordTable.addCell(sk.getSearchCount().toString());
            }
            document.add(keywordTable);

            //  Τμήμα Κατηγοριών 
            document.add(new Paragraph("\nΆρθρα ανά Κατηγορία:"));
            
            // Δημιουργία πίνακα με 2 στήλες ,70-30
            Table categoryTable = new Table(UnitValue.createPercentArray(new float[]{70, 30})).useAllAvailableWidth();
            categoryTable.addHeaderCell("Κατηγορία");
            categoryTable.addHeaderCell("Πλήθος Άρθρων");
            
            // Προσθήκη γραμμών για κάθε κατηγορία και εμφάνισης πλήθους των άρθρων τους.
            for (Category cat : categories) {
                categoryTable.addCell(cat.getName());
                categoryTable.addCell(String.valueOf(cat.getArticles().size()));
            }
            document.add(categoryTable);
            
            // Κλείσιμο του document (αποθήκευση PDF).
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF: " + e.getMessage(), e);
        }
    }
}
