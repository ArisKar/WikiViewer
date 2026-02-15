package gr.eap.wikiviewer.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Οντότητα που αναπαριστά ένα άρθρο της Wikipedia αποθηκευμένο στην local db.
 */
@Entity
@Table(name = "ARTICLE")
public class Article implements Serializable {
    
    // Μοναδικό αναγνωριστικό κατηγορίας (auto-increment).
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Τίτλος του άρθρου (υποχρεωτικό)
    @Column(nullable = false)
    private String title;

    // Σύντομη περίληψη του άρθρου (αποθηκεύεται ως CLOB για μεγάλα κείμενα)
    private String snippet;

    // Ημερομηνία και ώρα αποθήκευσης
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    /**
     * Η κατηγορία στην οποία ανήκει το άρθρο.
     * Many-to-One σχέση: πολλά άρθρα ανήκουν σε μία κατηγορία.
     * Η category_id είναι foreign key
     * Δείχνει στο primary key της οντότητας Category (το id).
     */
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    // Σχόλια χρήστη για το άρθρο (CLOB για μεγάλα κείμενα)
    private String comments;

    // Βαθμολογία άρθρου από τον χρήστη (1-5).
    private Integer rating; 

    // Wikipedia ID σελίδα, για αποφυγή διπλότυπων (μοναδικό).
    @Column(unique = true)
    private Integer pageId; // Wikipedia Page ID to avoid duplicates

    // Default constructor (υποχρεωτικό από το JPA).
    public Article() {}

    // Getters και Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSnippet() { return snippet; }
    public void setSnippet(String snippet) { this.snippet = snippet; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }
}
