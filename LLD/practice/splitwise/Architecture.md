# Splitwise — Final Architecture

## Class Diagram

```
┌─────────────────────┐          ┌──────────────────────────────────────┐
│        User         │          │                Group                 │
├─────────────────────┤          ├──────────────────────────────────────┤
│ - id: String        │◄────────▶│ - id: String                        │
│ - name: String      │  *    *  │ - name: String                      │
│ - email: String     │          │ - members: List<User>               │
├─────────────────────┤          │ - transactions: List<Transaction>   │
│ + equals() [by id]  │          ├──────────────────────────────────────┤
│ + hashCode() [by id]│          │ + addMember(User)                   │
└─────────────────────┘          │ + isMember(User): boolean           │
                                 │ + addTransaction(Transaction)       │
                                 │ + getTransactions(): List (unmod.)  │
                                 │ + getMembers(): List (unmod.)       │
                                 └──────────────────────────────────────┘

┌──────────────────────────────────────────────┐
│            Transaction  <<abstract>>         │
├──────────────────────────────────────────────┤
│ - id: String                                 │
│ - amount: BigDecimal                         │
│ - description: String                        │
│ - timestamp: LocalDateTime                   │
├──────────────────────────────────────────────┤
│ + getType(): TransactionType  <<abstract>>   │
└────────────────┬─────────────────────────────┘
                 │
       ┌─────────┴──────────┐
       │                    │
┌──────▼───────────┐  ┌─────▼─────────────┐
│     Expense      │  │    Settlement      │
├──────────────────┤  ├────────────────────┤
│ - paidBy: User   │  │ - fromUser: User   │
│ - splits:        │  │ - toUser: User     │
│   Map<User,      │  ├────────────────────┤
│   BigDecimal>    │  │ + getType()        │
│ - splitType:     │  │   → SETTLEMENT     │
│   SplitType      │  └────────────────────┘
├──────────────────┤
│ + getType()      │
│   → EXPENSE      │
└──────────────────┘

┌──────────────────────────────────────────────────────┐
│            <<interface>> SplitStrategy               │
├──────────────────────────────────────────────────────┤
│ + split(amount: BigDecimal,                          │
│         participants: List<User>,                    │
│         splitInput: Map<User, BigDecimal>)           │
│   → Map<User, BigDecimal>                            │
└───────────────┬──────────────┬───────────────────────┘
                │              │              │
  ┌─────────────▼──┐  ┌────────▼───────┐  ┌──▼─────────────────┐
  │ EqualSplit     │  │ ExactSplit     │  │ PercentSplit        │
  │ Strategy       │  │ Strategy       │  │ Strategy            │
  │                │  │                │  │                     │
  │ splitInput     │  │ splitInput     │  │ splitInput          │
  │ ignored        │  │ = exact amounts│  │ = percentages (∑100)│
  └────────────────┘  └────────────────┘  └─────────────────────┘
                              ▲
                              │ creates via
                    ┌─────────┴──────────────┐
                    │  SplitStrategyFactory  │
                    ├────────────────────────┤
                    │ - strategies:          │
                    │   EnumMap<SplitType,   │
                    │   SplitStrategy>       │
                    ├────────────────────────┤
                    │ + getStrategy(type)    │
                    └────────────────────────┘

┌────────────────────────────────────────────────────────────────┐
│                       ExpenseService                           │
├────────────────────────────────────────────────────────────────┤
│ - users:   Map<String, User>                                   │
│ - groups:  Map<String, Group>                                  │
│ - factory: SplitStrategyFactory                                │
├────────────────────────────────────────────────────────────────┤
│ + addUser(name, email): User                                   │
│ + createGroup(name, memberIds): Group                          │
│ + addMemberToGroup(groupId, userId)                            │
│ + addExpense(groupId, paidByUserId, amount,                    │
│              participantIds, splitType,                        │
│              splitInput: Map<String,BigDecimal>): Expense      │
│ + settleUp(groupId, fromUserId, toUserId, amount): Settlement  │
│ + printBalances(userId, groupId)                               │
│ + printMinimumTransfers(groupId)                               │
│ + getGroupHistory(groupId): List<Transaction>                  │
├────────────────────────────────────────────────────────────────┤
│ - getUser(userId): User                                        │
│ - getGroup(groupId): Group                                     │
│ - computeNetBalances(userId, groupId): Map<User, BigDecimal>   │
└────────────────────────────────────────────────────────────────┘

Enums:
  TransactionType { EXPENSE, SETTLEMENT }
  SplitType       { EQUAL, EXACT, PERCENTAGE }
```

---

## Entities

### `abstract Transaction`
```java
abstract class Transaction {
    private final String id;               // UUID
    private final BigDecimal amount;
    private final String description;
    private final LocalDateTime timestamp;

    public abstract TransactionType getType();
}
```

