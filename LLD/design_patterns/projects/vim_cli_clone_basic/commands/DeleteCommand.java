package projects.vim_cli_clone_basic.commands;

import projects.vim_cli_clone_basic.editor.Editor;

public class DeleteCommand implements Command {
    @Override
    public void execute(Editor editor) {
        editor.getBuffer().deleteChar(editor.getCursor().getLine(), editor.getCursor().getColumn());
        System.out.println("Deleted character.");
    }
}
