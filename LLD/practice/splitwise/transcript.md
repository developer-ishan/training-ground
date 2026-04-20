# LLD Interview: Expense Sharing App (Splitwise)
**Date:** 2026-04-20

---

## Interviewer:
Hi! Thanks for joining today. Let's do a Low Level Design problem.

We'd like you to **design an expense sharing application like Splitwise.**

Take a moment to think, and then walk me through:
1. What clarifying questions do you have before diving in?

*(Take your time — good questions matter here.)*

---

## Candidate:
What are the various functional and non-functional requirements?

---

## Interviewer:

Good question to start with. Here's what we're targeting:

### Functional Requirements (In Scope)
- Users can **add expenses** and split them among a group of people
- Support multiple split types: **equal, exact amount, percentage**
- Users can **view balances** — how much they owe or are owed (per person or overall)
- Users can **settle up** — record a payment between two people
- Support **group expenses** (e.g., a trip) as well as **one-on-one expenses**
- Users can view their **expense history**

### Out of Scope (for this session)
- Actual payment processing / bank integrations
- Notifications / reminders
- Currency conversion
- Authentication & Authorization details

### Non-Functional Requirements
- The design should be **extensible** — easy to add new split strategies
- Code should follow **OOP principles** — clean class design, separation of concerns
- Correctness: balances must always be **consistent** (no floating point drift)

---

## Interviewer:
Now that you have the requirements, walk me through your **core entities/classes**. What are the main objects you'd model in this system?

---

## Candidate:

User(id, name)

Group(id, name, List<User> users, List<Transaction> transactions)
- addUser(User user)
- recordExpense(SplitStrategy strategy, User payedBy, Double amount, List<User> users)
- settleUp(Double amount, User from, User to)
- showBalances()

interface SplitStrategy
- Transaction split(User payedBy, Double amount, List<User> users)

ExactAmountSplitStrategy, EqualAmountSplitStrategy, PercentageSplitStrategy implements SplitStrategy
- Transaction split(User payedBy, Double amount, List<User> users)

Transaction(amount, payedBy, users)
- Enum Type(Expense or Settlement)

---

## Interviewer:

Good start — Strategy pattern is the right instinct, and transactions on Group is reasonable. But I have several pointed issues:

**Issue 1:** `SplitStrategy.split()` returns a `Transaction` — SRP violation. Strategy should only compute the split, not create domain objects. What should it return instead?

**Issue 2:** Your `split()` signature has no way to pass exact amounts or percentages per user for ExactSplit/PercentageSplit. How would you fix the signature?

**Issue 3:** `Transaction` with an enum type flag — `Settlement` is between two users, `Expense` has a payer + participant shares. These are structurally different. How would you redesign using inheritance?

**Issue 4:** `Group` does member management, business logic, AND presentation (`showBalances`). God Class — what layer would you introduce?

**Issue 5:** You said no floating point drift in NFRs, yet you're using `Double` everywhere. What's the problem and what would you use instead?

Take them one at a time.

---

## Candidate:

Issue 1: Solution is to return Map<User, Double> split.

---

## Interviewer:

Correct. `Map<User, Double>` gives each participant mapped to their share. Transaction creation belongs to the service layer, not the strategy.

This connects to Issue 2 — `EqualSplit` ignores extra input, but `ExactSplit` needs per-user amounts and `PercentageSplit` needs percentages. How do you handle that in a single interface?

---

## Candidate:

I don't know — please explain.

---

## Interviewer:

Fair. Here are three approaches and why one wins:

### Option A: Separate methods per strategy
```java
interface SplitStrategy {
    Map<User, Double> splitEqual(double amount, List<User> users);
    Map<User, Double> splitExact(Map<User, Double> exactAmounts);
    Map<User, Double> splitByPercent(double amount, Map<User, Double> percentages);
}
```
**Problem:** Every implementation must implement all methods. Breaks Interface Segregation and defeats polymorphism entirely.

---

### Option B: Single unified signature with a generic extra param ✅
```java
interface SplitStrategy {
    Map<User, Double> split(double amount, List<User> users, Map<User, Double> splitInput);
}
```
`splitInput` is null/empty for Equal, exact amounts for Exact, percentages for Percentage.
Each strategy interprets it differently and **validates its own input internally**.

```
EqualSplit   → ignores splitInput
ExactSplit   → validates splitInput values sum to amount, returns splitInput
PercentSplit → validates splitInput values sum to 100, computes (pct/100 * amount) per user
```

**This is the right answer.** One clean interface, each strategy owns its validation, zero changes needed to add a new strategy — pure Open/Closed.

