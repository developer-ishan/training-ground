package behavioral.memento;

import org.w3c.dom.Text;

import behavioral.memento.originator.TextEditor;
import behavioral.memento.caretaker.History;
import behavioral.memento.caretaker.UndoRedoHistory;

public class Main {
  public static void main(String[] args) {
    TextEditor textEditor = new TextEditor();
    History history = new History();
    UndoRedoHistory undoRedoHistory = new UndoRedoHistory();


    textEditor.setText("Version 1");
    textEditor.print();
    history.push(textEditor.save());
    undoRedoHistory.saveState(textEditor.save());

    textEditor.setText("Version 2");
    textEditor.print();

    textEditor.setText("Version 3");
    textEditor.print();

    // textEditor.restore(history.pop());
    // textEditor.print();

    textEditor.restore(undoRedoHistory.undo(textEditor.save()));
    textEditor.print();
    textEditor.restore(undoRedoHistory.redo(textEditor.save()));
    textEditor.print();
    textEditor.restore(undoRedoHistory.undo(textEditor.save()));
    textEditor.print();

    textEditor.setText("Final Verdict");
    textEditor.print();
    undoRedoHistory.saveState(textEditor.save());
  }
}
