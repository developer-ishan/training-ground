package behavioral.command;

public class Main {
    public static void main(String[] args) {
        TextEditor editor = new TextEditor();
        CommandManager manager = new CommandManager();

        manager.executeCommand(new WriteCommand(editor, "Hello "));
        manager.executeCommand(new WriteCommand(editor, "World"));
        System.out.println("After writing: " + editor.getContent()); // Hello World

        manager.executeCommand(new ClearCommand(editor));
        System.out.println("After clearing: " + editor.getContent()); // empty

        manager.undo();
        System.out.println("After undo: " + editor.getContent()); // Hello World

        manager.undo();
        System.out.println("Undo again: " + editor.getContent()); // Hello

        manager.undo();
        System.out.println("Undo again: " + editor.getContent()); // (empty)

        manager.undo();
        System.out.println("Undo again: " + editor.getContent()); // (empty)



        manager.executeCommand(new WriteCommand(editor, "Hello amazing world"));
        System.out.println("Before deleting word: " + editor.getContent()); // Hello amazing world

        manager.executeCommand(new DeleteLastWordCommand(editor));
        System.out.println("After deleting word: " + editor.getContent()); // Hello amazing

        manager.undo();
        System.out.println("After undo delete: " + editor.getContent()); // Hello amazing world

    }
}
