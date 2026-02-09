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
    
    // Μοναδικό instance της κλάσης
    private static DBManager instance;
    
    // Factory για δημιουργία EntityManager
    private final EntityManagerFactory emf;
    
    // private constructor ώστε να μην μπορεί να δημιουργηθεί άλλο instance.
    private DBManager() {
        emf = Persistence.createEntityManagerFactory("WikiViewerPU");
    }
    // Επιστρέφει το μοναδικό instance της κλάσης DBManager.
    public static synchronized DBManager getInstance() {
        if (instance == null) {
            instance = new DBManager();
        }
        return instance;
    }
    // Παίρνουμε έναν EntityManager για εκτέλεση queries / transactions για την συγκεκριμένη βάση που βλέπει το factory DBManager.
    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    // 4 Μέθοδοι Άρθρων
    
    // Αποθήκευση ή ενημέρωση ενός Άρθρου στην βάση
    public void saveArticle(Article article) {
        // Δημιουργία EntityManager() για να επικοινωνήσουμε με τη βάση.
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            // Αν το άρθρο είναι νέο, γίνεται persist, αλλιώς merge.
            // Ελέγχουμε το auto generated id πεδίο του άρθρου. 
            // Αν αυτό είναι κενό, τότε μιλάμε για νεο άρθρο (δεν έχει μπεί ποτέ στην βάση)
            // αλλιώς κάνουμε UPDATE το υπάρχον.
            
            if (article.getId() == null) {
                em.persist(article);
            } 
            // UPDATE το υπάρχον.
            else {
                em.merge(article);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    // Γυρνάει στο πάνελ όλα τα άρθρα που είναι αποθηκευμένα στην βάση.
    public List<Article> getAllArticles() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT a FROM Article a", Article.class).getResultList();
        } finally {
            em.close();
        }
    }
    
    // Γυρνάει στο πάνελ όλα τα άρθρα της συγκεκριμένης κατηγορίας του φίλτρου.
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

    // Έλεγχος αν υπάρχει το άρθρο αυτό ήδη στην βάση, με βάση το μοναδικό ID σελίδας (page ID).
    public Article getArticleByPageId(Integer pageId) {
        // Δημιουργία EntityManager() για να επικοινωνήσουμε με τη βάση.
        EntityManager em = getEntityManager();
        try {
            // Έλεγχος αν υπάρχει το συγκεκριμένο άρθρο στην βάση στον πίνακα Article. Ο έλεγχος γίνεται με το κλειδί pageId.
            List<Article> results = em.createQuery("SELECT art FROM Article art WHERE art.pageId = :pid", Article.class)
                    .setParameter("pid", pageId)
                    .getResultList();
            // Επιστροφή αποτελεσμάτων.
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }

    // 2 Μέθοδοι Κατηγοριών
    
    // Αποθήκευση μιας νέας κατηγορίας.
    public int saveCategory(String categoryName) {
        EntityManager em = getEntityManager();
    try {
        // Έλεγχος αν υπάρχει ήδη στην βάση κατηγορία με ίδιο όνομα.
        List<Category> results = em.createQuery(
            "SELECT c FROM Category c WHERE UPPER(c.name) = UPPER(:name)", Category.class)
            .setParameter("name", categoryName.trim())
            .getResultList();

        if (results.isEmpty()) {
            em.getTransaction().begin();
            // Φτιάχνουμε το νέο αντικείμενο και το αποθηκεύουμε στην βάση.
            Category newCat = new Category(categoryName.trim());
            em.persist(newCat);
            em.getTransaction().commit();
            return 1;
        }
        return 0;
    } finally {
        em.close();
    }
    }

    // Επέστρεψε στο πάνελ όλες τις κατηγορίες που υπάρχουν στην βάση.
    public List<Category> getAllCategories() {
        // Δημιουργία EntityManager() για να επικοινωνήσουμε με τη βάση.
        EntityManager em = getEntityManager();
        try {
            // Επιστροφή σε μορφή λίστας όλων των κατηγοριών που υπάρχουν στην βάση.
            return em.createQuery("SELECT c FROM Category c", Category.class).getResultList();
        } finally {
            em.close();
        }
    }

    //Καταγραφή Λέξης-Κλειδιού για Στατιστικά
    // Αν η λέξη-keyword υπάρχει ήδη, αυξάνεται ο μετρητής searchCount κατά 1.
    // Αν δεν υπάρχει, δημιουργείται νέα εγγραφή με searchCount = 1.
    
    public void trackKeyword(String keyword) {
        // Δημιουργία EntityManager() για να επικοινωνήσουμε με τη βάση.
        EntityManager em = getEntityManager();
        try {
            // Εύρεση αν υπάρχει το keyword στον πίνακα SearchKeyword στην βάση.
            em.getTransaction().begin();
            List<SearchKeyword> results = em.createQuery("SELECT k FROM SearchKeyword k WHERE k.keyword = :kw", SearchKeyword.class)
                    .setParameter("kw", keyword)
                    .getResultList();
            
            if (results.isEmpty()) {
                // Αν δεν υπάρχει δημιούργησε έναν νέο αντικείμενο keyword και βάλτο στον πίνακα SearchKeyword.
                // Δημιουργία Νέας Εγγραφής.
                em.persist(new SearchKeyword(keyword));
            } else {
                // Αν υπάρχει ήδη, αύξησε τις εμφανίσεις του κατά ένα.
                SearchKeyword sk = results.get(0);
                sk.incrementCount();
                // Εντολή UPDATE στην βάση για ενημέρωση των εμφανίσεων.
                em.merge(sk);
            }
            // Εντολή COMMIT.
            em.getTransaction().commit();
        } finally {
            // Κλείσιμο του EntityManager.
            em.close();
        }
    }

    public List<SearchKeyword> getTopKeywords() {
        // Δημιουργία EntityManager() για να επικοινωνήσουμε με τη βάση.
        EntityManager em = getEntityManager();
        try {
            //Επιστροφή των 10 πιο συχνών λέξεων-κλειδιών απο τον χρήστη απο το πιο συχνό στο λιγότερο.
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
