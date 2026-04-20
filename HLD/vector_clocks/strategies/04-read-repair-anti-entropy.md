# Strategy 4: Read Repair and Anti-Entropy

[Back to Vector Clocks Overview](../vector-clocks.md)

---

## Overview

These are **background reconciliation mechanisms** that ensure replicas converge even without explicit conflict resolution by the client. They work alongside the other strategies (LWW, semantic merge, CRDTs), not as standalone replacements.

- **Read Repair**: Fix stale replicas lazily, during normal read operations.
- **Anti-Entropy**: Fix stale replicas proactively, via background Merkle tree comparison.

---

## Read Repair

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

---

## Anti-Entropy (Merkle Tree Sync)

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
- Reconciliation still requires one of the other strategies (LWW, semantic merge, CRDT) for truly concurrent versions.

---

## Real-World Use Case: Apache Cassandra — Cluster-Wide Consistency Healing

### The System

**Apache Cassandra** uses both read repair and anti-entropy as core consistency mechanisms. Cassandra is designed for **eventual consistency** across globally distributed clusters — writes go to a subset of replicas, and the rest catch up asynchronously.

### Read Repair in Cassandra

Cassandra's coordinator queries replicas on every read (at configurable consistency levels). When responses diverge:

```
Cluster: 3 replicas (A, B, C) for partition key "user:42"
Consistency Level: QUORUM (need 2 of 3 to respond)

1. Coordinator sends read to A, B, C.
2. A responds immediately: row version with timestamp T=500
3. B responds: row version with timestamp T=450 (stale — missed a recent write)
4. C hasn't responded yet (slow/partitioned).

Coordinator:
  - Returns T=500 data to client (latest among responses).
  - Sends T=500 data to Node B in background → READ REPAIR.
  - Node B updates its local copy.
```

**Cassandra's read repair modes:**
- `read_repair_chance` (pre-4.0): probabilistic — only triggers repair on X% of reads to reduce overhead.
- **Blocking read repair**: waits for repair before responding (stronger consistency, higher latency).
- **Non-blocking read repair**: repairs in background after responding (lower latency, briefly inconsistent).

### Real Production Impact

At **Netflix** (one of Cassandra's largest users):
- Read repair catches ~99% of staleness on **hot keys** (frequently accessed data like user profiles, viewing history).
- But **cold keys** (rarely accessed data like old order records) can remain stale for days without read repair, because nobody reads them.

This is where anti-entropy fills the gap.

### Anti-Entropy in Cassandra: `nodetool repair`

Cassandra's anti-entropy uses **Merkle trees** built over SSTables (sorted string tables — Cassandra's on-disk storage format):

```
Node A builds Merkle tree over its data for token range [0, 1000]:
  Root: hash(left, right)
  ├── Left:  hash of keys [0-500]
  └── Right: hash of keys [501-1000]
       ├── hash of keys [501-750]
       └── hash of keys [751-1000]  ← differs from Node B

Node B builds same tree.

Exchange roots → different.
Walk down → only keys [751-1000] differ.
Exchange ~50 keys instead of ~1000.  ← 95% bandwidth saved.
```

**In production:**
- Cassandra operators run `nodetool repair` on a scheduled basis (daily or weekly) to ensure all replicas converge.
- **Incremental repair** (Cassandra 4.0+) only processes SSTables that have changed since the last repair, dramatically reducing I/O.
- Full anti-entropy repair of a large cluster (hundreds of TB) can take hours. Incremental repair reduces this to minutes.

### Real Numbers from Production

| Metric | Value |
|---|---|
| Netflix Cassandra nodes | ~15,000+ |
| Read repair coverage | ~99% of hot data converges within 1 read |
| Anti-entropy repair frequency | Daily incremental, weekly full |
| Time for full repair (1TB node) | ~2-4 hours |
| Time for incremental repair | ~5-15 minutes |
| Bandwidth saved by Merkle trees | ~90-95% vs full data exchange |

---

## Real-World Use Case: Amazon Dynamo — Dual-Layer Consistency

### The System

The original **Dynamo** paper describes both mechanisms working together:

### Read Repair

```
Dynamo read with N=3, R=2 (read from 2 of 3 replicas):

1. Coordinator sends read to replicas A, B, C.
2. A responds: value="v5", clock=[A:5, B:3]
3. B responds: value="v4", clock=[A:4, B:3]  ← stale
4. Coordinator returns "v5" to client.
5. Coordinator sends "v5" to B → repaired.
```

### Anti-Entropy (Merkle Trees)

Dynamo runs background Merkle tree sync between replicas responsible for the same key ranges:

```
Every node maintains a Merkle tree per virtual node (vnode).

Sync process (runs every few minutes):
  1. Node A sends Merkle root for vnode 42 to Node B.
  2. If roots match → everything in sync, done.
  3. If roots differ → walk the tree, find divergent keys.
  4. Exchange only divergent keys.
  5. Apply reconciliation (LWW or return siblings, depending on config).
```

### Why Both?

| Mechanism | Covers | Latency | Cost |
|---|---|---|---|
| Read Repair | Hot data (frequently read) | Immediate (on read) | Free (piggybacks on reads) |
| Anti-Entropy | Cold data + hot data | Minutes (background cycle) | CPU + network (Merkle tree build + sync) |

Together, they guarantee that **all data** eventually converges — hot data quickly (via read repair) and cold data eventually (via anti-entropy).

---

## Real-World Use Case: Riak — Active Anti-Entropy (AAE)

### The System

**Riak** introduced **Active Anti-Entropy (AAE)** in version 1.3, which runs continuously rather than on a schedule:

```
Riak AAE process:
  1. Each vnode maintains a persistent Merkle tree (stored as a LevelDB instance).
  2. Trees are updated incrementally on every write (no full rebuild needed).
  3. Background process continuously compares trees between replica vnodes.
  4. Divergent keys are exchanged and reconciled.
  5. Cycle time: configurable, typically completes a full comparison every ~1 hour.
```

**Key improvement over Cassandra's approach:** Riak's AAE is **always-on** and **incremental**. There's no manual `nodetool repair` to schedule — the system self-heals continuously.

---

## Systems That Use Read Repair / Anti-Entropy

| System | Read Repair | Anti-Entropy | Details |
|---|---|---|---|
| **Apache Cassandra** | Yes (blocking/non-blocking) | Yes (Merkle tree, `nodetool repair`) | Most widely used implementation |
| **Amazon Dynamo** | Yes | Yes (Merkle tree background sync) | Original design from the 2007 paper |
| **Riak** | Yes | Yes (Active Anti-Entropy, always-on) | Continuous, incremental Merkle trees |
| **ScyllaDB** | Yes | Yes (row-level repair) | Cassandra-compatible with optimizations |
| **Voldemort** | Yes | Yes | LinkedIn's Dynamo-inspired store |

---

## Key Takeaway

Read repair and anti-entropy are **complementary, not competing** mechanisms. Read repair is cheap and fast but only covers data that's actively read. Anti-entropy is thorough but adds background CPU and network cost. Production systems use **both** to ensure full convergence: read repair for the fast path, anti-entropy as the safety net that catches everything else. Neither resolves true concurrent conflicts on its own — they still rely on LWW, semantic merge, or CRDTs for the actual reconciliation logic.
