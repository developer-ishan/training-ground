# Expense Sharing System — Final Design

## Class Diagram (Overview)

```
┌──────────────────────┐         ┌──────────────────────────────────┐
│        User          │         │         Group                    │
├──────────────────────┤         ├──────────────────────────────────┤
│ - id: String         │◄───────▶│ - id: String                    │
│ - name: String       │   *  *  │ - name: String                  │
│ - email: String      │         │ - members: List<User>           │
│ - phone: String      │         │ - transactions: List<Transaction>│
└──────────────────────┘         ├──────────────────────────────────┤
                                 │ + addMember(User)               │
                                 │ + removeMember(User)            │
                                 │ + addTransaction(Transaction)   │
                                 │ + isMember(User): boolean       │
                                 │ + getTransactions(): List       │
                                 │ + printMinimumTransfers(): void │
                                 └──────────────────────────────────┘

┌─────────────────────────────────────┐
│     Transaction (abstract)          │
├─────────────────────────────────────┤
│ - id: String                        │
│ - amount: double                    │
│ - description: String               │
│ - timestamp: LocalDateTime          │
├─────────────────────────────────────┤
│ + getId(): String                   │
│ + getAmount(): double               │
│ + getDescription(): String          │
│ + getTimestamp(): LocalDateTime     │
│ + getType(): TransactionType {abs}  │
└─────────────┬───────────────────────┘
              │
    ┌─────────┴───────────┐
    │                     │
┌───▼──────────────┐ ┌───▼──────────────┐
│    Expense       │ │   Settlement     │
├──────────────────┤ ├──────────────────┤
│ - paidBy: User   │ │ - fromUser: User │
│ - group: Group   │ │ - toUser: User   │
│ - splitType:     │ │                  │
│     SplitType    │ └──────────────────┘
│ - splits:        │
│   Map<User,Double>│
└──────────────────┘

┌─────────────────────────────────────────────────────┐
│          <<interface>> SplitStrategy                 │
├─────────────────────────────────────────────────────┤
│ + split(amount: double,                             │
│         participants: List<User>,                   │
│         splitInput: Map<User, Double>)              │
│     → Map<User, Double>                             │
│   (validates internally, throws on failure)         │
└──────────┬──────────────┬───────────────┬───────────┘
           │              │               │
    ┌──────▼───┐  ┌───────▼────┐  ┌───────▼──────┐
    │EqualSplit│  │ ExactSplit │  │PercentSplit  │
    └──────────┘  └────────────┘  └──────────────┘

┌─────────────────────────────────────────────────┐
│             ExpenseService                      │
├─────────────────────────────────────────────────┤
│ - users: Map<String, User>                      │
│ - groups: Map<String, Group>                    │
│ - strategyFactory: SplitStrategyFactory         │
├─────────────────────────────────────────────────┤
│  (full API listed below)                        │
│  Balances computed on the fly from group txns   │
└─────────────────────────────────────────────────┘
```

---

## 1. Entities

### 1.1 User

```java
public class User {
    private final String id;
    private final String name;
    private final String email;
    private final String phone;
}
```

- Immutable value object.
- `id` is unique, generated on creation (UUID).
- Equality based on `id`.

### 1.2 Group

```java
public class Group {
    private final String id;
    private final String name;
    private final List<User> members;
    private final List<Transaction> transactions;

    public void addMember(User user);
    public void removeMember(User user);
    public boolean isMember(User user);
    public void addTransaction(Transaction transaction);
    public List<Transaction> getTransactions();
    public void printMinimumTransfers();
}
```

- Owns its member list and its transaction history.
- Validation: minimum 2 members on creation.
- `transactions` stores both Expenses and Settlements (polymorphic list).
- `printMinimumTransfers()` computes and prints the minimum set of payments
  needed to settle all debts within the group (see Section 3.4).

### 1.3 TransactionType (Enum)

```java
public enum TransactionType {
    EXPENSE,
    SETTLEMENT
}
```

### 1.4 Transaction (Abstract)

```java
public abstract class Transaction {
    private final String id;
    private final double amount;
    private final String description;
    private final LocalDateTime timestamp;

    public abstract TransactionType getType();
}
```

Common base for all transaction types. Supports polymorphic storage in
`List<Transaction>` for unified history queries.

### 1.5 Expense (extends Transaction)

