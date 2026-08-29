package com.inforetrieve.ranking;

import java.util.*;

/**
 * Implements Levenshtein Distance dynamic programming algorithm for typo tolerance and fuzzy term matching.
 */
public class LevenshteinDistance {

    /**
     * Computes the Levenshtein edit distance between string s1 and string s2.
     */
    public int computeDistance(String s1, String s2) {
        if (s1 == null || s2 == null) {
            throw new IllegalArgumentException("Strings must not be null");
        }
        if (s1.equals(s2)) return 0;
        if (s1.isEmpty()) return s2.length();
        if (s2.isEmpty()) return s1.length();

        int len1 = s1.length();
        int len2 = s2.length();
        int[] dp = new int[len2 + 1];

        for (int j = 0; j <= len2; j++) {
            dp[j] = j;
        }

        for (int i = 1; i <= len1; i++) {
            int prev = dp[0];
            dp[0] = i;
            char char1 = s1.charAt(i - 1);

            for (int j = 1; j <= len2; j++) {
                int temp = dp[j];
                char char2 = s2.charAt(j - 1);
                int cost = (char1 == char2) ? 0 : 1;

                dp[j] = Math.min(Math.min(dp[j] + 1, dp[j - 1] + 1), prev + cost);
                prev = temp;
            }
        }
        return dp[len2];
    }

    /**
     * Finds vocabulary terms within a maximum Levenshtein edit distance from queryTerm.
     */
    public List<String> findSimilarTerms(String queryTerm, Collection<String> vocabulary, int maxDistance) {
        if (queryTerm == null || vocabulary == null) {
            return Collections.emptyList();
        }

        List<TermDistance> matches = new ArrayList<>();
        String target = queryTerm.toLowerCase();

        for (String term : vocabulary) {
            // Quick length filter optimization
            if (Math.abs(term.length() - target.length()) > maxDistance) {
                continue;
            }
            int dist = computeDistance(target, term);
            if (dist <= maxDistance) {
                matches.add(new TermDistance(term, dist));
            }
        }

        matches.sort(Comparator.comparingInt(td -> td.distance));
        List<String> result = new ArrayList<>(matches.size());
        for (TermDistance td : matches) {
            result.add(td.term);
        }
        return result;
    }

    private static class TermDistance {
        final String term;
        final int distance;

        TermDistance(String term, int distance) {
            this.term = term;
            this.distance = distance;
        }
    }
}
