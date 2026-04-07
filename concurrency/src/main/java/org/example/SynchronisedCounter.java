package org.example;

import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SynchronisedCounter {
    private int counter = 0;
    public int increment(){
        synchronized (this){
            return ++counter;
        }
    }

    public int getCounter() {
        return counter;
    }

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        SynchronisedCounter counter = new SynchronisedCounter();
        executor.invokeAll(Collections.nCopies(10000, ()->{
            return counter.increment();
        }));

        System.out.println(counter.getCounter());
        executor.shutdown();
    }
}
