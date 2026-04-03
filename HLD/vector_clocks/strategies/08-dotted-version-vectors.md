# Strategy 8: Version Vectors with Causal Context (Dotted Version Vectors)

[Back to Vector Clocks Overview](../vector-clocks.md)

---

## Overview

An evolution of basic vector clocks that solves the **sibling explosion** problem. Standard vector clocks can generate false concurrency when a client reads one sibling, resolves the conflict, and writes back — but the system can't tell which siblings the client already saw.

**Dotted version vectors** attach a precise "dot" (node + sequence number) to each write, so the system knows exactly which events a new write has observed and superseded.

---

## The Problem with Standard Vector Clocks

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

### Detailed Example of the Problem

```
System with 3 vnodes: X, Y, Z.
Key "user:42" is replicated across all three.

Step 1: Client writes via vnode X.
  X stores: value="Alice", clock=[X:1]
  Replicates to Y and Z.

Step 2: Network partition. Two concurrent writes:
  Client A writes via X: value="Alice-A", clock=[X:2]
  Client B writes via Y: value="Alice-B", clock=[Y:1]

Step 3: Partition heals. Key has 2 siblings:
  Sibling 1: "Alice-A", clock=[X:2]
  Sibling 2: "Alice-B", clock=[Y:1]

Step 4: Client C reads both siblings, merges them, writes back via Z:
  Merged: "Alice-AB", clock=[X:2, Y:1]   ← client's causal context

  WITH STANDARD VECTOR CLOCKS:
    Z assigns clock=[X:2, Y:1, Z:1]
    But now compare with sibling 1: [X:2] vs [X:2, Y:1, Z:1]
    [X:2] is NOT dominated (missing Y:1 and Z:1 entries).
    Depending on implementation, system may think S1 is concurrent with V3.
    → False sibling! S1 reappears alongside the merge result.

  WITH DOTTED VERSION VECTORS:
    The write carries a causal context: {(X,2), (Y,1)} — the exact dots it saw.
    Z adds its own dot: (Z,1).
    System prunes any sibling whose dot is in the causal context.
    S1's dot (X,2) is in context → pruned.
    S2's dot (Y,1) is in context → pruned.
    Only the merged value survives. ✅
```

---

## How Dotted Version Vectors Work

A dotted version vector has two components:

1. **Causal context** (a version vector): represents everything the write has "seen" — the cumulative knowledge.
2. **Dot** (a single event identifier: `(node, sequence)`): uniquely identifies this specific write.

```
Write by client C via vnode Z:
  Causal context: [X:2, Y:1]     ← "I've seen everything up to X:2 and Y:1"
  Dot: (Z, 1)                     ← "This specific write is Z's 1st event"

Full DVV: { context: [X:2, Y:1], dot: (Z, 1) }
```

**Pruning rule:** When a new write arrives, discard any existing sibling whose dot is dominated by the new write's causal context.

---

## When to Use It

- You're building a Dynamo-style system with sibling retention.
- You need to prevent sibling explosion from client read-modify-write cycles.
- You want precise causal tracking without over-reporting concurrency.

## Drawbacks

- More complex implementation than basic vector clocks.
- Requires the server to maintain per-key event history (dots).
- Clients must echo back the causal context they received on reads when writing.

---

## Real-World Use Case: Riak 2.0+ — Replacing Vector Clocks with DVVs

### The System

**Riak** is the most prominent system to adopt dotted version vectors in production. In **Riak 2.0** (released 2014), the Riak team replaced traditional vector clocks with DVVs as the default versioning mechanism for all keys.

### The Problem Riak Faced

Before DVVs, Riak suffered from **sibling explosion** in production:

```
Pre-DVV Riak (version 1.x):

Scenario: High-traffic key with frequent concurrent writes.

T=0: Key has 2 siblings (from a genuine concurrent write).
T=1: Client A reads both siblings, merges, writes back.
     But Riak can't tell which siblings Client A saw.
     Riak compares the new write's vector clock against existing siblings.
     Some siblings appear "concurrent" with the new write → NOT pruned.
     Key now has 3 siblings (2 original + 1 merged).

T=2: Client B reads 3 siblings, merges, writes back.
     Same problem → 4 siblings.

T=3: ...5 siblings.

T=100: ...102 siblings. Key is essentially unusable.
       Each read returns 102 versions. Memory explodes.
```

This was a **real operational crisis** for Riak users:
- Keys that should have had 1-2 siblings grew to hundreds.
- Read latency spiked because the server had to serialize and transmit all siblings.
- Memory usage grew unboundedly for hot keys.
- Riak had to introduce `max_siblings` as a safety valve — a crude cap that discards siblings via LWW when the count exceeds the limit.

### The DVV Solution