---

### Option C: Subclassed request objects
Wrap inputs in a `SplitRequest` hierarchy. More type-safe but overkill for 3 strategies — adds complexity without real benefit.

---

**Key takeaway:** The generic `Map<User, Double> splitInput` is the pragmatic sweet spot. Each strategy validates what it needs and ignores the rest.

---

Now take a shot at **Issue 3**: how would you redesign `Transaction` using inheritance instead of an enum type flag?

---

## Candidate:

Issue 3. Split into abstract class and 2 classes (Expense and Settlement)

---

## Interviewer:

Good instinct — that's the right direction. Now push deeper:

> What fields go on the abstract `Transaction` base vs. each subclass? Walk me through all three classes. And what abstract method, if any, would you put on `Transaction`?

Think about what `Expense` and `Settlement` share vs. what differs structurally.

---

## Candidate:

Expense can be between multiple people whereas Settlement is always between 2 people.

---

## Interviewer:

Correct observation — but you haven't answered the actual question. I asked for **fields on each class**.

> What does `abstract Transaction` hold? What does `Expense` add? What does `Settlement` add? And what abstract method ties them together?

Be specific — write out the fields like you did in your initial design.

---

## Candidate:

Transaction holds amount and paidBy, rest goes into individual implementation classes.

---

## Interviewer:

Two issues with this:

**1. `paidBy` doesn't belong on the base class.**
A `Settlement` doesn't have a single "payer" — it has a `fromUser` and `toUser`. Putting `paidBy` on `Transaction` means `Settlement` inherits a field that's semantically wrong for it. That's exactly the kind of misuse inheritance should prevent.

> What fields are truly shared between *both* Expense and Settlement?

**2. You're missing obvious shared fields.**
`amount` is right — but what else does every transaction need regardless of type?

**3. You haven't mentioned an abstract method.**
What method would you force every subclass to implement — something that lets a caller identify the type polymorphically without using `instanceof`?

Take another pass at all three.

---

## Candidate:

Please explain.

---

## Interviewer:

Here's the clean design:

### Abstract base — `Transaction`
Fields shared by **every** transaction regardless of type:
```java
abstract class Transaction {
    private final String id;          // unique ID (UUID)
    private final double amount;      // total amount involved
    private final String description; // human-readable note
    private final LocalDateTime timestamp;

    public abstract TransactionType getType(); // force subclasses to identify themselves
}
```
`id` and `timestamp` belong here because every transaction needs them. `paidBy` does NOT belong here — it only makes sense for `Expense`.

---

### `Expense extends Transaction`
```java
class Expense extends Transaction {
    private final User paidBy;              // who fronted the money
    private final Map<User, Double> splits; // each participant's share
    private final SplitType splitType;

    @Override
    public TransactionType getType() { return TransactionType.EXPENSE; }
}
```

---

### `Settlement extends Transaction`
```java
class Settlement extends Transaction {
    private final User fromUser; // who is paying off debt
    private final User toUser;   // who is receiving payment

    @Override
    public TransactionType getType() { return TransactionType.SETTLEMENT; }
}
```

---

### Why `getType()` as an abstract method?
It lets you store both in a `List<Transaction>` and identify them without `instanceof`:
```java
for (Transaction t : transactions) {
    if (t.getType() == TransactionType.EXPENSE) { ... }
}
```
Though in practice you'll still cast after the check — `instanceof` with pattern matching (Java 16+) is also acceptable.

---

**Key rule:** Only put a field on the base class if *every* subclass needs it with the same semantic meaning. `amount` — yes. `timestamp` — yes. `paidBy` — no, it only makes sense for Expense.

---

Moving on — **Issue 4:** `Group` is doing too much. What layer would you introduce to fix the God Class problem?

---

## Candidate:

Introduce a service layer.

---

## Interviewer:

Correct — but that's vague. Push further:

> What **specifically** moves to the service, and what **stays** on `Group`? Name the service class, its key methods, and what `Group` is left with after the refactor.

---

## Candidate:

Methods which can be made static will be moved to service layer. Creation of group and adding members stays, rest is moved out.

---

## Interviewer:

"Can be made static" is the wrong criteria — that's an implementation detail, not a design principle. A method belongs in a service because it **orchestrates across multiple objects or requires external state**, not because it's stateless.

> `recordExpense` and `settleUp` are on `Group` right now — do they stay or move? And `createGroup` — does that belong inside `Group` itself, or in the service?

---

## Candidate:

`recordExpense` and `settleUp` move out since they need User as well as transactions.

