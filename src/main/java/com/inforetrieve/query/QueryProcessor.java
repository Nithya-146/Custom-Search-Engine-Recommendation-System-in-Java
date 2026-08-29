package com.inforetrieve.query;

import com.inforetrieve.indexer.Document;
import com.inforetrieve.indexer.InvertedIndex;
import com.inforetrieve.indexer.Posting;
import com.inforetrieve.indexer.Tokenizer;
import com.inforetrieve.ranking.CosineSimilarity;
import com.inforetrieve.ranking.LevenshteinDistance;
import com.inforetrieve.ranking.TfIdfCalculator;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Main query processor orchestrating free-text ranked search, boolean queries,
 * phrase search, fuzzy typo correction, snippet generation, and pagination.
 */
public class QueryProcessor {

    private final InvertedIndex index;
    private final Tokenizer tokenizer;
    private final TfIdfCalculator tfIdfCalculator;
    private final CosineSimilarity cosineSimilarity;
    private final BooleanQueryEvaluator booleanEvaluator;
    private final PhraseQueryMatcher phraseMatcher;
    private final LevenshteinDistance levenshteinDistance;
    private final AutocompleteTrie autocompleteTrie;

    public QueryProcessor(InvertedIndex index) {
        this.index = index;
        this.tokenizer = index.getTokenizer();
        this.tfIdfCalculator = new TfIdfCalculator();
        this.cosineSimilarity = new CosineSimilarity();
        this.booleanEvaluator = new BooleanQueryEvaluator(index);
        this.phraseMatcher = new PhraseQueryMatcher(index);
        this.levenshteinDistance = new LevenshteinDistance();
        this.autocompleteTrie = new AutocompleteTrie();
        buildTrieFromIndex();
    }

    /**
     * Populates the AutocompleteTrie with terms from the index vocabulary.
     */
    public void buildTrieFromIndex() {
        for (String term : index.getAllTerms()) {
            autocompleteTrie.insert(term);
        }
    }

    public AutocompleteTrie getAutocompleteTrie() {
        return autocompleteTrie;
    }

    /**
     * Main entry point for query processing. Detects phrase search ("..."), boolean queries (AND/OR/NOT),
     * or free-text ranked queries with fuzzy matching fallback.
     */
    public SearchResult processQuery(String rawQuery, int page, int pageSize) {
        long startTime = System.currentTimeMillis();
        if (rawQuery == null || rawQuery.trim().isEmpty()) {
            return new SearchResult("", Collections.emptyList(), 0, page, pageSize, 0L);
        }

        String query = rawQuery.trim();
        List<SearchResult.Hit> hits;
        boolean fuzzyApplied = false;
        String correctedQuery = null;

        // 1. Phrase Search check: enclosed in double quotes
        if (query.startsWith("\"") && query.endsWith("\"") && query.length() > 2) {
            String phrase = query.substring(1, query.length() - 1);
            Set<String> matchedDocIds = phraseMatcher.matchPhrase(phrase);
            hits = rankDocIdsForQuery(matchedDocIds, phrase);
        }
        // 2. Boolean Query check: contains AND, OR, or NOT keywords
        else if (containsBooleanOperators(query)) {
            Set<String> matchedDocIds = booleanEvaluator.evaluate(query);
            hits = rankDocIdsForQuery(matchedDocIds, query);
        }
        // 3. Free-Text Ranked Search
        else {
            hits = executeFreeTextRankedSearch(query);

            // 4. Fuzzy fallback if 0 exact matches found
            if (hits.isEmpty()) {
                List<String> correctedTokens = performFuzzyCorrection(query);
                if (!correctedTokens.isEmpty()) {
                    correctedQuery = String.join(" ", correctedTokens);
                    hits = executeFreeTextRankedSearch(correctedQuery);
                    if (!hits.isEmpty()) {
                        fuzzyApplied = true;
                    }
                }
            }
        }

        int totalHits = hits.size();
        // Pagination slicing
        int fromIndex = Math.min((page - 1) * pageSize, totalHits);
        int toIndex = Math.min(fromIndex + pageSize, totalHits);
        List<SearchResult.Hit> pagedHits = hits.subList(fromIndex, toIndex);

        long executionTimeMs = System.currentTimeMillis() - startTime;
        SearchResult result = new SearchResult(query, pagedHits, totalHits, page, pageSize, executionTimeMs);
        result.setFuzzyApplied(fuzzyApplied);
        result.setCorrectedQuery(correctedQuery);
        return result;
    }

    private boolean containsBooleanOperators(String q) {
        String[] tokens = q.split("\\s+");
        for (String t : tokens) {
            String upper = t.toUpperCase();
            if (upper.equals("AND") || upper.equals("OR") || upper.equals("NOT")) {
                return true;
            }
        }
        return false;
    }

