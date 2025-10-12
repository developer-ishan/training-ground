package buffer;

public class Cursor {
    private int line;
    private int column;

    public Cursor() {
        line = 0;
        column = 0;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public void setPosition(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public void moveLeft() {
        if (column > 0) {
            column--;
        }
    }

    public void moveRight(int lineLength) {
        if (column < lineLength - 1) {
            column++;
        }
    }

    public void moveUp() {
        if (line > 0) {
            line--;
        }
    }

    public void moveDown(int maxLine) {
        if (line < maxLine - 1) {
            line++;
        }
    }
}
