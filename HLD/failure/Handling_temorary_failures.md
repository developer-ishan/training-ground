# Handling Temporary Failures in Distributed Systems

[← Failure Detection](Failure_Detection.md) | [Handling Permanent Failures →](Handling_permanent_failuers.md)

---

## The Problem

A node goes down — maybe a server reboots, a GC pause stalls it for 30 seconds, a network cable gets bumped, or a data center switch flaps. The node *will come back*, usually within seconds to minutes. But right now, it's unreachable.

The system has a choice:

```
Option A: STOP and wait for the node to come back.
  ✓ Consistency — no stale reads, no conflicting writes.
  ✗ Availability — every request that touches this node fails.
  ✗ User experience — "Service Unavailable" errors.

Option B: CONTINUE without the failed node.
  ✓ Availability — the system keeps serving requests.
  ✗ Consistency — the failed node misses updates, data diverges.
  ✗ When the node comes back, it has stale data that must be reconciled.
```

This is the CAP theorem in action. Most modern systems choose **Option B** (availability) and use clever techniques to repair the inconsistency when the node returns. This document covers those techniques.

---

## Strict Quorum Refresher

Before understanding how temporary failures are handled, you need to understand **quorum** — the foundation that makes everything else possible.

In a replicated system with **n** replicas, a **quorum** defines the minimum number of replicas that must participate in an operation for it to be considered successful.

```
For a system with n replicas:
  W = number of replicas that must acknowledge a WRITE
  R = number of replicas that must respond to a READ
  
  Guarantee: If W + R > n, every read sees the latest write.
  (Because at least one node participated in BOTH the write and the read.)
```

**Example with n=3, W=2, R=2:**

```
Write "user:42 = Alice" to the system.

  ── write ──► Replica 1  ✓ ACK     ← participated in write
  ── write ──► Replica 2  ✓ ACK     ← participated in write
  ── write ──► Replica 3  ✗ DOWN    ← missed the write

  W=2 achieved. Write succeeds. Replica 3 has stale data.

Later, read "user:42":

  ── read ──► Replica 1  → "Alice"   ← has latest
  ── read ──► Replica 2  → "Alice"   ← has latest  
  ── read ──► Replica 3  → (still down, or returns stale "Bob")

  R=2 achieved with Replicas 1 and 2. Both return "Alice". Correct!

  Even if Replica 3 came back and was included:
  Two out of three say "Alice" — system returns "Alice".
```

**The problem with strict quorum:**

If a node is down and it's one of the designated replicas, you might not be able to form a quorum:

```
n=3, W=2. Designated replicas: [A, B, C]

  A is DOWN. B and C are alive.
  ── write ──► A  ✗ DOWN
  ── write ──► B  ✓ ACK
  ── write ──► C  ✓ ACK
  
  W=2 achieved. Fine.

But what if A AND B are down?
  ── write ──► A  ✗ DOWN
  ── write ──► B  ✗ DOWN
  ── write ──► C  ✓ ACK
  
  Only 1 ACK. W=2 not achieved. Write FAILS.
  System is unavailable for this key range.
```

This is where **sloppy quorum** comes in.

---

## Strategy 1: Sloppy Quorum

**Core Idea:** When the designated replicas aren't available, temporarily use *other healthy nodes* as stand-ins to meet the quorum requirement. This keeps the system available even during partial failures.

### How It Works

In a strict quorum, writes *must* go to the designated replicas (the nodes responsible for a given key). In a sloppy quorum, writes go to the **first n healthy nodes** encountered, even if they aren't the designated replicas.

```
Ring with 7 nodes: A, B, C, D, E, F, G

Key "user:42" is normally replicated on: [A, B, C]  (n=3)

Normal operation (all healthy):
  write ──► A ✓   B ✓   C ✓    (W=2, any 2 ACKs suffice)

Node A is DOWN:
  Strict quorum: write ──► A ✗   B ✓   C ✓   → W=2 OK (just barely)
  
  But sloppy quorum can do better:
  write ──► B ✓   C ✓   D ✓   → W=2 achieved easily.
  D is NOT a designated replica for this key — it's a temporary stand-in.
```

