package behavioral.mediator.chat.command;

public interface Command {
    void execute();
    void undo();
}
