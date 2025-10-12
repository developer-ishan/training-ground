package behavioral.mediator.chat.mediator;

import behavioral.mediator.chat.observer.User;

import java.util.ArrayList;
import java.util.List;

public class ChatMediatorImpl implements ChatMediator {
    private final List<User> users = new ArrayList<>();

    @Override
    public void sendMessage(String message, User sender) {
        for (User user : users) {
            if (!user.equals(sender)) {
                user.receive(message, sender.getName());
            }
        }
    }

    @Override
    public void registerUser(User user) {
        users.add(user);
    }
}

