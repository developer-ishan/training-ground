## Question: Implement a Text Editor using the Command Design Pattern

You are building a simple text editor with support for the following commands:

- `WriteCommand`: Appends a string to the current content.
- `UndoCommand`: Reverts the last command.
- `ClearCommand`: Clears the entire content.

### Requirements:
1. Use the **Command Design Pattern** to encapsulate all editing operations.
2. The editor should support:
    - Executing commands.
    - Maintaining a **history** of commands.
    - Undoing the last command.
3. Design the system such that adding new commands (e.g., `DeleteLastWordCommand`) is easy in the future.

### Functional Example:

```java
TextEditor editor = new TextEditor();
CommandManager manager = new CommandManager();

manager.executeCommand(new WriteCommand(editor, "Hello "));
manager.executeCommand(new WriteCommand(editor, "World"));
manager.executeCommand(new ClearCommand(editor));

System.out.println(editor.getContent()); // Should print empty string

manager.undo();
System.out.println(editor.getContent()); // Should print "Hello World"