package com.inforetrieve.indexer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InvertedIndexTest {

    private InvertedIndex index;

    @BeforeEach
    void setUp() {
        index = new InvertedIndex();
    }

    @Test
    @DisplayName("InvertedIndex correctly indexes document postings and frequencies")
    void testAddDocument() {
        Document doc1 = new Document("DOC-1", "Java Search", "Java search engine inverted index Java", "Tech", "Author1");
        index.addDocument(doc1);

        assertEquals(1, index.getTotalDocuments());
        List<Posting> postings = index.getPostings("java");
        assertNotNull(postings);
        assertFalse(postings.isEmpty());

        Posting javaPosting = postings.get(0);
        assertEquals("DOC-1", javaPosting.getDocId());
        assertEquals(3, javaPosting.getTermFrequency()); // "Java" appears 3 times
    }

    @Test
    @DisplayName("Incremental indexing updates postings without rebuilding entire index")
    void testIncrementalIndexing() {
        Document doc1 = new Document("DOC-1", "First Doc", "information retrieval systems", "Tech", "Author");
        Document doc2 = new Document("DOC-2", "Second Doc", "advanced search systems", "Tech", "Author");

        index.addDocument(doc1);
        assertEquals(1, index.getTotalDocuments());

        index.addDocument(doc2);
        assertEquals(2, index.getTotalDocuments());

        assertEquals(2, index.getDocumentFrequency("system"));
        assertEquals(1, index.getDocumentFrequency("retriev"));
    }

    @Test
    @DisplayName("Document removal cleans up posting lists and document catalog")
    void testRemoveDocument() {
        Document doc = new Document("DOC-99", "Temp Doc", "temporary data content", "Temp", "Author");
        index.addDocument(doc);
        assertTrue(index.getDocumentMap().containsKey("DOC-99"));

        index.removeDocument("DOC-99");
        assertFalse(index.getDocumentMap().containsKey("DOC-99"));
        assertTrue(index.getPostings("temporari").isEmpty());
    }
}
