package com.inforetrieve.eval;

import com.inforetrieve.query.QueryProcessor;
import com.inforetrieve.query.SearchResult;

import java.util.*;

/**
 * Benchmark evaluator calculating Precision@K, Recall@K, F1-Score, MAP, and query latencies.
 */
public class EvaluationBenchmark {

    public static class TestCase {
        private final String query;
        private final Set<String> relevantDocIds;

        public TestCase(String query, Set<String> relevantDocIds) {
            this.query = query;
            this.relevantDocIds = relevantDocIds;
        }

        public String getQuery() { return query; }
        public Set<String> getRelevantDocIds() { return relevantDocIds; }
    }

    public static class BenchmarkMetrics {
        private double precisionAtK;
        private double recallAtK;
        private double f1Score;
        private double meanAveragePrecision;
        private double averageLatencyMs;
        private int totalQueries;

        public BenchmarkMetrics(double precisionAtK, double recallAtK, double f1Score, double meanAveragePrecision, double averageLatencyMs, int totalQueries) {
            this.precisionAtK = precisionAtK;
            this.recallAtK = recallAtK;
            this.f1Score = f1Score;
            this.meanAveragePrecision = meanAveragePrecision;
            this.averageLatencyMs = averageLatencyMs;
            this.totalQueries = totalQueries;
        }

        public double getPrecisionAtK() { return precisionAtK; }
        public double getRecallAtK() { return recallAtK; }
        public double getF1Score() { return f1Score; }
        public double getMeanAveragePrecision() { return meanAveragePrecision; }
        public double getAverageLatencyMs() { return averageLatencyMs; }
        public int getTotalQueries() { return totalQueries; }
    }

    /**
     * Runs evaluation over a test set of labeled queries.
     */
    public BenchmarkMetrics runBenchmark(QueryProcessor queryProcessor, List<TestCase> testCases, int k) {
        if (testCases == null || testCases.isEmpty() || k <= 0) {
            return new BenchmarkMetrics(0, 0, 0, 0, 0, 0);
        }

        double totalPrecision = 0.0;
        double totalRecall = 0.0;
        double totalAp = 0.0;
        long totalLatency = 0L;

        for (TestCase tc : testCases) {
            SearchResult result = queryProcessor.processQuery(tc.getQuery(), 1, k);
            totalLatency += result.getExecutionTimeMs();

            List<SearchResult.Hit> hits = result.getHits();
            Set<String> relevant = tc.getRelevantDocIds();

            int relevantRetrieved = 0;
            double sumPrecision = 0.0;

            for (int i = 0; i < hits.size(); i++) {
                String docId = hits.get(i).getDocument().getDocId();
                if (relevant.contains(docId)) {
                    relevantRetrieved++;
                    double precisionAtRankI = (double) relevantRetrieved / (i + 1);
                    sumPrecision += precisionAtRankI;
                }
            }

            double precision = hits.isEmpty() ? 0.0 : (double) relevantRetrieved / hits.size();
            double recall = relevant.isEmpty() ? 0.0 : (double) relevantRetrieved / relevant.size();
            double ap = relevant.isEmpty() ? 0.0 : sumPrecision / relevant.size();

            totalPrecision += precision;
            totalRecall += recall;
            totalAp += ap;
        }

        int n = testCases.size();
        double avgPrecision = totalPrecision / n;
        double avgRecall = totalRecall / n;
        double map = totalAp / n;
        double f1 = (avgPrecision + avgRecall) > 0 ? (2 * avgPrecision * avgRecall) / (avgPrecision + avgRecall) : 0.0;
        double avgLatency = (double) totalLatency / n;

        return new BenchmarkMetrics(avgPrecision, avgRecall, f1, map, avgLatency, n);
    }
}
