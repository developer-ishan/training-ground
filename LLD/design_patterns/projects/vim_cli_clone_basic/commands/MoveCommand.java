package projects.vim_cli_clone_basic.commands;

import projects.vim_cli_clone_basic.editor.Editor;

public class MoveCommand implements Command {
    private final int line;
    private final int column;

    public MoveCommand(String args) {
        String[] parts = args.split("\\s+");
        this.line = Integer.parseInt(parts[0]);
        this.column = Integer.parseInt(parts[1]);
    }

    @Override
    public void execute(Editor editor) {
        editor.getCursor().moveTo(line, column);
        System.out.println("Moved cursor to " + line + ", " + column);
    }
}

