package creational.singleton;

public class Main {
    public static void main(String[] args) {
        Logger logger = Logger.getInstance();

        // Clear any existing logs (just in case)
        logger.clearLogs();

        // Create multiple threads to log concurrently
        Runnable loggingTask = () -> {
            Logger log = Logger.getInstance();
            for (int i = 0; i < 5; i++) {
                log.log("Message " + i + " from thread " + Thread.currentThread().getName());
            }
        };

        Thread t1 = new Thread(loggingTask, "Worker-1");
        Thread t2 = new Thread(loggingTask, "Worker-2");
        Thread t3 = new Thread(loggingTask, "Worker-3");

        t1.start();
        t2.start();
        t3.start();

        // Wait for all threads to finish
        try {
            t1.join();
            t2.join();
            t3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Display all logs
        System.out.println("\n--- Log History ---");
        for (String log : logger.getLogHistory()) {
            System.out.println(log);
        }
    }
}
