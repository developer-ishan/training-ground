package behavioral.mediator.chat.command;

import java.util.Stack;

public class CommandManager {
    private final Stack<Command> history = new Stack<>();
    private final Stack<Command> redoStack = new Stack<>();

    public void executeCommand(Command command) {
        command.execute();
        history.push(command);
        redoStack.clear(); // once a new command is executed, redo stack is reset
    }

    public void undo() {
        if (!history.isEmpty()) {
            Command last = history.pop();
            last.undo();
            redoStack.push(last);
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            Command command = redoStack.pop();
            command.execute();
            history.push(command);
        }
    }
}
