package com.inforetrieve.indexer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a posting entry in the inverted index posting list.
 * Stores document ID, term frequency within the document, and token position offsets.
 */
public class Posting implements Serializable {
    private static final long serialVersionUID = 1L;

    private String docId;
    private int termFrequency;
    private List<Integer> positions;

    public Posting() {
        this.positions = new ArrayList<>();
        this.termFrequency = 0;
    }

    public Posting(String docId) {
        this.docId = docId;
        this.termFrequency = 0;
        this.positions = new ArrayList<>();
    }

    public Posting(String docId, int termFrequency, List<Integer> positions) {
        this.docId = docId;
        this.termFrequency = termFrequency;
        this.positions = positions != null ? positions : new ArrayList<>();
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public int getTermFrequency() {
        return termFrequency;
    }

    public void setTermFrequency(int termFrequency) {
        this.termFrequency = termFrequency;
    }

    public List<Integer> getPositions() {
        return positions;
    }

    public void setPositions(List<Integer> positions) {
        this.positions = positions;
    }

    /**
     * Adds a word position offset to this posting and increments term frequency.
     */
    public void addPosition(int position) {
        this.positions.add(position);
        this.termFrequency = this.positions.size();
    }

    @Override
    public String toString() {
        return "Posting{" +
                "docId='" + docId + '\'' +
                ", tf=" + termFrequency +
                ", positions=" + positions +
                '}';
    }
}
