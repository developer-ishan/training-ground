package org.example;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

class Task implements Callable<String> {

    int taskID;
    public Task(int taskID){
        this.taskID = taskID;
    }

    @Override
    public String call() throws Exception {
        System.out.println("Thread: " + Thread.currentThread().getName() + " is working on task: " + taskID);
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            System.out.println("Thread: " + Thread.currentThread().getName() + " interrupted.");
        }
        System.out.println("Thread: " + Thread.currentThread().getName() + " completed task: " + taskID);

        return null;
    }
}
public class ExecutorServiceDemo {
    public static void main(String[] args) {
        int NUM_CORES = Runtime.getRuntime().availableProcessors();
        System.out.println("Number of cores: " + NUM_CORES);
        ExecutorService executorService = Executors.newCachedThreadPool();

        for(int i=1; i<=500; i++){
            executorService.submit(new Task(i));
        }
        executorService.shutdown();
        System.out.println("Shutdown initiated.");

        try {
            if(!executorService.awaitTermination(10, TimeUnit.SECONDS)){
                executorService.shutdownNow();
                System.out.println("Executor service force terminated.");
            }
        } catch (InterruptedException e) {
            System.out.println("Interrupted while waiting for executor service termination.");
            executorService.shutdownNow();
        }
    }
}
