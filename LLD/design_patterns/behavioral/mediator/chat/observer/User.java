package behavioral.mediator.chat.observer;

public interface User {
    void receive(String message, String fromUser);
    void send(String message);
    String getName();
}
