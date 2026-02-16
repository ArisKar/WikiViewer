package gr.eap.wikiviewer.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity που αναπαριστά μια κατηγορία άρθρων.
 */
@Entity
@Table(name = "CATEGORY")
public class Category implements Serializable {
    
    // Μοναδικό αναγνωριστικό κατηγορίας (auto-increment).
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Όνομα κατηγορίας (υποχρεωτικό και μοναδικό).
    @Column(nullable = false, unique = true)
    private String name;

    /**
    * Η λίστα των άρθρων που ανήκουν σε αυτή την κατηγορία.
    * One-to-Many σχέση: μια κατηγορία έχει πολλά άρθρα.
    * EAGER loading: τα άρθρα φορτώνονται αυτόματα μαζί με την κατηγορία (στο ίδιο db session).
    * Το getArticles() επιστρέφει τη λίστα που είναι ήδη φορτωμένη στη μνήμη.
    * Cascade: οι αλλαγές στην κατηγορία επηρεάζουν και τα άρθρα της.
    */
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Article> articles = new ArrayList<>();

    // Default constructor (υποχρεωτικό από το JPA).
    public Category() {}

    // Constructor με παράμετρο το όνομα της κατηγορίας.
    public Category(String name) {
        this.name = name;
    }

    // Getters και Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Article> getArticles() { return articles; }
    
    public void setArticles(List<Article> articles) 
    { this.articles = articles; }

    // Επιστρέφει το όνομα της κατηγορίας για εμφάνιση σε UI.
    @Override
    public String toString() {
        return name;
    }
}
