# V2 Recommendations — Deep Dive

Two critical gaps that need to be solid before we code.

---

## 1. Transaction Hierarchy — How to Think About It

### The Problem

You have `Transaction`, `Expense`, and `SettleUp` floating around without
a clear relationship. In an interview, the interviewer will say:

> "Draw the class diagram for Transaction. What's abstract? What's concrete?
> What fields go where?"

You need a crisp, instant answer.

### How to Design a Class Hierarchy (General Technique)

**Step 1 — List all concrete "things" that exist in your system.**

In our case, two things can happen:
- Someone adds an expense (dinner was $100, split 4 ways)
- Someone settles up (Alice pays Bob $30)

These are your **concrete classes**.

**Step 2 — Find what's common between them.**

Ask: "If I had a list of both expenses and settlements, what fields would
I need on every item to display a transaction history?"

| Field       | Expense? | Settlement? | Common? |
| ----------- | -------- | ----------- | ------- |
| id          | Yes      | Yes         | Yes     |
| amount      | Yes      | Yes         | Yes     |
| timestamp   | Yes      | Yes         | Yes     |
| description | Yes      | Maybe       | Yes     |
| type/label  | Yes      | Yes         | Yes     |

These common fields go into the **abstract base class**.

**Step 3 — Find what's unique to each.**

| Field          | Expense Only        | Settlement Only |
| -------------- | ------------------- | --------------- |
| paidBy         | Yes (who paid)      | -               |
| participants   | Yes (who owes)      | -               |
| splitStrategy  | Yes (how to divide) | -               |
| splitDetails   | Yes (per-user owes) | -               |
| fromUser       | -                   | Yes (who pays)  |
| toUser         | -                   | Yes (who receives) |

These unique fields go into the **concrete subclasses**.

**Step 4 — Draw it out.**

```
┌─────────────────────────────────┐
│     Transaction (abstract)      │
├─────────────────────────────────┤
│ - id: String                    │
│ - amount: double                │
│ - timestamp: LocalDateTime      │
│ - description: String           │
├─────────────────────────────────┤
│ + getId()                       │
│ + getAmount()                   │
│ + getTimestamp()                 │
│ + getDescription()              │
│ + getType(): TransactionType    │
└──────────┬──────────────────────┘
           │
     ┌─────┴──────┐
     │            │
┌────▼─────┐ ┌───▼────────┐
│ Expense  │ │ Settlement │
├──────────┤ ├────────────┤
│ paidBy   │ │ fromUser   │
│ group    │ │ toUser     │
│ splits   │ │            │
│ strategy │ │            │
└──────────┘ └────────────┘
```

### Why This Works

- **Polymorphism**: You can store `List<Transaction>` and iterate over both
  types. History display, filtering, serialization — all work generically.
- **Type safety**: Each subclass has only the fields relevant to it. No
  null `fromUser` on an expense or empty `splits` on a settlement.
- **Extensibility**: Want to add a "Payment Request" type later? Just add
  another subclass. Nothing else changes.

### The `Expense` Data Question

In your V2, `Expense` was a separate entity with `lenders` and `split`.
Here's the clean resolution:

**Make Expense a subclass of Transaction directly.** It carries its own
data (paidBy, splits). No need for a separate `ExpenseTransaction` wrapper
around an `Expense` data object — that's over-engineering for our scope.

```java
public class Expense extends Transaction {
    private final User paidBy;
    private final Group group;
    private final Map<User, Double> splits;  // what each person owes
    private final SplitType splitType;

    // paidBy is also the one whose balance goes UP (they are owed money)
    // each user in splits has their balance go DOWN (they owe money)
}
```

```java
public class Settlement extends Transaction {
    private final User fromUser;  // the one paying off debt
    private final User toUser;    // the one receiving payment

    // fromUser's debt to toUser decreases by amount
}
```

### Interview Tip

When the interviewer asks about your hierarchy, say:

> "Transaction is my abstract base with id, amount, timestamp, and
> description. Expense and Settlement are concrete subclasses. Expense
> adds paidBy, group, splitType, and a splits map. Settlement adds fromUser
> and toUser. I can store both in a `List<Transaction>` for unified history."

That's a 15-second answer. Crisp.

---

## 2. Service API — How to Design It

### The Problem

You added a `Splitwise` class with `List<User>` and `List<Group>`, but no
methods. In an interview, the service API is what the interviewer **actually
evaluates**. Your entities are the nouns; the service methods are the verbs.
The verbs are what make the system *do* things.

### How to Design a Service API (General Technique)

**Step 1 — List every user action from the requirements.**

Go back to the requirements doc and extract every action a user can perform:

| User Action                         | Requirement |
| ----------------------------------- | ----------- |
| Create a user                       | FR-1        |
| Create a group                      | FR-2        |
| Add member to group                 | FR-2        |
| Add an expense to a group           | FR-3        |
| Settle up with another user         | FR-5        |
| View balance with a specific user   | FR-4        |
| View all my balances                | FR-4        |
| View group balances                 | FR-4        |
| View my transaction history         | FR-6        |
| View group transaction history      | FR-6        |

