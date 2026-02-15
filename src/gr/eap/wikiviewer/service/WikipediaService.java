package gr.eap.wikiviewer.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import gr.eap.wikiviewer.model.Article;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Service για αλληλεπίδραση με το Wikipedia MediaWiki API.
 */
public class WikipediaService {
    // URL του Greek Wikipedia API.
    private static final String API_URL = "https://el.wikipedia.org/w/api.php";
    
     /**
     * Συγκεκριμένο User-Agent για να αποφευχθεί το σφάλμα 403 Forbidden.
     */
    
    private static final String USER_AGENT = "MyJavaWikiApp/1.0 (talepis@unipi.gr)";
    
    
    // HTTP client για τις κλήσεις στο API
    private final OkHttpClient client;

    public WikipediaService() {
        this.client = new OkHttpClient();
    }

     /**
     * Αναζητά άρθρα βάσει λέξης-κλειδί.
     
     * @param query Η λέξη-κλειδί αναζήτησης
     * @return Λίστα με Article objects (δεν έχουν αποθηκευτεί ακόμα στη βάση).
     * @throws IOException σε περίπτωση αποτυχίας.
     */
    public List<Article> searchArticles(String query) throws IOException {

        // Κάνει URL encoding στο query για ασφαλή μετάδοση
        String url = API_URL + "?action=query&list=search&srsearch=" + 
                     URLEncoder.encode(query, StandardCharsets.UTF_8) + 
                     "&format=json";

        // Δημιουργία request με το συγκεκριμένο User-Agent που δόθηκε.
        // Το User-Agent header είναι υποχρεωτικό για να αποφευχθεί το 403 error
        
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .build();
                
        // Εκτέλεση του request και επεξεργασία της απάντησης.
        try (Response response = client.newCall(request).execute()) {
            // Έλεγχος για HTTP errors (π.χ. 403..).
            if (!response.isSuccessful()) {
                throw new IOException("API Error: " + response.code() + " " + response.message());
            }
            // Έλεγχος για κενή απάντηση
            if (response.body() == null) {
                throw new IOException("Empty response body from Wikipedia API");
            }
            // Λήψη του JSON response ως String
            String responseData = response.body().string();
            // Parsing του JSON και επιστροφή λίστας των άρθρων.
            return parseSearchResponse(responseData);
        }
    }

    private List<Article> parseSearchResponse(String json) {
        // Δημιουργία κενής λίστας για να αποθηκεύσουμε τα άρθρα που θα εξάγουμε.
        List<Article> articles = new ArrayList<>();
        try {
            // Μετατροπή του JSON string σε JsonObject για ευκολότερη επεξεργασία
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            if (!root.has("query")) return articles;
            
            JsonObject queryObj = root.getAsJsonObject("query");
            if (!queryObj.has("search")) return articles;

            // Εξαγωγή του πίνακα "search" που περιέχει τα αποτελέσματα.
            JsonArray searchArray = queryObj.getAsJsonArray("search");
            for (JsonElement element : searchArray) {
                
                // Μετατροπή του κάθε στοιχείου σε JsonObject
                JsonObject item = element.getAsJsonObject();
                Article article = new Article();
                
                // Εξαγωγή του τίτλου του άρθρου από το πεδίο "title"
                article.setTitle(item.get("title").getAsString());
                // Εξαγωγή του μοναδικού ID της σελίδας από το πεδίο "pageid"
                article.setPageId(item.get("pageid").getAsInt());
                
                // Χρήση της βιβλιοθήκης JSoup για να αφαιρέσουμε όλα τα HTML tags
                // Το Jsoup.parse() μετατρέπει το HTML σε Document και το .text() εξάγει μόνο το καθαρό κείμενο
                String rawSnippet = item.has("snippet") ? item.get("snippet").getAsString() : "";
                article.setSnippet(Jsoup.parse(rawSnippet).text());
                
                // Ορισμός της ημερομηνίας αποθήκευσης στην τρέχουσα ημερομηνία/ώρα
                article.setTimestamp(new Date()); 
                
                // Προσθήκη του ολοκληρωμένου Article στη λίστα
                articles.add(article);
            }
        } catch (Exception e) {
            System.err.println("Error parsing Wikipedia JSON: " + e.getMessage());
        }
        return articles;
    }
}
