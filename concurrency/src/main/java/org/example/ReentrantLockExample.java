package org.example;

import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockExample {
    private int count = 0;
    Lock lock = new ReentrantLock();

    public void increment(){
        lock.lock();

        try {
            count++;
        } finally {
            lock.unlock();
        }
    }

    public int getCount(){
        return count;
    }

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        ReentrantLockExample reentrantLockExample = new ReentrantLockExample();

        for(int i=1; i<=10000; i++){
            executorService.execute(() -> reentrantLockExample.increment());
        }

        try {
            executorService.shutdown();
            executorService.awaitTermination(10, TimeUnit.SECONDS); // wait for completion
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println(reentrantLockExample.getCount());
    }
}
