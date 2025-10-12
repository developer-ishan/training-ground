package projects.vim_cli_clone_basic.editor;

public class Cursor {
    private int line;
    private int column;

    public Cursor() {
        this.line = line;
        this.column = column;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public void moveTo(int line, int column) {
        this.line = line;
        this.column = column;
    }
}
