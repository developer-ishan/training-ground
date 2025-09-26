package com.example;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class IndexedSimpleDB {
    private final File dbFile;
    private final File indexFile;
    private final Map<String, Long> indexMap = new HashMap<>();

    public IndexedSimpleDB(String dbFilename, String indexFilename) throws IOException {
        this.dbFile = new File(dbFilename);
        this.indexFile = new File(indexFilename);
        loadIndex();
    }

    // Load index file into memory
    private void loadIndex() throws IOException {
        if (!indexFile.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(indexFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    indexMap.put(parts[0], Long.parseLong(parts[1]));
                }
            }
        }
    }

    // Set a key-value pair and update index
    public void set(String key, String value) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(dbFile, "rw")) {
            raf.seek(dbFile.length());
            long offset = raf.getFilePointer();
            raf.writeBytes(key + "," + value + "\n");
            indexMap.put(key, offset);
            saveIndex();
        }
    }

    // Save index map to index file
    private void saveIndex() throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(indexFile))) {
            for (Map.Entry<String, Long> entry : indexMap.entrySet()) {
                bw.write(entry.getKey() + "," + entry.getValue());
                bw.newLine();
            }
        }
    }

    // Get value for a key using index
    public String get(String key) throws IOException {
        Long offset = indexMap.get(key);
        if (offset == null) return null;
        try (RandomAccessFile raf = new RandomAccessFile(dbFile, "r")) {
            raf.seek(offset);
            String line = raf.readLine();
            if (line != null && line.startsWith(key + ",")) {
                return line.substring((key + ",").length());
            }
        }
        return null;
    }
}
