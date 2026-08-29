package com.inforetrieve.recommender;

import com.inforetrieve.indexer.Document;
import com.inforetrieve.indexer.InvertedIndex;

import java.util.*;

/**
 * Collaborative Filtering Recommender implemented manually from scratch.
 * Supports User-Based and Item-Based Collaborative Filtering via Pearson Correlation Coefficient.
 */
public class CollaborativeFilteringRecommender {

    // User-Item Matrix: userId -> (docId -> rating 1.0 to 5.0)
    private final Map<String, Map<String, Double>> userItemMatrix;
    private final InvertedIndex index;

    public CollaborativeFilteringRecommender(InvertedIndex index) {
        this.index = index;
        this.userItemMatrix = new HashMap<>();
    }

    /**
     * Adds user rating for a document/item.
     */
    public void addRating(String userId, String docId, double rating) {
        userItemMatrix.computeIfAbsent(userId, k -> new HashMap<>()).put(docId, rating);
    }

    public Map<String, Map<String, Double>> getUserItemMatrix() {
        return userItemMatrix;
    }

    /**
     * Computes Pearson Correlation Coefficient between user1 and user2 manually.
     */
    public double calculatePearsonCorrelation(String user1, String user2) {
        Map<String, Double> ratings1 = userItemMatrix.get(user1);
        Map<String, Double> ratings2 = userItemMatrix.get(user2);

        if (ratings1 == null || ratings2 == null) return 0.0;

        // Find co-rated items
        Set<String> coRatedItems = new HashSet<>(ratings1.keySet());
        coRatedItems.retainAll(ratings2.keySet());

        if (coRatedItems.size() < 2) return 0.0; // Need at least 2 co-rated items for correlation

        // Compute average ratings
        double mean1 = ratings1.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double mean2 = ratings2.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        double numerator = 0.0;
        double sumSq1 = 0.0;
        double sumSq2 = 0.0;

        for (String item : coRatedItems) {
            double diff1 = ratings1.get(item) - mean1;
            double diff2 = ratings2.get(item) - mean2;

            numerator += diff1 * diff2;
            sumSq1 += diff1 * diff1;
            sumSq2 += diff2 * diff2;
        }

        double denominator = Math.sqrt(sumSq1) * Math.sqrt(sumSq2);
        if (denominator == 0.0) return 0.0;

        return numerator / denominator;
    }

    /**
     * User-Based Collaborative Filtering: Predicts unrated items for target user.
     */
    public List<ContentBasedRecommender.RecommendedItem> recommendUserBased(String targetUserId, int topK) {
        Map<String, Double> targetRatings = userItemMatrix.get(targetUserId);
        if (targetRatings == null || topK <= 0) {
            return Collections.emptyList();
        }

        double targetMean = targetRatings.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        // Compute Pearson correlation with all other users
        Map<String, Double> userSimilarities = new HashMap<>();
        for (String otherUser : userItemMatrix.keySet()) {
            if (!otherUser.equals(targetUserId)) {
                double sim = calculatePearsonCorrelation(targetUserId, otherUser);
                if (sim > 0.0) {
                    userSimilarities.put(otherUser, sim);
                }
            }
        }

        if (userSimilarities.isEmpty()) {
            return Collections.emptyList();
        }

        // Identify unrated candidate items
        Set<String> candidateDocIds = new HashSet<>();
        for (String otherUser : userSimilarities.keySet()) {
            for (String docId : userItemMatrix.get(otherUser).keySet()) {
                if (!targetRatings.containsKey(docId)) {
                    candidateDocIds.add(docId);
                }
            }
        }

        List<ContentBasedRecommender.RecommendedItem> recommendations = new ArrayList<>();

        // Predict rating for each candidate item
        for (String docId : candidateDocIds) {
            double num = 0.0;
            double den = 0.0;

            for (Map.Entry<String, Double> entry : userSimilarities.entrySet()) {
                String otherUser = entry.getKey();
                double sim = entry.getValue();

                Double otherRating = userItemMatrix.get(otherUser).get(docId);
                if (otherRating != null) {
                    double otherMean = userItemMatrix.get(otherUser).values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                    num += sim * (otherRating - otherMean);
                    den += Math.abs(sim);
                }
            }

            if (den > 0.0) {
                double predictedRating = targetMean + (num / den);
                Document doc = index.getDocument(docId);
                if (doc != null) {
                    recommendations.add(new ContentBasedRecommender.RecommendedItem(doc, predictedRating, "User-Based Collaborative Filtering"));
                }
            }
        }

        recommendations.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        return recommendations.subList(0, Math.min(topK, recommendations.size()));
    }
}
