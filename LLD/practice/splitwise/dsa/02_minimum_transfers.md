# Minimum Number of Transfers to Settle Debts

> Classic problem. Also appears as **LeetCode 465 — Optimal Account Balancing**.

## Problem

You are given `n` people. After computing net balances across all shared expenses, each person has a **net amount**:

- Positive → they are owed money (creditor).
- Negative → they owe money (debtor).
- Zero → settled.

Find the **minimum number of transfers** needed so that every person's balance becomes zero.

Each transfer moves money from one person to another.

## Input

```
n              ← number of people with non-zero balance
b[0..n-1]      ← net balance of each person (can be + or -)
                 (sum of all b[i] == 0, guaranteed)
```

## Example

```
n = 4
balances = [30, -10, -10, -10]
```

Person 0 is owed 30. Persons 1,2,3 each owe 10.

Minimum transfers = **3** (each debtor pays person 0 once).

---

```
n = 3
balances = [10, -5, -5]  → 2 transfers

n = 4
balances = [10, 10, -10, -10]  → 2 transfers  (not 4)
```

---

## Greedy Approach (used in ExpenseService.java)

**Intuition:** At each step, match the largest creditor with the largest debtor. Transfer `min(credit, debt)`. Advance the pointer that reaches zero.

```
Sort creditors descending (most owed first)
Sort debtors ascending   (most owing first)

ci = 0, di = 0
while ci < creditors.size() and di < debtors.size():
    transfer = min(creditors[ci], abs(debtors[di]))
    print debtors[di] → creditors[ci]: transfer
    creditors[ci] -= transfer
    debtors[di]   += transfer          ← debtors[di] is negative, so this reduces magnitude
    if creditors[ci] ≈ 0: ci++
    if debtors[di]   ≈ 0: di++
```

**This greedy is NOT always optimal** in terms of number of transfers (it's optimal in total amount moved, but may use more transactions than necessary in adversarial cases). For the minimum *count*, the exact optimum requires backtracking / bitmask DP (see below). In practice, greedy is used because it's simple and nearly optimal.

---

## Optimal Solution — Bitmask DP (exact minimum count)

For small `n` (≤ 20 after filtering zero-balance users), use bitmask DP.

**Key insight:** A subset of people can "internally settle" if and only if their balances sum to zero. Such a subset needs at most `|subset| - 1` transfers.

Minimize total transfers = sum over chosen subsets of `(|subset| - 1)`.

```cpp
#include <bits/stdc++.h>
using namespace std;

int minTransfers(vector<int>& balance) {
    // Remove zero balances
    vector<int> b;
    for (int x : balance)
        if (x != 0) b.push_back(x);

    int n = b.size();
    if (n == 0) return 0;

    // dp[mask] = min transfers to settle exactly the people in mask
    vector<int> dp(1 << n, INT_MAX);
    dp[0] = 0;

    // Precompute subset sums
    vector<int> sum(1 << n, 0);
    for (int mask = 1; mask < (1 << n); mask++) {
        int lsb = mask & (-mask);
        int idx = __builtin_ctz(lsb);
        sum[mask] = sum[mask ^ lsb] + b[idx];
    }

    for (int mask = 1; mask < (1 << n); mask++) {
        if (sum[mask] != 0) continue; // can't self-settle

        // This subset can settle in (__builtin_popcount(mask) - 1) transfers
        int cost = __builtin_popcount(mask) - 1;

        // Try to split mask into two valid sub-subsets
        // (enumerate all subsets of mask)
        for (int sub = (mask - 1) & mask; sub > 0; sub = (sub - 1) & mask) {
            if (dp[sub] == INT_MAX) continue;
            int rest = mask ^ sub;
            if (sum[rest] == 0) {
                dp[mask] = min(dp[mask], dp[sub] + __builtin_popcount(rest) - 1);
            }
            if (sub == 0) break;
        }
        dp[mask] = min(dp[mask], cost); // settle whole subset at once
    }

    return dp[(1 << n) - 1];
}
```

> **Simpler DP formulation:** `dp[mask]` = minimum transfers to zero out all people in `mask`. Transition: pick any person `i` in mask, try pairing them with every other `j` in mask (transfer from i to j or vice versa), recurse on remaining.

---

## Cleaner Backtracking Solution

```cpp
#include <bits/stdc++.h>
using namespace std;

int dfs(vector<int>& b, int start) {
    while (start < (int)b.size() && b[start] == 0)
        start++;
    if (start == (int)b.size()) return 0;

    int res = INT_MAX;
    for (int i = start + 1; i < (int)b.size(); i++) {
        // Only try if signs differ (one owes the other)
        if ((long long)b[i] * b[start] < 0) {
            b[i] += b[start];          // transfer b[start]'s balance into b[i]
            res = min(res, 1 + dfs(b, start + 1));
            b[i] -= b[start];          // backtrack
        }
    }
    return res;
}

int minTransfers(vector<int>& balance) {
    vector<int> b;
    for (int x : balance)
        if (x != 0) b.push_back(x);
    return dfs(b, 0);
}
```

---

## Complexity Comparison

| Approach | Time | Optimal? |
|---|---|---|
| Greedy (two-pointer) | O(n log n) | No (transfer count), Yes (total amount) |
| Bitmask DP | O(3^n) subset enumeration | Yes |
| Backtracking DFS | O(n!) worst case, pruned | Yes |

For `n ≤ 12` (typical group size), backtracking is fast enough. For `n > 20`, greedy is the practical choice.

---

## Trace — Greedy on `[30, -10, -10, -10]`

```
creditors = [30]      (sorted desc)
debtors   = [-10, -10, -10]  (sorted asc)

Step 1: transfer = min(30, 10) = 10  →  debtor[0] pays creditor[0] 10
        creditors = [20], debtors = [0, -10, -10],  di++
Step 2: transfer = min(20, 10) = 10  →  debtor[1] pays creditor[0] 10
        creditors = [10], debtors = [0,  0, -10],   di++
Step 3: transfer = min(10, 10) = 10  →  debtor[2] pays creditor[0] 10
        creditors = [0],  debtors = [0,  0,   0],   both advance

Total transfers: 3  ✓
```

## Edge Cases

- All balances zero → 0 transfers.
- One creditor, many debtors → each debtor needs exactly one transfer.
- Two people with opposite equal balances → 1 transfer (greedy handles correctly).
- Circular debt `A→B→C→A` each 10 → net all zero → 0 transfers needed (greedy correctly outputs nothing since all net balances are 0).