### `Expense extends Transaction`
```java
class Expense extends Transaction {
    private final User paidBy;
    private final Map<User, BigDecimal> splits; // per-participant share; unmodifiable after construction
    private final SplitType splitType;

    public TransactionType getType() { return TransactionType.EXPENSE; }
}
```

### `Settlement extends Transaction`
```java
class Settlement extends Transaction {
    private final User fromUser; // paying off debt
    private final User toUser;   // receiving payment

    public TransactionType getType() { return TransactionType.SETTLEMENT; }
}
```

### `User`
```java
class User {
    private final String id;   // UUID — basis for equals/hashCode
    private final String name;
    private final String email;

    // REQUIRED: equals() and hashCode() based on id only.
    // User is used as a Map key in Expense.splits — without this, HashMap lookups break.
    @Override public boolean equals(Object o) { ... id-based ... }
    @Override public int hashCode() { return id.hashCode(); }
}
```

### `Group` — pure domain entity
```java
class Group {
    private final String id;
    private final String name;
    private final List<User> members;         // mutable list, initialized in constructor
    private final List<Transaction> transactions; // mutable list, initialized in constructor

    void addMember(User user);                // validates: user not already a member
    boolean isMember(User user);
    void addTransaction(Transaction t);       // no member validation here — service validates before calling
    List<Transaction> getTransactions();      // returns unmodifiable view
    List<User> getMembers();                  // returns unmodifiable view
}
```
No business logic. No orchestration. State mutations only.

---

## Enums

```java
enum TransactionType { EXPENSE, SETTLEMENT }
enum SplitType { EQUAL, EXACT, PERCENTAGE }
```

---

## Split Strategy (Strategy Pattern)

### Interface
```java
interface SplitStrategy {
    // splitInput: null/empty for EQUAL; Map<User,amount> for EXACT; Map<User,percentage> for PERCENTAGE
    // Never null-safe by contract — callers must pass empty map for EQUAL, not null.
    Map<User, BigDecimal> split(
        BigDecimal amount,
        List<User> participants,
        Map<User, BigDecimal> splitInput
    );
}
```
Each implementation **validates its own input** and throws `IllegalArgumentException` on failure.

### Implementations

| Strategy | `splitInput` meaning | Validation |
|---|---|---|
| `EqualSplitStrategy` | Ignored (pass empty map) | participants non-empty |
| `ExactSplitStrategy` | Per-user exact amount | every participant has entry; values sum to `amount` |
| `PercentSplitStrategy` | Per-user percentage (0–100) | every participant has entry; values sum to 100 |

**Rounding:** `EqualSplit` and `PercentSplit` use `RoundingMode.HALF_UP`. Remainder cents assigned to the first participant so splits always sum to exactly `amount`.

### Factory
```java
class SplitStrategyFactory {
    private final Map<SplitType, SplitStrategy> strategies;

    public SplitStrategyFactory() {
        strategies = new EnumMap<>(SplitType.class);
        strategies.put(SplitType.EQUAL,      new EqualSplitStrategy());
        strategies.put(SplitType.EXACT,      new ExactSplitStrategy());
        strategies.put(SplitType.PERCENTAGE, new PercentSplitStrategy());
    }

    public SplitStrategy getStrategy(SplitType type); // throws if unknown type
}
```
To add a new split type: new enum value + new implementation + one line in constructor. Zero changes elsewhere.

---

## Service Layer

```java
class ExpenseService {
    private final Map<String, User> users;    // userId → User
    private final Map<String, Group> groups;  // groupId → Group
    private final SplitStrategyFactory factory;

    User addUser(String name, String email);

    Group createGroup(String name, List<String> memberIds);
    // validates: ≥2 members, all memberIds exist

    void addMemberToGroup(String groupId, String userId);
    // validates: group exists, user exists, user not already a member

    Expense addExpense(
        String groupId, String paidByUserId, BigDecimal amount,
        List<String> participantIds, SplitType splitType,
        Map<String, BigDecimal> splitInput  // String userId keys — service resolves to User before calling strategy
    );
    // validates: group exists, paidBy is member, all participants are members, amount > 0
    // flow: resolve User objects → call strategy.split() → create Expense → group.addTransaction()

    Settlement settleUp(String groupId, String fromUserId, String toUserId, BigDecimal amount);
    // validates: group exists, both users are members, fromUser ≠ toUser, amount > 0

    void printBalances(String userId, String groupId);
    void printMinimumTransfers(String groupId);
    List<Transaction> getGroupHistory(String groupId);

    // Private helpers
    private User getUser(String userId);     // throws if not found
    private Group getGroup(String groupId);  // throws if not found
    private Map<User, BigDecimal> computeNetBalances(String userId, String groupId);
}
```

### splitInput type conversion (String → User)
`addExpense` receives `Map<String, BigDecimal> splitInput` (user IDs as keys — caller-friendly). Before calling the strategy, the service resolves each key to a `User` object, producing `Map<User, BigDecimal>`.

---

## Balance Computation

