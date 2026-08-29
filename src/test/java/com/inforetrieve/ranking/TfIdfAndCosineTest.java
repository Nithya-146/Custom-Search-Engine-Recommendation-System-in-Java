package com.inforetrieve.ranking;

import com.inforetrieve.indexer.Document;
import com.inforetrieve.indexer.InvertedIndex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TfIdfAndCosineTest {

    private TfIdfCalculator tfIdfCalculator;
    private CosineSimilarity cosineSimilarity;

    @BeforeEach
    void setUp() {
        tfIdfCalculator = new TfIdfCalculator();
        cosineSimilarity = new CosineSimilarity();
    }

    @Test
    @DisplayName("TF formula computes 1 + log10(tf) accurately")
    void testTermFrequencyFormula() {
        assertEquals(0.0, tfIdfCalculator.calculateTF(0));
        assertEquals(1.0, tfIdfCalculator.calculateTF(1));
        assertEquals(2.0, tfIdfCalculator.calculateTF(10));
    }

    @Test
    @DisplayName("Cosine similarity yields 1.0 for identical vectors and 0.0 for orthogonal vectors")
    void testCosineSimilarityMath() {
        Map<String, Double> vecA = new HashMap<>();
        vecA.put("search", 0.5);
        vecA.put("engine", 0.8);

        Map<String, Double> vecB = new HashMap<>();
        vecB.put("search", 0.5);
        vecB.put("engine", 0.8);

        double identicalSim = cosineSimilarity.calculateSimilarity(vecA, vecB);
        assertEquals(1.0, identicalSim, 0.0001);

        Map<String, Double> vecOrthogonal = new HashMap<>();
        vecOrthogonal.put("banana", 0.9);

        double orthogonalSim = cosineSimilarity.calculateSimilarity(vecA, vecOrthogonal);
        assertEquals(0.0, orthogonalSim, 0.0001);
    }
}
