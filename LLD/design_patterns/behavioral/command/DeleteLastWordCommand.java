package behavioral.command;

public class DeleteLastWordCommand implements Command {
    private final TextEditor editor;
    private String deletedWord = "";

    public DeleteLastWordCommand(TextEditor editor) {
        this.editor = editor;
    }

    @Override
    public void execute() {
        String content = editor.getContent().trim();
        if (content.isEmpty()) return;

        int lastSpaceIndex = content.lastIndexOf(' ');
        deletedWord = lastSpaceIndex == -1 ? content : content.substring(lastSpaceIndex + 1);

        int charsToDelete = deletedWord.length();
        // Also remove the space before it, if not the only word
        if (lastSpaceIndex != -1) charsToDelete += 1;

        editor.removeLast(charsToDelete);
    }

    @Override
    public void undo() {
        if (!deletedWord.isEmpty()) {
            if (!editor.getContent().isEmpty()) {
                editor.append(" ");
            }
            editor.append(deletedWord);
        }
    }
}
