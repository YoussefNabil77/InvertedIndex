package invertedIndex;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;

public class WebCrawler {

    private static final int MAX_PAGES = 10;
    private static final String BASE_URL = "https://en.wikipedia.org";

    public static void main(String[] args) {

        String seedUrl = BASE_URL + "/wiki/List_of_pharaohs";

        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        queue.add(seedUrl);
        visited.add(seedUrl);

        int count = 0;

        System.out.println("===== Wikipedia Crawler Started =====");

        while (!queue.isEmpty() && count < MAX_PAGES) {

            String url = queue.poll();

            try {

                System.out.println("\n===== DOC ID: " + count + " =====");
                System.out.println("Crawling URL: " + url);

                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0")
                        .timeout(10000)
                        .get();

                System.out.println("Title: " + doc.title());

                count++;

                Elements links = doc.select("a[href]");

                for (Element link : links) {

                    String href = link.attr("href");

                    if (href.startsWith("/wiki/")
                            && !href.contains(":")
                            && !href.contains("#")) {

                        String fullUrl = BASE_URL + href;

                        if (!visited.contains(fullUrl)) {
                            visited.add(fullUrl);
                            queue.add(fullUrl);
                        }
                    }
                }

            } catch (Exception e) {

                System.out.println("Failed: " + url);
            }
        }

        System.out.println("\n--- FINISHED ---");
        System.out.println("Pages Crawled = " + count);
    }
}