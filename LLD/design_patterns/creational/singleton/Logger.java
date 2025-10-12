package creational.singleton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Logger {
    private static volatile Logger instance;
    private final List<String> logs;

    private Logger() {
        logs = Collections.synchronizedList(new ArrayList<>());
    }

    public static Logger getInstance() {
        if (instance == null) {
            synchronized (Logger.class) {
                if (instance == null) {
                    instance = new Logger();
                }
            }
        }
        return instance;
    }

    public void log(String msg) {
        if (msg == null) {
            throw new IllegalArgumentException("Log message cannot be null");
        }
        String entry = String.format("[%s] %s", Thread.currentThread().getName(), msg);
        logs.add(entry);
    }

    public List<String> getLogHistory() {
        synchronized (logs) {
            return new ArrayList<>(logs);
        }
    }

    public void clearLogs() {
        synchronized (logs) {
            logs.clear();
        }
    }
}
