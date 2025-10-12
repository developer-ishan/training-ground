package behavioral.chain_of_responsibility;

public class InfoLogger implements Logger {
    public Logger other = new DebugLogger();

    @Override
    public void log(String message, LogLevel level) {
        if(level == LogLevel.INFO) {
            System.out.println( "info: " + message);
        } else {
            other.log(message, level);
        }

    }
}
