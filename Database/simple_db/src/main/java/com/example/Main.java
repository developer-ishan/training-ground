package com.example;


public class Main {

    public static void main(String[] args) {
        try {
            // Demo for IndexedSimpleDB
            IndexedSimpleDB indexedDb = new IndexedSimpleDB("database.txt", "index.txt");
            indexedDb.set("123456", "hello");
            indexedDb.set("7890", "world");

            String value1 = indexedDb.get("123456");
            String value2 = indexedDb.get("7890");

            System.out.println("Indexed Value for 123456: " + value1);
            System.out.println("Indexed Value for 7890: " + value2);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}