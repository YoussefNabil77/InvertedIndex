package invertedIndex;

import java.io.*;

public class Test2 {

    public static void main(String[] args) throws IOException {

        // ================================================================
        //  Task 1 — Web Crawler
        //  WikiCrawler visits up to 10 Wikipedia pages starting from seed
        // ================================================================
        System.out.println("========================================");
        System.out.println("  STEP 1: Web Crawling");
        System.out.println("========================================");

        WikiCrawler crawler = new WikiCrawler();
        crawler.crawl(new String[]{"https://en.wikipedia.org/wiki/List_of_pharaohs"});
        int N = crawler.pageContents.size(); // total number of crawled pages
        System.out.println("Total pages crawled: " + N);

        if (N == 0) {
            System.out.println("No pages crawled. Check your internet connection.");
            return;
        }

        // ================================================================
        //  Task 2 — Build Inverted Index
        //  Feeds crawled page text into Index5 and builds the index
        // ================================================================
        System.out.println("\n========================================");
        System.out.println("  STEP 2: Building Inverted Index");
        System.out.println("========================================");

        Index5 index = new Index5();
        index.buildIndexFromWeb(crawler.pageContents, crawler.pageUrls);

        System.out.println("Unique terms indexed: " + index.getIndexSize());
        
        // ===== TASK 3: Query Processing =====
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.print("\nEnter query (or type exit): ");
            String query = br.readLine();
            if (query.equalsIgnoreCase("exit")) {
                break;
            }
            System.out.println("\nSearch Results:");
            System.out.println(index.find_24_01(query));
        }


        System.out.println("\nProgram ended.");
    }
}