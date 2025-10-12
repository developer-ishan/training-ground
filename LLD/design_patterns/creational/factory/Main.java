package creational.factory;

public class Main {
    public static void main(String[] args) {
        Notification sms = NotificationFactory.getNotification("sms");
        sms.send("Hello World");

        Notification email = NotificationFactory.getNotification("email");
        email.send("Hello World");

        Notification whatsapp = NotificationFactory.getNotification("whatsapp");
        whatsapp.send("Hello World");
    }
}
