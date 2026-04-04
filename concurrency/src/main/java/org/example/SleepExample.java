package org.example;

public class SleepExample {
    public static void main(String[] args) {
        System.out.println("Thread is going to sleep");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Thread woke up after sleeping");
    }
}