**Nodes A AND B are both DOWN:**

```
Strict quorum:
  write ──► A ✗   B ✗   C ✓   → Only 1 ACK. W=2 FAILS. UNAVAILABLE.

Sloppy quorum:
  write ──► C ✓   D ✓   E ✓   → W=2 achieved. System stays available!
  D and E are temporary homes for data that belongs on A and B.
```

### The Tradeoff

Sloppy quorum trades **consistency** for **availability**:

```
With sloppy quorum during failure:

  Write goes to D (stand-in for A).
  Read comes in for the same key. 
  Read goes to A, B, C (the designated replicas).
  
  A is still down. B and C respond — but B missed the write too!
  Only C has the latest data. 
  
  If R=2 and the read hits B and C:
    B returns stale data, C returns fresh data.
    Which is correct? The system needs version info (vector clocks!) to decide.
  
  If R=2 and the read hits B and A (both down/stale):
    Read fails or returns stale data!
```

**Important:** Sloppy quorum does **not** guarantee that `W + R > n` will ensure reading the latest write. The guarantee only holds for the **designated** replicas. Sloppy quorum is a **durability** mechanism (the data is stored somewhere), not a **consistency** mechanism.

---

## Strategy 2: Hinted Handoff

Hinted handoff is the **companion technique** to sloppy quorum. When a node acts as a temporary stand-in, it holds the data as a **"hint"** — a note that says "this data belongs on Node A, deliver it when A comes back."

### How It Works

```
Step 1: WRITE arrives during failure

  Key "user:42" belongs on [A, B, C]. Node A is down.
  Write goes to B, C, and D (sloppy quorum).
  
  D stores the data in a special "hints" directory:
  
  D's storage:
  ┌──────────────────────────────────────────────┐
  │ Regular data (D's own keys):                 │
  │   "product:99" → { name: "Widget", ... }     │
  │   "order:201"  → { total: 45.00, ... }       │
  │                                              │
  │ Hints (data meant for other nodes):          │
  │   HINT for Node A:                           │
  │   ┌────────────────────────────────────────┐ │
  │   │ target: A                              │ │
  │   │ key: "user:42"                         │ │
  │   │ value: "Alice"                         │ │
  │   │ timestamp: 2024-01-15T10:30:00Z        │ │
  │   │ vector_clock: [A:0, B:5, C:3]          │ │
  │   └────────────────────────────────────────┘ │
  └──────────────────────────────────────────────┘

Step 2: Node A comes back online

  D detects A is alive (via gossip/heartbeat).
  D sends the hint to A:
  
  D ── "Here's a write you missed: user:42 = Alice" ──► A
  A ◄── ACK ─── A stores it.
  
  D deletes the hint. Done.

Step 3: System is back to normal

  All three designated replicas (A, B, C) have the latest data.
  D no longer holds any foreign data.
```

### Concrete Real-World Scenario

```
E-commerce system with 5 database nodes. 
Customer adds item to cart (stored on nodes 1, 2, 3).

  16:30:00  Node 1 reboots for security patch.
  16:30:01  Customer adds "Blue Shirt" to cart.
            → Write goes to Node 2, Node 3, Node 4 (sloppy quorum).
            → Node 4 stores hint: {target: Node 1, data: "Blue Shirt added"}.
  
  16:30:15  Customer adds "Red Hat" to cart.
            → Write goes to Node 2, Node 3, Node 4 (Node 1 still rebooting).
            → Node 4 stores another hint for Node 1.
  
  16:31:30  Node 1 comes back online.
            → Node 4 detects Node 1 is alive.
            → Node 4 sends both hints to Node 1:
               Hint 1: "Blue Shirt added"
               Hint 2: "Red Hat added"
            → Node 1 applies both. Now in sync.
            → Node 4 deletes hints.

  Customer's cart is correct on all replicas. No data lost.
  Customer never saw an error. The failure was invisible.
```

