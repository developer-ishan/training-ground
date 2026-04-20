# V1 Solution — Review & Comments

## What You Got Right

1. **Core entities identified** — User, Group, Transaction, Expense, SplitStrategy
   are all the right building blocks. Good instinct.

2. **Strategy pattern for splits** — Pulling out `SplitStrategy` as an interface
   is exactly the right call. This is the textbook application of the Strategy
   pattern and satisfies Open/Closed for new split types.

3. **Transaction hierarchy** — Making `Transaction` abstract with
   `ExpenseTransaction` and `SettleUp` as subtypes shows good thinking about
   polymorphism and extensibility.

4. **Group owns transactions** — Putting `List<Transaction>` inside Group
   is a reasonable starting point for group-scoped history.

---

## Things to Reconsider / Clarify

### 1. `Expense` vs `ExpenseTransaction` — What's the relationship?

You have both `ExpenseTransaction extends Transaction` and a separate `Expense`
entity. Ask yourself:

- Is `Expense` the domain data (who paid, what for, how much) and
  `ExpenseTransaction` the wrapper that makes it a `Transaction`?
- Or are they the same thing?

**Think about it this way**: A Transaction is something that happened (an event).
An Expense contains the details of *what* happened. How do these two relate?
Does `ExpenseTransaction` *contain* an `Expense`, or *is it* the expense?

### 2. `lenders` — Naming and structure

You wrote: `List<Map<User, amount>> lenders`

- The word "lender" is a bit confusing here. The person who **paid** is
  typically called the **payer**. The people who owe money are the
  **participants** or **borrowers**.
- `List<Map<User, amount>>` — why a List of Maps? Each Map would have one
  entry? This is really just a `Map<User, Double>`. Think about what data
  structure naturally represents "user X paid amount Y".
- For single payer (our current scope), do you even need a map? A simple
  `User paidBy` + `double totalAmount` might be cleaner. The map structure
  could be a future extension for multi-payer.

### 3. `SplitStrategy.getSplit()` — Signature needs refinement

Your signature: `Map<User, amount> getSplit(List<User> participants, List<Map<User, amount>> lenders)`

Questions to think about:
- Does the strategy need to know about lenders/payers? The split strategy's
  job is: *given a total amount and participants, tell me how much each person
  owes*. The payer info is used elsewhere (to update balances), not inside
  the strategy.
- A cleaner signature might be:
  `Map<User, Double> split(double totalAmount, List<User> participants, ...)`
- For EXACT and PERCENTAGE, the strategy needs extra input (the exact amounts
  or percentages). How would you pass that in? Think about what each strategy
  *specifically* needs as input.

### 4. Where do **balances** live?

This is a missing piece. Your entities don't show where balances are tracked.
Think about:
- Who owes whom — this is a **pairwise** relationship.
- Where does `getBalance(userA, userB)` get its data from?
- Should balances be computed on-the-fly from transaction history, or
  maintained as a running tally that updates with each transaction?
- Consider: `Map<(UserA, UserB), Double>` — what class owns this?

### 5. Group — `admins` and `removeGroup`

- `admins` — is this in scope? We didn't discuss admin roles. It's not wrong,
  but in an interview, adding things outside agreed scope can signal you're
  not managing scope well. Keep it if you want, but know why.
- `removeGroup` on Group — this reads like the group removes itself? Did you
  mean `removeUser(User)`?

### 6. Missing: A Service / Manager Layer

You've identified the data entities well, but who **orchestrates** the
operations? In an interview, you'd typically have:

- **ExpenseService** or **ExpenseManager** — the entry point that handles
  `addExpense(group, payer, amount, participants, strategy)`, computes
  splits, updates balances, records transactions.
- This keeps your entities clean (they hold data) and your service handles
  business logic + coordination.

Think about: who calls `splitStrategy.getSplit()`? Who updates the balances
after a split is computed? That's your service layer.

---

## Key Questions For Your Next Iteration

Before writing V2, think through these:

1. **Expense vs ExpenseTransaction** — one class or two? What does each own?
2. **Where do balances live?** — Dedicated class? Inside a service? A map
   somewhere? What's the key structure?
3. **SplitStrategy input** — EQUAL just needs amount + participants. EXACT
   needs per-user amounts. PERCENTAGE needs per-user percentages. How do you
   design one interface that handles all three cleanly?
4. **Who orchestrates?** — Identify your service/manager class and what
   methods it exposes. This is your "API" in an interview.
5. **SettleUp details** — What fields does a settlement have? (fromUser,
   toUser, amount, timestamp?) How does it update balances?

---

## Quick Scoreboard

| Aspect                  | Status                     |
| ----------------------- | -------------------------- |
| Core entities           | Good                       |
| Strategy pattern        | Good                       |
| Transaction hierarchy   | Good direction              |
| Data structures         | Needs refinement           |
| Balance tracking        | Missing                    |
| Service/orchestration   | Missing                    |
| Method signatures       | Need clarification         |
| Scope discipline        | Minor (admins)             |

**Overall**: Solid foundation — you've identified the right abstractions. The
gaps are around *data flow* (how does an expense turn into balance updates?)
and *orchestration* (who drives the process?). That's normal for a first pass.

---

Take a crack at V2 when you're ready, focusing on the questions above. I'll
review it the same way.