**Step 2 — Turn each action into a method signature.**

For each row, ask:
- What **inputs** does this need?
- What **output** does the caller get back?
- What **validation** must happen?

```java
public class ExpenseService {

    // --- User Management ---
    User addUser(String name, String email, String phone);
    User getUser(String userId);

    // --- Group Management ---
    Group createGroup(String name, List<String> memberUserIds);
    void addMemberToGroup(String groupId, String userId);

    // --- Core: Expense ---
    Expense addExpense(
        String groupId,
        String paidByUserId,
        double amount,
        List<String> participantUserIds,
        SplitType splitType,
        Map<String, Double> splitInput  // empty for EQUAL, amounts for EXACT, percentages for PERCENTAGE
    );

    // --- Core: Settlement ---
    Settlement settleUp(
        String fromUserId,
        String toUserId,
        double amount
    );

    // --- Queries: Balances ---
    Map<String, Double> getBalances(String userId);
    // Returns: { "user-2": -50.0, "user-3": 25.0 }
    // Negative = you owe them, Positive = they owe you

    double getBalance(String userId1, String userId2);
    // Returns the net balance between two specific users

    // --- Queries: History ---
    List<Transaction> getTransactionHistory(String userId);
    List<Transaction> getGroupHistory(String groupId);
}
```

**Step 3 — Trace the data flow for the most critical method.**

The interviewer will often say: "Walk me through what happens when
`addExpense()` is called." Here's the flow:

```
addExpense(groupId, paidBy, 100.0, [A, B, C, D], EQUAL, {})
│
├─ 1. Validate: group exists, paidBy is member, all participants are members
│
├─ 2. Compute split using SplitStrategy
│      EQUAL: 100 / 4 = 25.0 each
│      → { A: 25.0, B: 25.0, C: 25.0, D: 25.0 }
│
├─ 3. Update balances
│      paidBy = A (she paid the full 100)
│      For each participant who isn't A:
│        B owes A: += 25.0
│        C owes A: += 25.0
│        D owes A: += 25.0
│      (A's own share cancels out — she paid 100, her share is 25, net: others owe her 75)
│
├─ 4. Create Expense object (the Transaction)
│      Store paidBy, amount, splits map, group, timestamp
│
├─ 5. Record in history
│      Add to group's transaction list
│      Add to global transaction list (for user-level queries)
│
└─ 6. Return the Expense
```

### Balance Tracking — The Backing Data Structure

You asked where balances live. Here's the concrete recommendation:

**Use a `Map<String, Map<String, Double>>` — userId → (otherUserId → netAmount)**

```
balanceSheet:
  "alice" → { "bob": 50.0, "charlie": -20.0 }
  "bob"   → { "alice": -50.0 }
  "charlie" → { "alice": 20.0 }
```

- Positive value: the other person owes you.
- Negative value: you owe them.
- **Invariant**: `balances[A][B] == -balances[B][A]` (always mirror).

**Where does it live?** In the `ExpenseService` itself. It's system-wide
(settlements can happen across groups), so it doesn't belong inside a Group.

**When does it update?**
- On `addExpense()`: payer's balance goes up, each participant's goes down.
- On `settleUp()`: fromUser's debt to toUser decreases.

```java
private void updateBalance(String userId1, String userId2, double amount) {
    // userId1 is OWED 'amount' BY userId2
    balances
        .computeIfAbsent(userId1, k -> new HashMap<>())
        .merge(userId2, amount, Double::sum);

    // Mirror: userId2 OWES 'amount' TO userId1
    balances
        .computeIfAbsent(userId2, k -> new HashMap<>())
        .merge(userId1, -amount, Double::sum);
}
```

### Interview Tip

When presenting your service API, say:

> "ExpenseService is my main entry point. It exposes addUser, createGroup,
> addExpense, settleUp, and query methods for balances and history. The
> addExpense flow is: validate → compute split via strategy → update balance
> sheet → create and store transaction. Balances are maintained as a running
> map keyed by user pairs, updated on every expense and settlement."

That's a complete, confident answer.

---

## Summary: What V3 Should Look Like

After absorbing this, your V3 should have:

1. **Transaction hierarchy** — abstract Transaction with id/amount/timestamp,
   Expense and Settlement as concrete subclasses with their specific fields.
2. **SplitStrategy** — `Map<User, Double> split(double amount, List<User> participants, Map<User, Double> input)`.
3. **ExpenseService API** — full method list with signatures, covering all
   requirements.
4. **Balance sheet structure** — `Map<UserId, Map<UserId, Double>>` owned by
   the service.
5. **Data flow** — be able to narrate addExpense and settleUp step by step.
