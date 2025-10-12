package behavioral.chain_of_responsibility;

public class DebugLogger implements Logger {
    public Logger other = new ErrorLogger();

    @Override
    public void log(String message, LogLevel level) {
        if(level == LogLevel.DEBUG || level == LogLevel.INFO) {
            System.out.println("debug: " + message);
        } else {
            other.log(message, level);
        }
    }
}
