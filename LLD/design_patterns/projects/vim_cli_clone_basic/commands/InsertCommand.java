package projects.vim_cli_clone_basic.commands;

import projects.vim_cli_clone_basic.editor.Editor;

public class InsertCommand implements Command {
    @Override
    public void execute(Editor editor) {
        editor.toggleMode();
        System.out.println("-- INSERT MODE -- (type '::esc' to exit insert mode)");
    }
}
