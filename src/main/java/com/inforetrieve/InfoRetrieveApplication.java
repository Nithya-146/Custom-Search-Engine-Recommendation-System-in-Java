package com.inforetrieve;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inforetrieve.cli.InteractiveCLI;
import com.inforetrieve.indexer.*;
import com.inforetrieve.query.QueryProcessor;
import com.inforetrieve.recommender.RecommendationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Spring Boot Application Driver for InfoRetrieve.
 * Ingests sample documents, initializes InvertedIndex, and configures core DSA services.
 */
@SpringBootApplication
public class InfoRetrieveApplication {

    private static final String INDEX_FILE_PATH = "data/index_cache.dat";

    public static void main(String[] args) {
        if (Arrays.asList(args).contains("--cli")) {
            System.setProperty("spring.main.web-application-type", "none");
        }
        SpringApplication.run(InfoRetrieveApplication.class, args);
    }

    @Bean
    public InvertedIndex invertedIndex() {
        // Attempt disk persistence restoration
        try {
            File cacheFile = new File(INDEX_FILE_PATH);
            if (cacheFile.exists()) {
                System.out.println("Restoring InvertedIndex from disk cache: " + INDEX_FILE_PATH);
                return IndexSerializer.loadIndex(INDEX_FILE_PATH);
            }
        } catch (Exception e) {
            System.err.println("Could not load cached index. Rebuilding: " + e.getMessage());
        }

        InvertedIndex index = new InvertedIndex();
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = getClass().getResourceAsStream("/dataset/movies.json");
            List<Document> docs = mapper.readValue(is, new TypeReference<List<Document>>() {});

            System.out.printf("Ingesting %d documents into InvertedIndex via ConcurrentDocumentIndexer...\n", docs.size());
            ConcurrentDocumentIndexer indexer = new ConcurrentDocumentIndexer(4);
            long timeMs = indexer.indexDocuments(docs, index);
            indexer.shutdown();

            System.out.printf("Indexing complete! Processed %d documents, Vocabulary size: %d terms in %d ms.\n",
                    index.getTotalDocuments(), index.getVocabularySize(), timeMs);

            // Persist index to disk
            IndexSerializer.saveIndex(index, INDEX_FILE_PATH);
            System.out.println("InvertedIndex persisted to disk cache: " + INDEX_FILE_PATH);
        } catch (Exception e) {
            System.err.println("Failed to load dataset: " + e.getMessage());
        }
        return index;
    }

    @Bean
    public QueryProcessor queryProcessor(InvertedIndex index) {
        return new QueryProcessor(index);
    }

    @Bean
    public RecommendationService recommendationService(InvertedIndex index) {
        RecommendationService service = new RecommendationService(index);

        // Seed synthetic user rating interaction matrix for Collaborative Filtering
        service.getCollaborativeFilteringRecommender().addRating("USER-1", "DOC-101", 5.0); // Inception
        service.getCollaborativeFilteringRecommender().addRating("USER-1", "DOC-102", 4.5); // Matrix
        service.getCollaborativeFilteringRecommender().addRating("USER-1", "DOC-103", 5.0); // Interstellar
        service.getCollaborativeFilteringRecommender().addRating("USER-1", "DOC-107", 4.0); // Ex Machina

        service.getCollaborativeFilteringRecommender().addRating("USER-2", "DOC-101", 4.5); // Inception
        service.getCollaborativeFilteringRecommender().addRating("USER-2", "DOC-102", 5.0); // Matrix
        service.getCollaborativeFilteringRecommender().addRating("USER-2", "DOC-107", 4.5); // Ex Machina
        service.getCollaborativeFilteringRecommender().addRating("USER-2", "DOC-108", 4.8); // Blade Runner 2049

        service.getCollaborativeFilteringRecommender().addRating("USER-3", "DOC-104", 5.0); // Dark Knight
        service.getCollaborativeFilteringRecommender().addRating("USER-3", "DOC-105", 4.8); // Pulp Fiction
        service.getCollaborativeFilteringRecommender().addRating("USER-3", "DOC-109", 5.0); // Godfather

        return service;
    }

    @Bean
    public CommandLineRunner commandLineRunner(InvertedIndex index,
                                               QueryProcessor queryProcessor,
                                               RecommendationService recommendationService) {
        return args -> {
            if (Arrays.asList(args).contains("--cli")) {
                InteractiveCLI cli = new InteractiveCLI(index, queryProcessor, recommendationService);
                cli.start();
            } else {
                System.out.println("\nInfoRetrieve REST API & Web Dashboard initialized.");
                System.out.println("Access UI: http://localhost:8080");
                System.out.println("API Search endpoint: GET http://localhost:8080/api/search?q=query\n");
            }
        };
    }
}
