package com.inforetrieve.api;

import com.inforetrieve.eval.EvaluationBenchmark;
import com.inforetrieve.indexer.Document;
import com.inforetrieve.indexer.InvertedIndex;
import com.inforetrieve.query.QueryProcessor;
import com.inforetrieve.query.SearchResult;
import com.inforetrieve.recommender.ContentBasedRecommender;
import com.inforetrieve.recommender.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Spring Boot REST Controller exposing InfoRetrieve search, recommendation, dynamic indexing,
 * autocomplete, and evaluation benchmark APIs.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class SearchController {

    private final InvertedIndex index;
    private final QueryProcessor queryProcessor;
    private final RecommendationService recommendationService;
    private final EvaluationBenchmark benchmark;

    public SearchController(InvertedIndex index,
                            QueryProcessor queryProcessor,
                            RecommendationService recommendationService) {
        this.index = index;
        this.queryProcessor = queryProcessor;
        this.recommendationService = recommendationService;
        this.benchmark = new EvaluationBenchmark();
    }

    /**
     * Executes relevance-ranked, boolean, or phrase search with pagination.
     */
    @GetMapping("/search")
    public ResponseEntity<SearchResult> search(
            @RequestParam(name = "q") String query,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        SearchResult result = queryProcessor.processQuery(query, page, size);
        return ResponseEntity.ok(result);
    }

    /**
     * Trie-based autocomplete endpoint.
     */
    @GetMapping("/autocomplete")
    public ResponseEntity<List<String>> autocomplete(
            @RequestParam(name = "prefix") String prefix,
            @RequestParam(name = "limit", defaultValue = "5") int limit) {
        List<String> suggestions = queryProcessor.getAutocompleteTrie().getSuggestions(prefix, limit);
        return ResponseEntity.ok(suggestions);
    }

    /**
     * Content-Based recommendation endpoint.
     */
    @GetMapping("/recommend/content")
    public ResponseEntity<List<ContentBasedRecommender.RecommendedItem>> recommendContent(
            @RequestParam(name = "docId") String docId,
            @RequestParam(name = "limit", defaultValue = "5") int limit) {
        List<ContentBasedRecommender.RecommendedItem> items = recommendationService.recommendSimilarDocuments(docId, limit);
        return ResponseEntity.ok(items);
    }

    /**
     * Collaborative filtering recommendation endpoint.
     */
    @GetMapping("/recommend/collaborative")
    public ResponseEntity<List<ContentBasedRecommender.RecommendedItem>> recommendCollaborative(
            @RequestParam(name = "userId") String userId,
            @RequestParam(name = "limit", defaultValue = "5") int limit) {
        List<ContentBasedRecommender.RecommendedItem> items = recommendationService.recommendForUser(userId, limit);
        return ResponseEntity.ok(items);
    }

    /**
     * Dynamic document ingestion endpoint (Incremental Indexing).
     */
    @PostMapping("/index/document")
    public ResponseEntity<Map<String, Object>> addDocument(@RequestBody Document doc) {
        long startTime = System.currentTimeMillis();
        index.addDocument(doc);
        queryProcessor.buildTrieFromIndex();
        long timeMs = System.currentTimeMillis() - startTime;

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("docId", doc.getDocId());
        response.put("indexingTimeMs", timeMs);
        response.put("totalDocuments", index.getTotalDocuments());
        response.put("vocabularySize", index.getVocabularySize());
        return ResponseEntity.ok(response);
    }

    /**
     * System statistics endpoint.
     */
    @GetMapping("/index/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDocuments", index.getTotalDocuments());
        stats.put("vocabularySize", index.getVocabularySize());
        stats.put("status", "Active");
        return ResponseEntity.ok(stats);
    }

    /**
     * Evaluation Benchmark endpoint.
     */
    @GetMapping("/eval")
    public ResponseEntity<EvaluationBenchmark.BenchmarkMetrics> runEvaluation() {
        List<EvaluationBenchmark.TestCase> testCases = Arrays.asList(
                new EvaluationBenchmark.TestCase("dream technology manipulation", new HashSet<>(Arrays.asList("DOC-101"))),
                new EvaluationBenchmark.TestCase("virtual reality hacker", new HashSet<>(Arrays.asList("DOC-102"))),
                new EvaluationBenchmark.TestCase("space wormhole gravity", new HashSet<>(Arrays.asList("DOC-103"))),
                new EvaluationBenchmark.TestCase("synthetic intelligence android robot", new HashSet<>(Arrays.asList("DOC-107", "DOC-108"))),
                new EvaluationBenchmark.TestCase("crime mafia mob", new HashSet<>(Arrays.asList("DOC-105", "DOC-109")))
        );

        EvaluationBenchmark.BenchmarkMetrics metrics = benchmark.runBenchmark(queryProcessor, testCases, 5);
        return ResponseEntity.ok(metrics);
    }
}