```
Post-DVV Riak (version 2.0+):

T=0: Key has 2 siblings:
  Sibling 1: "v1", dot=(X, 5)
  Sibling 2: "v2", dot=(Y, 3)

T=1: Client A reads both siblings. Riak returns:
  Data: [sibling1, sibling2]
  Causal context: [X:5, Y:3]    ← "you've seen up to X:5 and Y:3"

T=2: Client A merges and writes back:
  New value: "merged"
  Echoed causal context: [X:5, Y:3]   ← "I've seen these"
  Riak assigns new dot: (Z, 7)

T=3: Riak checks each existing sibling:
  Sibling 1's dot (X,5): X:5 ≤ context[X]=5 → PRUNED ✅
  Sibling 2's dot (Y,3): Y:3 ≤ context[Y]=3 → PRUNED ✅
  
  Only the merged value remains. Sibling count: 1.

T=4: Even with another concurrent write:
  New write: dot=(W, 2), context=[X:4, Y:2]   ← didn't see latest
  
  Merged value's dot (Z,7): Z:7 NOT in new write's context → keep
  New write's dot (W,2): NOT in merged value's context → keep
  
  Exactly 2 siblings. Genuine concurrency, correctly detected. ✅
```

### Production Impact

The Riak team published benchmarks showing:

| Metric | Before DVVs (Riak 1.x) | After DVVs (Riak 2.0) |
|---|---|---|
| Max siblings (hot key, 1hr) | 500+ (unbounded) | 2-3 (bounded by actual concurrency) |
| Read latency (p99, key with siblings) | ~200ms (serializing hundreds of siblings) | ~5ms (1-3 siblings) |
| Memory per key (worst case) | ~50KB (hundreds of siblings) | ~500B (a few siblings) |
| Need for `max_siblings` safety valve | Required (set to 100) | Optional (rarely hit) |

### How Riak Implements DVVs

Riak's DVV implementation is called **DVVSet** (Dotted Version Vector Set):

```erlang
%% Riak's DVV representation (simplified):
-record(dvvset, {
    entries :: [{Actor, Counter, [Value]}],  % per-actor: latest counter + values
    clock   :: [{Actor, Counter}]            % causal context (version vector)
}).

%% On write:
%% 1. Client echoes the causal context from its last read.
%% 2. Server creates a new dot for the writing vnode.
%% 3. Server prunes all entries whose dots are dominated by the context.
%% 4. Server adds the new value under the new dot.
```

### Configuration in Riak 2.0+

```
%% DVVs are enabled by default for all bucket types in Riak 2.0+.
%% No configuration needed — it just works.

%% Creating a bucket type with sibling retention:
riak-admin bucket-type create my_type '{"props":{"allow_mult":true}}'
riak-admin bucket-type activate my_type

%% DVVs handle the versioning automatically.
%% Siblings are only created for genuinely concurrent writes.
%% Read-modify-write cycles no longer cause sibling explosion.
```

---

## Real-World Use Case: Distributed Databases Research — Improving on Dynamo

### Academic Origin

DVVs were formalized in a 2010 paper by **Preguiça, Baquero, Almeida, Fonte, and Gonçalves** at the University of Minho (Portugal). The paper specifically targeted the sibling explosion problem observed in Dynamo-style systems.

### Key Insight from the Paper

The authors demonstrated that:

```
Traditional vector clocks conflate two purposes:
  1. Tracking causal history (what events have been seen).
  2. Identifying a specific event (this particular write).

By separating these into:
  1. Causal context (version vector) — what the writer saw.
  2. Dot (node, sequence) — this specific write.

...the system can precisely determine which siblings a new write supersedes,
eliminating false concurrency detection.
```

### Adoption Beyond Riak

| System | DVV Adoption | Details |
|---|---|---|
| **Riak 2.0+** | Full adoption (default) | Replaced vector clocks entirely; called DVVSet |
| **Antidote DB** | Uses DVVs | Research database for CRDTs; built by the DVV authors' group |
| **Akka Distributed Data** | DVV-inspired | Uses "version vectors" with dot-like semantics for CRDT replication |
| **Research prototypes** | Various | Many Dynamo-inspired research systems adopted DVVs post-2010 |

---

## DVVs vs Other Versioning Mechanisms

| Mechanism | Sibling Explosion? | False Concurrency? | Complexity |
|---|---|---|---|
| **Timestamps (LWW)** | No siblings at all | N/A (no concurrency tracking) | Very low |
| **Vector Clocks** | Yes (read-modify-write cycles cause growth) | Yes (can't distinguish "seen" from "unseen" siblings) | Medium |
| **Dotted Version Vectors** | No (precise pruning via causal context) | No (dots uniquely identify each write) | Medium-high |
| **Interval Tree Clocks** | No | No | High (supports dynamic node join/leave) |

---

## Key Takeaway

Dotted version vectors are a **surgical fix** for the biggest practical problem with vector clocks in Dynamo-style systems: sibling explosion from read-modify-write cycles. By separating "what I've seen" (causal context) from "who I am" (dot), DVVs let the system precisely prune obsolete siblings while preserving genuinely concurrent writes. Riak 2.0's adoption proved the approach in production, turning unbounded sibling growth into a bounded, predictable system. If you're building any system that stores siblings, DVVs should be the default versioning mechanism — not traditional vector clocks.