```java
public class Expense extends Transaction {
    private final User paidBy;
    private final Group group;
    private final SplitType splitType;
    private final Map<User, Double> splits;

    @Override
    public TransactionType getType() {
        return TransactionType.EXPENSE;
    }
}
```

| Field       | Description                                     |
| ----------- | ----------------------------------------------- |
| `paidBy`    | The single user who paid the full amount        |
| `group`     | The group this expense belongs to               |
| `splitType` | EQUAL / EXACT / PERCENTAGE                      |
| `splits`    | Per-participant share: what each person **owes** |

- `paidBy` is *not* excluded from `splits` — their entry represents their
  own share (which cancels out in balance updates).

### 1.6 Settlement (extends Transaction)

```java
public class Settlement extends Transaction {
    private final User fromUser;
    private final User toUser;

    @Override
    public TransactionType getType() {
        return TransactionType.SETTLEMENT;
    }
}
```

| Field      | Description                                       |
| ---------- | ------------------------------------------------- |
| `fromUser` | The person paying off debt (reducing what they owe)|
| `toUser`   | The person receiving payment                      |
| `amount`   | How much is being settled (inherited)              |

---

## 2. Split Strategies (Strategy Pattern)

### 2.1 SplitType (Enum)

```java
public enum SplitType {
    EQUAL,
    EXACT,
    PERCENTAGE
}
```

### 2.2 SplitStrategy (Interface)

```java
public interface SplitStrategy {

    Map<User, Double> split(
        double totalAmount,
        List<User> participants,
        Map<User, Double> splitInput
    );
}
```

- `splitInput` is a generic map that each strategy interprets differently.
- Each implementation validates internally at the start of `split()` and
  throws on failure. No separate `validate()` needed.

### 2.3 EqualSplitStrategy

```java
public class EqualSplitStrategy implements SplitStrategy {

    Map<User, Double> split(double totalAmount, List<User> participants, ...) {
        // Validate: participants must be non-empty
        // splitInput is ignored (can be null/empty)
        double perPerson = totalAmount / participants.size();
        // Handle rounding: assign remainder cents to first participant(s)
        // so that splits sum to exactly totalAmount
        return map of { participant → perPerson (adjusted) };
    }
}
```

**Rounding strategy**: `totalAmount = 100.0`, 3 participants →
33.34, 33.33, 33.33 (first person absorbs the extra cent).

### 2.4 ExactSplitStrategy

```java
public class ExactSplitStrategy implements SplitStrategy {

    Map<User, Double> split(double totalAmount, List<User> participants, Map<User, Double> splitInput) {
        // Validate: every participant has an entry, values sum to totalAmount
        return splitInput;  // amounts are already per-user
    }
}
```

### 2.5 PercentSplitStrategy

```java
public class PercentSplitStrategy implements SplitStrategy {

    Map<User, Double> split(double totalAmount, List<User> participants, Map<User, Double> splitInput) {
        // Validate: every participant has an entry, percentages sum to 100.0
        // For each participant: (percentage / 100) * totalAmount
        // Handle rounding similar to EqualSplit
        return map of { participant → computed amount };
    }
}
```

### 2.6 SplitStrategyFactory

```java
public class SplitStrategyFactory {

    private final Map<SplitType, SplitStrategy> strategies;

    public SplitStrategyFactory() {
        strategies = new EnumMap<>(SplitType.class);
        strategies.put(SplitType.EQUAL, new EqualSplitStrategy());
        strategies.put(SplitType.EXACT, new ExactSplitStrategy());
        strategies.put(SplitType.PERCENTAGE, new PercentSplitStrategy());
    }

    public SplitStrategy getStrategy(SplitType type) {
        SplitStrategy strategy = strategies.get(type);
        if (strategy == null) throw new IllegalArgumentException("Unknown split type: " + type);
        return strategy;
    }
}
```

**Extensibility**: To add a new split type (e.g., SHARES), add a new enum
value, implement `SplitStrategy`, register it in the factory. Zero changes
to existing classes (Open/Closed).

---

## 3. Balance Computation (On the Fly)

### 3.1 Approach

Balances are **not cached**. They are computed from the group's transaction
history every time they are queried. Each `Group.transactions` list is the
source of truth for that group.

