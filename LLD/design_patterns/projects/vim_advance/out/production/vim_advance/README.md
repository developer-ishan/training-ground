## LLD Question: Build a CLI Text Editor (Vim-like) Using Object-Oriented Design Principles

**Problem Statement:**

Design and implement a command-line based text editor inspired by `vim`. The editor should support a modal interface (insert mode, command mode) and allow users to open, edit, and save files. Your goal is to build a simplified version of `vim` that supports basic editing functionality while demonstrating sound object-oriented design.

---

### Key Requirements

1. **Modal Interface:**
    - Support at least two modes:
        - `Insert mode`: For entering and editing text.
        - `Command mode`: For interpreting user commands (e.g., `:w`, `:q`, etc.).
    - Editor should launch in `Command mode` by default.

2. **Text Editing Features:**
    - File operations:
        - `:open <filename>` — open a text file into the editor.
        - `:w` — save the current buffer to file.
        - `:q` — quit the editor.
        - `:wq` — save and quit.
    - Cursor navigation using keys: `h`, `j`, `k`, `l`.
    - `dd` — delete the current line.
    - `i` — enter insert mode.
    - `Esc` — return to command mode.
    - `:undo` and `:redo` — support for undoing and redoing edits.

3. **Architecture Requirements:**
    - Use **appropriate design patterns** (e.g., Command, Strategy, State, Memento, Observer, etc.) where suitable.
    - Your implementation should reflect a clean separation of concerns (text buffer, input handling, command execution, file I/O, etc.).
    - Ensure the codebase is extensible (e.g., adding new commands or modes should not require significant rewrites).

4. **Undo/Redo System:**
    - Maintain history of actions to support undo and redo.

5. **Interactive CLI:**
    - Allow line-by-line or character-level input depending on mode.
    - Display minimal UI information such as mode, cursor location, and filename.

---

### Bonus Features (Optional)

- Line numbering or visual cursor indicator.
- Yank/Paste (`yy`, `p`).
- Search functionality (`/term`).
- Macro recording and replaying.
- Syntax highlighting for a toy language.
- Command chaining (`:w | :q`).

---

### Deliverables

- A full Java implementation with:
    - Well-structured, modular classes.
    - Clean use of OOP principles and appropriate design patterns.
    - CLI interface mimicking a vim-like experience.
- README file containing:
    - Overview of the architecture.
    - Supported commands.
    - Instructions to run the editor.

---

### Evaluation Criteria

- Correctness of the editing behavior and features.
- Appropriate use of design patterns and software principles.
- Clean, maintainable, and extensible code.
- Clear CLI interface and user experience.

---

### Example Usage

```text
> java VimClone
-- NORMAL MODE --
sample.txt | Line: 1, Col: 1

Hello world!
This is a second line.
~
~
~

Commands:
  i       → insert mode
  dd      → delete current line
  :w      → write to file
  :q      → quit
  :undo   → undo last operation
  Esc     → switch to normal mode
