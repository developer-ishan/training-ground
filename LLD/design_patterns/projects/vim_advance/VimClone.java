import java.io.*;
import java.util.*;

public class VimClone {
    private static final String CLEAR_SCREEN = "\033[2J";
    private static final String CURSOR_HOME = "\033[H";
    private static final String HIDE_CURSOR = "\033[?25l";
    private static final String SHOW_CURSOR = "\033[?25h";

    enum Mode { COMMAND, INSERT, VIEW }

    private List<StringBuilder> buffer = new ArrayList<>();
    private int cursorRow = 0;
    private int cursorCol = 0;
    private Mode mode = Mode.COMMAND;
    private String filename = null;

    public VimClone(String filename) throws IOException {
        this.filename = filename;
        loadFile(filename);
    }

    private void loadFile(String filename) throws IOException {
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

    private void saveFile() throws IOException {
        if (filename == null) return;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            for (StringBuilder line : buffer) {
                bw.write(line.toString());
                bw.newLine();
            }
        }
    }

    private void render() {
        System.out.print(HIDE_CURSOR);
        System.out.print(CLEAR_SCREEN);
        System.out.print(CURSOR_HOME);

        // Render buffer
        for (int i = 0; i < buffer.size(); i++) {
            StringBuilder line = buffer.get(i);
            if (i == cursorRow) {
                // Show cursor as a block (swap char with underscore)
                String l = line.toString();
                if (cursorCol < l.length()) {
                    System.out.print(l.substring(0, cursorCol));
                    System.out.print("\033[7m" + l.charAt(cursorCol) + "\033[0m"); // inverted colors
                    System.out.println(l.substring(cursorCol + 1));
                } else {
                    // Cursor at end of line: show underscore
                    System.out.print(l);
                    System.out.print("\033[7m \033[0m");
                    System.out.println();
                }
            } else {
                System.out.println(line);
            }
        }
        System.out.printf("-- %s MODE --  %s  Ln %d, Col %d\n", mode, filename == null ? "[No file]" : filename, cursorRow + 1, cursorCol + 1);
        System.out.print(SHOW_CURSOR);
        System.out.flush();
    }

    // Read keypress, including arrow keys, without blocking for Enter
    private int readKey() throws IOException {
        InputStream in = System.in;
        int c = in.read();
        if (c == 0x1B) { // Escape sequence
            if (in.available() >= 2) {
                int c2 = in.read();
                int c3 = in.read();
                if (c2 == '[') {
                    switch (c3) {
                        case 'A': return 1000; // Up
                        case 'B': return 1001; // Down
                        case 'C': return 1002; // Right
                        case 'D': return 1003; // Left
                    }
                }
            }
        }
        return c;
    }

    private void moveCursorLeft() {
        if (cursorCol > 0) cursorCol--;
        else if (cursorRow > 0) {
            cursorRow--;
            cursorCol = buffer.get(cursorRow).length();
        }
    }

    private void moveCursorRight() {
        if (cursorCol < buffer.get(cursorRow).length()) cursorCol++;
        else if (cursorRow + 1 < buffer.size()) {
            cursorRow++;
            cursorCol = 0;
        }
    }

    private void moveCursorUp() {
        if (cursorRow > 0) {
            cursorRow--;
            cursorCol = Math.min(cursorCol, buffer.get(cursorRow).length());
        }
    }

    private void moveCursorDown() {
        if (cursorRow + 1 < buffer.size()) {
            cursorRow++;
            cursorCol = Math.min(cursorCol, buffer.get(cursorRow).length());
        }
    }

    private void insertChar(char c) {
        buffer.get(cursorRow).insert(cursorCol, c);
        cursorCol++;
    }

    private void deleteChar() {
        StringBuilder line = buffer.get(cursorRow);
        if (cursorCol > 0) {
            line.deleteCharAt(cursorCol - 1);
            cursorCol--;
        } else if (cursorRow > 0) {
            int prevLen = buffer.get(cursorRow - 1).length();
            buffer.get(cursorRow - 1).append(line);
            buffer.remove(cursorRow);
            cursorRow--;
            cursorCol = prevLen;
        }
    }

    public void run() throws IOException, InterruptedException {
        render();

        // Disable terminal buffering for Unix
        Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "stty raw -echo < /dev/tty"}).waitFor();

        while (true) {
            int key = readKey();

            if (mode == Mode.COMMAND) {
                if (key == 'i') {
                    mode = Mode.INSERT;
                } else if (key == ':') {
                    System.out.print("\n:");
                    System.out.flush();
                    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                    String cmd = br.readLine();
                    if ("q".equals(cmd)) {
                        break;
                    } else if ("w".equals(cmd)) {
                        saveFile();
                    } else if ("wq".equals(cmd)) {
                        saveFile();
                        break;
                    }
                } else {
                    // Navigation in command mode
                    switch (key) {
                        case 1000: moveCursorUp(); break;
                        case 1001: moveCursorDown(); break;
                        case 1002: moveCursorRight(); break;
                        case 1003: moveCursorLeft(); break;
                        default:
                            // Ignore other keys
                    }
                }
            } else if (mode == Mode.INSERT) {
                if (key == 27) { // ESC
                    mode = Mode.COMMAND;
                } else if (key == 127 || key == 8) { // Backspace
                    deleteChar();
                } else if (key == 10 || key == 13) { // Enter
                    StringBuilder currentLine = buffer.get(cursorRow);
                    String newLine = currentLine.substring(cursorCol);
                    currentLine.delete(cursorCol, currentLine.length());
                    buffer.add(cursorRow + 1, new StringBuilder(newLine));
                    cursorRow++;
                    cursorCol = 0;
                } else {
                    insertChar((char) key);
                }
            }

            render();
        }

        // Restore terminal
        Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "stty cooked echo < /dev/tty"}).waitFor();
        System.out.println("Goodbye!");
    }

    public static void main(String[] args) throws Exception {
        String filename = args.length > 0 ? args[0] : null;
        VimClone editor = new VimClone(filename);
        editor.run();
    }
}
