# Expense Sharing System — Requirements

## Functional Requirements

### FR-1: User Management

- Users can be created with a unique ID, name, email, and phone number.
- Users can be retrieved by their ID.

### FR-2: Group Management

- Users can create groups with a name and a set of members.
- Members can be added to or removed from a group.
- A group must have at least 2 members.
- Currently, **all expenses are group expenses**.
  - The design should be extensible to support non-group (direct/peer-to-peer)
  expenses in the future without major refactoring.

### FR-3: Add Expense

- Any member of a group can add an expense.
- An expense captures:
  - **Who paid** (the payer — muliple payers)
  - **Total amount**
  - **Participants** (who the expense is split among)
  - **Split strategy** (how to divide the amount)
  - **Description** (what the expense is for)
- Supported split strategies (extensible):
  - **EQUAL** — Total divided equally among all participants.
  - **EXACT** — Each participant owes a specific, explicitly stated amount.
    - Validation: individual amounts must sum to the total.
  - **PERCENTAGE** — Each participant owes a percentage of the total.
    - Validation: percentages must sum to 100%.
- Adding an expense updates the balances between the payer and each participant.

### FR-4: Balance Tracking

- The system tracks **every transaction** (no net simplification).
- At any point, a user can query:
  - Their balance with a specific user (how much they owe / are owed).
  - Their overall balances across all users.
  - Group-level balances (who owes whom within a group).
- Balances are derived from the history of expenses and settlements.

### FR-5: Settle Up

- A user can record a settlement (payment) to another user.
- A settlement is a **distinct transaction type** — not an expense.
- Settling up reduces the outstanding balance between two users.
- Partial settlements are allowed (you don't have to clear the full debt).
- Settlements are recorded in the transaction history.

### FR-6: Transaction History

- The system maintains a full, ordered history of all transactions.
- Transaction types: **EXPENSE** and **SETTLEMENT**.
- History can be queried:
  - For a specific user (all transactions involving them).
  - For a specific group (all expenses in that group).
  - Global (all transactions in the system).

---

## Non-Functional Requirements

### NFR-1: Extensibility

- Adding a new split strategy should require **no changes** to existing code
(Open/Closed Principle).
- The system should be designed to accommodate non-group expenses in the future
with minimal changes.
- New transaction types (e.g., "request payment") should be easy to add.

### NFR-2: Clean OOP Design

- Follow SOLID principles.
- Use design patterns where they naturally fit (Strategy, Factory, etc.).
- Prefer composition over inheritance.
- Keep classes focused — single responsibility.

### NFR-3: Correctness

- All monetary calculations must be precise (handle rounding properly for
equal splits).
- Validations on split amounts (exact amounts must sum to total, percentages
must sum to 100%).

### NFR-4: In-Memory

- No database or file I/O.
- All data lives in memory for the lifetime of the application.

### NFR-5: Single Currency

- All amounts are in a single currency.
- No conversion logic needed.

---

## Out of Scope (Explicit Exclusions)


| Feature                     | Reason                        |
| --------------------------- | ----------------------------- |
| Non-group expenses          | Future extensibility only     |
| Notifications               | Not core functionality        |
| Multi-currency              | Simplicity                    |
| Concurrency / Thread safety | Single-threaded exercise      |
| Database / Persistence      | In-memory only                |
| Authentication / Security   | Not relevant for LLD exercise |
| Debt simplification         | Track every transaction as-is |
| Multiple payers per expense | Single payer for now          |
| Recurring expenses          | Out of scope                  |
| UI / API layer              | Focus is on core domain model |


---

## Key Validations Summary


| Operation        | Validation                             |
| ---------------- | -------------------------------------- |
| Create Group     | At least 2 members                     |
| Add Expense      | Payer must be a group member           |
| Add Expense      | All participants must be group members |
| Add Expense      | Amount must be positive                |
| EXACT split      | Individual amounts must sum to total   |
| PERCENTAGE split | Percentages must sum to 100%           |
| Settle Up        | Amount must be positive                |
| Settle Up        | Cannot settle with yourself            |