**Trade-off**:
- Pros: Single source of truth, no sync issues, simpler write path.
- Cons: O(T) per query where T = transactions in that group.
- Optimization (future): If reads become a bottleneck, add a cached
  balance map updated on each write.

### 3.2 Print Balances for a User in a Group

Computes the net balance between the given user and every other member
within a specific group, using that group's transactions (expenses +
settlements). Then prints what the user owes and what they are owed.

```java
void printBalances(String userId, String groupId) {
    Group group = getGroup(groupId);
    User user = getUser(userId);
    Map<String, Double> netByUser = new HashMap<>();

    for (Transaction t : group.getTransactions()) {
        if (t instanceof Expense expense) {
            if (expense.getPaidBy().getId().equals(userId)) {
                for (Map.Entry<User, Double> entry : expense.getSplits().entrySet()) {
                    if (!entry.getKey().getId().equals(userId)) {
                        netByUser.merge(entry.getKey().getId(), entry.getValue(), Double::sum);
                    }
                }
            } else if (expense.getSplits().containsKey(user)) {
                double owes = expense.getSplits().get(user);
                netByUser.merge(expense.getPaidBy().getId(), -owes, Double::sum);
            }
        }

        if (t instanceof Settlement settlement) {
            if (settlement.getFromUser().getId().equals(userId)) {
                // I settled debt with toUser → positive shift
                netByUser.merge(settlement.getToUser().getId(),
                                settlement.getAmount(), Double::sum);
            }
            if (settlement.getToUser().getId().equals(userId)) {
                // someone settled debt with me → negative shift
                netByUser.merge(settlement.getFromUser().getId(),
                                -settlement.getAmount(), Double::sum);
            }
        }
    }

    System.out.println("=== Balances for " + user.getName()
                       + " in group: " + group.getName() + " ===");

    boolean hasEntries = false;
    for (Map.Entry<String, Double> entry : netByUser.entrySet()) {
        double net = entry.getValue();
        String otherName = getUser(entry.getKey()).getName();
        if (net < -0.01) {
            System.out.printf("You owe %s: %.2f%n", otherName, -net);
            hasEntries = true;
        } else if (net > 0.01) {
            System.out.printf("%s owes you: %.2f%n", otherName, net);
            hasEntries = true;
        }
    }

    if (!hasEntries) {
        System.out.println("All settled in this group!");
    }
}
```

**Example**:

```
=== Balances for Bob in group: Goa Trip ===
You owe Alice: 75.00
Dave owes you: 15.00
```

### 3.3 Minimum Transfers to Settle a Group

**Goal**: Given all expenses in a group, print the minimum set of payments
so that all debts are cleared.

**Algorithm (Greedy)**:

```
Step 1: Compute net balance per member from group transactions
Step 2: Separate into creditors (net > 0, owed money) and debtors (net < 0, owe money)
Step 3: Sort both lists by amount (descending for creditors, ascending for debtors)
Step 4: Match largest creditor with largest debtor, settle min of their amounts, repeat
```

**Why greedy works here**: At each step we fully settle at least one person
(the one with the smaller absolute balance), so we use at most N-1 transfers
where N is the number of people with nonzero balances. This is optimal for
most practical cases.

**Step 1 — Compute net balances from group transactions**:

For each expense in the group:
- `paidBy.net += amount` (they fronted the money)
- For each participant: `participant.net -= splits[participant]` (they consumed)
- Net effect for paidBy: `+amount - splits[paidBy]` (positive if they paid more than their share)
- Net effect for others: `-splits[P]` (they owe their share)

Note: Group transactions only contain Expenses (settlements are system-wide
and not stored on the group). This is intentional — `printMinimumTransfers`
shows what *needs* to happen to settle the group, not what has already been
settled.

**Full implementation**:

