# Conflict Detection and Reconciliation Using Vector Clocks

## The Problem

In distributed systems, multiple nodes (servers, replicas) can independently modify the same piece of data. Without a global clock, there's no reliable way to determine which update happened "first." Physical timestamps drift between machines, making them unreliable for ordering events. This is where **vector clocks** come in.

---

## What Is a Vector Clock?

A vector clock is a data structure that tracks **causal history** — not wall-clock time, but the logical sequence of events across all nodes. It's a vector (array/map) of counters, one per node in the system.

For a system with nodes A, B, and C, a vector clock looks like:

```
[A:2, B:3, C:1]
```

This means: "This version incorporates 2 events from A, 3 from B, and 1 from C."

---

## Rules of Vector Clocks

1. **On local update**: A node increments its own counter.
   - Node A writes data → `[A:1, B:0, C:0]` becomes `[A:2, B:0, C:0]`

2. **On sending a message/replica**: Attach the current vector clock.

3. **On receiving**: Merge by taking the element-wise **max** of both vectors, then increment the receiver's own counter.

---

## Conflict Detection

The key insight is that vector clocks define a **partial order** over events. Given two vector clocks `V1` and `V2`:

| Comparison | Meaning |
|---|---|
| `V1 < V2` (every entry in V1 ≤ V2, at least one strictly less) | V1 **happened before** V2 — no conflict, V2 supersedes V1 |
| `V1 > V2` | V2 happened before V1 — no conflict, V1 supersedes V2 |
| `V1 = V2` | Same version — no conflict |
| `V1 ∥ V2` (neither dominates — V1 is greater on some entries, V2 on others) | **Concurrent** modifications — **CONFLICT detected** |

### Concrete Example

```
Node A writes:  V_a = [A:2, B:1, C:0]
Node B writes:  V_b = [A:1, B:2, C:0]
```

Comparing element-wise:
- A's counter: 2 > 1 (V_a wins)
- B's counter: 1 < 2 (V_b wins)

Neither dominates the other → these are **concurrent** → **conflict**.

Contrast with:

```
V_a = [A:2, B:1, C:0]
V_c = [A:3, B:1, C:1]
```

Every entry in V_a ≤ V_c → V_a **happened before** V_c → no conflict, V_c is the latest version.

---

## Reconciliation Strategies

Once a conflict is detected (concurrent vector clocks), you need a **reconciliation** strategy. Each strategy below has a dedicated deep-dive file with real-world production use cases in the [strategies/](strategies/) folder.

### 1. Last-Writer-Wins (LWW)

> Deep dive with real-world use cases: [strategies/01-last-writer-wins.md](strategies/01-last-writer-wins.md)

The simplest strategy. Each write is tagged with a **physical timestamp** (or a logical tiebreaker like a node ID). When two versions conflict, the one with the higher timestamp wins — the other is **silently discarded**.

**How it works:**

```
Node A writes at T=100:  value="Alice",  clock=[A:2, B:1]
Node B writes at T=105:  value="Bob",    clock=[A:1, B:2]

Clocks are concurrent (conflict detected).
T=105 > T=100 → "Bob" wins. "Alice" is discarded forever.
```