Balances are computed **on-the-fly** from `group.getTransactions()` — no cached state.

- **Pro:** single source of truth, no sync issues
- **Con:** O(T) per query — acceptable for LLD; cache `Map<String, Map<String, BigDecimal>>` if bottleneck

### Logic (for a given user in a group)
```
netByCounterparty = Map<UserId, BigDecimal>  (positive = they owe us, negative = we owe them)

For each Expense:
  if paidBy == user:
    for each (participant, share) in splits where participant ≠ user:
      netByCounterparty[participant] += share   // they owe us

  else if user is in splits:
    netByCounterparty[paidBy] -= splits[user]   // we owe paidBy our share

For each Settlement:
  if fromUser == user:
    netByCounterparty[toUser] += amount         // we paid toUser, they owe us less (or we owe them less)

  if toUser == user:
    netByCounterparty[fromUser] -= amount       // fromUser paid us, we owe them less
```

---

## Minimum Transfers Algorithm (Greedy)

Goal: minimum number of payments to settle all outstanding debts in a group.

**Important:** operates on **expenses only** (not settlements) — shows what *needs* to happen to clear all debts from scratch.

```
1. Compute net balance per member from Expense transactions only:
   for each Expense:
     paidBy.net += expense.amount
     for each (participant, share) in splits:
       participant.net -= share

2. Separate into:
   creditors: net > 0.01  (owed money)
   debtors:   net < -0.01 (owe money)

3. Sort: creditors descending by net, debtors ascending by net (most negative first)

4. Greedy match:
   while creditors and debtors remain:
     transfer = min(creditor.net, |debtor.net|)
     print: "debtor pays creditor: transfer"
     creditor.net -= transfer
     debtor.net   += transfer
     if creditor.net < 0.01 → advance creditor pointer
     if debtor.net > -0.01  → advance debtor pointer
```

Produces ≤ N−1 transfers for N people with nonzero balances.

---

## Validation Rules

| Operation | Validations |
|---|---|
| `createGroup` | ≥ 2 members; all memberIds exist |
| `addMemberToGroup` | group exists; user exists; user not already a member |
| `addExpense` | group exists; paidBy is member; all participants are members; amount > 0 |
| `addExpense` (EXACT) | splitInput covers all participants; values sum to amount |
| `addExpense` (PERCENTAGE) | splitInput covers all participants; values sum to 100 |
| `settleUp` | group exists; both users are members; fromUser ≠ toUser; amount > 0 |

---

## Design Patterns

| Pattern | Where | Why |
|---|---|---|
| Strategy | `SplitStrategy` + implementations | Swap split algorithms without changing callers |
| Factory | `SplitStrategyFactory` | Centralized creation; single place to extend |
| Template Method | `Transaction` abstract class | Shared structure, subclass-specific behaviour |

---

## SOLID Principles

| Principle | How applied |
|---|---|
| **S**RP | `User`/`Group` hold state; `ExpenseService` orchestrates; strategies compute splits |
| **O**CP | New split type = new class + one factory line; zero existing changes |
| **L**SP | `Expense` and `Settlement` interchangeable as `Transaction` in lists |
| **I**SP | `SplitStrategy` has one focused method |
| **D**IP | `ExpenseService` depends on `SplitStrategy` interface, not concrete classes |

---

## Key Design Decisions

| Decision | Choice | Reason |
|---|---|---|
| Money type | `BigDecimal` | No IEEE 754 drift; `HALF_UP` rounding; never construct from `double` literal |
| Balance storage | On-the-fly from transactions | Single source of truth; simpler write path |
| Transaction hierarchy | Abstract class + subclasses | Avoids null fields and type-flag switch statements |
| Split input (service) | `Map<String, BigDecimal>` (userId keys) | Caller-friendly; service resolves to `User` before calling strategy |
| Split input (strategy) | `Map<User, BigDecimal>` (User keys) | Strategy works with domain objects, not IDs |
| `User.equals/hashCode` | Based on `id` | User is a Map key in `Expense.splits` — required for correctness |
| `getters` on `Group` | Return unmodifiable views | Prevents external mutation bypassing `addMember` validation |
| `createGroup` location | `ExpenseService` | Group can't store itself; service owns the registry |
| Minimum transfers scope | Expenses only (not settlements) | Shows what needs to happen to clear debts; settlements are already-done payments |

---

## Package Structure

```
src/
├── models/
│   ├── User.java
│   ├── Group.java
│   ├── Expense.java
│   ├── Settlement.java
│   ├── abstractions/
│   │   └── Transaction.java      (abstract)
│   └── enums/
│       ├── SplitType.java
│       └── TransactionType.java
├── strategy/
│   ├── SplitStrategy.java        (interface)
│   ├── EqualSplitStrategy.java
│   ├── ExactSplitStrategy.java
│   ├── PercentSplitStrategy.java
│   └── SplitStrategyFactory.java
└── service/
    └── ExpenseService.java
```
