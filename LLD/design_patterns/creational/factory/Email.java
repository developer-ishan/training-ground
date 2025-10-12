package creational.factory;

public class Email implements Notification {

    @Override
    public boolean send(String message) {
        System.out.println("Sent: " + message + " via email");
        return true;
    }
}
