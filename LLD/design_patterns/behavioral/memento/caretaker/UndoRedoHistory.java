package behavioral.memento.caretaker;

import java.util.Stack;

import behavioral.memento.Memento;

public class UndoRedoHistory {
  private Stack<Memento> undoStack = new Stack<>();
  private Stack<Memento> redoStack = new Stack<>();

  public void saveState(Memento state) {
    undoStack.push(state);
    redoStack.clear(); // Clear redo stack on new action
  }

  public Memento undo(Memento currentState) {
    if (!undoStack.isEmpty()) {
      redoStack.push(currentState);
      return undoStack.pop();
    }
    return null; // No change if undo stack is empty
  }

  public Memento redo(Memento currentState) {
    if (!redoStack.isEmpty()) {
      undoStack.push(currentState);
      return redoStack.pop();
    }
    return null; // No change if redo stack is empty
  }
}
