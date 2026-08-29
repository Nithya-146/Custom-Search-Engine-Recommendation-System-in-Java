package com.inforetrieve.cli;

import com.inforetrieve.eval.EvaluationBenchmark;
import com.inforetrieve.indexer.Document;
import com.inforetrieve.indexer.InvertedIndex;
import com.inforetrieve.query.QueryProcessor;
import com.inforetrieve.query.SearchResult;
import com.inforetrieve.recommender.ContentBasedRecommender;
import com.inforetrieve.recommender.RecommendationService;

import java.util.*;

/**
 * Command-Line Interface (CLI) for InfoRetrieve Search Engine and Recommendation System.
 */
public class InteractiveCLI {

    private final InvertedIndex index;
    private final QueryProcessor queryProcessor;
    private final RecommendationService recommendationService;

    public InteractiveCLI(InvertedIndex index, QueryProcessor queryProcessor, RecommendationService recommendationService) {
        this.index = index;
        this.queryProcessor = queryProcessor;
        this.recommendationService = recommendationService;
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n=======================================================");
        System.out.println("   InfoRetrieve: Custom Java Search Engine & Recommender   ");
        System.out.println("=======================================================\n");

        while (true) {
            System.out.println("Select an Option:");
            System.out.println("1. Free-Text Ranked Search");
            System.out.println("2. Boolean Query (e.g. sci-fi AND intelligence NOT robot)");
            System.out.println("3. Exact Phrase Search (e.g. \"artificial intelligence\")");
            System.out.println("4. Content-Based Document Recommendations");
            System.out.println("5. User Collaborative Filtering Recommendations");
            System.out.println("6. Add New Document (Incremental Indexing)");
            System.out.println("7. Run Precision/Recall Evaluation Benchmark");
            System.out.println("8. Exit");
            System.out.print("> Choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    System.out.print("Enter free-text search query: ");
                    String q1 = scanner.nextLine();
                    displaySearchResult(queryProcessor.processQuery(q1, 1, 5));
                    break;
                case "2":
                    System.out.print("Enter boolean query (AND, OR, NOT): ");
                    String q2 = scanner.nextLine();
                    displaySearchResult(queryProcessor.processQuery(q2, 1, 5));
                    break;
                case "3":
                    System.out.print("Enter exact phrase query: ");
                    String q3 = scanner.nextLine();
                    if (!q3.startsWith("\"")) q3 = "\"" + q3 + "\"";
                    displaySearchResult(queryProcessor.processQuery(q3, 1, 5));
                    break;
                case "4":
                    System.out.print("Enter Document ID for recommendations (e.g. DOC-101): ");
                    String docId = scanner.nextLine();
                    List<ContentBasedRecommender.RecommendedItem> recs = recommendationService.recommendSimilarDocuments(docId, 5);
                    displayRecommendations(recs);
                    break;
                case "5":
                    System.out.print("Enter User ID for recommendations (e.g. USER-1): ");
                    String userId = scanner.nextLine();
                    List<ContentBasedRecommender.RecommendedItem> userRecs = recommendationService.recommendForUser(userId, 5);
                    displayRecommendations(userRecs);
                    break;
                case "6":
                    System.out.print("Enter Doc ID: ");
                    String id = scanner.nextLine();
                    System.out.print("Enter Title: ");
                    String title = scanner.nextLine();
                    System.out.print("Enter Content: ");
                    String content = scanner.nextLine();
                    Document newDoc = new Document(id, title, content, "Custom", "User");
                    index.addDocument(newDoc);
                    queryProcessor.buildTrieFromIndex();
                    System.out.println("Document successfully indexed into InvertedIndex!");
                    break;
                case "7":
                    runBenchmark();
                    break;
                case "8":
                    System.out.println("Exiting InfoRetrieve CLI. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid option. Try again.");
            }
            System.out.println();
        }
    }

    private void displaySearchResult(SearchResult result) {
        System.out.println("\n-------------------------------------------------------");
        System.out.printf("Search Results for '%s' (Hits: %d, Time: %d ms)\n",
                result.getQuery(), result.getTotalHits(), result.getExecutionTimeMs());
        if (result.isFuzzyApplied()) {
            System.out.printf("[Typo Corrected Query: '%s']\n", result.getCorrectedQuery());
        }
        System.out.println("-------------------------------------------------------");

        for (int i = 0; i < result.getHits().size(); i++) {
            SearchResult.Hit hit = result.getHits().get(i);
            System.out.printf("%d. [%s] %s (Score: %.4f)\n", i + 1, hit.getDocument().getDocId(), hit.getDocument().getTitle(), hit.getScore());
            System.out.printf("   Snippet: %s\n", hit.getSnippet());
        }
        if (result.getHits().isEmpty()) {
            System.out.println("No matching documents found.");
        }
    }

    private void displayRecommendations(List<ContentBasedRecommender.RecommendedItem> recs) {
        System.out.println("\n-------------------------------------------------------");
        System.out.println("Top Recommended Items:");
        System.out.println("-------------------------------------------------------");
        for (int i = 0; i < recs.size(); i++) {
            ContentBasedRecommender.RecommendedItem item = recs.get(i);
            System.out.printf("%d. [%s] %s (Score: %.4f) - %s\n",
                    i + 1, item.getDocument().getDocId(), item.getDocument().getTitle(), item.getScore(), item.getStrategy());
        }
        if (recs.isEmpty()) {
            System.out.println("No recommendations available.");
        }
    }

    private void runBenchmark() {
        EvaluationBenchmark benchmark = new EvaluationBenchmark();
        List<EvaluationBenchmark.TestCase> testCases = Arrays.asList(
                new EvaluationBenchmark.TestCase("dream technology manipulation", new HashSet<>(Arrays.asList("DOC-101"))),
                new EvaluationBenchmark.TestCase("virtual reality hacker", new HashSet<>(Arrays.asList("DOC-102"))),
                new EvaluationBenchmark.TestCase("space wormhole gravity", new HashSet<>(Arrays.asList("DOC-103"))),
                new EvaluationBenchmark.TestCase("synthetic intelligence android robot", new HashSet<>(Arrays.asList("DOC-107", "DOC-108"))),
                new EvaluationBenchmark.TestCase("crime mafia mob", new HashSet<>(Arrays.asList("DOC-105", "DOC-109")))
        );

        EvaluationBenchmark.BenchmarkMetrics m = benchmark.runBenchmark(queryProcessor, testCases, 5);
        System.out.println("\n=======================================================");
        System.out.println("               BENCHMARK EVALUATION RESULTS            ");
        System.out.println("=======================================================");
        System.out.printf("Total Test Queries: %d\n", m.getTotalQueries());
        System.out.printf("Precision@5:        %.4f\n", m.getPrecisionAtK());
        System.out.printf("Recall@5:           %.4f\n", m.getRecallAtK());
        System.out.printf("F1-Score:           %.4f\n", m.getF1Score());
        System.out.printf("Mean Avg Precision: %.4f\n", m.getMeanAveragePrecision());
        System.out.printf("Avg Query Latency:  %.2f ms\n", m.getAverageLatencyMs());
        System.out.println("=======================================================");
    }
}
