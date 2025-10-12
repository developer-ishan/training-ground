## LLD Question: Template Design Pattern

**Problem Statement:**

You're designing a report generation system for an analytics platform. Different types of reports (e.g., Sales Report, Inventory Report, User Activity Report) follow a common structure:

1. Fetch data
2. Analyze data
3. Format results
4. Export to PDF/CSV

However, each report type has a unique implementation for steps 1–3 but uses a common method to export the data. Design a system using the **Template Design Pattern** to enforce this fixed structure while allowing customization of specific steps.

---

### Requirements:

- A base class should define the template method to control the flow.
- Subclasses should override only the relevant steps.
- The export step must be **common and reused**.
- Add hooks if needed to allow optional customization.

---

### Bonus:

- Demonstrate the usage of this pattern with at least two concrete report types.
- Write unit tests for each type of report.
- Ensure the design is extensible for new report types without modifying the base class.

---

### Constraints:

- Use Java.
- Ensure adherence to the **Open/Closed Principle**.
- Keep the design clean, modular, and production-ready.
