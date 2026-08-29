package com.inforetrieve.indexer;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Normalizes, tokenizes, filters stopwords, and stems input text.
 */
public class Tokenizer {

    private final Set<String> stopwords;
    private final PorterStemmer stemmer;
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-zA-Z0-9]+");

    public static final Set<String> DEFAULT_STOPWORDS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are", "aren't",
            "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by", "can",
            "cannot", "could", "couldn't", "did", "didn't", "do", "does", "doesn't", "doing", "don't", "down",
            "during", "each", "few", "for", "from", "further", "had", "hadn't", "has", "hasn't", "have", "haven't",
            "having", "he", "he'd", "he'll", "he's", "her", "here", "here's", "hers", "herself", "him", "himself",
            "his", "how", "how's", "i", "i'd", "i'll", "i'm", "i've", "if", "in", "into", "is", "isn't", "it",
            "it's", "its", "itself", "let's", "me", "more", "most", "mustn't", "my", "myself", "no", "nor", "not",
            "of", "off", "on", "once", "only", "or", "other", "ought", "our", "ours", "ourselves", "out", "over",
            "own", "same", "shan't", "she", "she'd", "she'll", "she's", "should", "shouldn't", "so", "some", "such",
            "than", "that", "that's", "the", "their", "theirs", "them", "themselves", "then", "there", "there's",
            "these", "they", "they'd", "they'll", "they're", "they've", "this", "those", "through", "to", "too",
            "under", "until", "up", "very", "was", "wasn't", "we", "we'd", "we'll", "we're", "we've", "were",
            "weren't", "what", "what's", "when", "when's", "where", "where's", "which", "while", "who", "who's",
            "whom", "why", "why's", "with", "won't", "would", "wouldn't", "you", "you'd", "you'll", "you're",
            "you've", "your", "yours", "yourself", "yourselves"
    )));

    public Tokenizer() {
        this(DEFAULT_STOPWORDS);
    }

    public Tokenizer(Set<String> customStopwords) {
        this.stopwords = new HashSet<>(customStopwords);
        this.stemmer = new PorterStemmer();
    }

    /**
     * Splits raw text into raw normalized token strings preserving token position index.
     */
    public List<String> tokenize(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String[] rawTokens = NON_ALPHANUMERIC.split(text.toLowerCase());
        List<String> tokens = new ArrayList<>();
        for (String raw : rawTokens) {
            if (!raw.isEmpty() && !stopwords.contains(raw)) {
                tokens.add(raw);
            }
        }
        return tokens;
    }

    /**
     * Returns stemmed token for a single input token.
     */
    public String stemToken(String token) {
        if (token == null || token.isEmpty()) return "";
        return stemmer.stem(token.toLowerCase());
    }

    /**
     * Tokenizes and stems input text.
     */
    public List<String> tokenizeAndStem(String text) {
        List<String> rawTokens = tokenize(text);
        List<String> stemmedTokens = new ArrayList<>(rawTokens.size());
        for (String token : rawTokens) {
            stemmedTokens.add(stemmer.stem(token));
        }
        return stemmedTokens;
    }

    /**
     * Tokenizes text and returns tokens along with their exact word position sequence.
     */
    public List<TokenPosition> tokenizeWithPositions(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String[] rawTokens = NON_ALPHANUMERIC.split(text.toLowerCase());
        List<TokenPosition> result = new ArrayList<>();
        int positionIndex = 0;
        for (String raw : rawTokens) {
            if (!raw.isEmpty()) {
                if (!stopwords.contains(raw)) {
                    String stemmed = stemmer.stem(raw);
                    result.add(new TokenPosition(raw, stemmed, positionIndex));
                }
                positionIndex++;
            }
        }
        return result;
    }

    public static class TokenPosition {
        private final String rawToken;
        private final String stemmedToken;
        private final int position;

        public TokenPosition(String rawToken, String stemmedToken, int position) {
            this.rawToken = rawToken;
            this.stemmedToken = stemmedToken;
            this.position = position;
        }

        public String getRawToken() { return rawToken; }
        public String getStemmedToken() { return stemmedToken; }
        public int getPosition() { return position; }
    }
}
