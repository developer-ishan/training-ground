package creational.factory;

public class SMS implements Notification {

    @Override
    public boolean send(String message) {
        System.out.println("Sent: " + message + " via sms");
        return true;
    }
}