```java
public void printMinimumTransfers() {
    // Step 1: Compute net balance per member
    Map<User, Double> netBalance = new HashMap<>();
    for (Transaction t : transactions) {
        if (t instanceof Expense expense) {
            netBalance.merge(expense.getPaidBy(), expense.getAmount(), Double::sum);
            for (Map.Entry<User, Double> entry : expense.getSplits().entrySet()) {
                netBalance.merge(entry.getKey(), -entry.getValue(), Double::sum);
            }
        }
    }

    // Step 2: Separate into creditors and debtors
    // creditor = net > 0 (they are owed money)
    // debtor   = net < 0 (they owe money)
    List<Map.Entry<User, Double>> creditors = new ArrayList<>();
    List<Map.Entry<User, Double>> debtors = new ArrayList<>();

    for (Map.Entry<User, Double> entry : netBalance.entrySet()) {
        if (entry.getValue() > 0.01) {
            creditors.add(entry);
        } else if (entry.getValue() < -0.01) {
            debtors.add(entry);
        }
    }

    // Step 3: Sort — largest creditor first, largest debtor (most negative) first
    creditors.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
    debtors.sort((a, b) -> Double.compare(a.getValue(), b.getValue()));

    // Step 4: Greedy matching
    int i = 0, j = 0;
    System.out.println("=== Minimum transfers to settle group: " + name + " ===");

    while (i < creditors.size() && j < debtors.size()) {
        User creditor = creditors.get(i).getKey();
        double owed = creditors.get(i).getValue();

        User debtor = debtors.get(j).getKey();
        double owes = -debtors.get(j).getValue();  // make positive

        double transfer = Math.min(owed, owes);

        System.out.printf("%s pays %s: %.2f%n", debtor.getName(), creditor.getName(), transfer);

        creditors.get(i).setValue(owed - transfer);
        debtors.get(j).setValue(debtors.get(j).getValue() + transfer);

        if (creditors.get(i).getValue() < 0.01) i++;
        if (debtors.get(j).getValue() > -0.01) j++;
    }

    if (i == 0 && j == 0) {
        System.out.println("All settled! No transfers needed.");
    }
}
```

**Example walkthrough**:

```
Group: Goa Trip (Alice, Bob, Charlie, Dave)

Expenses:
  1. Alice paid 400, split equally → each owes 100
  2. Bob paid 200, split equally → each owes 50

Net balances:
  Alice: +400 - 100 - 50 = +250  (owed 250)
  Bob:   +200 - 100 - 50 = +50   (owed 50)
  Charlie:    - 100 - 50 = -150  (owes 150)
  Dave:       - 100 - 50 = -150  (owes 150)

Greedy matching:
  Creditors (sorted): Alice (+250), Bob (+50)
  Debtors (sorted):   Charlie (-150), Dave (-150)

  Round 1: Charlie pays Alice min(250, 150) = 150  → Alice now +100, Charlie settled
  Round 2: Dave pays Alice min(100, 150) = 100     → Alice settled, Dave now -50
  Round 3: Dave pays Bob min(50, 50) = 50           → Both settled

Output:
  === Minimum transfers to settle group: Goa Trip ===
  Charlie pays Alice: 150.00
  Dave pays Alice: 100.00
  Dave pays Bob: 50.00
```

3 transfers to settle 4 people — optimal.

---

## 4. ExpenseService — Public API

```java
public class ExpenseService {

    private final Map<String, User> users;
    private final Map<String, Group> groups;
    private final SplitStrategyFactory strategyFactory;
```

### 4.1 User Management

```java
    User addUser(String name, String email, String phone);
    // → Creates user with generated UUID, stores in users map, returns User.

    User getUser(String userId);
    // → Looks up user by ID. Throws if not found.
```

### 4.2 Group Management

```java
    Group createGroup(String name, List<String> memberUserIds);
    // → Validates: at least 2 members, all userIds exist.
    // → Creates Group, stores in groups map, returns Group.

    void addMemberToGroup(String groupId, String userId);
    // → Validates: group exists, user exists, user not already a member.
    // → Adds user to group's member list.
```

### 4.3 Add Expense (Core)

```java
    Expense addExpense(
        String groupId,
        String paidByUserId,
        double amount,
        List<String> participantUserIds,
        SplitType splitType,
        Map<String, Double> splitInput
    );
```

**Data flow**:

```
addExpense("group-1", "alice", 100.0, ["alice","bob","charlie","dave"], EQUAL, {})
│
├─ 1. VALIDATE
│     ├─ group-1 exists
│     ├─ alice is a member of group-1
│     ├─ bob, charlie, dave are all members of group-1
│     └─ amount > 0
│
├─ 2. COMPUTE SPLIT (validates internally, throws on bad input)
│     └─ strategy.split(100.0, [alice,bob,charlie,dave], {})
│        → { alice: 25.0, bob: 25.0, charlie: 25.0, dave: 25.0 }
│
├─ 3. CREATE & STORE
│     ├─ Create Expense object with paidBy, amount, splits, group, timestamp
│     └─ group.addTransaction(expense)
│
└─ 4. RETURN expense
```

