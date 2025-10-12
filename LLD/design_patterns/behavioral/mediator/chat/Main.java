package behavioral.mediator.chat;

import behavioral.mediator.chat.mediator.ChatMediator;
import behavioral.mediator.chat.mediator.ChatMediatorImpl;
import behavioral.mediator.chat.observer.User;
import behavioral.mediator.chat.observer.UserImpl;

public class Main {
    public static void main(String[] args) {
        ChatMediator mediator = new ChatMediatorImpl();

        User user1 = new UserImpl("user1", mediator);
        User user2 = new UserImpl("user2", mediator);
        User user3 = new UserImpl("user3", mediator);
        User user4 = new UserImpl("user4", mediator);
        User user5 = new UserImpl("user5", mediator);

        user1.send("Hello World!");
    }
}
