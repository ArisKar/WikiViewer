package gr.eap.wikiviewer.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import gr.eap.wikiviewer.model.Category;
import gr.eap.wikiviewer.model.SearchKeyword;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * Service to generate PDF reports for statistics.
 */
public class PDFService {

    public void generateStatisticsReport(String dest, List<SearchKeyword> topKeywords, List<Category> categories) throws FileNotFoundException {
        PdfWriter writer = new PdfWriter(dest);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("WikiViewer Statistics Report").setBold().setFontSize(18));
        document.add(new Paragraph("\nTop Search Keywords:"));

        Table keywordTable = new Table(UnitValue.createPercentArray(new float[]{70, 30})).useAllAvailableWidth();
        keywordTable.addHeaderCell("Keyword");
        keywordTable.addHeaderCell("Count");

        for (SearchKeyword sk : topKeywords) {
            keywordTable.addCell(sk.getKeyword());
            keywordTable.addCell(sk.getSearchCount().toString());
        }
        document.add(keywordTable);

        document.add(new Paragraph("\nArticles per Category:"));
        Table categoryTable = new Table(UnitValue.createPercentArray(new float[]{70, 30})).useAllAvailableWidth();
        categoryTable.addHeaderCell("Category");
        categoryTable.addHeaderCell("Article Count");

        for (Category cat : categories) {
            categoryTable.addCell(cat.getName());
            categoryTable.addCell(String.valueOf(cat.getArticles().size()));
        }
        document.add(categoryTable);

        document.close();
    }
}
