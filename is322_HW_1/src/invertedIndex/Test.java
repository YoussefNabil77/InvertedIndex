package invertedIndex;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Test {

    public static void main(String args[]) throws IOException {

        Index5 index = new Index5();

        String files = "/Users/youssef/Downloads/4_5990032386258115504/tmp11/tmp11/rl/collection/";

        File file = new File(files);
        String[] fileList = file.list();

        fileList = index.sort(fileList);
        index.N = fileList.length;

        for (int i = 0; i < fileList.length; i++) {
            fileList[i] = files + fileList[i];
        }

        index.buildIndex(fileList);
        index.store("index");
        index.printDictionary();

        // Task 6 - Positional Index (Bonus)
        Index5 testIndex = new Index5();
        testIndex.sources.put(0, new SourceRecord(0, "doc1", "doc1", "notext"));
        testIndex.sources.put(1, new SourceRecord(1, "doc2", "doc2", "notext"));
        testIndex.N = 2;

        testIndex.indexOneLine("Cairo University Zayed City is a new CU branch", 0, 0);
        testIndex.indexOneLine("Zayed attending in AinShams University in Cairo", 1, 0);

        System.out.println("doc1: Cairo University Zayed City is a new CU branch");
        System.out.println("doc2: Zayed attending in AinShams University in Cairo");
        System.out.println("\nPositional Index Result for query 'Cairo University Zayed City':");
        System.out.println(testIndex.phraseSearch("Cairo University Zayed City"));

        // Task 3 - Query Loop
        String phrase = "";
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        do {
            System.out.println("Print search query: ");
            phrase = in.readLine();
            System.out.println(index.find_24_01(phrase));
        } while (!phrase.isEmpty());

    }
}