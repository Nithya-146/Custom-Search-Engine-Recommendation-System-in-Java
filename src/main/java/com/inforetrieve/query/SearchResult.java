package com.inforetrieve.query;

import com.inforetrieve.indexer.Document;
import java.util.List;

/**
 * Encapsulates search result hits, pagination metadata, query latency, and fuzzy correction notes.
 */
public class SearchResult {

    private String query;
    private List<Hit> hits;
    private int totalHits;
    private int page;
    private int pageSize;
    private int totalPages;
    private long executionTimeMs;
    private boolean fuzzyApplied;
    private String correctedQuery;

    public SearchResult() {}

    public SearchResult(String query, List<Hit> hits, int totalHits, int page, int pageSize, long executionTimeMs) {
        this.query = query;
        this.hits = hits;
        this.totalHits = totalHits;
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = pageSize > 0 ? (int) Math.ceil((double) totalHits / pageSize) : 0;
        this.executionTimeMs = executionTimeMs;
        this.fuzzyApplied = false;
    }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public List<Hit> getHits() { return hits; }
    public void setHits(List<Hit> hits) { this.hits = hits; }

    public int getTotalHits() { return totalHits; }
    public void setTotalHits(int totalHits) { this.totalHits = totalHits; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }

    public boolean isFuzzyApplied() { return fuzzyApplied; }
    public void setFuzzyApplied(boolean fuzzyApplied) { this.fuzzyApplied = fuzzyApplied; }

    public String getCorrectedQuery() { return correctedQuery; }
    public void setCorrectedQuery(String correctedQuery) { this.correctedQuery = correctedQuery; }

    public static class Hit {
        private Document document;
        private double score;
        private String snippet;

        public Hit() {}

        public Hit(Document document, double score, String snippet) {
            this.document = document;
            this.score = score;
            this.snippet = snippet;
        }

        public Document getDocument() { return document; }
        public void setDocument(Document document) { this.document = document; }

        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }

        public String getSnippet() { return snippet; }
        public void setSnippet(String snippet) { this.snippet = snippet; }
    }
}
