package creational.factory;

public class NotificationFactory {
    public static Notification getNotification(String type) {
        if(type == null || type.isEmpty()) {
            throw new IllegalArgumentException("Notification type must not be null");
        }
        if (type.equalsIgnoreCase("email")) {
            return new Email();
        }
        if (type.equalsIgnoreCase("push")) {
            return new Push();
        }
        if (type.equalsIgnoreCase("sms")) {
            return new SMS();
        }
        throw new IllegalArgumentException("Invalid notification type: " + type);
    }
}
