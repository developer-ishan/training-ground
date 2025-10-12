## 🚀 Problem Statement

You are designing a **Cross-Platform UI Toolkit** that supports creating UI elements for both **Windows** and **MacOS** platforms.

Each platform has its own versions of:

- `Button`
- `Checkbox`

Design a system where:
- The client code can create buttons and checkboxes without knowing which platform it is targeting.
- Adding a new platform (like `Linux`) should require minimal changes to existing code.

Use the **Abstract Factory Design Pattern** to implement this.

---

### ✨ Requirements

1. Create interfaces for `Button` and `Checkbox`.
2. Implement these interfaces for both `Windows` and `MacOS`.
3. Define an abstract factory `GUIFactory` that creates families of related objects (buttons and checkboxes).
4. Implement concrete factories: `WindowsFactory` and `MacOSFactory`.
5. Create a `Client` class that uses only the `GUIFactory` interface to create and render UI elements.

---

### 🧪 Bonus

- Add a new `LinuxFactory` with corresponding `LinuxButton` and `LinuxCheckbox`.
- Ensure the client code remains unchanged when switching platforms.

---

### 📌 Deliverables

- Interfaces and concrete classes for UI components.
- Abstract factory interface and its implementations.
- Client class demonstrating cross-platform flexibility.