### Edge Cases and Problems

**Hint accumulation:**

```
If Node A is down for a long time (hours/days), hints pile up on D:

  D's hint queue:
    Hint 1:   2KB   (1 minute old)
    Hint 2:   2KB   (2 minutes old)
    ...
    Hint 5000: 2KB  (3 hours old)
    
    Total: 10MB of hints for Node A alone.

If many nodes are down, D could run out of disk space storing hints.
```

**Solution:** Most systems set a **hint TTL** (time to live) and a **max hint size**:

```
Cassandra defaults:
  max_hint_window_in_ms: 10800000  (3 hours)
  
  If Node A is down for > 3 hours:
    - New hints are DROPPED (not stored).
    - When A comes back, it will be missing data.
    - Need a different mechanism to repair: Read Repair or Anti-Entropy 
      (covered below and in Handling Permanent Failures).
```

**Hint delivery failure:**

```
What if D crashes BEFORE delivering hints to A?

  Scenario:
    1. A goes down at T=0.
    2. D stores hints for A.
    3. D crashes at T=5 minutes (before A comes back).
    4. A comes back at T=10 minutes.
    
  The hints on D are lost (unless D's disk survives and D restarts).
  A is now missing updates and doesn't know it.
  
  Solution: Hinted handoff is a BEST-EFFORT mechanism. 
  It's not the only repair path — Read Repair and Anti-Entropy 
  catch what hinted handoff misses.
```

---

## Strategy 3: Read Repair

**Core Idea:** Every time a read operation occurs, the coordinator compares the responses from all replicas. If any replica has stale data, the coordinator sends the latest version to that replica. Reads *repair* inconsistencies as a side effect.

### How It Works

```
Read request for key "user:42" with n=3, R=2:

Step 1: Coordinator sends read to all 3 replicas (even though R=2).

  Coordinator ── read ──► Replica A → "Alice" (clock [A:3, B:2])
  Coordinator ── read ──► Replica B → "Alice" (clock [A:3, B:2])  
  Coordinator ── read ──► Replica C → "Bob"   (clock [A:1, B:2])  ← STALE!

Step 2: Coordinator compares responses.

  A and B: clock [A:3, B:2] — latest version.
  C:       clock [A:1, B:2] — stale (A:1 < A:3).
  
  Coordinator returns "Alice" to the client (R=2 satisfied by A and B).

Step 3: Coordinator sends repair to C (in the background).

  Coordinator ── "Update user:42 to Alice, clock [A:3, B:2]" ──► Replica C
  
  C updates its local copy. Now all 3 replicas are in sync.
```

### Read Repair Strategies

**Full read repair (on every read):**

```
Every single read triggers a comparison across all replicas.

Pros: Maximum consistency — inconsistencies are caught immediately.
Cons: Every read is expensive — must contact ALL replicas, not just R.
      Higher latency (wait for slowest replica).
      High network overhead.
```

**Probabilistic read repair (with a probability p):**

```
Each read triggers a full comparison with probability p (e.g., 10%).

  Read comes in. Random number: 0.07 (7%).
  7% < 10% → YES, do read repair for this read.
  
  Next read. Random number: 0.45 (45%).
  45% ≥ 10% → NO, skip read repair. Just read from R replicas.

Pros: Much lower overhead — only 10% of reads do full comparison.
Cons: Stale data may persist longer (until a read repair "lottery" hits).

Cassandra uses this approach:
  read_repair_chance: 0.1       (10% of reads)
  dclocal_read_repair_chance: 0  (no cross-DC repair by default)
```

### Read Repair: What It Catches and What It Misses

