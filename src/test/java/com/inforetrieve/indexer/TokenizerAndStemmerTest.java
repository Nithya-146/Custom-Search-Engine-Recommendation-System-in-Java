package com.inforetrieve.indexer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TokenizerAndStemmerTest {

    private PorterStemmer stemmer;
    private Tokenizer tokenizer;

    @BeforeEach
    void setUp() {
        stemmer = new PorterStemmer();
        tokenizer = new Tokenizer();
    }

    @Test
    @DisplayName("PorterStemmer reduces words to morphological root stems correctly")
    void testPorterStemmer() {
        assertEquals("connect", stemmer.stem("connecting"));
        assertEquals("connect", stemmer.stem("connection"));
        assertEquals("connect", stemmer.stem("connections"));
        assertEquals("retriev", stemmer.stem("retrieval"));
        assertEquals("comput", stemmer.stem("computing"));
    }

    @Test
    @DisplayName("Tokenizer removes punctuation, lowercases, and filters stopwords")
    void testTokenizerStopwordsAndPunctuation() {
        String text = "The quick brown fox jumps OVER the lazy dog!";
        List<String> tokens = tokenizer.tokenize(text);

        assertFalse(tokens.contains("the"));
        assertFalse(tokens.contains("over"));
        assertTrue(tokens.contains("quick"));
        assertTrue(tokens.contains("brown"));
        assertTrue(tokens.contains("fox"));
        assertTrue(tokens.contains("jumps"));
        assertTrue(tokens.contains("lazy"));
        assertTrue(tokens.contains("dog"));
    }

    @Test
    @DisplayName("Tokenizer computes correct word position offsets")
    void testTokenizerPositions() {
        String text = "Search engines compute TF-IDF relevance.";
        List<Tokenizer.TokenPosition> positions = tokenizer.tokenizeWithPositions(text);

        assertFalse(positions.isEmpty());
        assertEquals("search", positions.get(0).getRawToken());
        assertEquals(0, positions.get(0).getPosition());
    }
}
