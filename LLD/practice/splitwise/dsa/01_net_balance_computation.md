# Net Balance Computation

## Problem

You are given a list of expense transactions between users in a group.
Each transaction is either:

- **Expense**: one user (`paidBy`) paid a total amount, shared among a set of participants with individual share amounts.
- **Settlement**: one user (`from`) paid another user (`to`) a given amount to reduce debt.

Given a `targetUser`, compute the net balance between them and every other user:

- Positive balance `+X` for user `U` → `U` owes `targetUser` X.
- Negative balance `-X` for user `U` → `targetUser` owes `U` X.

## Input Format

```
T          ← number of transactions
For each transaction:
  EXPENSE paidBy amount k p1 s1 p2 s2 ... pk sk
    paidBy  = user who paid
    amount  = total expense amount (ignored in balance logic, splits are explicit)
    k       = number of participants
    pi si   = participant id and their share

  SETTLE from to amount
```

First line after T: `targetUser` id.

## Example

```
targetUser = A

Transactions:
  EXPENSE paidBy=A amount=90 splits={B:30, C:30, A:30}
  EXPENSE paidBy=B amount=60 splits={A:20, B:20, C:20}
  SETTLE  from=C   to=A  amount=10
```

Expected output (net balances from A's perspective):
```
B owes A: 10        (B has 30 share in A's expense, A has 20 share in B's expense → net 10)
C owes A: 20        (C has 30 share, then paid 10 via settlement → net 20)
```

## Constraints

- 1 ≤ T ≤ 10^4
- All amounts are positive decimals with at most 2 decimal places.
- At most 500 distinct users.

---

## Key Insight

Model this as a **running ledger using a hash map**.

For each transaction, apply delta updates:

**Expense (paidBy = target):**
- For each participant `p ≠ target`: `net[p] += share[p]` (p owes target)

**Expense (paidBy ≠ target, target is a participant):**
- `net[paidBy] -= share[target]` (target owes paidBy)

**Settlement (from = target):**
- `net[to] += amount` (target paid to, so to owes target less now... wait, settle reduces debt)
- Actually: target paid `to`, meaning target reduced what target owed `to` → `net[to] += amount`

**Settlement (to = target):**
- `net[from] -= amount` (from paid target, so from's debt to target decreased)

---

## C++ Solution

```cpp
#include <bits/stdc++.h>
using namespace std;

struct Expense {
    string paidBy;
    map<string, double> splits; // participant → share
};

struct Settlement {
    string from, to;
    double amount;
};

variant<Expense, Settlement> parseTransaction(/* ... */) { /* parse input */ }

map<string, double> computeNetBalances(
    const string& target,
    const vector<Expense>& expenses,
    const vector<pair<int,int>>& txOrder, // ordering not needed here
    const vector<Settlement>& settlements,
    const vector<pair<string, int>>& transactions // ("EXPENSE"|"SETTLE", index)
) {
    map<string, double> net;

    // Process all transactions in order
    // For simplicity, assume two separate lists here

    for (const auto& exp : expenses) {
        if (exp.paidBy == target) {
            for (auto& [user, share] : exp.splits) {
                if (user != target)
                    net[user] += share;
            }
        } else {
            auto it = exp.splits.find(target);
            if (it != exp.splits.end()) {
                net[exp.paidBy] -= it->second;
            }
        }
    }

    for (const auto& s : settlements) {
        if (s.from == target) {
            net[s.to] += s.amount;   // target paid s.to → s.to owes less → net goes up?
            // Careful: settlement means from PAID to. So target's debt to `to` reduced.
            // net[to] += amount means to now owes target more (or target owes to less)
            // Re-check: from paid to → from's balance with `to` improves by amount
            // If net[to] was -X (target owed to X), after settle net[to] = -X + amount
        } else if (s.to == target) {
            net[s.from] -= s.amount; // from paid target → from's outstanding debt reduced
        }
    }

    return net;
}

int main() {
    // Example from problem statement
    vector<Expense> expenses = {
        {"A", {{"B", 30}, {"C", 30}, {"A", 30}}},
        {"B", {{"A", 20}, {"B", 20}, {"C", 20}}}
    };
    vector<Settlement> settlements = {
        {"C", "A", 10}
    };

    string target = "A";
    auto net = computeNetBalances(target, expenses, {}, settlements, {});

    const double EPS = 0.01;
    for (auto& [user, balance] : net) {
        if (abs(balance) < EPS) continue;
        if (balance > 0)
            cout << user << " owes " << target << ": " << balance << "\n";
        else
            cout << target << " owes " << user << ": " << -balance << "\n";
    }
    return 0;
}
```

## Complexity

| | |
|---|---|
| Time | O(T × P) where P = max participants per expense |
| Space | O(U) where U = number of distinct users |

## Edge Cases

- User paid for an expense but is also a participant (their own share doesn't create a self-debt).
- Settlement that over-pays (net flips sign) — valid, handle naturally.
- User has no transactions — simply absent from the map (balance = 0).