---

## Interviewer:

Closer — but the reasoning is off. `recordExpense` moves out not because it "needs User and transactions" (Group already has both), but because it needs to **look up users and groups by ID from a registry**. Group has no way to do that — it doesn't know about other groups or the global user store.

Two follow-ups:

> **1.** You said "creation of group stays on Group" — do you still stand by that? Who creates the Group object and where does it get stored so other parts of the system can look it up?

> **2.** After the refactor, what methods are left on `Group`?

---

## Candidate:

Please explain.

---

## Interviewer:

Here's the clean split:

### What stays on `Group` — pure domain state only
```java
class Group {
    private final String id;
    private final String name;
    private final List<User> members;
    private final List<Transaction> transactions;

    void addMember(User user);
    boolean isMember(User user);
    void addTransaction(Transaction t);
    List<Transaction> getTransactions();
    List<User> getMembers();
}
```
No business logic. No orchestration. No display. Just a container with basic state mutations.

### What moves to `ExpenseService` — orchestration + registries
```java
class ExpenseService {
    private final Map<String, User> users;    // global user registry
    private final Map<String, Group> groups;  // global group registry
    private final SplitStrategyFactory factory;

    User addUser(String name, String email);
    Group createGroup(String name, List<String> memberIds); // creates + stores in map
    void addMemberToGroup(String groupId, String userId);
    Expense addExpense(String groupId, String paidByUserId, double amount,
                       List<String> participantIds, SplitType splitType,
                       Map<String, Double> splitInput);
    Settlement settleUp(String groupId, String fromUserId, String toUserId, double amount);
    void printBalances(String userId, String groupId);
    List<Transaction> getGroupHistory(String groupId);
}
```

### Why `createGroup` belongs in the service, not `Group`
`Group` can't store itself — it has no access to the registry. The service creates the `Group` object, validates all member IDs exist, and stores it in `groups` map.

### The rule
> A method belongs on the **entity** if it only touches that entity's own state.
> It belongs in the **service** if it needs to look something up, validate across objects, or coordinate multiple entities.

---

One issue left — **Issue 5:** `Double` for money. What's the problem and what would you use instead?

---

## Candidate:

A Money class e.g. Joda Money can be used.

---

## Interviewer:

Good — that's a valid answer. Here's what you should be able to articulate:

**Why `Double` is wrong for money:**
`0.1 + 0.2 == 0.30000000000000004` in IEEE 754 floating point. Split 100 three ways with `double` and you get 33.333... — now your splits don't sum back to exactly 100. Over many transactions, errors compound. You explicitly said "no floating point drift" in NFRs.

**Two acceptable approaches:**
1. **`BigDecimal`** — built into Java, arbitrary precision, no drift. Use `HALF_UP` rounding explicitly. Simple and sufficient for LLD.
2. **Joda Money / `javax.money`** — wraps `BigDecimal` with currency awareness. Better for production, slight overkill for an LLD interview but shows awareness.

**Also acceptable:** store amounts as `long` in paise/cents (multiply by 100), do all arithmetic in integers, display by dividing. Zero floating point risk.

**In an interview context:** mentioning `BigDecimal` with explicit rounding is enough. Joda Money is a bonus point.

---

## Candidate:

Explain difference between BigDecimal and Double.

---

## Interviewer:

### Double
- 64-bit IEEE 754 **binary** floating point
- Cannot represent most decimal fractions exactly — `0.1` is actually `0.100000000000000005551...` in memory
- Fast (hardware-native), accumulates rounding errors
- `0.1 + 0.2 = 0.30000000000000004`

### BigDecimal
- Stores numbers as an **arbitrary-precision integer + a decimal scale**
- `0.1` stored exactly as `1 × 10⁻¹` — no binary approximation
- Slower (software arithmetic), but correct
- Requires explicit rounding mode (`HALF_UP`, `HALF_EVEN`) when dividing

| | `double` | `BigDecimal` |
|---|---|---|
| `0.1 + 0.2` | `0.30000000000000004` | `0.3` |
| Speed | Fast (hardware) | Slower (software) |
| Use for money | ❌ | ✅ |
| Rounding control | None | Explicit (`RoundingMode`) |

### Critical gotcha
```java
new BigDecimal(0.1)    // ❌ still wrong — passes imprecise double first
new BigDecimal("0.1")  // ✅ correct — parsed from string
BigDecimal.valueOf(0.1) // ✅ correct — uses string conversion internally
```
Always construct from a `String` or `BigDecimal.valueOf()`, never from a `double` literal.

---

## Candidate:

