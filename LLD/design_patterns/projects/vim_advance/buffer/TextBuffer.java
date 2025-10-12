package buffer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TextBuffer {
    private final List<StringBuilder> buffer;

    public TextBuffer() {
        buffer = new ArrayList<>();
        buffer.add(new StringBuilder());
    }

    public void loadFromFile(String filename) throws IOException {
        buffer.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                buffer.add(new StringBuilder(line));
            }
        }
        if (buffer.isEmpty()) {
            buffer.add(new StringBuilder());
        }
    }

    public List<StringBuilder> getBuffer() {
        return buffer;
    }

    public int getLineCount() {
        return buffer.size();
    }

    public String getLine(int index) {
        if (index < 0 || index >= buffer.size()) {
            return "";
        }
        return buffer.get(index).toString();
    }
}

