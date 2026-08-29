package com.inforetrieve.query;

import com.inforetrieve.indexer.InvertedIndex;
import com.inforetrieve.indexer.Posting;
import com.inforetrieve.indexer.Tokenizer;

import java.util.*;

/**
 * Matches exact multi-word positional phrases using word offset positions in postings.
 */
public class PhraseQueryMatcher {

    private final InvertedIndex index;
    private final Tokenizer tokenizer;

    public PhraseQueryMatcher(InvertedIndex index) {
        this.index = index;
        this.tokenizer = index.getTokenizer();
    }

    /**
     * Evaluates exact positional match for a phrase (e.g. "artificial intelligence").
     * Returns matching Document IDs.
     */
    public Set<String> matchPhrase(String phrase) {
        if (phrase == null || phrase.trim().isEmpty()) {
            return Collections.emptySet();
        }

        List<String> stemmedTokens = tokenizer.tokenizeAndStem(phrase);
        if (stemmedTokens.isEmpty()) {
            return Collections.emptySet();
        }

        if (stemmedTokens.size() == 1) {
            Set<String> docs = new HashSet<>();
            for (Posting p : index.getPostings(stemmedTokens.get(0))) {
                docs.add(p.getDocId());
            }
            return docs;
        }

        // Retrieve posting list for first word
        List<Posting> firstPostings = index.getPostings(stemmedTokens.get(0));
        if (firstPostings.isEmpty()) return Collections.emptySet();

        Set<String> matchingDocIds = new HashSet<>();

        for (Posting firstPosting : firstPostings) {
            String docId = firstPosting.getDocId();
            List<Integer> candidatePositions = new ArrayList<>(firstPosting.getPositions());

            boolean phraseFoundInDoc = true;

            for (int i = 1; i < stemmedTokens.size(); i++) {
                String nextTerm = stemmedTokens.get(i);
                List<Posting> nextPostings = index.getPostings(nextTerm);

                // Find posting for next term in same document
                Posting nextDocPosting = null;
                for (Posting p : nextPostings) {
                    if (p.getDocId().equals(docId)) {
                        nextDocPosting = p;
                        break;
                    }
                }

                if (nextDocPosting == null) {
                    phraseFoundInDoc = false;
                    break;
                }

                // Verify positional offset: position(t_{i}) == position(t_{i-1}) + 1
                List<Integer> validNextPositions = new ArrayList<>();
                for (int pos : candidatePositions) {
                    if (nextDocPosting.getPositions().contains(pos + 1)) {
                        validNextPositions.add(pos + 1);
                    }
                }

                if (validNextPositions.isEmpty()) {
                    phraseFoundInDoc = false;
                    break;
                }

                candidatePositions = validNextPositions;
            }

            if (phraseFoundInDoc) {
                matchingDocIds.add(docId);
            }
        }

        return matchingDocIds;
    }
}
