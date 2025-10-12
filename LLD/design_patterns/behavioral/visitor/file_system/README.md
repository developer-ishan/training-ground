Here's a well-framed **LLD question** on the **Visitor Design Pattern**, modeled on a full-fledged **file system management** use case:

---

### 💡 **LLD Question: Implementing a File System using the Visitor Design Pattern**

You are tasked with designing a simplified **File System** that consists of `File` and `Folder` elements.

Each `Folder` can contain multiple `FileSystemElement`s — either `File`s or other `Folder`s (i.e., it is recursive). The system should support extensible operations without changing the core file and folder structure.

#### ✅ Your goal:

Design the system using the **Visitor Design Pattern** so that **new operations** (e.g., virus scanning, indexing, size calculation, file listing, etc.) can be added **without modifying the existing File/Folder classes**.

---

### 📂 Requirements:

1. Define the following structure:

    * `FileSystemElement` interface: defines the `accept(Visitor)` method.
    * `File` class: represents a file with a name and size.
    * `Folder` class: contains a name and a list of children (`FileSystemElement`s).

2. Define a `FileSystemVisitor` interface:

    * It should have separate visit methods for `File` and `Folder`.

3. Implement the following visitors:

    * `SizeCalculatorVisitor`: calculates total size of all files.
    * `FileListingVisitor`: prints a tree-like listing of the structure.
    * (Optional for bonus) `VirusScanVisitor`: prints scanning each file.

---

### 🧠 Constraints:

* The system should be open for extension but closed for modification.
* Ensure visitors can traverse **deep nested folder structures**.
* Try to keep your design **modular and reusable**.

---

Would you like me to provide the full Java implementation of this solution step-by-step?
