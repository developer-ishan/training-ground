package projects.vim_cli_clone_basic.editor;

import java.util.ArrayList;
import java.util.List;

public class Buffer {
    private final List<StringBuilder> lines;

    public Buffer() {
        lines = new ArrayList<>();
        lines.add(new StringBuilder());
    }

    public StringBuilder getLine(int index) {
        while (index >= lines.size()) lines.add(new StringBuilder());
        return lines.get(index);
    }

    public List<StringBuilder> getLines() {
        return lines;
    }

    public void deleteChar(int line, int column) {
        if (line < lines.size() && column < lines.get(line).length()) {
            lines.get(line).deleteCharAt(column);
        }
    }

    public void insertChar(int line, int column, char ch) {
        getLine(line).insert(column, ch);
    }

    public void loadFromLines(List<String> fileLines) {
        lines.clear();
        for (String line : fileLines) {
            lines.add(new StringBuilder(line));
        }
    }

    public List<String> toStringList() {
        List<String> result = new ArrayList<>();
        for (StringBuilder sb : lines) {
            result.add(sb.toString());
        }
        return result;
    }
}
