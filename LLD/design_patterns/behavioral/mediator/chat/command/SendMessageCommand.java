package behavioral.mediator.chat.command;

import behavioral.mediator.chat.observer.User;

public class SendMessageCommand implements Command {
    private final User sender;
    private final String message;
    private final StringBuilder history;

    public SendMessageCommand(User sender, String message, StringBuilder history) {
        this.sender = sender;
        this.message = message;
        this.history = history;
    }

    @Override
    public void execute() {
        sender.send(message);
        history.append(message).append("\n");
    }

    @Override
    public void undo() {
        // Simulate undo by removing the last message from history
        int lastIndex = history.lastIndexOf(message);
        if (lastIndex != -1) {
            history.delete(lastIndex, lastIndex + message.length() + 1);
            System.out.printf("[UNDO] Removed last message from [%s]: \"%s\"%n", sender.getName(), message);
        }
    }
}
