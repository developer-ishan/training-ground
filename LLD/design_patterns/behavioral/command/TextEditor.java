package behavioral.command;

public class TextEditor {
    private final StringBuilder content = new StringBuilder();

    public void append(String text) {
        content.append(text);
    }

    public void removeLast(int length) {
        int start = content.length() - length;
        if (start >= 0) {
            content.delete(start, content.length());
        }
    }

    public void clear() {
        content.setLength(0);
    }

    public String getContent() {
        return content.toString();
    }
}
