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
 * Service to interact with the Wikipedia MediaWiki API.
 * Updated with the official fix provided by the professor.
 */
public class WikipediaService {
    private static final String API_URL = "https://el.wikipedia.org/w/api.php";
    
    /**
     * The specific User-Agent provided by the professor to bypass the 403 Forbidden error.
     */
    private static final String USER_AGENT = "MyJavaWikiApp/1.0 (talepis@unipi.gr)";
    
    private final OkHttpClient client;

    public WikipediaService() {
        this.client = new OkHttpClient();
    }

    /**
     * Search for articles by keyword.
     * @param query The search term.
     * @return A list of Article objects (not yet saved to DB).
     * @throws IOException If the network request fails.
     */
    public List<Article> searchArticles(String query) throws IOException {
        // Construct URL as per professor's example
        String url = API_URL + "?action=query&list=search&srsearch=" + 
                     URLEncoder.encode(query, StandardCharsets.UTF_8) + 
                     "&format=json";

        // Build request using the professor's specific User-Agent
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .build();
                
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("API Error: " + response.code() + " " + response.message());
            }

            if (response.body() == null) {
                throw new IOException("Empty response body from Wikipedia API");
            }

            String responseData = response.body().string();
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
