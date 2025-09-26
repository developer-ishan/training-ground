package com.example;

import java.io.*;
import java.util.Random;

public class DBLoadTest {
    public static void main(String[] args) throws Exception {
        int numRecords = 10000; // Adjust for ~1MB file size
        String dbFile = "database.txt";
        String indexFile = "index.txt";

        // Clean up old files
        new File(dbFile).delete();
        new File(indexFile).delete();

        // Generate random key-value pairs
        Random rand = new Random(42);
        String[] keys = new String[numRecords];
        String[] values = new String[numRecords];
        for (int i = 0; i < numRecords; i++) {
            keys[i] = "key" + i;
            values[i] = "val" + rand.nextInt(1000000);
        }

        // Write to SimpleDB
        SimpleDB simpleDB = new SimpleDB(dbFile);
        long startSimple = System.currentTimeMillis();
        for (int i = 0; i < numRecords; i++) {
            simpleDB.set(keys[i], values[i]);
        }
        long endSimple = System.currentTimeMillis();
        System.out.println("SimpleDB write time: " + (endSimple - startSimple) + " ms");

        // Read from SimpleDB
        startSimple = System.currentTimeMillis();
        for (int i = 0; i < numRecords; i++) {
            simpleDB.get(keys[i]);
        }
        endSimple = System.currentTimeMillis();
        System.out.println("SimpleDB read time: " + (endSimple - startSimple) + " ms");

        // Clean up files for IndexedSimpleDB
        new File(dbFile).delete();
        new File(indexFile).delete();

        // Write to IndexedSimpleDB
        IndexedSimpleDB indexedDB = new IndexedSimpleDB(dbFile, indexFile);
        long startIndexed = System.currentTimeMillis();
        for (int i = 0; i < numRecords; i++) {
            indexedDB.set(keys[i], values[i]);
        }
        long endIndexed = System.currentTimeMillis();
        System.out.println("IndexedSimpleDB write time: " + (endIndexed - startIndexed) + " ms");

        // Read from IndexedSimpleDB
        startIndexed = System.currentTimeMillis();
        for (int i = 0; i < numRecords; i++) {
            indexedDB.get(keys[i]);
        }
        endIndexed = System.currentTimeMillis();
        System.out.println("IndexedSimpleDB read time: " + (endIndexed - startIndexed) + " ms");

        //clean up
        new File(dbFile).delete();
        new File(indexFile).delete();
    }
}