```
CATCHES:
  ✓ Stale replicas for FREQUENTLY READ keys.
    "user:42" is read 1,000 times/day → stale replica repaired quickly.

  ✓ Inconsistencies from hinted handoff failures.
    D crashed before delivering hint to A → next read of the key repairs A.

MISSES:
  ✗ Keys that are NEVER READ (or rarely read).
    "archived_order:9999" hasn't been read in 6 months.
    Even with 100% read repair chance, if nobody reads it, it never gets repaired.
    
  ✗ Keys where ALL replicas have the SAME stale data.
    If all 3 replicas somehow got the same wrong value, read repair 
    can't detect the problem (nothing to compare against).
```

### Concrete Scenario

```
Social media platform — user profile service.

Scenario: Network blip caused Replica C to miss an update to user 42's 
display name (changed from "john_doe" to "johndoe2024").

  Replica A: "johndoe2024"  (correct)
  Replica B: "johndoe2024"  (correct)
  Replica C: "john_doe"     (stale)

Without read repair:
  If a read goes to A and B → returns "johndoe2024" ✓
  If a read goes to A and C → returns "johndoe2024" (A has higher clock) ✓
  If a read goes to B and C → returns "johndoe2024" (B has higher clock) ✓
  
  But Replica C stays stale FOREVER. Over time, more keys drift.
  If C becomes the sole survivor after a disaster, data loss.

With read repair:
  First read that includes C detects the stale value.
  Coordinator sends repair to C in the background.
  C is now up to date. Problem solved.
```

---

## Strategy 4: Anti-Entropy with Merkle Trees (Background Repair)

Read repair only fixes keys that are read. For comprehensive repair (including keys nobody reads), systems use **anti-entropy** — a background process that systematically compares replicas and fixes any differences.

### The Naive Approach (and Why It Fails)

```
Naive approach: Compare EVERY key-value pair between two replicas.

Replica A has 50 million keys.
Replica B has 50 million keys.

Approach 1: Send all of A's data to B for comparison.
  50M keys × 1KB average = 50GB of data transferred.
  Over the network. Every time you want to check.
  Completely impractical.

Approach 2: Send all of A's keys (not values) to B.
  50M keys × 100 bytes = 5GB of key data.
  Better, but still painful at high frequency.
  
  And then for every difference found, you need to exchange values.
  If data has drifted significantly, this is still huge.
```

### Merkle Trees to the Rescue

A **Merkle tree** (hash tree) is a data structure that lets you compare two large datasets by exchanging only **O(log n)** hashes instead of the full datasets.

```
How a Merkle tree works:

  Your key space is divided into ranges (leaves of the tree).
  Each leaf is a HASH of all key-value pairs in that range.
  Each internal node is a HASH of its children.
  The ROOT is a single hash that summarizes the ENTIRE dataset.

Example with 8 key ranges:

                        ROOT
                    hash: a1b2c3
                   /              \
              Node 1              Node 2
           hash: d4e5           hash: f6g7
           /        \           /        \
        Node 3    Node 4    Node 5    Node 6
       hash:x1   hash:y2   hash:z3   hash:w4
       /    \     /    \     /    \     /    \
     L1    L2   L3    L4   L5    L6   L7    L8
     
  L1 = hash of all keys in range [0000-1FFF]
  L2 = hash of all keys in range [2000-3FFF]
  ...
  L8 = hash of all keys in range [E000-FFFF]
```

### Anti-Entropy Comparison Using Merkle Trees

