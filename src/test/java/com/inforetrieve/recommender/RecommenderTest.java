package com.inforetrieve.recommender;

import com.inforetrieve.indexer.Document;
import com.inforetrieve.indexer.InvertedIndex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecommenderTest {

    private InvertedIndex index;
    private RecommendationService recommendationService;

    @BeforeEach
    void setUp() {
        index = new InvertedIndex();
        Document d1 = new Document("DOC-1", "Space Exploration", "Astronauts travel through space, black holes, and cosmic wormholes.", "Sci-Fi", "Author");
        Document d2 = new Document("DOC-2", "Cosmic Black Holes", "Space explorers study gravity, black holes, and relativity.", "Sci-Fi", "Author");
        Document d3 = new Document("DOC-3", "Baking Pastries", "Baking delicious chocolate cakes, pastries, and sweet desserts in the kitchen.", "Food", "Author");

        index.addDocument(d1);
        index.addDocument(d2);
        index.addDocument(d3);

        recommendationService = new RecommendationService(index);
    }

    @Test
    @DisplayName("Content-Based Recommender identifies content-similar document vectors")
    void testContentBasedRecommender() {
        List<ContentBasedRecommender.RecommendedItem> recs = recommendationService.recommendSimilarDocuments("DOC-1", 2);

        assertNotNull(recs);
        assertFalse(recs.isEmpty());
        // DOC-2 ("Cosmic Black Holes") is content-similar to DOC-1 ("Space Exploration")
        assertEquals("DOC-2", recs.get(0).getDocument().getDocId());
    }

    @Test
    @DisplayName("Collaborative Filtering computes Pearson correlation over user matrix")
    void testCollaborativeFiltering() {
        CollaborativeFilteringRecommender cf = recommendationService.getCollaborativeFilteringRecommender();

        cf.addRating("USER-A", "DOC-1", 5.0);
        cf.addRating("USER-A", "DOC-2", 4.0);

        cf.addRating("USER-B", "DOC-1", 5.0);
        cf.addRating("USER-B", "DOC-2", 4.0);
        cf.addRating("USER-B", "DOC-3", 1.0);

        double correlation = cf.calculatePearsonCorrelation("USER-A", "USER-B");
        assertTrue(correlation > 0.0); // Positive correlation for matching item preference order

        List<ContentBasedRecommender.RecommendedItem> userRecs = cf.recommendUserBased("USER-A", 1);
        assertNotNull(userRecs);
    }
}
