package com.inforetrieve.query;

import com.inforetrieve.indexer.InvertedIndex;
import com.inforetrieve.indexer.Posting;
import com.inforetrieve.indexer.Tokenizer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Evaluates Boolean queries (AND, OR, NOT) using inverted index set operations.
 */
public class BooleanQueryEvaluator {

    private final InvertedIndex index;
    private final Tokenizer tokenizer;

    public BooleanQueryEvaluator(InvertedIndex index) {
        this.index = index;
        this.tokenizer = index.getTokenizer();
    }

    /**
     * Evaluates a boolean query string and returns matching document IDs.
     */
    public Set<String> evaluate(String queryStr) {
        if (queryStr == null || queryStr.trim().isEmpty()) {
            return Collections.emptySet();
        }

        String[] tokens = queryStr.trim().split("\\s+");
        if (tokens.length == 0) return Collections.emptySet();

        Set<String> currentDocs = null;
        String currentOp = "OR"; // Default boolean operator

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            String upper = token.toUpperCase();

            if (upper.equals("AND") || upper.equals("OR") || upper.equals("NOT")) {
                currentOp = upper;
                continue;
            }

            // Retrieve document postings for term
            Set<String> termDocs = getDocumentIdsForTerm(token);

            if (currentDocs == null) {
                if (currentOp.equals("NOT")) {
                    // NOT as first token -> All docs minus termDocs
                    currentDocs = new HashSet<>(index.getDocumentMap().keySet());
                    currentDocs.removeAll(termDocs);
                } else {
                    currentDocs = new HashSet<>(termDocs);
                }
            } else {
                switch (currentOp) {
                    case "AND":
                        currentDocs.retainAll(termDocs); // Intersection
                        break;
                    case "OR":
                        currentDocs.addAll(termDocs); // Union
                        break;
                    case "NOT":
                        currentDocs.removeAll(termDocs); // Set Difference
                        break;
                }
            }
            currentOp = "AND"; // Default consecutive terms to AND
        }

        return currentDocs != null ? currentDocs : Collections.emptySet();
    }

    private Set<String> getDocumentIdsForTerm(String term) {
        String stemmed = tokenizer.stemToken(term);
        List<Posting> postings = index.getPostings(stemmed);
        return postings.stream().map(Posting::getDocId).collect(Collectors.toSet());
    }
}