```
Replica A and Replica B both build Merkle trees over their data.

Step 1: Compare roots.

  A's root: a1b2c3
  B's root: a1b2c3
  
  SAME! → A and B are perfectly in sync. Done.
  Total data exchanged: 1 hash (32 bytes). For 50 million keys.

Step 2 (if roots differ): Drill down.

  A's root: a1b2c3
  B's root: x9y8z7    ← DIFFERENT
  
  Compare children:
    A's Node 1: d4e5    B's Node 1: d4e5   ← Same. Left subtree is in sync.
    A's Node 2: f6g7    B's Node 2: k3m4   ← Different! Drill into right subtree.
  
  Compare Node 2's children:
    A's Node 5: z3      B's Node 5: z3     ← Same.
    A's Node 6: w4      B's Node 6: p8     ← Different! Drill deeper.
  
  Compare Node 6's children (leaves):
    A's L7: hash_a      B's L7: hash_a     ← Same.
    A's L8: hash_b      B's L8: hash_c     ← Different!
  
  → Only keys in range L8 [E000-FFFF] need to be compared and synced.
  
  Total data exchanged: ~7 hashes to find the one mismatched range,
  then only the actual differing keys in that range.
  
  If 50 million keys total, and only 100 keys in range L8 are different:
    Exchanged: 7 × 32 bytes + 100 × 1KB = ~100KB
    vs. naive approach: 50GB
```

### Step-by-Step Anti-Entropy Protocol

```
Background anti-entropy process (runs periodically, e.g., every hour):

1. Node A picks a replica partner (Node B) that shares a key range.

2. Both nodes build/update their Merkle trees.
   (Trees are maintained incrementally — updated on every write, not rebuilt.)

3. A sends its root hash to B.

4. B compares:
   - If roots match → "We're in sync!" → Done.
   - If roots differ → B sends back its root + children hashes.

5. A compares children, identifies which subtrees differ.
   A sends back: "Subtrees 2 and 5 differ. Here are my hashes for those."

6. Process continues recursively until leaf nodes are reached.

7. For each differing leaf range, A and B exchange the actual 
   key-value pairs and reconcile:
   - If A has a key B doesn't → send to B.
   - If B has a key A doesn't → send to A.
   - If both have the key but different values → use vector clocks 
     to determine which is newer, or flag as conflict.

8. Both nodes are now in sync for the examined key range.
```

### Advantages and Limitations

```
Advantages:
  ✓ Catches ALL inconsistencies — including keys that are never read.
  ✓ Extremely efficient — O(log n) hashes to identify differences.
  ✓ Works even after extended outages — doesn't depend on hints or reads.
  ✓ Can run in the background without impacting foreground traffic.

Limitations:
  ✗ Merkle tree must be maintained — every write updates the tree.
  ✗ Rebuilding the tree from scratch is expensive (full scan of all data).
  ✗ Tree granularity is a tradeoff:
      - Fine-grained (many leaves) → precise identification of differences, 
        but larger tree, more hashes to compare.
      - Coarse-grained (few leaves) → smaller tree, but once a difference 
        is found, more data in that range to compare.
  ✗ In systems with frequent writes, the tree changes constantly, making 
    snapshot-based comparison tricky.
```

---

## How These Strategies Work Together

These aren't competing strategies — they're **layers of defense**. Each catches what the others miss.

```
Timeline of a node failure and recovery:

T=0     Node A fails (temporary — will come back in 20 minutes).
        │
        ├─ SLOPPY QUORUM kicks in immediately.
        │  Writes that would go to A are sent to stand-in node D.
        │  System remains available. No requests fail.
        │
        ├─ HINTED HANDOFF: D stores hints for A.
        │  Every write meant for A is saved with metadata:
        │  "Deliver to A when it comes back."
        │
T=20m   Node A comes back online.
        │
        ├─ HINTED HANDOFF delivers stored hints.
        │  D sends all accumulated hints to A.
        │  A applies them. ~95% of missed writes are recovered.
        │
        │  But what if some hints were lost? (D ran out of space,
        │  or the hint window expired, or D itself restarted.)
        │
T=21m   A starts serving reads.
        │
        ├─ READ REPAIR catches remaining inconsistencies.
        │  Every read that touches A compares A's data with other replicas.
        │  Stale data on A is repaired on the fly.
        │  Frequently-read keys are fixed within minutes.
        │
        │  But what about rarely-read keys?
        │
T=1h    Background ANTI-ENTROPY runs.
        │
        ├─ MERKLE TREE COMPARISON
        │  A compares its Merkle tree with B's tree.
        │  Finds 47 keys that are still stale (never read, hints were lost).
        │  Syncs those 47 keys. A is now 100% consistent.
        │
T=1h+   System is fully consistent again.
```

