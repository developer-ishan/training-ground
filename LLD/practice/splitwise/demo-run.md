# Splitwise — Demo Run

## Scenario
4 friends (Alice, Bob, Charlie, Dave) on a Goa Trip.

| # | Who Paid | Amount | Description | Split Type | Details |
|---|---|---|---|---|---|
| 1 | Alice | ₹400 | Hotel | Equal | ₹100 each |
| 2 | Bob | ₹150 | Dinner | Exact | Alice:60, Bob:30, Charlie:40, Dave:20 |
| 3 | Charlie | ₹200 | Transport | Percentage | Alice:40%, Bob:30%, Charlie:20%, Dave:10% |

---

## Balances (before settlement)

```
Balances for Alice in group Goa Trip:
  Dave owes you 100.00
  Bob owes you 40.00
  Charlie owes you 20.00

Balances for Bob in group Goa Trip:
  You owe Alice 40.00
  Dave owes you 20
  You owe Charlie 20.00

Balances for Charlie in group Goa Trip:
  You owe Alice 20.00
  Dave owes you 20.00
  Bob owes you 20.00

Balances for Dave in group Goa Trip:
  You owe Alice 100.00
  You owe Bob 20
  You owe Charlie 20.00
```

---

## Minimum Transfers to Settle Group

```
Minimum transfers to settle group Goa Trip:
  Dave pays Alice: 140.00
  Bob pays Alice: 20.00
  Bob pays Charlie: 20.00
```

3 transfers to settle 4 people — optimal (N-1).

---

## Settlement: Dave pays Alice ₹100

---

## Balances (after Dave's settlement)

```
Balances for Alice in group Goa Trip:
  Bob owes you 40.00
  Charlie owes you 20.00

Balances for Dave in group Goa Trip:
  You owe Bob 20
  You owe Charlie 20.00
```

Dave no longer owes Alice anything. Remaining debts redistributed correctly.

---

## Group Transaction History

```
[EXPENSE]    Alice        paid 400       desc: Hotel
[EXPENSE]    Bob          paid 150       desc: Dinner
[EXPENSE]    Charlie      paid 200       desc: Transport
[SETTLEMENT] Dave         paid 100       to: Alice
```
