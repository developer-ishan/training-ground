package projects.vim_cli_clone_basic.commands;

import projects.vim_cli_clone_basic.editor.Editor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class OpenCommand implements Command {
    private final String filePath;

    public OpenCommand(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void execute(Editor editor) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            editor.getBuffer().loadFromLines(lines);
            System.out.println("File loaded.");
        } catch (IOException e) {
            System.out.println("Error loading file: " + e.getMessage());
        }
    }
}