```
Defense Layer Summary:

┌─────────────────────┬───────────────┬───────────────┬──────────────────┐
│ Strategy            │ When Active   │ What It Fixes │ Coverage         │
├─────────────────────┼───────────────┼───────────────┼──────────────────┤
│ Sloppy Quorum       │ During failure│ Availability  │ All writes       │
│ Hinted Handoff      │ On recovery   │ Missed writes │ ~95% of writes   │
│ Read Repair         │ On every read │ Stale replicas│ Frequently read  │
│ Anti-Entropy        │ Background    │ All drift     │ 100% (eventual)  │
└─────────────────────┴───────────────┴───────────────┴──────────────────┘
```

---

## Real-World Implementations

### Amazon Dynamo / DynamoDB

```
The Dynamo paper (2007) introduced most of these concepts.

Sloppy Quorum:
  - Called "preference list" — first n healthy nodes in the ring.
  - Default: n=3, W=2, R=2.
  - Coordinators walk the ring to find healthy nodes.

Hinted Handoff:
  - Hints stored locally with TTL.
  - Delivered on periodic checks (every few seconds).
  - If target is still down after TTL → hints discarded, anti-entropy takes over.

Anti-Entropy:
  - Merkle trees per key range per replica pair.
  - Trees built on background compaction.
  - Comparison runs periodically between replicas.

The Dynamo paper's key insight: "Customers should be able to add items 
to their shopping cart even if disks are failing, network routes are 
flapping, or data centers are being destroyed by tornados."
```

### Apache Cassandra

```
Cassandra implements ALL four strategies:

Sloppy Quorum:
  - Not the default! Cassandra uses strict quorum by default.
  - Can be enabled by using consistency level ANY for writes.
  - CL=ANY means "write to ANY node, even if it's a hint on a non-replica."

Hinted Handoff:
  - Enabled by default.
  - max_hint_window_in_ms: 10800000 (3 hours default).
  - Hints stored in system.hints table.
  - Configurable per-endpoint and globally.
  
  Key tuning:
    hinted_handoff_enabled: true
    max_hint_window_in_ms: 10800000
    hinted_handoff_throttle_in_kb: 1024
    max_hints_delivery_threads: 2

Read Repair:
  - read_repair_chance: 0.0 (disabled by default in newer versions)
  - dclocal_read_repair_chance: 0.0
  - Cassandra 4.0+ replaced probabilistic read repair with 
    "monotonic reads" and relies more on anti-entropy repair.

Anti-Entropy Repair:
  - `nodetool repair` — manual or scheduled full repair.
  - Uses Merkle trees built per token range.
  - Incremental repair (Cassandra 4.0+): tracks which SSTables have been 
    repaired, only repairs new/unrepaired data.
  - Recommendation: run repair at least once within gc_grace_seconds 
    (default 10 days) to prevent zombie data resurrection.

  Full repair:
    $ nodetool repair -full keyspace_name

  Incremental repair:
    $ nodetool repair keyspace_name
```

### Riak

```
Riak was one of the closest implementations to the original Dynamo paper.

Sloppy Quorum:
  - Default behavior. n=3, r=2, w=2.
  - Fallback vnodes automatically used when primary vnodes are down.
  - "Sloppy quorum" is the default — you opt INTO strict quorum.

Hinted Handoff:
  - Managed automatically by the "handoff manager."
  - Separate from "ownership handoff" (when ring membership changes).
  - Anti-entropy with AAE (Active Anti-Entropy) trees maintained 
    continuously — not rebuilt from scratch each time.

Read Repair:
  - Enabled by default (100% of reads trigger read repair).
  - Can be tuned per-bucket.
  - Conflicts resolved via vector clocks + configurable resolution 
    strategy (LWW, siblings, CRDTs).

Anti-Entropy:
  - "Active Anti-Entropy" (AAE) — continuously maintained hash trees.
  - Exchanges happen in the background every few minutes.
  - Much more efficient than periodic full rebuilds.
```

