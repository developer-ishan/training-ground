package org.example.mini_project;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Demonstrates synchronized, wait(), notify(), and notifyAll()
 * using a Producer-Consumer pattern with a shared buffer.
 */
public class ProducerConsumerDemo {

    public static void main(String[] args) {
        SharedBuffer buffer = new SharedBuffer(5);

        // Create multiple producers
        Thread producer1 = new Thread(new Producer(buffer, "Producer-1"), "Producer-1");
        Thread producer2 = new Thread(new Producer(buffer, "Producer-2"), "Producer-2");

        // Create multiple consumers
        Thread consumer1 = new Thread(new Consumer(buffer, "Consumer-1"), "Consumer-1");
        Thread consumer2 = new Thread(new Consumer(buffer, "Consumer-2"), "Consumer-2");
        Thread consumer3 = new Thread(new Consumer(buffer, "Consumer-3"), "Consumer-3");

        // Start all threads
        producer1.start();
        producer2.start();
        consumer1.start();
//        consumer2.start();
//        consumer3.start();

        // Let them run for a while
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        // Interrupt all threads to stop them
//        producer1.interrupt();
//        producer2.interrupt();
//        consumer1.interrupt();
//        consumer2.interrupt();
//        consumer3.interrupt();

    }
}

/**
 * Shared buffer with bounded capacity.
 * Demonstrates synchronized methods and wait/notify mechanisms.
 */
class SharedBuffer {
    private final Queue<Integer> queue = new LinkedList<>();
    private final int capacity;
    private int itemCount = 0;

    public SharedBuffer(int capacity) {
        this.capacity = capacity;
    }

    /**
     * Producer calls this to add items.
     * Uses synchronized, wait(), and notifyAll().
     */
    public synchronized void produce(int item) throws InterruptedException {
        // Wait while buffer is full
        while (queue.size() == capacity) {
            System.out.println(Thread.currentThread().getName() + " waiting - buffer FULL");
            wait(); // Release lock and wait for notification
        }

        // Add item to buffer
        queue.add(item);
        System.out.println(Thread.currentThread().getName() + " produced: " + item +
                         " | Buffer size: " + queue.size());

        // Notify all waiting consumers that an item is available
        notifyAll();
    }

    /**
     * Consumer calls this to remove items.
     * Uses synchronized, wait(), and notifyAll().
     */
    public synchronized int consume() throws InterruptedException {
        // Wait while buffer is empty
        while (queue.isEmpty()) {
            System.out.println(Thread.currentThread().getName() + " waiting - buffer EMPTY");
            wait(); // Release lock and wait for notification
        }

        // Remove item from buffer
        int item = queue.poll();
        System.out.println(Thread.currentThread().getName() + " consumed: " + item +
                         " | Buffer size: " + queue.size());

        // Notify all waiting producers that space is available
//        notifyAll();

        return item;
    }

    public synchronized int size() {
        return queue.size();
    }
}

class Producer implements Runnable {
    private final SharedBuffer buffer;
    private final String name;

    public Producer(SharedBuffer buffer, String name) {
        this.buffer = buffer;
        this.name = name;
    }

    @Override
    public void run() {
        int item = 0;
        try {
            while (!Thread.currentThread().isInterrupted()) {
                buffer.produce(++item);
                Thread.sleep((int) (Math.random() * 1000)); // Random delay
            }
        } catch (InterruptedException e) {
            System.out.println(name + " stopped");
        }
    }
}

class Consumer implements Runnable {
    private final SharedBuffer buffer;
    private final String name;

    public Consumer(SharedBuffer buffer, String name) {
        this.buffer = buffer;
        this.name = name;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                buffer.consume();
                Thread.sleep((int) (Math.random() * 1500)); // Random delay
            }
        } catch (InterruptedException e) {
            System.out.println(name + " stopped");
        }
    }
}
