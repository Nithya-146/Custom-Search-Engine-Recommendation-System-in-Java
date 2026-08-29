package com.inforetrieve.indexer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a document in the search engine repository.
 */
public class Document implements Serializable {
    private static final long serialVersionUID = 1L;

    private String docId;
    private String title;
    private String content;
    private String category;
    private String author;
    private int tokenCount;
    private double vectorMagnitude; // L2 norm of document's TF-IDF vector
    private Map<String, String> metadata;

    public Document() {
        this.metadata = new HashMap<>();
    }

    public Document(String docId, String title, String content, String category, String author) {
        this.docId = docId;
        this.title = title;
        this.content = content;
        this.category = category;
        this.author = author;
        this.tokenCount = 0;
        this.vectorMagnitude = 0.0;
        this.metadata = new HashMap<>();
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getTokenCount() {
        return tokenCount;
    }

    public void setTokenCount(int tokenCount) {
        this.tokenCount = tokenCount;
    }

    public double getVectorMagnitude() {
        return vectorMagnitude;
    }

    public void setVectorMagnitude(double vectorMagnitude) {
        this.vectorMagnitude = vectorMagnitude;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "Document{" +
                "docId='" + docId + '\'' +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", tokenCount=" + tokenCount +
                '}';
    }
}
