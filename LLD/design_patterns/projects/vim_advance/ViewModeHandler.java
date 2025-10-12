import buffer.Cursor;
import buffer.TextBuffer;

public class ViewModeHandler implements ModeHandler {
    private final Editor editor;

    public ViewModeHandler(Editor editor) {
        this.editor = editor;
    }

    @Override
    public void handleInput(char input) {
        Cursor cursor = editor.getCursor();
        TextBuffer buffer = editor.getBuffer();

        switch (input) {
            case 'h': // move left
                cursor.moveLeft();
                break;
            case 'l': // move right
                int lineLength = buffer.getLine(cursor.getLine()).length();
                cursor.moveRight(lineLength);
                break;
            case 'k': // move up
                cursor.moveUp();
                // Adjust column if line length is shorter
                int newLineLength = buffer.getLine(cursor.getLine()).length();
                if (cursor.getColumn() >= newLineLength) {
                    cursor.setPosition(cursor.getLine(), Math.max(0, newLineLength - 1));
                }
                break;
            case 'j': // move down
                cursor.moveDown(buffer.getLineCount());
                // Adjust column if line length is shorter
                int downLineLength = buffer.getLine(cursor.getLine()).length();
                if (cursor.getColumn() >= downLineLength) {
                    cursor.setPosition(cursor.getLine(), Math.max(0, downLineLength - 1));
                }
                break;
            // Can add more navigation or quit commands here
            default:
                System.out.println("Unknown command in View Mode: " + input);
        }
    }

    @Override
    public void render() {
        System.out.println("-- VIEW MODE --");
        TextBuffer buffer = editor.getBuffer();
        Cursor cursor = editor.getCursor();

        for (int i = 0; i < buffer.getLineCount(); i++) {
            String line = buffer.getLine(i);
            if (i == cursor.getLine()) {
                // Visual cursor indication
                System.out.print("> ");
                if (cursor.getColumn() < line.length()) {
                    // Show line with cursor char highlighted (simple)
                    System.out.println(line.substring(0, cursor.getColumn()) +
                            "|" +
                            line.substring(cursor.getColumn()));
                } else {
                    // Cursor beyond line end (shouldn't happen if adjusted)
                    System.out.println(line + "|");
                }
            } else {
                System.out.println("  " + line);
            }
        }
        System.out.println();
        System.out.printf("Cursor: Line %d, Col %d\n", cursor.getLine() + 1, cursor.getColumn() + 1);
        System.out.println("Commands: h,j,k,l to navigate, Esc to switch mode");
    }
}