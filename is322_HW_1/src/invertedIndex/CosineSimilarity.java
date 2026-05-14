package invertedIndex;

import java.util.*;

public class CosineSimilarity {
    private Index5 index5;
    private int N; // Total number of documents

    public CosineSimilarity(Index5 index5) {
        this.index5 = index5;
        this.N = index5.sources.size();
        computeNorms(); // Pre-compute the document vector lengths
    }

    /**
     * Computes the Euclidean length (norm) of the TF-IDF vector for every document.
     * This is needed for the denominator of the Cosine Similarity formula.
     */
    private void computeNorms() {
        // Initialize norms to 0
        for (Integer docId : index5.sources.keySet()) {
            index5.sources.get(docId).norm = 0.0;
        }

        // Calculate the squared weights for each document
        for (Map.Entry<String, DictEntry> entry : index5.index.entrySet()) {
            DictEntry dictEntry = entry.getValue();
            int df = dictEntry.doc_freq;
            if (df == 0) continue;

            // IDF for the term
            double idf = Math.log10((double) N / df);

            Posting p = dictEntry.pList;
            while (p != null) {
                double tf = p.dtf;
                double weight = tf * idf;

                double currentNormSq = index5.sources.get(p.docId).norm;
                index5.sources.get(p.docId).norm = currentNormSq + (weight * weight);

                p = p.next;
            }
        }

        // Take the square root to get the final norm
        for (Integer docId : index5.sources.keySet()) {
            double normSq = index5.sources.get(docId).norm;
            index5.sources.get(docId).norm = Math.sqrt(normSq);
        }
    }

    /**
     * Executes a search query using Cosine Similarity scoring.
     *
     * @param query the search phrase
     * @return Map of document IDs and their normalized cosine similarity scores
     */
    public Map<Integer, Double> getCosineSimilarities(String query) {
        String[] words = query.split("\\W+");
        
        // 1. Calculate query Term Frequencies (TF)
        Map<String, Integer> queryTf = new HashMap<>();
        for (String word : words) {
            word = word.toLowerCase();
            if (index5.stopWord(word)) continue; // Reuse Index5's stopWord method
            word = index5.stemWord(word);        // Reuse Index5's stemWord method
            queryTf.put(word, queryTf.getOrDefault(word, 0) + 1);
        }

        if (queryTf.isEmpty()) {
            return new HashMap<>();
        }

        double queryNormSq = 0.0;
        Map<String, Double> queryWeights = new HashMap<>();
        
        // 2. Calculate Query TF-IDF weights and Query Norm
        for (Map.Entry<String, Integer> entry : queryTf.entrySet()) {
            String term = entry.getKey();
            int tf = entry.getValue();
            
            DictEntry dictEntry = index5.index.get(term);
            if (dictEntry != null) {
                double idf = Math.log10((double) N / dictEntry.doc_freq);
                double weight = tf * idf;
                queryWeights.put(term, weight);
                queryNormSq += weight * weight;
            }
        }
        
        if (queryNormSq == 0.0) {
            return new HashMap<>();
        }
        
        double queryNorm = Math.sqrt(queryNormSq);

        // 3. Accumulate dot products for documents
        Map<Integer, Double> scores = new HashMap<>();
        
        for (Map.Entry<String, Double> entry : queryWeights.entrySet()) {
            String term = entry.getKey();
            double qWeight = entry.getValue();
            
            DictEntry dictEntry = index5.index.get(term);
            Posting p = dictEntry.pList;
            
            double idf = Math.log10((double) N / dictEntry.doc_freq);
            
            while (p != null) {
                double dtf = p.dtf;
                double docWeight = dtf * idf; // Document TF-IDF weight
                
                scores.put(p.docId, scores.getOrDefault(p.docId, 0.0) + (qWeight * docWeight));
                p = p.next;
            }
        }
        
        // 4. Calculate Final Normalized Score (Scores[doc] = Scores[doc] / Length[doc])
        Map<Integer, Double> normalizedScores = new HashMap<>();
        for (Map.Entry<Integer, Double> entry : scores.entrySet()) {
            int docId = entry.getKey();
            double dotProduct = entry.getValue(); // This is Scores[doc]
            
            // Length[doc] (Euclidean length of the document vector)
            double docLength = index5.sources.get(docId).norm; 
            
            if (docLength > 0) {
                // Normalize exactly as requested: Scores[doc] = Scores[doc] / Length[doc]
                double normalizedScore = dotProduct / docLength;
                normalizedScores.put(docId, normalizedScore);
            }
        }
        
        return normalizedScores;
    }
}
