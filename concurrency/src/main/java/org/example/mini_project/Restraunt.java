package org.example.mini_project;


import java.util.Scanner;

class Waiter implements Runnable{

    private final Object lock;
    int id;

    public Waiter(Object lock, int id){
        this.lock = lock;
        this.id = id;
    }

    @Override
    public void run() {
        synchronized (lock){
            System.out.printf("Waiter %d is waiting to pick up the order.\n", id);
            try {
                lock.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.printf("Waiter %d picked up the order.\n", id);
        }
    }
}

public class Restraunt {
    public static void main(String[] args) {
        Object lock = new Object();
        Scanner scanner = new Scanner(System.in);

        Waiter waiter_1 = new Waiter(lock, 1);
        new Thread(waiter_1).start();

        Waiter waiter_2 = new Waiter(lock, 2);
        new Thread(waiter_2).start();

        while (true){
            System.out.println("Ring Bell when order is ready");
            String cmd = scanner.nextLine();
            if("ready".equalsIgnoreCase(cmd)){
                synchronized (lock) {
                    lock.notify();
                }
            }
        }
    }
}
