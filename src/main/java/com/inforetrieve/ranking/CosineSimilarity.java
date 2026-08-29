package com.inforetrieve.ranking;

import java.util.Map;

/**
 * Calculates Cosine Similarity between sparse term weight vectors.
 */
public class CosineSimilarity {

    /**
     * Calculates cosine similarity between vector u and vector v.
     */
    public double calculateSimilarity(Map<String, Double> vecU, Map<String, Double> vecV) {
        if (vecU == null || vecV == null || vecU.isEmpty() || vecV.isEmpty()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        // Optimize dot product by iterating over the smaller vector
        Map<String, Double> smaller = vecU.size() < vecV.size() ? vecU : vecV;
        Map<String, Double> larger = vecU.size() < vecV.size() ? vecV : vecU;

        for (Map.Entry<String, Double> entry : smaller.entrySet()) {
            String term = entry.getKey();
            Double weightV = larger.get(term);
            if (weightV != null) {
                dotProduct += entry.getValue() * weightV;
            }
        }

        if (dotProduct <= 0.0) return 0.0;

        double normU = calculateMagnitude(vecU);
        double normV = calculateMagnitude(vecV);

        if (normU == 0.0 || normV == 0.0) return 0.0;

        return dotProduct / (normU * normV);
    }

    /**
     * Calculates cosine similarity when pre-computed L2 magnitudes are provided.
     */
    public double calculateSimilarity(Map<String, Double> vecU, Map<String, Double> vecV, double normU, double normV) {
        if (vecU == null || vecV == null || vecU.isEmpty() || vecV.isEmpty() || normU <= 0.0 || normV <= 0.0) {
            return 0.0;
        }

        double dotProduct = 0.0;
        Map<String, Double> smaller = vecU.size() < vecV.size() ? vecU : vecV;
        Map<String, Double> larger = vecU.size() < vecV.size() ? vecV : vecU;

        for (Map.Entry<String, Double> entry : smaller.entrySet()) {
            String term = entry.getKey();
            Double weightV = larger.get(term);
            if (weightV != null) {
                dotProduct += entry.getValue() * weightV;
            }
        }

        return dotProduct / (normU * normV);
    }

    private double calculateMagnitude(Map<String, Double> vec) {
        double sumSquares = 0.0;
        for (double val : vec.values()) {
            sumSquares += val * val;
        }
        return Math.sqrt(sumSquares);
    }
}
