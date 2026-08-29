package com.inforetrieve.query;

import java.util.*;

/**
 * Trie data structure for fast O(k) prefix search and autocomplete suggestions.
 */
public class AutocompleteTrie {

    private static class TrieNode {
        final Map<Character, TrieNode> children = new HashMap<>();
        boolean isWord = false;
        int frequency = 0;
        String word = null;
    }

    private final TrieNode root;

    public AutocompleteTrie() {
        this.root = new TrieNode();
    }

    /**
     * Inserts a word into the Trie.
     */
    public void insert(String word) {
        if (word == null || word.trim().isEmpty()) return;
        String normalized = word.toLowerCase().trim();

        TrieNode current = root;
        for (char ch : normalized.toCharArray()) {
            current = current.children.computeIfAbsent(ch, c -> new TrieNode());
        }
        current.isWord = true;
        current.frequency++;
        current.word = normalized;
    }

    /**
     * Retrieves top prefix autocomplete suggestions sorted by term frequency.
     */
    public List<String> getSuggestions(String prefix, int limit) {
        if (prefix == null || prefix.trim().isEmpty() || limit <= 0) {
            return Collections.emptyList();
        }

        String normalized = prefix.toLowerCase().trim();
        TrieNode current = root;
        for (char ch : normalized.toCharArray()) {
            current = current.children.get(ch);
            if (current == null) {
                return Collections.emptyList(); // Prefix not found
            }
        }

        // Collect all descendant words
        List<TrieNode> wordNodes = new ArrayList<>();
        collectWords(current, wordNodes);

        // Sort by frequency descending
        wordNodes.sort((a, b) -> Integer.compare(b.frequency, a.frequency));

        List<String> suggestions = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, wordNodes.size()); i++) {
            suggestions.add(wordNodes.get(i).word);
        }
        return suggestions;
    }

    private void collectWords(TrieNode node, List<TrieNode> results) {
        if (node == null) return;
        if (node.isWord) {
            results.add(node);
        }
        for (TrieNode child : node.children.values()) {
            collectWords(child, results);
        }
    }
}
