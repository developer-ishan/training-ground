package org.example.mini_project;

/**
 * Demonstrates synchronized methods with a realistic Bank Account scenario.
 * Shows how synchronized prevents race conditions and maintains data consistency.
 */
public class BankAccountDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Bank Account Synchronization Demo ===\n");

        BankAccount account = new BankAccount(1000);

        // Create deposit threads
        Thread depositor1 = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                account.deposit(100);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "Depositor-1");

        Thread depositor2 = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                account.deposit(150);
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "Depositor-2");

        // Create withdrawal threads
        Thread withdrawer1 = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                try {
                    account.withdraw(200);
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "Withdrawer-1");

        Thread withdrawer2 = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                try {
                    account.withdraw(250);
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "Withdrawer-2");

        // Start all threads
        depositor1.start();
        depositor2.start();
        withdrawer1.start();
        withdrawer2.start();

        // Wait for all threads to complete
        depositor1.join();
        depositor2.join();
        withdrawer1.join();
        withdrawer2.join();

        System.out.println("\n=== Final Balance: $" + account.getBalance() + " ===");
    }
}

class BankAccount {
    private double balance;

    public BankAccount(double initialBalance) {
        this.balance = initialBalance;
        System.out.println("Account created with balance: $" + balance + "\n");
    }

    /**
     * Synchronized method to deposit money.
     * Only one thread can execute this at a time.
     */
    public synchronized void deposit(double amount) {
        System.out.println(Thread.currentThread().getName() + " depositing $" + amount);
        balance += amount;
        System.out.println(Thread.currentThread().getName() + " completed. New balance: $" + balance);

        // Notify waiting threads that balance has changed
        notifyAll();
    }

    /**
     * Synchronized method to withdraw money.
     * Waits if insufficient balance.
     */
    public synchronized void withdraw(double amount) throws InterruptedException {
        System.out.println(Thread.currentThread().getName() + " attempting to withdraw $" + amount);

        // Wait while insufficient balance
        while (balance < amount) {
            System.out.println(Thread.currentThread().getName() +
                             " waiting - Insufficient balance (current: $" + balance + ")");
            wait();
        }

        balance -= amount;
        System.out.println(Thread.currentThread().getName() +
                         " withdrew $" + amount + ". New balance: $" + balance);
    }

    /**
     * Synchronized method to get balance.
     */
    public synchronized double getBalance() {
        return balance;
    }
}
