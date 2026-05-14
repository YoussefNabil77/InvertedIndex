package invertedIndex;

import java.util.*;

public class Ranker {

    /**
     * Rank top K documents according to cosine similarity scores
     *
     * @param scores Map<docId, similarityScore>
     * @param sources source documents information
     * @param k number of top documents
     */
    public static void rankTopK(
            Map<Integer, Double> scores,
            Map<Integer, SourceRecord> sources,
            int k) {

        // Convert map to list
        List<Map.Entry<Integer, Double>> rankedList =
                new ArrayList<>(scores.entrySet());

        // Sort descending by score
        rankedList.sort((a, b) ->
                Double.compare(b.getValue(), a.getValue()));

        System.out.println("\n=========== TOP " + k + " RESULTS ===========\n");

        int count = 0;

        for (Map.Entry<Integer, Double> entry : rankedList) {

            if (count >= k)
                break;

            int docId = entry.getKey();
            double score = entry.getValue();

            SourceRecord doc = sources.get(docId);

            System.out.println("Rank #" + (count + 1));
            System.out.println("Document ID : " + docId);
            System.out.println("URL         : " + doc.URL);
            System.out.println("Score       : " + score);
            System.out.println("-----------------------------------");

            count++;
        }
    }
}
