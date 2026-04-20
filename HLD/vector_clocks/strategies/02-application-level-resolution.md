# Strategy 2: Application-Level Resolution (Semantic Merge)

[Back to Vector Clocks Overview](../vector-clocks.md)

---

## Overview

Instead of the database deciding which version wins, the system **returns all conflicting versions** (called **siblings**) to the application. The application understands the domain semantics and merges them correctly.

---

## How It Works

```
1. Client reads key "cart:user42"
2. System detects two concurrent versions:
     Sibling 1: {milk, eggs}       clock=[A:2, B:1]
     Sibling 2: {milk, bread}      clock=[A:1, B:2]
3. System returns BOTH siblings to the client.
4. Client applies domain logic:
     - Shopping cart → union: {milk, eggs, bread}
     - User profile → prompt the user to pick
     - Counter → sum the deltas
5. Client writes the merged result back.
     Merged: {milk, eggs, bread}   clock=[A:2, B:2, C:1]
```

**Merge strategies vary by domain:**

| Data Type | Merge Logic |
|---|---|
| Shopping cart | Union of items |
| Counter/balance | Sum deltas from each sibling |
| User profile | Show diff to user, let them pick |
| Wiki/document | Three-way merge (like git) |
| Set of tags | Union (add-wins) or intersection (remove-wins) |

---

## When to Use It

- You cannot afford data loss.
- The data has meaningful merge semantics the application understands.
- You're willing to add complexity to the client/application layer.

## Drawbacks

- **Complexity pushed to the application** — every client must handle siblings correctly. If a buggy client writes back just one sibling, data is permanently lost.
- **Sibling explosion** — under sustained concurrent writes without reads, siblings accumulate. A single key can have dozens of versions, making reads expensive.
- **Every data type needs its own merge function** — there's no universal solution.

---

## Real-World Use Case: Amazon Dynamo — Shopping Cart Merge

### The System

Amazon's **Dynamo** (the internal key-value store described in the famous 2007 paper) is the canonical example of application-level reconciliation. Dynamo powers critical Amazon services where **availability must never be sacrificed** — even during network partitions, every write is accepted. This means conflicts are inevitable, and reconciliation is the application's responsibility.

### The Problem: Shopping Cart During a Partition

```
Scenario: Black Friday traffic spike. Network partition between US-East and US-West.

Timeline:
  T1: User adds "laptop" to cart on US-East
      US-East stores: cart = {laptop}, clock = [East:1]

  T2: User adds "headphones" to cart on US-West (routed differently due to partition)
      US-West stores: cart = {headphones}, clock = [West:1]

  T3: Partition heals. Two versions exist:
      Version A: {laptop}      clock=[East:1]
      Version B: {headphones}  clock=[West:1]
      
      Clocks are concurrent → CONFLICT
```

### How Dynamo Handles It

Dynamo does **not** resolve this conflict. Instead, on the next read:

```
  T4: User opens their cart page. Dynamo returns BOTH siblings:
      [
        { items: ["laptop"],      vclock: [East:1] },
        { items: ["headphones"],  vclock: [West:1] }
      ]

  T5: The cart service (application layer) applies its merge rule:
      - Shopping cart → UNION of items
      - Merged cart: {laptop, headphones}
      
  T6: Application writes merged result back to Dynamo:
      { items: ["laptop", "headphones"], vclock: [East:1, West:1, Coordinator:1] }
      
      This new vector clock dominates both siblings → conflict resolved.
```

### Why Application-Level?

Amazon chose this because **only the application knows the right merge**:

- **Shopping carts** → union (you never want to lose an item a customer added).
- **Customer addresses** → can't union two addresses. Show both and ask the customer.
- **Order status** → state machine logic: `pending` + `shipped` = `shipped` (the later state wins).
- **Wishlist** → union, but with deduplication.

A generic database-level rule couldn't handle all of these correctly. Each service team at Amazon writes their own merge function for their specific data type.

### The Add/Remove Problem

Application-level merge has a subtle pitfall with shopping carts:

```
Initial cart: {laptop, mouse}

User A removes "mouse":      cart = {laptop}       clock=[A:2]
User B adds "keyboard":      cart = {laptop, mouse, keyboard}  clock=[B:2]

These are concurrent. Application merges via union:
  {laptop} ∪ {laptop, mouse, keyboard} = {laptop, mouse, keyboard}

"mouse" reappears! The remove was lost.
```

Amazon's Dynamo paper acknowledges this: *"deletes can resurface."* Their solution:
- Accept that deleted items may reappear (the customer removes it again — minor annoyance).
- Use **tombstones** (markers that say "this item was deliberately removed") to make removes sticky.
- Or switch to a CRDT (OR-Set) for this specific data type.

### Production Reality

In practice, Amazon found that:
- Most reads return **a single version** (no conflict). Conflicts are the exception, not the norm.
- When conflicts do occur, the merge logic is typically a few lines of code per data type.
- The biggest operational challenge is **sibling explosion** — if a key receives many concurrent writes without any reads, siblings pile up. Dynamo caps siblings and falls back to LWW as a safety valve.

---

## Systems That Use Application-Level Resolution

| System | Details |
|---|---|
| **Amazon Dynamo** | The original design; all services implement their own merge functions |
| **Riak** | `allow_mult=true` returns siblings to the client; Riak's default since 2.0 |
| **Voldemort** (LinkedIn) | Dynamo-inspired; client-side conflict resolution |
| **CouchDB** | Stores conflicting revisions; app picks the winner via `_conflicts` API |
| **Git** | Merge conflicts surfaced to the developer; manual resolution |

---

## Key Takeaway

Application-level resolution is the **most correct** approach — no data is lost, and domain-specific logic ensures semantically meaningful merges. The cost is **complexity**: every client and every data type needs its own merge function, and bugs in that function can permanently corrupt data. It's the right choice when data integrity matters more than simplicity.
