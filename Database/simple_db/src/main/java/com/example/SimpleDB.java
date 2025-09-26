package com.example;

import java.io.*;

public class SimpleDB {
    private final File dbFile;

    public SimpleDB(String filename) {
        this.dbFile = new File(filename);
    }

    // Set a key-value pair (append to file)
    public void set(String key, String value) throws IOException {
        try (FileWriter fw = new FileWriter(dbFile, true)) {
            fw.write(key + "," + value + "\n");
        }
    }

    // Get the latest value for a key
    public String get(String key) throws IOException {
        String result = null;
        try (BufferedReader br = new BufferedReader(new FileReader(dbFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(key + ",")) {
                    result = line.substring((key + ",").length());
                }
            }
        }
        return result;
    }
}
