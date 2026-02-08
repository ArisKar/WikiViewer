package gr.eap.wikiviewer.model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity representing a search keyword used by the user to track statistics.
 */
@Entity
@Table(name = "SEARCH_KEYWORD")
public class SearchKeyword implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String keyword;

    @Column(nullable = false)
    private Integer searchCount;

    public SearchKeyword() {}

    public SearchKeyword(String keyword) {
        this.keyword = keyword;
        this.searchCount = 1;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public Integer getSearchCount() { return searchCount; }
    public void setSearchCount(Integer searchCount) { this.searchCount = searchCount; }

    public void incrementCount() {
        this.searchCount++;
    }
}
