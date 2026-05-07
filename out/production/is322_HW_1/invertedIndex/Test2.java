package invertedIndex;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;

public class Test {

    static final int MAX_PAGES = 10;
    static final String BASE_URL = "https://en.wikipedia.org";

    public static void main(String[] args) {

        Index5 index = new Index5();

        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        String seed = BASE_URL + "/wiki/List_of_pharaohs";

        queue.add(seed);
        visited.add(seed);

        int docId = 0;


        System.out.println("Wiki Crawler & Indexer Started");


        while (!queue.isEmpty() && docId < MAX_PAGES) {

            String url = queue.poll();

            try {

                System.out.println("\n Processing Doc #" + docId);
                System.out.println(" URL: " + url);

                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0")
                        .timeout(10000)
                        .get();

                String title = doc.title();
                String text = doc.body().text();

                System.out.println(" Title: " + title);


                // ADD TO INDEX

                index.sources.put(
                        docId,
                        new SourceRecord(
                                docId,
                                url,
                                title,
                                text.length(),
                                0.0,
                                text
                        )
                );

                index.indexOneLine(text, docId, 0);


                // EXTRACT LINKS

                Elements links = doc.select("a[href]");

                System.out.println(" New links added: " + links.size());

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

                docId++;

            } catch (Exception e) {

                System.out.println(" Error: " + url);
                e.printStackTrace();
            }
        }


        System.out.println(" Finished! Pages Indexed: " + docId);



        // SEARCH TEST

        Scanner sc = new Scanner(System.in);

        while (true) {

            System.out.print("\n Enter search query (type 'exit'): ");
            String q = sc.nextLine();

            if (q.equalsIgnoreCase("exit")) break;

            System.out.println(index.find_24_01(q));
        }
    }
}
