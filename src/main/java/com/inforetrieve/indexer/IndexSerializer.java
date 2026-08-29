package com.inforetrieve.indexer;

import java.io.*;

/**
 * Handles disk persistence for the InvertedIndex and document catalog.
 * Supports saving and restoring the index state without rebuilds.
 */
public class IndexSerializer {

    /**
     * Serializes an InvertedIndex instance to disk.
     */
    public static void saveIndex(InvertedIndex index, String filePath) throws IOException {
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(index.getIndexMap());
            oos.writeObject(index.getDocumentMap());
        }
    }

    /**
     * Deserializes an InvertedIndex instance from disk.
     */
    @SuppressWarnings("unchecked")
    public static InvertedIndex loadIndex(String filePath) throws IOException, ClassNotFoundException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("Index file not found at " + filePath);
        }

        InvertedIndex index = new InvertedIndex();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object indexObj = ois.readObject();
            Object docObj = ois.readObject();

            if (indexObj instanceof java.util.Map && docObj instanceof java.util.Map) {
                index.getIndexMap().putAll((java.util.Map<String, java.util.List<Posting>>) indexObj);
                index.getDocumentMap().putAll((java.util.Map<String, Document>) docObj);
            }
        }
        return index;
    }
}