**When to use it:**
- Data where "latest value" is all that matters (e.g., a user's last-seen status, sensor readings, cache entries).
- Situations where simplicity and availability outweigh correctness of every write.

**Drawbacks:**
- **Data loss** — concurrent writes are silently dropped, not merged.
- **Clock skew** — if Node A's clock is 5 seconds ahead, its writes always win regardless of true ordering.
- **No composability** — you can't LWW a shopping cart or a collaborative document; independent additions would overwrite each other.

**Used by:** Cassandra (default), DynamoDB (optional), Riak (optional).

---

### 2. Application-Level Resolution (Semantic Merge)

> Deep dive with real-world use cases: [strategies/02-application-level-resolution.md](strategies/02-application-level-resolution.md)

Instead of the database deciding which version wins, the system **returns all conflicting versions** (called **siblings**) to the application. The application understands the domain semantics and merges them correctly.

**How it works:**

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

**When to use it:**
- You cannot afford data loss.
- The data has meaningful merge semantics the application understands.
- You're willing to add complexity to the client/application layer.

**Drawbacks:**
- **Complexity pushed to the application** — every client must handle siblings correctly. If a buggy client writes back just one sibling, data is permanently lost.
- **Sibling explosion** — under sustained concurrent writes without reads, siblings accumulate. A single key can have dozens of versions, making reads expensive.
- **Every data type needs its own merge function** — there's no universal solution.

**Used by:** Amazon Dynamo (the paper), Riak (with `allow_mult=true`).

---

### 3. CRDTs (Conflict-free Replicated Data Types)

> Deep dive with real-world use cases: [strategies/03-crdts.md](strategies/03-crdts.md)

CRDTs eliminate the need for reconciliation logic entirely by designing the **data structure itself** so that concurrent updates always converge to the same result, regardless of order or duplication.

**The mathematical guarantee:** A CRDT's merge function is:
- **Commutative**: `merge(A, B) = merge(B, A)` — order doesn't matter.
- **Associative**: `merge(A, merge(B, C)) = merge(merge(A, B), C)` — grouping doesn't matter.
- **Idempotent**: `merge(A, A) = A` — duplicates don't matter.

This means replicas can sync in any order, at any time, with any amount of duplication, and they will **always converge**.

**Common CRDT types:**

| CRDT | What it does | How conflicts resolve |
|---|---|---|
| **G-Counter** (Grow-only Counter) | Each node maintains its own counter; total = sum of all nodes | No conflict possible — counters only grow |
| **PN-Counter** | Two G-Counters: one for increments, one for decrements | Subtract decrement counter from increment counter |
| **G-Set** (Grow-only Set) | Elements can be added, never removed | Union of all additions |
| **OR-Set** (Observed-Remove Set) | Elements can be added and removed; each add is tagged with a unique ID | Add wins over concurrent remove (add-wins semantics) |
| **LWW-Register** | Single value with a timestamp | Higher timestamp wins (like LWW, but as a formal CRDT) |
| **MV-Register** (Multi-Value Register) | Stores concurrent values | Returns all concurrent values (like siblings) |
| **RGA** (Replicated Growable Array) | Ordered list / text sequence | Unique IDs + causal ordering resolve insert position |

**G-Counter example:**

```
System has 3 nodes: A, B, C.
Each tracks its own increment count.

Node A: [A:5, B:0, C:0]  → local value = 5
Node B: [A:0, B:3, C:0]  → local value = 3
Node C: [A:0, B:0, C:7]  → local value = 7

After sync (element-wise max):
All nodes: [A:5, B:3, C:7] → total = 15

No conflicts. No coordination. Always correct.
```

**OR-Set example (add-wins):**

```
Initial state on all nodes: {apple}

Node A: add(banana)  →  {apple, banana}
Node B: remove(apple) →  {}

These are concurrent. After merge:
  - banana was added (and not removed) → keep banana
  - apple was removed by B, but not re-added → remove apple
  Result: {banana}

If Node A also re-added apple concurrently with B's remove:
  - A's add(apple) has a new unique tag that B's remove didn't see
  - Add wins over concurrent remove
  Result: {apple, banana}
```

**When to use CRDTs:**
- High-availability systems where nodes must accept writes during partitions.
- Data types that map naturally to CRDTs (counters, sets, flags, text).
- You want zero-coordination, automatic convergence.

**Drawbacks:**
- **Limited data types** — not every domain model maps to a known CRDT. Complex objects require composition of multiple CRDTs.
- **Metadata overhead** — OR-Sets track unique tags per element per add; tombstones and version vectors consume memory.
- **No "true" deletes in some CRDTs** — tombstones (markers for deleted items) may need garbage collection.
- **Semantic limitations** — CRDTs resolve structural conflicts, but they can't enforce business invariants (e.g., "balance must not go negative").

**Used by:** Riak (built-in CRDT types), Redis (CRDTs in Redis Enterprise), Automerge, Yjs (collaborative editing), SoundCloud (counters).

---

### 4. Read Repair / Anti-Entropy

> Deep dive with real-world use cases: [strategies/04-read-repair-anti-entropy.md](strategies/04-read-repair-anti-entropy.md)

These are **background reconciliation mechanisms** that ensure replicas converge even without explicit conflict resolution by the client. They work alongside the strategies above, not as a standalone replacement.

#### Read Repair

When a coordinator node handles a read request, it queries multiple replicas. If the replicas return different versions, the coordinator:

1. Determines the latest version (using vector clock dominance).
2. Returns the latest version to the client.
3. **Sends the latest version back to the stale replicas** to bring them up to date.

```
Client reads key "user:42" from coordinator.
Coordinator queries replicas A, B, C:
  A responds: value="v3", clock=[A:3, B:2]
  B responds: value="v2", clock=[A:2, B:2]   ← stale (dominated by A's clock)
  C responds: value="v3", clock=[A:3, B:2]

Coordinator returns "v3" to client.
Coordinator sends "v3" with clock=[A:3, B:2] to Node B (repair).
```

This is **lazy** — stale replicas are only repaired when they happen to be read. Keys that are rarely read may stay diverged for a long time.

#### Anti-Entropy (Merkle Tree Sync)

A **background process** that proactively scans for divergence between replicas, even for data that isn't being read.

**How it works:**

1. Each replica maintains a **Merkle tree** (hash tree) over its key-value data.
2. Periodically, two replicas exchange their Merkle tree roots.
3. If roots differ, they walk down the tree to find exactly which key ranges diverge.
4. Only the differing keys are exchanged and reconciled.

```
Replica A's Merkle root: 0xABC123
Replica B's Merkle root: 0xABC456  ← different!

Walk the tree:
  Left subtree hashes match → skip
  Right subtree → left child matches, right child differs
    → Keys in range [M-Z] differ
    → Exchange only those keys
    → Reconcile using vector clock comparison
```

**Advantages:**
- Efficient — only transfers data that actually differs (logarithmic comparison).
- Catches divergence that read repair misses (cold/rarely-read data).
- Works continuously in the background without impacting read/write latency.

**Drawbacks:**
- Merkle trees must be rebuilt or incrementally updated as data changes — CPU and I/O cost.
- Adds background network traffic between replicas.
- Reconciliation still requires one of the above strategies (LWW, semantic merge, CRDT) for truly concurrent versions.

**Used by:** Cassandra (Merkle-tree anti-entropy), Dynamo (both read repair and anti-entropy), Riak (active anti-entropy).

---

### 5. Sibling Version Retention (Multi-Version Storage)

> Deep dive with real-world use cases: [strategies/05-sibling-version-retention.md](strategies/05-sibling-version-retention.md)

Rather than resolving conflicts immediately, the system **stores all conflicting versions** (called siblings) and defers reconciliation to a later time — either on the next read or via an explicit merge process.

**How it works:**

```
1. Node A writes: value="v1",  clock=[A:1]
2. Node B writes: value="v2",  clock=[B:1]   (concurrent with v1)

3. System stores BOTH as siblings under the same key:
     Sibling 1: "v1", clock=[A:1]
     Sibling 2: "v2", clock=[B:1]

4. On next read, client receives both siblings.
5. Reconciliation happens at read time (or via background job).
```

**When to use it:**
- You cannot afford data loss under any circumstance.
- Reconciliation logic isn't available at write time (e.g., only certain services know how to merge).
- You want flexibility to choose a merge strategy later.

**Drawbacks:**
- **Storage overhead** — multiple versions per key consume disk and memory.
- **Read complexity** — clients must be prepared to handle multiple values for a single key.
- **Sibling explosion** — without timely reconciliation, sustained concurrent writes accumulate unbounded siblings.

**Used by:** Amazon Dynamo, Riak.

---

### 6. Server-side Custom Merge Logic

> Deep dive with real-world use cases: [strategies/06-server-side-merge.md](strategies/06-server-side-merge.md)

Instead of pushing conflict resolution to the client, the **database itself** applies predefined merge rules automatically when concurrent versions are detected.

**How it works:**

```
Conflict detected for key "page_views":
  Version A: 1042    clock=[A:3, B:1]
  Version B: 1057    clock=[A:1, B:4]

Server merge rule for numeric counters: sum the deltas.
  Common ancestor value: 1000
  Delta A: 1042 - 1000 = 42
  Delta B: 1057 - 1000 = 57
  Merged: 1000 + 42 + 57 = 1099
```

**Common server-side merge rules:**

| Field Type | Merge Rule |
|---|---|
| Numeric counters | Sum deltas from common ancestor |
| Sets / lists | Union |
| Timestamps | Max (latest) |
| Strings | Pick longest, or LWW |
| Booleans / flags | OR (true wins) or AND (depending on semantics) |

**When to use it:**
- Your data model has well-defined, deterministic merge semantics.
- You want clients to be unaware of conflicts entirely.
- Merge logic is simple enough to express as generic rules.

**Drawbacks:**
- **Cannot handle all edge cases** — domain-specific conflicts (e.g., two users editing different fields of the same profile) may need richer logic than a generic rule.
- **Risk of incorrect merges** — if the rule doesn't match the actual intent (e.g., summing a field that should have been overwritten), data corruption occurs silently.
- **Tight coupling** — merge rules are baked into the database layer, making them harder to evolve as business logic changes.

**Used by:** CouchDB (custom merge functions), Firebase Realtime Database (server-side rules), custom database middleware.

---

### 7. Operational Transformation / Event Merging

> Deep dive with real-world use cases: [strategies/07-operational-transformation.md](strategies/07-operational-transformation.md)

Instead of storing and merging **state** (the current value), this approach stores **operations** (the changes) and merges them.

**How it works:**

```
Document: "Hello"

User A (concurrent): insert " World" at position 5  → "Hello World"
User B (concurrent): insert "!" at position 5        → "Hello!"

Without OT: applying B's op to A's result puts "!" at position 5:
  "Hello World" → "Hello!World"  ← WRONG

With OT: transform B's operation against A's:
  A inserted 6 chars before position 5 → shift B's position to 11
  Transformed B: insert "!" at position 11
  Result: "Hello World!"  ← CORRECT
```

**Key concepts:**
- **Transform function**: Given two concurrent operations `Op_A` and `Op_B`, produce `Op_A'` and `Op_B'` such that applying `Op_A` then `Op_B'` gives the same result as applying `Op_B` then `Op_A'`.
- **Causal ordering**: Operations carry vector clocks or sequence numbers so the system knows which operations are concurrent and need transformation.
- **Operation log**: The system maintains a log of all operations, enabling replay, undo, and conflict resolution.

**OT vs CRDTs for collaborative editing:**

| Aspect | OT | CRDTs (e.g., Yjs, Automerge) |
|---|---|---|
| Coordination | Needs central server for ordering | Fully decentralized |
| Complexity | Transform functions are hard to get right | Data structure design is complex |
| Proven at scale | Google Docs, Google Wave | Figma (partial), local-first apps |
| Undo support | Natural (reverse the operation) | Harder (state-based) |

**When to use it:**
- Real-time collaborative editing (text, diagrams, spreadsheets).
- You need fine-grained, intent-preserving conflict resolution.
- Operations are well-defined and composable.

**Drawbacks:**
- **High complexity** — designing correct transform functions for all operation pairs is notoriously error-prone. Google Wave's OT algorithm had subtle bugs for years.
- **Central sequencer often required** — pure peer-to-peer OT is extremely difficult; most implementations use a central server to assign a total order.
- **Operation design** — every user action must be decomposed into well-defined, transformable operations.

**Used by:** Google Docs, Google Sheets, Microsoft Office Online, Apache Wave, ShareDB.

---

### 8. Version Vectors with Causal Context (Dotted Version Vectors)

> Deep dive with real-world use cases: [strategies/08-dotted-version-vectors.md](strategies/08-dotted-version-vectors.md)

An evolution of basic vector clocks that solves the **sibling explosion** problem. Standard vector clocks can generate false concurrency when a client reads one sibling, resolves the conflict, and writes back — but the system can't tell which siblings the client already saw.

**Dotted version vectors** attach a precise "dot" (node + sequence number) to each write, so the system knows exactly which events a new write has observed and superseded.

```
Standard vector clock problem:
  Client reads siblings S1 and S2.
  Client writes merged value V3 based on both.
  But V3's vector clock may not dominate S1 and S2 individually.
  → System thinks V3 is concurrent with S1/S2 → more siblings!

Dotted version vector fix:
  V3 carries a causal context listing the exact dots it has seen.
  System prunes all siblings covered by V3's causal context.
  → Sibling count stays bounded.
```

**Used by:** Riak 2.0+ (replaced traditional vector clocks with dotted version vectors).

---

### Strategy Comparison

| Strategy | Data Loss? | Complexity | Coordination Needed? | Best For |
|---|---|---|---|---|
| **LWW** | Yes | Very low | None | Caches, last-seen status, idempotent writes |
| **Client-side Semantic Merge** | No (if done right) | High (app logic) | None (at write time) | Shopping carts, user profiles, domain-specific data |
| **CRDTs** | No | Medium (library) | None | Counters, sets, collaborative text, flags |
| **Read Repair** | No | Low | None | Passive consistency healing on reads |
| **Anti-Entropy** | No | Medium (infra) | Background only | Full replica convergence, cold data |
| **Sibling Retention** | No | Medium | None | Deferred resolution, zero-loss storage |
| **Server-side Merge** | Maybe | Medium | None | Numeric fields, sets, simple deterministic rules |
| **OT / Event Merging** | No | Very high | Central sequencer (usually) | Real-time collaborative editing |
| **Dotted Version Vectors** | No | Medium | None | Preventing sibling explosion in sibling-based systems |

---

## After Reconciliation

Once conflicts are resolved, the reconciled value gets a **new vector clock** that dominates both conflicting versions:

```
Conflict:
  V_a = [A:2, B:1, C:0]
  V_b = [A:1, B:2, C:0]

Reconciled (by node C):
  V_merged = [A:2, B:2, C:1]   ← element-wise max, then C increments its own
```

Now `V_merged` dominates both `V_a` and `V_b`, so any future comparison knows these conflicts have been resolved.

---

## Practical Example: Dynamo-Style System

```
1. Client writes key "user:42" to Node A
   Node A: value="Alice", clock=[A:1]

2. Network partition — Nodes A and B can't communicate

3. Client writes to Node A:  value="Alice V2",  clock=[A:2]
   Client writes to Node B:  value="Alice V3",  clock=[B:1]

4. Partition heals. System compares clocks:
   [A:2] vs [B:1] → concurrent (conflict!)

5. On next read, client receives BOTH versions as siblings.
   Client merges them → writes back merged result.
   New clock: [A:2, B:1, C:1] (or whichever node coordinates)
```

---

## Trade-offs

| Advantage | Limitation |
|---|---|
| Accurately captures causality | Vector size grows with number of nodes |
| Detects true conflicts (no false negatives) | Requires reconciliation logic for concurrent writes |
| No dependency on synchronized physical clocks | Siblings can accumulate if conflicts aren't resolved promptly |

**Clock pruning** is used in practice to prevent unbounded growth — old entries for nodes that haven't participated recently are truncated, at the cost of occasional false conflicts.

---

## Key Insight

**Vector clocks don't resolve conflicts — they only:**

1. **Detect causality** — determine if one event happened before another.
2. **Identify conflicts** — flag concurrent updates that have no causal ordering.

Reconciliation is a **separate design decision** entirely. The choice of strategy depends on your data model, consistency requirements, tolerance for data loss, and system architecture. There is no single "best" strategy — most production systems combine multiple approaches (e.g., CRDTs for counters + semantic merge for complex objects + read repair for background healing).

---

## Summary

- **Vector clocks** track causal relationships between events across distributed nodes.
- **Conflict detection** = comparing two vector clocks: if neither dominates, the writes are concurrent and conflicting.
- **Reconciliation** = choosing how to merge conflicting versions — via LWW, application logic, CRDTs, or other strategies.
- The merged result gets a new vector clock that dominates all prior versions, preventing re-detection of the same conflict.
