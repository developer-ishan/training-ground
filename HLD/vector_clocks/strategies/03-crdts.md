# Strategy 3: CRDTs (Conflict-free Replicated Data Types)

[Back to Vector Clocks Overview](../vector-clocks.md)

---

## Overview

CRDTs eliminate the need for reconciliation logic entirely by designing the **data structure itself** so that concurrent updates always converge to the same result, regardless of order or duplication.

**The mathematical guarantee:** A CRDT's merge function is:
- **Commutative**: `merge(A, B) = merge(B, A)` — order doesn't matter.
- **Associative**: `merge(A, merge(B, C)) = merge(merge(A, B), C)` — grouping doesn't matter.
- **Idempotent**: `merge(A, A) = A` — duplicates don't matter.

This means replicas can sync in any order, at any time, with any amount of duplication, and they will **always converge**.

---

## Common CRDT Types

| CRDT | What it does | How conflicts resolve |
|---|---|---|
| **G-Counter** (Grow-only Counter) | Each node maintains its own counter; total = sum of all nodes | No conflict possible — counters only grow |
| **PN-Counter** | Two G-Counters: one for increments, one for decrements | Subtract decrement counter from increment counter |
| **G-Set** (Grow-only Set) | Elements can be added, never removed | Union of all additions |
| **OR-Set** (Observed-Remove Set) | Elements can be added and removed; each add is tagged with a unique ID | Add wins over concurrent remove (add-wins semantics) |
| **LWW-Register** | Single value with a timestamp | Higher timestamp wins (like LWW, but as a formal CRDT) |
| **MV-Register** (Multi-Value Register) | Stores concurrent values | Returns all concurrent values (like siblings) |
| **RGA** (Replicated Growable Array) | Ordered list / text sequence | Unique IDs + causal ordering resolve insert position |

---

## Examples

### G-Counter

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

### OR-Set (add-wins)

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

---

## When to Use CRDTs

- High-availability systems where nodes must accept writes during partitions.
- Data types that map naturally to CRDTs (counters, sets, flags, text).
- You want zero-coordination, automatic convergence.

## Drawbacks

- **Limited data types** — not every domain model maps to a known CRDT. Complex objects require composition of multiple CRDTs.
- **Metadata overhead** — OR-Sets track unique tags per element per add; tombstones and version vectors consume memory.
- **No "true" deletes in some CRDTs** — tombstones (markers for deleted items) may need garbage collection.
- **Semantic limitations** — CRDTs resolve structural conflicts, but they can't enforce business invariants (e.g., "balance must not go negative").

---

## Real-World Use Case: SoundCloud — Distributed Play Counters

### The System

**SoundCloud** serves hundreds of millions of track plays per day. Every time someone presses play, a counter increments. These counters are displayed on every track page and are used for royalty calculations, charts, and recommendations.

### The Problem

Play count increments arrive at different data centers simultaneously:

```
Track "Summer Hit" currently at 1,000,000 plays.

Data Center EU:  +142 plays in the last second
Data Center US:  +287 plays in the last second
Data Center Asia: +93 plays in the last second

If these are concurrent (partition or async replication):
  LWW would pick ONE → lose the other counts
  Last value = 1,000,287 instead of correct 1,000,522
```

### How CRDTs Solve It

SoundCloud uses a **PN-Counter CRDT** (based on the G-Counter pattern):

```
Each data center maintains its own increment vector:

             EU    US    Asia
EU sees:   [142,    0,    0]   → local total delta = 142
US sees:   [  0,  287,    0]   → local total delta = 287
Asia sees: [  0,    0,   93]   → local total delta = 93

After merge (element-wise max):
All DCs:   [142,  287,   93]   → total delta = 522

Final count: 1,000,000 + 522 = 1,000,522  ✅

Order of sync doesn't matter:
  EU syncs with Asia first → [142, 0, 93] → delta = 235
  Then syncs with US       → [142, 287, 93] → delta = 522  ✅ Same result.
```

### Why CRDTs Fit

