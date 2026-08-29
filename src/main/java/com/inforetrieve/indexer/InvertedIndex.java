package com.inforetrieve.indexer;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Inverted Index data structure mapping stemmed terms to posting lists.
 * Supports thread-safe incremental indexing, term posting queries, and document management.
 */
public class InvertedIndex implements Serializable {
    private static final long serialVersionUID = 1L;

    // Map: term -> List of Postings (ConcurrentHashMap for thread safety)
    private final Map<String, List<Posting>> indexMap;
    // Map: docId -> Document metadata catalog
    private final Map<String, Document> documentMap;
    private final Tokenizer tokenizer;

    public InvertedIndex() {
        this.indexMap = new ConcurrentHashMap<>();
        this.documentMap = new ConcurrentHashMap<>();
        this.tokenizer = new Tokenizer();
    }

    public InvertedIndex(Tokenizer tokenizer) {
        this.indexMap = new ConcurrentHashMap<>();
        this.documentMap = new ConcurrentHashMap<>();
        this.tokenizer = tokenizer != null ? tokenizer : new Tokenizer();
    }

    /**
     * Incrementally indexes a document. Parses, tokenizes, stems, and updates term postings.
     * Design choice: Uses ConcurrentHashMap + synchronized posting list updates for thread safety.
     */
    public void addDocument(Document doc) {
        if (doc == null || doc.getDocId() == null || doc.getContent() == null) {
            return;
        }

        // Remove existing document version if re-indexed
        if (documentMap.containsKey(doc.getDocId())) {
            removeDocument(doc.getDocId());
        }

        // Tokenize text with word positions
        String fullText = (doc.getTitle() != null ? doc.getTitle() + " " : "") + doc.getContent();
        List<Tokenizer.TokenPosition> tokenPositions = tokenizer.tokenizeWithPositions(fullText);

        doc.setTokenCount(tokenPositions.size());

        // Temporary map for this document: term -> Posting
        Map<String, Posting> docPostings = new HashMap<>();

        for (Tokenizer.TokenPosition tp : tokenPositions) {
            String term = tp.getStemmedToken();
            if (term.isEmpty()) continue;

            docPostings.computeIfAbsent(term, k -> new Posting(doc.getDocId())).addPosition(tp.getPosition());
        }

        // Merge into global indexMap
        for (Map.Entry<String, Posting> entry : docPostings.entrySet()) {
            String term = entry.getKey();
            Posting posting = entry.getValue();

            indexMap.compute(term, (k, list) -> {
                if (list == null) {
                    list = Collections.synchronizedList(new ArrayList<>());
                }
                list.add(posting);
                return list;
            });
        }

        // Store in document catalog
        documentMap.put(doc.getDocId(), doc);
    }

    /**
     * Removes a document from the index.
     */
    public synchronized void removeDocument(String docId) {
        if (!documentMap.containsKey(docId)) return;

        for (List<Posting> postings : indexMap.values()) {
            postings.removeIf(p -> p.getDocId().equals(docId));
        }
        indexMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        documentMap.remove(docId);
    }

    /**
     * Retrieves posting list for a term.
     */
    public List<Posting> getPostings(String term) {
        if (term == null) return Collections.emptyList();
        String stemmed = tokenizer.stemToken(term);
        List<Posting> list = indexMap.get(stemmed);
        return list != null ? new ArrayList<>(list) : Collections.emptyList();
    }

    /**
     * Gets document frequency df(t): number of documents containing term.
     */
    public int getDocumentFrequency(String term) {
        if (term == null) return 0;
        String stemmed = tokenizer.stemToken(term);
        List<Posting> list = indexMap.get(stemmed);
        return list != null ? list.size() : 0;
    }

    public Document getDocument(String docId) {
        return documentMap.get(docId);
    }

    public Collection<Document> getAllDocuments() {
        return documentMap.values();
    }

    public Set<String> getAllTerms() {
        return indexMap.keySet();
    }

    public int getTotalDocuments() {
        return documentMap.size();
    }

    public int getVocabularySize() {
        return indexMap.size();
    }

    public Map<String, List<Posting>> getIndexMap() {
        return indexMap;
    }

    public Map<String, Document> getDocumentMap() {
        return documentMap;
    }

    public Tokenizer getTokenizer() {
        return tokenizer;
    }

    public void clear() {
        indexMap.clear();
        documentMap.clear();
    }
}
