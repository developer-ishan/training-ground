# Expense Sharing System (Splitwise) — Problem Statement

## Elevator Pitch

Design an in-memory expense sharing application where users can create groups,
add shared expenses with flexible split strategies, track balances, settle debts,
and view full transaction history.

---

## Scenario

A group of friends goes on a trip. Different people pay for different things —
dinner, hotel, gas, tickets. At any point, anyone should be able to see:

- Who owes whom and how much
- The full history of expenses and settlements
- A clear path to settling all debts

The system must handle this **correctly**, **cleanly**, and be **extensible**
for future requirements.

---

## Scope

| Dimension        | Decision                                                    |
| ---------------- | ----------------------------------------------------------- |
| Expense Context  | Group expenses only (design for extensibility to non-group) |
| Split Strategies | Multiple — Equal, Exact, Percentage (extensible)            |
| Balance Tracking | Track every individual transaction (no simplification)      |
| Settle Up        | Supported — recorded as a distinct transaction type         |
| History          | Full expense + settlement history                           |
| Concurrency      | Not required                                                |
| Persistence      | In-memory only                                              |
| Scale            | LLD/OOP exercise — focus on clean design                    |
| Notifications    | Out of scope                                                |
| Currency         | Single currency                                             |
