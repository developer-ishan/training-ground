## 💡 LLD Question: Memento Design Pattern

Design a **text editor system** using the **Memento Design Pattern**.

### 🧾 Problem Statement

You are tasked with building a **Text Editor** that allows users to type, delete, and modify text with full support for **undo** and **redo** operations.
Every change to the text should be captured as a “snapshot” so the editor can **restore its previous states** or move forward to a later state when required.

### 🧱 Requirements

1. The system should maintain the current state of the text and allow the following operations:

   * `type(text)` → Add new text.
   * `delete(count)` → Remove the last `count` characters.
   * `undo()` → Revert to the previous state.
   * `redo()` → Move forward to the next state (if available).
2. The design should follow the **Memento Design Pattern**:

   * `Originator` → Represents the text editor and creates/uses snapshots.
   * `Memento` → Stores the editor’s state.
   * `Caretaker` → Manages the history of states for undo/redo.
3. The undo and redo operations should **not lose any intermediate states**, allowing users to navigate back and forth through their editing history.
4. The system should support **multiple sequential undo/redo actions**.
5. Ensure that adding new editor operations (e.g., formatting) in the future should not break the existing undo/redo logic.

### 🚀 Optional

* Add a limit on the number of stored states to prevent excessive memory usage.
* Support saving and restoring the cursor position along with text content.
* Allow branching history when a new edit happens after undo (similar to modern editors).