    private List<SearchResult.Hit> executeFreeTextRankedSearch(String query) {
        List<String> queryTokens = tokenizer.tokenizeAndStem(query);
        if (queryTokens.isEmpty()) {
            return Collections.emptyList();
        }

        // Collect candidate document IDs containing at least one query term
        Set<String> candidateDocIds = new HashSet<>();
        for (String token : queryTokens) {
            List<Posting> postings = index.getPostings(token);
            for (Posting p : postings) {
                candidateDocIds.add(p.getDocId());
            }
        }

        if (candidateDocIds.isEmpty()) {
            return Collections.emptyList();
        }

        // Compute Query TF-IDF Vector
        Map<String, Double> queryVector = tfIdfCalculator.computeQueryVector(queryTokens, index);
        double queryNorm = tfIdfCalculator.computeVectorMagnitude(queryVector);

        List<SearchResult.Hit> hits = new ArrayList<>();
        for (String docId : candidateDocIds) {
            Document doc = index.getDocument(docId);
            if (doc == null) continue;

            Map<String, Double> docVector = tfIdfCalculator.computeDocumentVector(docId, index);
            double docNorm = tfIdfCalculator.computeVectorMagnitude(docVector);

            double score = cosineSimilarity.calculateSimilarity(queryVector, docVector, queryNorm, docNorm);
            if (score > 0.0) {
                String snippet = generateSnippet(doc.getContent(), queryTokens);
                hits.add(new SearchResult.Hit(doc, score, snippet));
            }
        }

        // Sort hits by Cosine Similarity score descending
        hits.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        return hits;
    }

    private List<SearchResult.Hit> rankDocIdsForQuery(Set<String> docIds, String queryStr) {
        if (docIds == null || docIds.isEmpty()) return Collections.emptyList();
        List<String> queryTokens = tokenizer.tokenizeAndStem(queryStr);

        Map<String, Double> queryVector = tfIdfCalculator.computeQueryVector(queryTokens, index);
        double queryNorm = tfIdfCalculator.computeVectorMagnitude(queryVector);

        List<SearchResult.Hit> hits = new ArrayList<>();
        for (String docId : docIds) {
            Document doc = index.getDocument(docId);
            if (doc == null) continue;

            Map<String, Double> docVector = tfIdfCalculator.computeDocumentVector(docId, index);
            double docNorm = tfIdfCalculator.computeVectorMagnitude(docVector);

            double score = cosineSimilarity.calculateSimilarity(queryVector, docVector, queryNorm, docNorm);
            if (score == 0.0) score = 0.001; // Base score for boolean/phrase match if vector overlap is minimal

            String snippet = generateSnippet(doc.getContent(), queryTokens);
            hits.add(new SearchResult.Hit(doc, score, snippet));
        }

        hits.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        return hits;
    }

    private List<String> performFuzzyCorrection(String query) {
        List<String> rawTokens = tokenizer.tokenize(query);
        List<String> corrected = new ArrayList<>();
        Set<String> vocabulary = index.getAllTerms();

        for (String token : rawTokens) {
            String stemmed = tokenizer.stemToken(token);
            if (vocabulary.contains(stemmed)) {
                corrected.add(token);
            } else {
                List<String> similarTerms = levenshteinDistance.findSimilarTerms(stemmed, vocabulary, 2);
                if (!similarTerms.isEmpty()) {
                    corrected.add(similarTerms.get(0));
                } else {
                    corrected.add(token);
                }
            }
        }
        return corrected;
    }

    /**
     * Generates a context snippet highlighting matching query terms.
     */
    public String generateSnippet(String content, List<String> stemmedQueryTokens) {
        if (content == null || content.isEmpty()) return "";
        String[] words = content.split("\\s+");
        if (words.length <= 25) return content;

        int bestWindowStart = 0;
        int maxMatchesInWindow = -1;
        int windowSize = 25;

        for (int i = 0; i <= words.length - windowSize; i += 5) {
            int matchCount = 0;
            for (int j = i; j < i + windowSize; j++) {
                String stemmedWord = tokenizer.stemToken(words[j].replaceAll("[^a-zA-Z0-9]", ""));
                if (stemmedQueryTokens.contains(stemmedWord)) {
                    matchCount++;
                }
            }
            if (matchCount > maxMatchesInWindow) {
                maxMatchesInWindow = matchCount;
                bestWindowStart = i;
            }
        }

        StringBuilder sb = new StringBuilder();
        if (bestWindowStart > 0) sb.append("... ");

        int end = Math.min(bestWindowStart + windowSize, words.length);
        for (int i = bestWindowStart; i < end; i++) {
            sb.append(words[i]).append(" ");
        }
        if (end < words.length) sb.append("...");

        return sb.toString().trim();
    }
}