### 4.4 Settle Up (Core)

```java
    Settlement settleUp(
        String groupId,
        String fromUserId,
        String toUserId,
        double amount
    );
```

**Data flow**:

```
settleUp("group-1", "bob", "alice", 25.0)
│
├─ 1. VALIDATE
│     ├─ group exists
│     ├─ fromUser (bob) exists and is a group member
│     ├─ toUser (alice) exists and is a group member
│     ├─ fromUser ≠ toUser
│     └─ amount > 0
│
├─ 2. CREATE & STORE
│     ├─ Create Settlement object with fromUser, toUser, amount, timestamp
│     └─ group.addTransaction(settlement)
│
└─ 3. RETURN settlement
```

### 4.5 Balance Query

```java
    void printBalances(String userId, String groupId);
    // → Group-scoped. Iterates that group's expense transactions.
    //   Prints who the user owes and who owes the user within that group.
    //   See Section 3.2 for full logic.
```

### 4.6 History Queries

```java
    List<Transaction> getGroupHistory(String groupId);
    // → Returns group.getTransactions() (expenses + settlements in that group)
}
```

---

## 5. Design Patterns Used

| Pattern    | Where                       | Why                                          |
| ---------- | --------------------------- | -------------------------------------------- |
| Strategy   | SplitStrategy interface     | Swap split algorithms without changing caller |
| Factory    | SplitStrategyFactory        | Centralized strategy creation, easy to extend |
| Template   | Transaction abstract class  | Common structure, subclass-specific behavior  |

---

## 6. SOLID Principles Applied

| Principle                 | How                                                            |
| ------------------------- | -------------------------------------------------------------- |
| **S**ingle Responsibility | User holds user data. Group holds group data. Service orchestrates. |
| **O**pen/Closed           | New split strategy = new class + factory registration. No existing code changes. |
| **L**iskov Substitution   | Expense and Settlement are interchangeable as Transaction in lists/history. |
| **I**nterface Segregation | SplitStrategy has a focused interface — split + validate only. |
| **D**ependency Inversion  | Service depends on SplitStrategy interface, not concrete implementations. |

---

## 7. Key Validations

| Operation        | Validations                                                           |
| ---------------- | --------------------------------------------------------------------- |
| Create Group     | ≥ 2 members, all user IDs valid                                       |
| Add Expense      | Group exists, paidBy is member, all participants are members          |
| Add Expense      | Amount > 0                                                            |
| Add Expense      | Strategy-specific: EXACT sums to total, PERCENTAGE sums to 100%      |
| Settle Up        | Both users exist, fromUser ≠ toUser, amount > 0                      |

---

## 8. Extensibility Points

| Future Requirement      | What to Change                                                 |
| ----------------------- | -------------------------------------------------------------- |
| New split type (SHARES) | New SplitStrategy impl + register in factory                   |
| Non-group expenses      | Make `group` optional on Expense (nullable or use Optional)    |
| Multiple payers         | Change `paidBy: User` to `paidBy: Map<User, Double>`          |
| New transaction type    | New Transaction subclass + new TransactionType enum value      |
| Cached balance sheet    | Add Map<UserId, Map<UserId, Double>> updated on each write    |
| Persistence             | Swap in-memory maps for repository interfaces (DAO pattern)   |
| Notifications           | Add observer/listener on ExpenseService methods                |

---

## 9. File/Package Structure (for implementation)

```
src/
├── model/
│   ├── User.java
│   ├── Group.java
│   ├── Transaction.java          (abstract)
│   ├── Expense.java              (extends Transaction)
│   ├── Settlement.java           (extends Transaction)
│   ├── TransactionType.java      (enum)
│   └── SplitType.java            (enum)
├── strategy/
│   ├── SplitStrategy.java        (interface)
│   ├── EqualSplitStrategy.java
│   ├── ExactSplitStrategy.java
│   ├── PercentSplitStrategy.java
│   └── SplitStrategyFactory.java
├── service/
│   └── ExpenseService.java
└── Main.java                     (demo / driver)
```
