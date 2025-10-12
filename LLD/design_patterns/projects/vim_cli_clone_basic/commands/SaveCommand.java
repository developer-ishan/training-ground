package projects.vim_cli_clone_basic.commands;

import projects.vim_cli_clone_basic.editor.Editor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SaveCommand implements Command {
    private final String filePath;

    public SaveCommand(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void execute(Editor editor) {
        try {
            Files.write(Paths.get(filePath), editor.getBuffer().toStringList());
            System.out.println("File saved.");
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
        }
    }
}