### Amazon S3 (Internal)

```
S3 stores trillions of objects across millions of disks.

At this scale:
  - Hardware failures are a constant: multiple disk failures per day, 
    server failures per week, rack failures per month.
  - Hinted handoff alone can't keep up — too many concurrent failures.
  - Anti-entropy runs CONTINUOUSLY, not periodically.
  
S3's approach (simplified from public talks):
  1. Writes are replicated to 3 availability zones synchronously.
  2. If a write can't reach all AZs → buffered and retried (hinted handoff concept).
  3. Background "auditors" continuously walk the data, comparing checksums 
     between replicas (anti-entropy concept).
  4. Objects also have durability checks: each replica is checksummed, 
     and periodic "bit rot" detection runs to find silent data corruption.
  
  Result: 99.999999999% (11 nines) durability. You'd have to wait 
  10 million years to lose a single object.
```

---

## Common Pitfalls

### 1. Relying Only on Hinted Handoff

```
Problem: Hinted handoff has a finite window. If a node is down longer 
than the hint window (e.g., 3 hours in Cassandra), hints for new writes 
are DROPPED. When the node comes back, it's permanently missing data.

Solution: Always pair hinted handoff with anti-entropy repair.
Run repairs on a schedule (e.g., weekly) or after any extended outage.
```

### 2. Hint Storms on Recovery

```
Problem: A node was down for 3 hours. During that time, 10 other nodes 
each accumulated 50,000 hints for it. When it comes back, all 10 nodes 
simultaneously try to deliver 500,000 hints.

The recovering node is overwhelmed:
  - Disk I/O spikes to 100%
  - CPU maxed out processing hints
  - The node crashes again from overload (cascading failure!)

Solution:
  - Throttle hint delivery (Cassandra: hinted_handoff_throttle_in_kb).
  - Stagger hint delivery across sending nodes.
  - Monitor hint delivery progress.
```

### 3. Anti-Entropy Repair Storms

```
Problem: Full anti-entropy repair on a large cluster reads ALL data 
on ALL replicas, builds Merkle trees, and compares them. This can 
saturate network, disk I/O, and CPU.

A repair on a 100-node Cassandra cluster with 10TB per node:
  - Reads 1,000TB of data (100 × 10TB).
  - Transfers potentially hundreds of GB over the network.
  - Can take hours or days.
  - While running, normal read/write latency degrades significantly.

Solution:
  - Use incremental repair (only repair new data since last repair).
  - Repair one token range at a time, not the entire cluster.
  - Schedule repairs during low-traffic periods.
  - Use sub-range repair to parallelize across multiple repair sessions.
```

---

## Key Takeaways

1. **Sloppy quorum keeps you available** — by using healthy stand-in nodes when designated replicas are down, writes never fail (at the cost of consistency guarantees).

2. **Hinted handoff is the fast path for recovery** — temporary nodes store data with "delivery instructions" and forward it when the failed node returns. It's fast but not guaranteed.

3. **Read repair is opportunistic** — it piggybacks on normal reads to fix stale replicas. Great for hot data, useless for cold data.

4. **Anti-entropy is the safety net** — Merkle tree comparison finds and fixes ALL inconsistencies, including in data nobody reads. But it's expensive and runs in the background.

5. **Layer all four strategies** — each catches what the others miss. Sloppy quorum + hinted handoff for immediate availability, read repair for hot data consistency, anti-entropy for comprehensive repair.

6. **Temporary failure handling is about buying time** — these techniques don't prevent inconsistency; they *manage* it. The system tolerates short-term inconsistency to provide continuous availability, then repairs itself over time. This is the essence of **eventual consistency**.

---

*Next: [Handling Permanent Failures →](Handling_permanent_failuers.md)*
