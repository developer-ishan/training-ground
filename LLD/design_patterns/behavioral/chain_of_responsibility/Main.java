package behavioral.chain_of_responsibility;

public class Main {
    public static void main(String[] args) {
        InfoLogger infoLogger = new InfoLogger();
        infoLogger.log("error message", LogLevel.ERROR);
    }
}
