package projects.vim_cli_clone_basic.commands;

import projects.vim_cli_clone_basic.editor.Editor;

public interface Command {
    void execute(Editor editor);
}

