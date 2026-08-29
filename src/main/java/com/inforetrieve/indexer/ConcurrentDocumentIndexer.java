package com.inforetrieve.indexer;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * Multithreaded document indexer using Java ExecutorService for high throughput indexing.
 */
public class ConcurrentDocumentIndexer {

    private final int threadCount;
    private final ExecutorService executorService;

    public ConcurrentDocumentIndexer() {
        this(Runtime.getRuntime().availableProcessors());
    }

    public ConcurrentDocumentIndexer(int threadCount) {
        this.threadCount = threadCount;
        this.executorService = Executors.newFixedThreadPool(threadCount);
    }

    /**
     * Concurrently indexes a collection of documents into the target InvertedIndex.
     * Returns total time taken in milliseconds.
     */
    public long indexDocuments(Collection<Document> documents, InvertedIndex targetIndex) {
        if (documents == null || documents.isEmpty()) {
            return 0L;
        }

        long startTime = System.currentTimeMillis();
        CountDownLatch latch = new CountDownLatch(documents.size());

        for (Document doc : documents) {
            executorService.submit(() -> {
                try {
                    targetIndex.addDocument(doc);
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Indexing interrupted: " + e.getMessage());
        }

        return System.currentTimeMillis() - startTime;
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public int getThreadCount() {
        return threadCount;
    }
}