- **No coordination required** — each DC increments independently with zero inter-DC communication at write time.
- **Partition tolerant** — during a network split, all DCs continue counting. When the partition heals, counters merge correctly.
- **Idempotent** — if a sync message is delivered twice, max-merge ignores duplicates.
- **Accuracy matters** — play counts drive royalty payments. Losing increments means paying artists less. LWW is unacceptable.

### Production Details

- SoundCloud's system processes **~4 billion counter events per day**.
- Counters converge within seconds under normal operation, and within minutes after a partition heals.
- They use Riak as the underlying store, leveraging Riak's built-in CRDT counter support.

---

## Real-World Use Case: Figma — Collaborative Design Tool

### The System

**Figma** allows multiple designers to edit the same design file simultaneously in real-time. Each user's edits must be reflected on every other user's screen without conflicts.

### How CRDTs Solve It

Figma uses a **custom CRDT** for their design document model:

```
User A: Moves rectangle to position (100, 200)
User B: Changes rectangle color to red
User C: Resizes rectangle to 50x50

All three are concurrent. With CRDTs:
  - Position is an LWW-Register → last mover wins
  - Color is an LWW-Register → last picker wins
  - Size is an LWW-Register → last resizer wins

Since all three edit DIFFERENT properties, all changes are preserved:
  Rectangle: position=(100,200), color=red, size=50x50  ✅
```

For the same property edited concurrently:

```
User A: Moves rectangle to (100, 200)
User B: Moves rectangle to (300, 400)

LWW-Register: higher timestamp wins → (300, 400)
User A sees their move "overridden" — acceptable UX for design tools.
```

### Why CRDTs Fit

- **Real-time collaboration** requires zero-latency local edits. Every user edits optimistically; CRDTs guarantee convergence.
- **Offline support** — users can edit while disconnected. Changes merge cleanly when they reconnect.
- **No central server bottleneck** — the CRDT merge can happen peer-to-peer or via a lightweight relay.

---

## Real-World Use Case: Redis Enterprise — Multi-Region Active-Active

### The System

**Redis Enterprise** uses CRDTs for its **Active-Active Geo-Distribution** feature. Multiple Redis instances across different regions accept writes independently and converge automatically.

### CRDT types in Redis Enterprise:

| Redis Type | CRDT Used | Merge Behavior |
|---|---|---|
| Strings | LWW-Register | Latest timestamp wins |
| Counters | PN-Counter | Sum across all regions |
| Sets | OR-Set | Add-wins; concurrent add and remove → add wins |
| Sorted Sets | OR-Set + LWW per score | Add-wins for membership, LWW for scores |
| Lists | Custom RGA-based | Append-only with causal ordering |

### Example: Global Rate Limiter

```
API rate limit: 1000 requests/minute per user.

Region US: user made 400 requests
Region EU: user made 350 requests
Region Asia: user made 200 requests

PN-Counter merge: 400 + 350 + 200 = 950
→ User is near limit. Next request from any region pushes to 951.

Without CRDTs (each region tracks independently):
  Each region thinks user made ~350 requests → allows 650 more each
  User could make 1950 requests total → 2x the limit ❌
```

---

## Systems That Use CRDTs

| System | CRDT Types | Use Case |
|---|---|---|
| **Riak** | Counters, Sets, Maps, Flags, Registers | General-purpose distributed KV |
| **Redis Enterprise** | Counters, Sets, Sorted Sets, Strings | Multi-region active-active |
| **SoundCloud** | PN-Counters | Play counts, like counts |
| **Figma** | Custom document CRDT | Real-time collaborative design |
| **Automerge** | JSON-like document CRDT | Local-first applications |
| **Yjs** | Text, Array, Map CRDTs | Collaborative text editing |
| **Apple (CoreData CloudKit)** | LWW-Registers | Cross-device sync |
| **TomTom** | Custom map CRDTs | Navigation data sync across devices |

---

## Key Takeaway

CRDTs are the gold standard for **zero-coordination conflict resolution**. If your data model fits a known CRDT type, you get automatic convergence with no data loss and no application-side merge logic. The cost is metadata overhead and the constraint that not every business rule can be expressed as a CRDT. In practice, most systems use CRDTs for the "easy" data (counters, sets, flags) and fall back to application-level resolution for complex domain objects.
