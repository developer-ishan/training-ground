package behavioral.mediator.chat.observer;

import behavioral.mediator.chat.mediator.ChatMediator;

public class UserImpl implements User {
    private final String name;
    private final ChatMediator mediator;

    public UserImpl(String name, ChatMediator mediator) {
        this.name = name;
        this.mediator = mediator;
        this.mediator.registerUser(this);
    }

    @Override
    public void receive(String message, String fromUser) {
        System.out.printf("[%s] received message from [%s]: %s%n", name, fromUser, message);
    }

    @Override
    public void send(String message) {
        mediator.sendMessage(message, this);
    }

    @Override
    public String getName() {
        return name;
    }
}
