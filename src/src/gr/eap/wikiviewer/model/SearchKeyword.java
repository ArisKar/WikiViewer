package gr.eap.wikiviewer.model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Οντότητα που αναπαριστά μια λέξη-κλειδί αναζήτησης για παρακολούθηση στατιστικών.
 */
@Entity
@Table(name = "SEARCH_KEYWORD")
public class SearchKeyword implements Serializable {
    
    // Μοναδικό αναγνωριστικό κατηγορίας (auto-increment).
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Η λέξη-κλειδί/όνομα, αναζήτησης (υποχρεωτική και μοναδική)
    @Column(nullable = false, unique = true)
    private String keyword;

    // Πλήθος φορών που έχει αναζητηθεί η λέξη-κλειδί (υποχρεωτικό).
    @Column(nullable = false)
    private Integer searchCount;

    // Default constructor (υποχρεωτικό από το JPA).
    public SearchKeyword() {}

    /**
     * Ο Constructor δημιουργεί νέα λέξη-κλειδί με αρχικό count = 1 (πρώτη εμφάνιση).
     */
    public SearchKeyword(String keyword) {
        this.keyword = keyword;
        this.searchCount = 1;
    }
    // Getters και Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public Integer getSearchCount() { return searchCount; }
    public void setSearchCount(Integer searchCount) { this.searchCount = searchCount; }

    // Αύξηση πλήθους αναζητήσεων της συγκεκριμένης λέξης-κλειδί.
    public void incrementCount() {
        this.searchCount++;
    }
}
