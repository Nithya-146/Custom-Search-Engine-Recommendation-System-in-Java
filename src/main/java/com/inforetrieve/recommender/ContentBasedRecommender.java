package com.inforetrieve.recommender;

import com.inforetrieve.indexer.Document;
import com.inforetrieve.indexer.InvertedIndex;
import com.inforetrieve.ranking.CosineSimilarity;
import com.inforetrieve.ranking.TfIdfCalculator;

import java.util.*;

/**
 * Content-Based Filtering Recommendation Engine.
 * Computes pairwise document similarity over TF-IDF vector representations.
 */
public class ContentBasedRecommender {

    private final InvertedIndex index;
    private final TfIdfCalculator tfIdfCalculator;
    private final CosineSimilarity cosineSimilarity;

    public ContentBasedRecommender(InvertedIndex index) {
        this.index = index;
        this.tfIdfCalculator = new TfIdfCalculator();
        this.cosineSimilarity = new CosineSimilarity();
    }

    /**
     * Recommends top-K documents content-similar to target docId based on TF-IDF cosine similarity.
     */
    public List<RecommendedItem> recommendSimilarDocuments(String targetDocId, int topK) {
        Document targetDoc = index.getDocument(targetDocId);
        if (targetDoc == null || topK <= 0) {
            return Collections.emptyList();
        }

        Map<String, Double> targetVector = tfIdfCalculator.computeDocumentVector(targetDocId, index);
        double targetNorm = tfIdfCalculator.computeVectorMagnitude(targetVector);

        if (targetVector.isEmpty() || targetNorm == 0.0) {
            return Collections.emptyList();
        }

        List<RecommendedItem> recommendations = new ArrayList<>();

        for (Document candidateDoc : index.getAllDocuments()) {
            if (candidateDoc.getDocId().equals(targetDocId)) {
                continue; // Skip target document itself
            }

            Map<String, Double> candidateVector = tfIdfCalculator.computeDocumentVector(candidateDoc.getDocId(), index);
            double candidateNorm = tfIdfCalculator.computeVectorMagnitude(candidateVector);

            double score = cosineSimilarity.calculateSimilarity(targetVector, candidateVector, targetNorm, candidateNorm);
            if (score > 0.0) {
                recommendations.add(new RecommendedItem(candidateDoc, score, "Content Similarity (TF-IDF)"));
            }
        }

        // Sort by similarity score descending
        recommendations.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        return recommendations.subList(0, Math.min(topK, recommendations.size()));
    }

    public static class RecommendedItem {
        private final Document document;
        private final double score;
        private final String strategy;

        public RecommendedItem(Document document, double score, String strategy) {
            this.document = document;
            this.score = score;
            this.strategy = strategy;
        }

        public Document getDocument() { return document; }
        public double getScore() { return score; }
        public String getStrategy() { return strategy; }
    }
}
