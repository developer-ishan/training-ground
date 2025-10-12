package behavioral.command;

public class WriteCommand implements Command {
    private final TextEditor editor;
    private final String text;

    public WriteCommand(TextEditor editor, String text) {
        this.editor = editor;
        this.text = text;
    }

    @Override
    public void execute() {
        editor.append(text);
    }

    @Override
    public void undo() {
        editor.removeLast(text.length());
    }
}
