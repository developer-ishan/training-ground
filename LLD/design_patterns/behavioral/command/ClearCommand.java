package behavioral.command;

public class ClearCommand implements Command {
    private final TextEditor editor;
    private String backup;

    public ClearCommand(TextEditor editor) {
        this.editor = editor;
    }

    @Override
    public void execute() {
        backup = editor.getContent();
        editor.clear();
    }

    @Override
    public void undo() {
        editor.append(backup);
    }
}
