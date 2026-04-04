# Strategy 1: Last-Writer-Wins (LWW)

[Back to Vector Clocks Overview](../vector-clocks.md)

---

## Overview

The simplest reconciliation strategy. Each write is tagged with a **physical timestamp** (or a logical tiebreaker like a node ID). When two versions conflict, the one with the higher timestamp wins — the other is **silently discarded**.

---

## How It Works

```
Node A writes at T=100:  value="Alice",  clock=[A:2, B:1]
Node B writes at T=105:  value="Bob",    clock=[A:1, B:2]

Clocks are concurrent (conflict detected).
T=105 > T=100 → "Bob" wins. "Alice" is discarded forever.
```

---

## When to Use It

- Data where "latest value" is all that matters (e.g., a user's last-seen status, sensor readings, cache entries).
- Situations where simplicity and availability outweigh correctness of every write.

## Drawbacks

- **Data loss** — concurrent writes are silently dropped, not merged.
- **Clock skew** — if Node A's clock is 5 seconds ahead, its writes always win regardless of true ordering.
- **No composability** — you can't LWW a shopping cart or a collaborative document; independent additions would overwrite each other.

---

## Real-World Use Case: Cassandra — User Session and Profile Storage

### The System

**Apache Cassandra** uses LWW as its **default** conflict resolution strategy. Every cell (column value) in Cassandra carries a client-supplied or server-generated microsecond timestamp. When two replicas hold different values for the same cell, the one with the higher timestamp wins during reads and compaction. There is no merge — the loser is garbage collected.

### Why LWW Fits

Cassandra was designed for workloads at companies like **Netflix, Instagram, and Apple** where:

- **User profile updates** — a user changes their display name from "Alice" to "Alice M." on two devices near-simultaneously. It doesn't matter which one "wins" — both are valid, and the user will simply see whichever was slightly later. There's no meaningful way to merge two full-name strings.
- **Session metadata** — "last active at", "last IP address", "last device" are all naturally LWW: you only care about the most recent value.
- **Sensor / IoT telemetry** — a temperature sensor reports 72.1°F and 72.3°F concurrently from different collection points. Either value is fine; no merge is meaningful.

### How It Works in Practice

```
                    Cassandra Cluster (RF=3)
                    ┌──────────────────────┐
                    │                      │
                    │   Node 1   Node 2    │
                    │   Node 3             │
                    └──────────────────────┘

1. Client writes: UPDATE users SET name='Alice M.' WHERE id=42;
   Timestamp attached: T=1712345678000001

2. Another client writes (concurrent, different coordinator):
   UPDATE users SET name='Alice Marie' WHERE id=42;
   Timestamp attached: T=1712345678000005

3. During read or compaction, Cassandra compares timestamps:
   T=...0005 > T=...0001 → "Alice Marie" wins.
   "Alice M." is discarded permanently.

4. No application logic needed. No siblings. No merge callbacks.
```

### Real Numbers

- Netflix runs **~500 Cassandra clusters** across tens of thousands of nodes. Nearly all use LWW. They accept the occasional "lost" concurrent write because their use cases (viewing history, bookmarks, session state) are all naturally idempotent or last-value-wins.
- Instagram used Cassandra for storing user feed data — if two writes to the same feed entry collide, the later timestamp wins. Feed items are inherently replaceable.

### Handling Clock Skew

Cassandra mitigates clock skew issues with:
- **NTP synchronization** across all nodes (typical skew < 10ms).
- **Client-supplied timestamps** — the application can attach its own monotonic timestamp, bypassing server clock drift.
- **Last-write-wins-per-column** — different columns of the same row can have different "winners", so a concurrent update to `name` and `email` on different nodes both survive (they're independent cells).

### When It Breaks

LWW fails badly for **additive** or **composable** data:
- Two users add different items to a shopping cart → one user's items vanish.
- Two users both append comments to a thread → one comment disappears.
- Two increments to a counter → one increment is lost.

For these cases, Cassandra offers **counter columns** (a built-in CRDT) or recommends application-level merge logic.

---

## Systems That Use LWW

| System | Details |
|---|---|
| **Apache Cassandra** | Default for all regular columns; client or server timestamps |
| **Amazon DynamoDB** | Optional; can be configured as default resolution |
| **Riak** | Optional via `allow_mult=false` (default pre-2.0) |
| **ScyllaDB** | Cassandra-compatible, same LWW semantics |
| **ArangoDB** | Uses LWW for multi-datacenter replication conflicts |

---

## Key Takeaway

LWW trades **correctness** (potential data loss) for **simplicity** (zero application-side conflict handling). It's the right choice when your data is naturally "last value wins" — status fields, timestamps, cache entries, sensor readings — and the wrong choice for anything that accumulates state over time.
