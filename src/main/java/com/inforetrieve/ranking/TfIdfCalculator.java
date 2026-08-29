package com.inforetrieve.ranking;

import com.inforetrieve.indexer.Document;
import com.inforetrieve.indexer.InvertedIndex;
import com.inforetrieve.indexer.Posting;

import java.util.*;

/**
 * Calculates TF-IDF weights and constructs sparse vector representations from scratch.
 */
public class TfIdfCalculator {

    /**
     * Calculates logarithmic Term Frequency: TF(t, d) = 1 + log10(tf) if tf > 0, else 0.
     */
    public double calculateTF(int termFrequency) {
        if (termFrequency <= 0) return 0.0;
        return 1.0 + Math.log10(termFrequency);
    }

    /**
     * Calculates Inverse Document Frequency: IDF(t) = log10(1 + (N / df(t))).
     */
    public double calculateIDF(int totalDocuments, int documentFrequency) {
        if (documentFrequency <= 0 || totalDocuments <= 0) return 0.0;
        return Math.log10(1.0 + ((double) totalDocuments / documentFrequency));
    }

    /**
     * Calculates the TF-IDF weight for a term in a document.
     */
    public double calculateTfIdf(int termFrequency, int totalDocuments, int documentFrequency) {
        double tf = calculateTF(termFrequency);
        double idf = calculateIDF(totalDocuments, documentFrequency);
        return tf * idf;
    }

    /**
     * Computes the sparse TF-IDF vector for a document across all indexed terms.
     */
    public Map<String, Double> computeDocumentVector(String docId, InvertedIndex index) {
        Map<String, Double> vector = new HashMap<>();
        int N = index.getTotalDocuments();
        if (N == 0) return vector;

        for (String term : index.getAllTerms()) {
            List<Posting> postings = index.getPostings(term);
            for (Posting posting : postings) {
                if (posting.getDocId().equals(docId)) {
                    double weight = calculateTfIdf(posting.getTermFrequency(), N, postings.size());
                    if (weight > 0.0) {
                        vector.put(term, weight);
                    }
                    break;
                }
            }
        }
        return vector;
    }

    /**
     * Computes the sparse TF-IDF vector for a tokenized query.
     */
    public Map<String, Double> computeQueryVector(List<String> queryTokens, InvertedIndex index) {
        Map<String, Double> vector = new HashMap<>();
        if (queryTokens == null || queryTokens.isEmpty()) return vector;

        // Frequency table for query terms
        Map<String, Integer> queryTfMap = new HashMap<>();
        for (String token : queryTokens) {
            queryTfMap.put(token, queryTfMap.getOrDefault(token, 0) + 1);
        }

        int N = index.getTotalDocuments();
        for (Map.Entry<String, Integer> entry : queryTfMap.entrySet()) {
            String term = entry.getKey();
            int queryTf = entry.getValue();
            int df = index.getDocumentFrequency(term);
            double idf = calculateIDF(N, df);
            double tf = calculateTF(queryTf);
            double weight = tf * idf;
            if (weight > 0.0) {
                vector.put(term, weight);
            }
        }
        return vector;
    }

    /**
     * Computes Euclidean L2 Norm (magnitude) of a sparse vector: sqrt(sum(w^2)).
     */
    public double computeVectorMagnitude(Map<String, Double> vector) {
        if (vector == null || vector.isEmpty()) return 0.0;
        double sumSquares = 0.0;
        for (double weight : vector.values()) {
            sumSquares += weight * weight;
        }
        return Math.sqrt(sumSquares);
    }
}
