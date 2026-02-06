package gr.eap.wikiviewer.service;

import gr.eap.wikiviewer.model.Article;
import gr.eap.wikiviewer.model.Category;
import gr.eap.wikiviewer.model.SearchKeyword;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;

/**
 * Singleton class to manage database operations using JPA.
 */
public class DBManager {
    private static DBManager instance;
    private final EntityManagerFactory emf;

    private DBManager() {
        emf = Persistence.createEntityManagerFactory("WikiViewerPU");
    }

    public static synchronized DBManager getInstance() {
        if (instance == null) {
            instance = new DBManager();
        }
        return instance;
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    // --- Article Operations ---

    public void saveArticle(Article article) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            if (article.getId() == null) {
                em.persist(article);
            } else {
                em.merge(article);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public List<Article> getAllArticles() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT a FROM Article a", Article.class).getResultList();
        } finally {
            em.close();
        }
    }

    public List<Article> getArticlesByCategory(Category category) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT a FROM Article a WHERE a.category = :cat", Article.class)
                    .setParameter("cat", category)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Article getArticleByPageId(Integer pageId) {
        EntityManager em = getEntityManager();
        try {
            List<Article> results = em.createQuery("SELECT a FROM Article a WHERE a.pageId = :pid", Article.class)
                    .setParameter("pid", pageId)
                    .getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }

    // --- Category Operations ---

    public void saveCategory(Category category) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(category);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public List<Category> getAllCategories() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT c FROM Category c", Category.class).getResultList();
        } finally {
            em.close();
        }
    }

    // --- Keyword Operations ---

    public void trackKeyword(String keyword) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            List<SearchKeyword> results = em.createQuery("SELECT k FROM SearchKeyword k WHERE k.keyword = :kw", SearchKeyword.class)
                    .setParameter("kw", keyword)
                    .getResultList();
            
            if (results.isEmpty()) {
                em.persist(new SearchKeyword(keyword));
            } else {
                SearchKeyword sk = results.get(0);
                sk.incrementCount();
                em.merge(sk);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public List<SearchKeyword> getTopKeywords() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT k FROM SearchKeyword k ORDER BY k.searchCount DESC", SearchKeyword.class)
                    .setMaxResults(10)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
