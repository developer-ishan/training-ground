package creational.factory;

public class Push implements Notification {

    @Override
    public boolean send(String message) {
        System.out.println("Sent: " + message + " via push");
        return true;
    }
}
