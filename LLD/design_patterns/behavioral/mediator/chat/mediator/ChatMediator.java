package behavioral.mediator.chat.mediator;

import behavioral.mediator.chat.observer.User;

public interface ChatMediator {
    void sendMessage(String message, User sender);
    void registerUser(User user);
}
