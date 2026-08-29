package com.inforetrieve.recommender;

import com.inforetrieve.indexer.InvertedIndex;

import java.util.List;

/**
 * Unified service providing Content-Based and Collaborative recommendation capabilities.
 */
public class RecommendationService {

    private final ContentBasedRecommender contentBasedRecommender;
    private final CollaborativeFilteringRecommender collaborativeFilteringRecommender;

    public RecommendationService(InvertedIndex index) {
        this.contentBasedRecommender = new ContentBasedRecommender(index);
        this.collaborativeFilteringRecommender = new CollaborativeFilteringRecommender(index);
    }

    public ContentBasedRecommender getContentBasedRecommender() {
        return contentBasedRecommender;
    }

    public CollaborativeFilteringRecommender getCollaborativeFilteringRecommender() {
        return collaborativeFilteringRecommender;
    }

    public List<ContentBasedRecommender.RecommendedItem> recommendSimilarDocuments(String docId, int topK) {
        return contentBasedRecommender.recommendSimilarDocuments(docId, topK);
    }

    public List<ContentBasedRecommender.RecommendedItem> recommendForUser(String userId, int topK) {
        return collaborativeFilteringRecommender.recommendUserBased(userId, topK);
    }
}
