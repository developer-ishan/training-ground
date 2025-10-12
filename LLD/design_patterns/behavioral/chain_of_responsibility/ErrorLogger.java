package behavioral.chain_of_responsibility;

public class ErrorLogger implements Logger {
    public Logger other;

    @Override
    public void log(String message, LogLevel level) {
        System.out.println("error: " + message);
    }
}
