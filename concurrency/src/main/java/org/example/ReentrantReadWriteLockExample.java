package org.example;

import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class ReentrantReadWriteLockExample {

    private int count = 0;
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    public void increment() {
        rwLock.writeLock().lock();
        try {
            System.out.println(Thread.currentThread().getName() + " acquired WRITE lock");
            count++;
            Thread.sleep(10000); // simulate heavy write
            System.out.println(Thread.currentThread().getName() + " updated count to " + count);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            System.out.println(Thread.currentThread().getName() + " releasing WRITE lock");
            rwLock.writeLock().unlock();
        }
    }

    public void read() {
        rwLock.readLock().lock();
        try {
            System.out.println(Thread.currentThread().getName() + " acquired READ lock");
            Thread.sleep(1000); // simulate read
            System.out.println(Thread.currentThread().getName() + " read count: " + count);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            System.out.println(Thread.currentThread().getName() + " releasing READ lock");
            rwLock.readLock().unlock();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        ReentrantReadWriteLockExample obj = new ReentrantReadWriteLockExample();

        // Step 1: Start multiple readers
        for (int i = 0; i < 3; i++) {
            executor.submit(obj::read);
        }

        // Small delay so readers start first
        Thread.sleep(500);

        // Step 2: Start a writer
        executor.submit(obj::increment);

        // Step 3: More readers after writer
        for (int i = 0; i < 3; i++) {
            executor.submit(obj::read);
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }
}