package org.example;


class SharedResource {
    synchronized void waitExample(){
        try {
            System.out.println(Thread.currentThread().getName() + " is waiting...");
            wait();
            System.out.println("xxxxxxxx");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println(Thread.currentThread().getName() + " resumed after notify.");
    }

    synchronized void notifyExample(){
        System.out.println("Notifying a waiting thread...");
        notify();
    }

}
public class WaitNotifyExample {
    static void main() {
        SharedResource sr = new SharedResource();

        Thread t1 = new Thread(()->sr.waitExample(), "Thread-1");
        Thread t2 = new Thread(()->{
            try {
                Thread.sleep(5000);
                sr.notifyExample();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "Thread-2");

        t1.start();
        t2.start();

    }
}
