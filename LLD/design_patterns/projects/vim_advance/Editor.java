import buffer.Cursor;
import buffer.TextBuffer;

import java.io.IOException;

public class Editor {
    private TextBuffer buffer;
    private Cursor cursor;
    private ModeHandler modeHandler;
    private Mode mode;

    public Editor() {
        buffer = new TextBuffer();
        cursor = new Cursor();
        mode = Mode.VIEW;
        modeHandler = new ViewModeHandler(this);
    }

    public TextBuffer getBuffer() {
        return buffer;
    }

    public Cursor getCursor() {
        return cursor;
    }

    public Mode getMode() {
        return mode;
    }

    public void switchMode(Mode newMode) {
        this.mode = newMode;
        switch (newMode) {
            case VIEW:
                modeHandler = new ViewModeHandler(this);
                break;
            // case INSERT:
            //     modeHandler = new InsertModeHandler(this);
            //     break;
            // case COMMAND:
            //     modeHandler = new CommandModeHandler(this);
            //     break;
            default:
                System.out.println("Mode not implemented: " + newMode);
        }
    }

    public void loadFile(String filename) throws IOException {
        buffer.loadFromFile(filename);
        cursor.setPosition(0, 0);
    }

    public void handleInput(char input) {
        modeHandler.handleInput(input);
    }

    public void render() {
        modeHandler.render();
    }
}
