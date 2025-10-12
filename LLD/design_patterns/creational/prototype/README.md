## Prototype Design Pattern - LLD Question

**Problem Statement:**

You are designing a document management system that supports multiple types of documents (e.g., `Resume`, `Report`, `Invoice`). These documents share common attributes such as title and content, but each type may have its own specific fields.

To improve performance and reduce overhead in the system, you want to use the Prototype design pattern to **clone** existing documents instead of creating new instances from scratch every time.

**Requirements:**
1. Create an abstract class or interface `Document` that supports cloning.
2. Implement at least two concrete classes like `Resume` and `Report` with their own specific fields.
3. Ensure cloned documents are **deep copies** where necessary (e.g., if a field is a list or object reference).
4. Demonstrate cloning behavior with a main class that:
    - Creates a prototype document
    - Clones it multiple times
    - Modifies a field in the cloned document to show it doesn’t affect the original

**Bonus:**
- Add a `DocumentRegistry` that maintains a map of prototype documents and allows users to retrieve clones on demand.

Design and implement this system using appropriate object-oriented design principles and patterns.
