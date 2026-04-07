package org.example.mini_project;

/**
 * Simple demonstration of wait() and notify() mechanics.
 * Shows the difference between notify() and notifyAll().
 */
public class WaitNotifyDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Demonstrating notify() vs notifyAll() ===\n");

//        demonstrateNotify();
        Thread.sleep(3000);

        System.out.println("\n==================================================\n");

        demonstrateNotifyAll();
    }

    /**
     * Demonstrates notify() - wakes up only ONE waiting thread
     */
    private static void demonstrateNotify() throws InterruptedException {
        System.out.println("--- NOTIFY DEMO (wakes only ONE thread) ---");

        final Object lock = new Object();

        // Create 3 waiting threads
        for (int i = 1; i <= 3; i++) {
            final int threadNum = i;
            new Thread(() -> {
                synchronized (lock) {
                    try {
                        System.out.println("Thread-" + threadNum + " is waiting...");
                        lock.wait();
                        System.out.println("Thread-" + threadNum + " woke up!");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, "Thread-" + i).start();
        }

        Thread.sleep(500); // Let threads start waiting

        // Use notify() - only wakes ONE thread
        synchronized (lock) {
            System.out.println("\nCalling notify()...");
            lock.notify(); // Only ONE thread will wake up
        }

        Thread.sleep(1000);
        System.out.println("\nNotice: Only ONE thread woke up, others still waiting!");
    }

    /**
     * Demonstrates notifyAll() - wakes up ALL waiting threads
     */
    private static void demonstrateNotifyAll() throws InterruptedException {
        System.out.println("--- NOTIFY ALL DEMO (wakes ALL threads) ---");

        final Object lock = new Object();

        // Create 3 waiting threads
        for (int i = 1; i <= 3; i++) {
            final int threadNum = i;
            new Thread(() -> {
                synchronized (lock) {
                    try {
                        System.out.println("Thread-" + threadNum + " is waiting...");
                        lock.wait();
                        System.out.println("Thread-" + threadNum + " woke up!");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, "Thread-" + i).start();
        }

        Thread.sleep(500); // Let threads start waiting

        // Use notifyAll() - wakes ALL threads
        synchronized (lock) {
            System.out.println("\nCalling notifyAll()...");
            lock.notifyAll(); // ALL threads will wake up
        }

        Thread.sleep(1000);
        System.out.println("\nNotice: ALL threads woke up!");
    }
}
