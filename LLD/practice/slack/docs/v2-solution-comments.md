# V2 Solution — Review & Comments

## Improvements from V1

1. **`getBalance(userA, userB)` on Group** — Great addition. You've acknowledged
   that balance tracking is needed and scoped it to the group level.

2. **`SplitStrategy.getSplit()` simplified** — Removed `lenders` from the
   signature. Better — the strategy shouldn't care about who paid.

3. **`Map<User, amount>` instead of `List<Map<...>>`** — Cleaner data structure
   for `lenders` and `split` on Expense. Good fix.

4. **`Splitwise` service class** — You've added an orchestrator that owns
   `users` and `groups`. This is the right direction — you need a top-level
   entry point.

---

## Still Needs Work

### 1. `SplitStrategy.getSplit(List<User> participants)` — Where's the amount?

The strategy needs to know the **total amount** to compute splits.

- EQUAL: needs `totalAmount` + `participants` → divide evenly.
- EXACT: needs `participants` + their **explicit amounts** → validate sum.
- PERCENTAGE: needs `totalAmount` + `participants` + their **percentages** → compute.

Your current signature has no way to pass the amount or the per-user
inputs (exact amounts / percentages). Think about:

- Should `totalAmount` be a parameter?
- For EXACT and PERCENTAGE, you need per-user data. How do you pass
  `Map<User, Double> percentages` or `Map<User, Double> exactAmounts`
  through one interface?

**Hint**: One clean approach is to pass a generic `Map<User, Double>` as
split input. For EQUAL it can be empty/null (just divide evenly). For EXACT
it's the per-user amounts. For PERCENTAGE it's the per-user percentages.
The strategy interprets it based on its type.

### 2. Transaction vs Expense — Still unclear

You wrote:
- `class Transaction` — "uniform abstraction over expense"
- `class SettleUp extends Transaction`
- `Expense` — separate entity with `lenders` and `split`

Questions:
- Does `Expense` extend `Transaction`? Or does `Transaction` *contain*
  an `Expense`?
- If `Transaction` is the "uniform abstraction", what fields does it have?
  Think about what's **common** between an Expense and a SettleUp:
  - `id`, `amount`, `timestamp`, `type` — these are shared.
- And what's **specific**:
  - Expense: `paidBy`, `participants`, `splitStrategy`, `splitDetails`
  - SettleUp: `fromUser`, `toUser`

**Suggestion**: Make this explicit. In an interview, the interviewer will
ask "walk me through the Transaction class hierarchy" and you need a
crisp answer.

### 3. `lenders` on Expense — Still the wrong name

You kept `Map<User, amount> lenders`. "Lender" implies someone who loans
money — that's confusing. Clearer options:
- `paidBy` (User) + `totalAmount` (double) — for single payer (our scope)
- `payers` / `contributions` — if you want to future-proof for multi-payer

Since we scoped to single payer, the simplest model is just:
```
User paidBy
double totalAmount
```

The `Map<User, amount> split` (what each participant owes) is correct and
useful.

### 4. `getBalance(userA, userB)` — What backs it?

You added the method, but what **data structure** supports it? This is
the key implementation question an interviewer will drill into.

Option A — **Compute from history**: iterate all transactions, sum up.
Clean but slow for frequent queries.

Option B — **Running balance map**: maintain a `Map<UserPair, Double>`
that updates on every transaction. Fast queries, slightly more complex updates.

Which would you pick and why? Where does this map live — inside Group?
In the Splitwise service? Think about the trade-off.

### 5. `Splitwise` — Needs methods

You've listed the data it holds (`users`, `groups`), but what **operations**
does it expose? This is your **public API** — the most important thing in
an LLD interview. Think about:

```
addUser(name, email, phone) → User
createGroup(name, List<User> members) → Group
addExpense(groupId, paidBy, amount, participants, splitStrategy, splitInput) → Transaction
settleUp(userId, owedToUserId, amount) → Transaction
getBalances(userId) → Map<User, Double>
getGroupBalances(groupId) → Map<UserPair, Double>
getHistory(userId) → List<Transaction>
getGroupHistory(groupId) → List<Transaction>
```

Listing these methods forces you to think about the **data flow** end to end.

### 6. Group — `admins` and `removeGroup` (repeated from V1)

These are still here. Not a big deal, but in an interview setting:
- `admins` is out of scope — remove it or explicitly say "future extension".
- `removeGroup` should be `removeUser(User)`.

---

## Updated Scoreboard

| Aspect                  | V1 Status           | V2 Status              |
| ----------------------- | ------------------- | ---------------------- |
| Core entities           | Good                | Good                   |
| Strategy pattern        | Good                | Good (signature needs work) |
| Transaction hierarchy   | Good direction      | Still ambiguous        |
| Data structures         | Needs refinement    | Improved               |
| Balance tracking        | Missing             | Method added, backing unclear |
| Service/orchestration   | Missing             | Class added, methods missing |
| Method signatures       | Need clarification  | Strategy still incomplete |
| Scope discipline        | Minor (admins)      | Still there             |

**Progress**: You're moving in the right direction. The two biggest gaps
for V3 are:

1. **Nail the Transaction hierarchy** — draw it out. What's abstract,
   what's concrete, what fields go where.
2. **Write out the Splitwise service API** — the full method list with
   signatures. This is what the interviewer will evaluate you on.

---

## What to Focus on for V3

1. Flesh out `Transaction` — common fields + subclass-specific fields.
2. Clarify `Expense` — is it a Transaction subclass or a separate data object?
3. Fix `SplitStrategy` signature — include amount and per-user input.
4. Define what data structure backs `getBalance()`.
5. Write full method signatures for `Splitwise` service.

Once you have V3 solid, we move to **actual code**.
