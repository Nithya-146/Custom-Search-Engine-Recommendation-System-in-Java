package com.inforetrieve.query;

import com.inforetrieve.indexer.Document;
import com.inforetrieve.indexer.InvertedIndex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QueryProcessorTest {

    private InvertedIndex index;
    private QueryProcessor queryProcessor;

    @BeforeEach
    void setUp() {
        index = new InvertedIndex();
        Document d1 = new Document("DOC-1", "Inception Movie", "A thief steals secrets using dream technology and artificial intelligence manipulation.", "Sci-Fi", "Nolan");
        Document d2 = new Document("DOC-2", "Matrix Movie", "Virtual reality hacker Neo fights machines and computer algorithms.", "Sci-Fi", "Wachowski");
        Document d3 = new Document("DOC-3", "Godfather Movie", "Mafia crime family patriarch transfers control in New York City.", "Crime", "Coppola");

        index.addDocument(d1);
        index.addDocument(d2);
        index.addDocument(d3);

        queryProcessor = new QueryProcessor(index);
    }

    @Test
    @DisplayName("Free text search returns relevance ranked hits")
    void testFreeTextSearch() {
        SearchResult result = queryProcessor.processQuery("dream technology", 1, 5);
        assertNotNull(result);
        assertTrue(result.getTotalHits() > 0);
        assertEquals("DOC-1", result.getHits().get(0).getDocument().getDocId());
    }

    @Test
    @DisplayName("Boolean query AND/OR/NOT evaluates doc intersections and exclusions")
    void testBooleanQuery() {
        SearchResult result = queryProcessor.processQuery("movie AND crime NOT dream", 1, 5);
        assertNotNull(result);
        assertEquals(1, result.getTotalHits());
        assertEquals("DOC-3", result.getHits().get(0).getDocument().getDocId());
    }

    @Test
    @DisplayName("Exact phrase search matches contiguous positional offsets")
    void testPhraseSearch() {
        SearchResult result = queryProcessor.processQuery("\"virtual reality\"", 1, 5);
        assertNotNull(result);
        assertEquals(1, result.getTotalHits());
        assertEquals("DOC-2", result.getHits().get(0).getDocument().getDocId());
    }

    @Test
    @DisplayName("Fuzzy search corrects typos via Levenshtein distance fallback")
    void testFuzzySearchFallback() {
        // "inceptin" has edit distance 1 from "inception"
        SearchResult result = queryProcessor.processQuery("inceptin", 1, 5);
        assertNotNull(result);
        assertTrue(result.isFuzzyApplied() || result.getTotalHits() > 0);
    }
}
