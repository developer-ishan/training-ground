# Weighted Split Distribution

## Problem

Given a total amount and a set of participants, distribute the amount according to a split type:

| Split Type | Description |
|---|---|
| **EQUAL** | Each participant gets `total / n`. Handle remainder cent. |
| **EXACT** | Each participant has a specified exact share. Validate they sum to total. |
| **PERCENTAGE** | Each participant has a percentage weight. Shares must sum to 100%. |

For each split type, compute the share for each participant **without floating point errors** (use integer arithmetic or fixed-point decimal).

---

## Input

```
splitType    ← "EQUAL" | "EXACT" | "PERCENTAGE"
total        ← integer amount in cents (e.g., 9000 = $90.00)
n            ← number of participants
participants ← list of n participant ids

For EXACT:      n values (share in cents per participant)
For PERCENTAGE: n values (percentage × 100, i.e., integer, e.g., 3333 = 33.33%)
```

## Output

For each participant, output their share in cents.

---

## Part A — Equal Split with Remainder Distribution

Total may not divide evenly. The remainder cents should be distributed to the first `r` participants (where `r = total % n`).

### Example

```
total = 100 cents, n = 3
base  = 33, remainder = 1
shares = [34, 33, 33]   ← first participant gets the extra cent
```

### C++ Solution

```cpp
#include <bits/stdc++.h>
using namespace std;

vector<long long> equalSplit(long long totalCents, int n) {
    long long base      = totalCents / n;
    long long remainder = totalCents % n;

    vector<long long> shares(n, base);
    for (int i = 0; i < remainder; i++)
        shares[i]++;

    return shares;
}
```

---

## Part B — Exact Split Validation

Given explicit shares, validate:
1. All shares are positive.
2. Sum of shares == total.

```cpp
#include <bits/stdc++.h>
using namespace std;

bool validateExactSplit(long long totalCents, const vector<long long>& shares) {
    long long sum = 0;
    for (long long s : shares) {
        if (s <= 0) return false;
        sum += s;
    }
    return sum == totalCents;
}
```

---

## Part C — Percentage Split (Fixed-Point, No Float)

Percentages are given as integers scaled by 100 (e.g., 3333 = 33.33%). They must sum to 10000 (= 100.00%).

Compute shares using integer arithmetic:

```
share[i] = (total * pct[i]) / 10000
```

Remainder from rounding is assigned to the participant with the largest fractional loss.

### C++ Solution

```cpp
#include <bits/stdc++.h>
using namespace std;

// pct[i] is in units of 0.01%  →  pct[i] = 3333 means 33.33%
// All pct must sum to 10000
vector<long long> percentageSplit(long long totalCents, const vector<int>& pct) {
    int n = pct.size();
    vector<long long> shares(n);
    long long assigned = 0;

    // Fractional remainder tracking for Largest Remainder Method
    vector<pair<long long, int>> fractions(n); // (fractional part × 10000, index)

    for (int i = 0; i < n; i++) {
        long long exact   = (long long)totalCents * pct[i]; // scaled by 10000
        shares[i]         = exact / 10000;
        fractions[i]      = {exact % 10000, i};  // remainder
        assigned         += shares[i];
    }

    long long leftover = totalCents - assigned;

    // Distribute leftover cents to those with largest fractional parts
    sort(fractions.begin(), fractions.end(), [](auto& a, auto& b) {
        return a.first > b.first; // descending fractional remainder
    });

    for (int i = 0; i < leftover; i++)
        shares[fractions[i].second]++;

    return shares;
}
```

### Example

```
total = 10 cents, pct = [3333, 3333, 3334]  (= 33.33% + 33.33% + 33.34% = 100%)

Exact:
  10 * 3333 / 10000 = 3.333  →  floor = 3, remainder = 3330
  10 * 3333 / 10000 = 3.333  →  floor = 3, remainder = 3330
  10 * 3334 / 10000 = 3.334  →  floor = 3, remainder = 3340

assigned = 9, leftover = 1
Largest remainder: index 2 (remainder 3340)
→ shares[2] gets +1

Final: [3, 3, 4]  ✓  (sums to 10)
```

---

## Part D — Validation Summary

| Split Type | Validation Rule |
|---|---|
| EQUAL | n ≥ 1 |
| EXACT | sum(shares) == total; all shares > 0 |
| PERCENTAGE | sum(pct) == 10000; all pct > 0 |

```cpp
bool validateSplit(const string& type, long long total,
                   const vector<long long>& shares,
                   const vector<int>& pct) {
    if (type == "EQUAL") {
        return !shares.empty();
    } else if (type == "EXACT") {
        long long s = 0;
        for (long long x : shares) { if (x <= 0) return false; s += x; }
        return s == total;
    } else if (type == "PERCENTAGE") {
        int s = 0;
        for (int p : pct) { if (p <= 0) return false; s += p; }
        return s == 10000;
    }
    return false;
}
```

---

## Why Integer Arithmetic?

Using `double` for money causes silent precision errors:

```cpp
double share = 100.0 / 3;  // 33.33333333...
// After 3 participants: sum = 99.99999999... ≠ 100.0
```

Using `long long` cents eliminates this entirely. All arithmetic is exact until the final remainder distribution, which is handled explicitly.

---

## Complexity

| Operation | Time | Space |
|---|---|---|
| Equal split | O(n) | O(n) |
| Exact validation | O(n) | O(1) |
| Percentage split | O(n log n) | O(n) |

The sort in percentage split is for the Largest Remainder Method; if remainder is small (≤ a few cents), a linear scan suffices too.
