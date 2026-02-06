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
        List<Article> articles = new ArrayList<>();
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            if (!root.has("query")) return articles;
            
            JsonObject queryObj = root.getAsJsonObject("query");
            if (!queryObj.has("search")) return articles;

            JsonArray searchArray = queryObj.getAsJsonArray("search");
            for (JsonElement element : searchArray) {
                JsonObject item = element.getAsJsonObject();
                Article article = new Article();
                
                article.setTitle(item.get("title").getAsString());
                article.setPageId(item.get("pageid").getAsInt());
                
                // Clean HTML tags from snippet using Jsoup
                String rawSnippet = item.has("snippet") ? item.get("snippet").getAsString() : "";
                article.setSnippet(Jsoup.parse(rawSnippet).text());
                
                article.setTimestamp(new Date()); 
                articles.add(article);
            }
        } catch (Exception e) {
            System.err.println("Error parsing Wikipedia JSON: " + e.getMessage());
        }
        return articles;
    }
}
