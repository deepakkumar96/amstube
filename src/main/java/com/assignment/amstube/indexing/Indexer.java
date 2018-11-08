package com.assignment.amstube.indexing;

public interface Indexer {
    public IndexingResult index(String filePath, String service, String persistPath);
}
