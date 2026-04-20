# Handling Permanent Failures in Distributed Systems

[← Failure Detection](Failure_Detection.md) | [← Handling Temporary Failures](Handling_temorary_failures.md)

---

## The Problem

A temporary failure is a node that will come back — a reboot, a GC pause, a network blip. You wait, deliver hints, repair on reads, and eventually everything is fine.

A **permanent failure** is a node that is **never coming back**. The disk is dead. The machine caught fire. The entire data center flooded. The data on that node is gone forever.

```
Temporary failure:
  Node A: [running] [DOWN for 20 min] [running again — repair via hinted handoff]

Permanent failure:
  Node A: [running] [DEAD — disk destroyed] [........................forever........................]
  
  Hinted handoff? Useless — A is never coming back to receive hints.
  Read repair? Can't repair a node that doesn't exist.
  
  The data that was ONLY on A is at risk. If other replicas also fail 
  before new replicas are created, that data is PERMANENTLY LOST.
```

**The stakes are higher:**

| Temporary Failure | Permanent Failure |
|---|---|
| Data is temporarily unreachable | Data may be permanently lost |
| Hints can be delivered later | No node to deliver hints to |
| Read repair fixes it eventually | Nothing to repair — node is gone |
| Wait and recover | Must actively create new replicas |
| Seconds to hours | The replacement itself takes hours to days |

---

## Why Permanent Failures Are Different

The key difference is **replica count degradation**. In a system with replication factor n=3, every key is stored on 3 nodes. When one node permanently fails:

```
Before failure:
  Key "user:42" stored on: [Node A, Node B, Node C]  → 3 copies ✓

After Node A permanently fails:
  Key "user:42" stored on: [DEAD, Node B, Node C]    → 2 copies ⚠️

If Node B also fails before we react:
  Key "user:42" stored on: [DEAD, DEAD, Node C]      → 1 copy ‼️

If Node C also fails:
  Key "user:42" stored on: [DEAD, DEAD, DEAD]         → 0 copies 💀
  DATA PERMANENTLY LOST.
```

**The race is on:** After a permanent failure, the system must create new replicas *faster* than additional failures can destroy the remaining ones.

**Failure probability math:**

```
Assume each node has a 0.1% daily failure probability.
With n=3 replicas, data is lost if all 3 fail before repair.

If repair takes 0 time (instant): 
  P(data loss) ≈ (0.001)³ = 10⁻⁹ per day (extremely rare)

If repair takes 24 hours:
  During those 24 hours, we only have 2 replicas.
  P(second failure in 24h) = 0.001
  P(data loss) = 0.001 × 0.001 = 10⁻⁶ per day (1000x worse!)

If repair takes 7 days:
  P(second failure in 7 days) = 0.007
  P(data loss) = 0.007 × 0.001 = 7 × 10⁻⁶ (7000x worse than instant!)

Key insight: REPAIR SPEED directly determines durability. 
Faster repair = exponentially better durability.
```

---

## Strategy 1: Replica Replacement and Re-replication

When a node permanently fails, the system must create **new replicas** on surviving nodes to restore the replication factor.

### How It Works

```
System: 6 nodes (A through F), replication factor n=3.
Consistent hashing ring:

        A ──── B ──── C ──── D ──── E ──── F
        
Key ranges and their replicas (simplified):
  Range 1: [A, B, C]
  Range 2: [B, C, D]
  Range 3: [C, D, E]
  Range 4: [D, E, F]
  Range 5: [E, F, A]
  Range 6: [F, A, B]

Node C permanently fails (disk destroyed).

Affected key ranges — anything C was a replica for:
  Range 1: [A, B, C] → [A, B, ???]  — need a new 3rd replica
  Range 2: [B, C, D] → [B, ???, D]  — need a new 2nd replica
  Range 3: [C, D, E] → [???, D, E]  — need a new 1st replica
```

**Step 1: Detect that the failure is permanent**

```
This is the hardest part. How do you know a node is PERMANENTLY dead 
vs. just temporarily down?

Approaches:
  1. TIMEOUT-BASED: If a node has been suspected/down for > X hours 
     (e.g., 24 hours), assume permanent.
     - Simple but can be wrong (node might come back after maintenance).
     
  2. OPERATOR-DRIVEN: A human runs a command to decommission the node.
     $ nodetool removenode <node-id>
     - Most reliable but requires human intervention.
     
  3. HARDWARE-AWARE: Integration with cloud provider APIs.
     AWS tells you the instance was terminated → permanent.
     - Only works in cloud environments.
     
  4. AUTOMATIC (with safeguards): Kubernetes-style — if a node is 
     unreachable for > pod-eviction-timeout AND the node object is 
     deleted, treat as permanent.
```

**Step 2: Identify what data needs re-replication**

```
For each key range that C was responsible for, find surviving replicas:

  Range 1: A and B have the data. Need to copy to a new node.
  Range 2: B and D have the data. Need to copy to a new node.
  Range 3: D and E have the data. Need to copy to a new node.

Which node gets the new replica? 
  → Walk the consistent hash ring to find the next eligible node 
    that doesn't already have a copy.

  Range 1 [A, B, C→?]: Next on ring after C is D. 
    D doesn't have Range 1 → D becomes the new replica.
    
  Range 2 [B, C→?, D]: Next after C is D, but D already has it. 
    Try E. E doesn't have Range 2 → E becomes the new replica.
    
  Range 3 [C→?, D, E]: Next available is F. 
    F becomes the new replica.
```

**Step 3: Copy data to new replicas**

```
Re-replication process:

  For Range 1 (source: A or B → destination: D):
  
  1. Pick the healthiest source (e.g., A — lowest load).
  2. Stream ALL key-value pairs for Range 1 from A to D.
  3. D writes them to disk.
  4. Update the cluster metadata: "D is now a replica for Range 1."
  5. New writes for Range 1 now go to [A, B, D].

This is essentially a FULL DATA COPY for every affected range.
For a node with 2TB of data, this means streaming 2TB over the network.
```

### The Bandwidth Problem

```
Node C had 2TB of data. It was a replica for 1/6 of the key space.
Re-replicating 2TB at 1 Gbps network speed:

  2TB = 2,000 GB = 16,000 Gb
  At 1 Gbps: 16,000 seconds = 4.4 hours
  
  But the network is also serving regular traffic.
  If you use 50% of bandwidth for repair: 8.8 hours.
  
  During those 8.8 hours, affected data has only 2 replicas.
  Every additional failure during this window is catastrophic.

At larger scales (10TB per node):
  At 1 Gbps, 50% bandwidth: 44 hours = nearly 2 days!
  
This is why large-scale systems use PARALLEL re-replication.
```

### Parallel Re-replication (Spreading the Load)

```
Instead of streaming all of C's data from ONE source to ONE destination,
spread the work across MANY nodes:

Sequential (slow):
  A ════════════════════════════════════► D   (2TB, 8.8 hours)

Parallel (fast):
  A ──(Range 1 data)──► D    (333GB each, in parallel)
  B ──(Range 2 data)──► E    
  D ──(Range 3 data)──► F    

  Each stream: 333GB at 50% of 1 Gbps = 1.5 hours
  Total wall-clock time: 1.5 hours (vs. 8.8 hours sequential)
  
Even more parallel with sub-range splitting:
  Split each range into 10 sub-ranges. 
  30 parallel streams, each 67GB.
  Wall-clock time: ~18 minutes.
```

This is why modern systems like HDFS and Cassandra divide data into many small chunks — it enables **massively parallel re-replication** when a node fails.

---

## Strategy 2: Merkle Trees for Consistency Verification

When re-replicating, how do you know the source replica has correct, up-to-date data? What if the source itself has stale or corrupted data? Merkle trees (introduced in Handling Temporary Failures) play a critical role in permanent failure recovery.

### Verification During Re-replication

```
Scenario: Node C permanently failed. Replicating Range 1 from A to D.

But what if A has slightly different data from B for Range 1?
(Maybe A missed a few writes due to an earlier temporary failure.)

Naive approach: Just copy from A. 
  → D gets A's (possibly stale) data. The stale data is now "locked in" 
    on 2 out of 3 replicas. Bad!

Better approach: Use Merkle trees to verify BEFORE copying.

  1. A builds Merkle tree for Range 1.
  2. B builds Merkle tree for Range 1.
  3. Compare trees (only O(log n) hashes exchanged).
  4. If they match → copy from either one. Both are correct.
  5. If they differ → reconcile A and B FIRST:
     a. Find differing keys using tree traversal.
     b. Use vector clocks to determine the correct version of each key.
     c. Update both A and B to have the correct data.
     d. THEN copy the reconciled data to D.
  
  Result: D gets verified, correct data. All replicas are consistent.
```

### Ongoing Integrity Checking

Even after re-replication, Merkle trees continue to serve as a verification mechanism:

```
After recovery, replicas for Range 1 are: [A, B, D]

Periodic anti-entropy (every hour):
  1. A and B compare trees → "In sync? Yes." ✓
  2. A and D compare trees → "In sync? Yes." ✓  
  3. B and D compare trees → "In sync? Yes." ✓

  If any comparison fails, the differing keys are synced immediately.
  
This catches:
  - Silent data corruption (bit rot on disk)
  - Bugs in the replication code
  - Missed writes from network glitches
  - Cosmic ray bit flips (yes, this happens at scale)
```

---

## Strategy 3: Erasure Coding (Space-Efficient Durability)

Full replication (n=3) means storing **3 copies** of every byte. For petabyte-scale systems, this is extremely expensive. Erasure coding provides the same (or better) durability with much less storage overhead.

### The Concept

```
Full replication (n=3):
  Original data: 1 GB
  Storage used:  3 GB (3 full copies)
  Can tolerate:  2 node failures
  Overhead:      3x

Erasure coding (e.g., Reed-Solomon 6+3):
  Original data: 1 GB
  Split into 6 data chunks + 3 parity chunks = 9 chunks
  Each chunk:    ~170 MB
  Storage used:  9 × 170 MB = 1.5 GB
  Can tolerate:  3 node failures (ANY 3 of the 9 chunks can be lost)
  Overhead:      1.5x
  
Same fault tolerance as 3-copy replication, but uses HALF the storage!
```

### How Erasure Coding Works

```
Reed-Solomon (4,2) example — 4 data chunks, 2 parity chunks:

Original file: "ABCDEFGHIJKLMNOP" (16 bytes)

Step 1: Split into 4 data chunks (4 bytes each):
  D1 = "ABCD"
  D2 = "EFGH"
  D3 = "IJKL"
  D4 = "MNOP"

Step 2: Compute 2 parity chunks using polynomial math:
  P1 = f₁(D1, D2, D3, D4) = "QRST"  (computed)
  P2 = f₂(D1, D2, D3, D4) = "UVWX"  (computed)

Step 3: Store 6 chunks on 6 different nodes:
  Node 1: D1 = "ABCD"
  Node 2: D2 = "EFGH"
  Node 3: D3 = "IJKL"
  Node 4: D4 = "MNOP"
  Node 5: P1 = "QRST"
  Node 6: P2 = "UVWX"

Failure scenario — Node 2 and Node 4 permanently fail:

  Surviving chunks: D1, D3, P1, P2 (4 chunks)
  
  With 4 surviving chunks (≥ k=4 original data chunks), we can 
  reconstruct D2 and D4 using the Reed-Solomon decode algorithm.
  
  After reconstruction:
    D2 = "EFGH" (recovered!)
    D4 = "MNOP" (recovered!)
  
  Store reconstructed chunks on new nodes. Full recovery complete.
```

### Replication vs. Erasure Coding: Tradeoffs

```
┌──────────────────────┬──────────────────────┬──────────────────────┐
│ Property             │ Full Replication     │ Erasure Coding       │
│                      │ (n=3)                │ (e.g., RS 6+3)      │
├──────────────────────┼──────────────────────┼──────────────────────┤
│ Storage overhead     │ 3x                   │ 1.5x                 │
│ Fault tolerance      │ 2 failures           │ 3 failures           │
│ Read performance     │ Fast (full copy)     │ Slower (must read    │
│                      │                      │ k chunks + decode)   │
│ Write performance    │ Fast (copy 3x)       │ Slower (encode +     │
│                      │                      │ write n+k chunks)    │
│ Repair speed         │ Fast (copy from      │ Slower (read k       │
│                      │ one replica)         │ chunks, decode,      │
│                      │                      │ re-encode, write)    │
│ Repair bandwidth     │ 1x data size         │ k/n × data size      │
│ CPU cost             │ Minimal              │ Significant (encode/ │
│                      │                      │ decode computation)  │
│ Best for             │ Hot data, low        │ Cold/warm data,      │
│                      │ latency, frequent    │ large storage,       │
│                      │ reads                │ archival, cost-      │
│                      │                      │ sensitive            │
└──────────────────────┴──────────────────────┴──────────────────────┘
```

### When to Use Which

```
Use FULL REPLICATION when:
  - Data is hot (read/written frequently)
  - Latency is critical (< 10ms reads)
  - Dataset is small to medium (< 100 TB)
  - Example: User sessions, shopping carts, real-time leaderboards

Use ERASURE CODING when:
  - Data is cold or warm (infrequent access)
  - Storage cost matters (petabyte scale)
  - Slightly higher latency is acceptable
  - Example: Object storage, backups, media files, logs

Many systems use BOTH:
  - Hot data tier: 3-way replication (fast access)
  - Cold data tier: Erasure coded (cost-efficient)
  - Data "ages" from hot to cold tier over time
```

---

## Strategy 4: Quorum-Based Permanent Failure Recovery

When a node is permanently lost, the system may need to adjust its quorum rules to maintain availability and consistency.

### Dynamic Quorum Reconfiguration

```
Original cluster: 5 nodes, W=3, R=3 (strict majority quorum).

Node E permanently fails. Now 4 nodes remain.

Option 1: Keep W=3, R=3.
  Need 3 out of 4 nodes for every operation.
  If one more node has a hiccup → UNAVAILABLE.
  Very fragile.

Option 2: Reconfigure to W=2, R=3 (or W=3, R=2).
  Still guarantees overlap (W+R = 5 > 4).
  More tolerant of additional failures.
  
  But wait — now reads contact 3 out of 4 nodes, which might 
  include the slow one. Latency impact.

Option 3: Add a replacement node, restore to 5 nodes.
  Best long-term solution.
  But takes time (data streaming, hours).
  Use Option 1 or 2 as a bridge.

Option 4 (Raft/Paxos consensus): Formally remove the failed node 
  from the membership. Now the cluster size IS 4, and majority = 3.
  This is a membership change via consensus protocol.
  
  etcd example:
    $ etcdctl member remove <member-id>
    $ etcdctl member add new-node --peer-urls=http://new-node:2380
```

### Membership Changes in Consensus Systems

```
In Raft-based systems (etcd, CockroachDB), membership changes are 
handled as part of the consensus protocol itself:

Step 1: Detect Node C is permanently dead.
Step 2: Leader proposes a configuration change: "Remove C from cluster."
Step 3: Majority of remaining nodes agree → C is removed.
Step 4: New cluster membership: [A, B, D, E] (4 nodes, majority = 3).
Step 5: Leader proposes: "Add new Node F to cluster."
Step 6: Majority agrees → F joins.
Step 7: Leader streams the full state to F (snapshot + log replay).
Step 8: F is caught up → cluster is back to 5 nodes.

Raft guarantees safety during this process:
  - At no point can two leaders exist.
  - At no point can conflicting writes be accepted.
  - The cluster is available (for reads and writes) throughout, 
    as long as a majority of the CURRENT membership is alive.

Important: Raft uses JOINT CONSENSUS for membership changes — 
the cluster briefly operates with both the old and new membership 
to prevent split-brain scenarios during the transition.
```

---

## Strategy 5: Cross-Datacenter Replication and Geo-Redundancy

Permanent failures aren't just about individual nodes — entire data centers can fail permanently (natural disasters, fire, prolonged power outage, decommissioning).

### Multi-DC Replication Strategies

```
Strategy 1: SYNCHRONOUS cross-DC replication.

  Write request arrives at DC-East:
    DC-East ── write ──► Replica 1 (local)   ✓ ACK
    DC-East ── write ──► Replica 2 (local)   ✓ ACK
    DC-East ── write ──────────────────────► DC-West Replica 3   ✓ ACK (200ms RTT)
    
    Write acknowledged to client only after ALL replicas ACK.
    
  Pros: Strong consistency. If DC-East is nuked, DC-West has everything.
  Cons: Every write has 200ms+ latency (cross-DC round trip).
        If the cross-DC link is slow/broken, writes stall.

Strategy 2: ASYNCHRONOUS cross-DC replication.

  Write request arrives at DC-East:
    DC-East ── write ──► Replica 1 (local)   ✓ ACK
    DC-East ── write ──► Replica 2 (local)   ✓ ACK
    Write acknowledged to client ✓ (fast, local latency only)
    
    In the background:
    DC-East ── replicate ──────────────────► DC-West Replica 3
    (delivered within seconds, but NOT guaranteed before ACK)
    
  Pros: Low write latency (local only). 
        Cross-DC link issues don't block writes.
  Cons: If DC-East is destroyed BEFORE async replication completes,
        the last few seconds of writes are lost.
        RPO (Recovery Point Objective) > 0.

Strategy 3: SEMI-SYNCHRONOUS (majority across DCs).

  3 DCs, 5 replicas total: DC-East (2), DC-West (2), DC-Central (1).
  W=3 (majority of 5).
  
  Write acknowledged after ANY 3 replicas ACK:
    DC-East R1 ✓ + DC-East R2 ✓ + DC-West R1 ✓ = W=3 achieved.
    
  Guarantees at least one remote DC has the write.
  Latency: one cross-DC round trip (unavoidable for durability).
  
  If DC-East is destroyed:
    DC-West and DC-Central have the data.
    Remaining 3 replicas can still form a majority (3 ≥ 3).
    System continues operating!
```

### Disaster Recovery: Full DC Failure

```
Scenario: DC-East (primary) is permanently destroyed.
  DC-East had: 500 servers, 2 PB of data, serving 100K requests/sec.

Recovery timeline:

T=0        DC-East goes dark. All monitoring alerts fire.
           DC-West starts receiving redirected traffic (DNS failover).

T=30s      DNS TTL expires. Clients connect to DC-West.
           DC-West has async-replicated data (maybe 2-5 seconds stale).

T=1m       DC-West is serving all traffic. READ operations work fine.
           WRITE operations work but with reduced redundancy 
           (only 2 local replicas instead of 2 local + 2 remote).

T=5m       Ops team confirms DC-East is permanently lost.
           Initiates re-replication: DC-West → DC-Central.
           Streaming 2 PB of data over dedicated inter-DC links.

T=4h       Re-replication is 10% complete. 
           System is functional but vulnerable.

T=2 days   Re-replication complete. Full redundancy restored.
           DC-Central has full copy. System is healthy again.

T=1 week   New DC-East2 hardware provisioned. 
           Data streamed from DC-West and DC-Central to DC-East2.

T=2 weeks  DC-East2 is fully operational. Original topology restored.
```

---

## Real-World Permanent Failure Handling

### HDFS (Hadoop Distributed File System)

```
HDFS stores files as 128MB blocks, each replicated 3 times.

Failure detection:
  - DataNodes send heartbeats to the NameNode every 3 seconds.
  - If no heartbeat for 10 minutes → DataNode marked dead.

Re-replication:
  - NameNode identifies all blocks that were on the dead node.
  - For each under-replicated block:
    1. Find a surviving replica.
    2. Pick a new target DataNode (considering rack awareness — 
       replicas should span racks for rack-level failure tolerance).
    3. Instruct the source to stream the block to the target.
  
  Parallelism:
    - Multiple blocks are re-replicated simultaneously.
    - Different source-target pairs for different blocks.
    - A DataNode with 10,000 blocks doesn't stream them all from 
      one source — it pulls from many sources in parallel.

  Prioritization:
    - Blocks with only 1 surviving replica (critical!) are repaired first.
    - Blocks with 2 surviving replicas are repaired next.
    - Blocks that are over-replicated (4+ copies) are deprioritized.

Rack awareness example:
  Before failure:
    Block X: [Rack1/Node1, Rack2/Node5, Rack2/Node7]
    
  Node1 permanently fails.
    New replica should go on Rack1 (to maintain cross-rack distribution).
    Chosen: Rack1/Node3.
    
    Block X: [Rack1/Node3, Rack2/Node5, Rack2/Node7]
    → Cross-rack redundancy preserved.
```

### Cassandra

```
Permanent node replacement in Cassandra:

Option 1: Replace the node (same token ranges).
  $ nodetool removenode <dead-node-host-id>
  
  Then bootstrap a new node that takes over the dead node's token ranges:
  
  In cassandra.yaml on the new node:
    replace_address_first_boot: <dead-node-IP>
  
  The new node:
    1. Takes ownership of the dead node's token ranges.
    2. Streams data from surviving replicas for those ranges.
    3. Starts serving traffic once streaming is complete.
  
  Streaming time depends on data volume:
    100 GB → ~15 minutes (at 1 Gbps)
    1 TB   → ~2.5 hours
    10 TB  → ~25 hours

Option 2: Decommission and re-add (rebalances the ring).
  $ nodetool removenode <dead-node-host-id>
  # Token ranges are redistributed among surviving nodes.
  # Then add a fresh node:
  $ # Start Cassandra on new node — it auto-bootstraps.
  
  This is cleaner but causes MORE data movement 
  (all nodes adjust their ranges, not just one replacement).

Post-replacement repair:
  After the new node is bootstrapped, run a full repair to ensure 
  consistency with existing replicas:
  $ nodetool repair -full
```

### Amazon S3

```
S3 achieves 99.999999999% (11 nines) durability. 
That's a 1-in-10-billion chance of losing an object per year.

How they handle permanent failures at massive scale:

1. REDUNDANCY: Each object is stored across ≥ 3 Availability Zones.
   An AZ is an independent data center with its own power, cooling, 
   and networking.

2. ERASURE CODING: Large objects use erasure coding across AZs.
   A 10 MB object might become 14 chunks (10 data + 4 parity) 
   spread across 3 AZs. Can lose any 4 chunks and still recover.

3. CONTINUOUS MONITORING: Background processes ("auditors") constantly 
   verify object integrity:
   - Checksum verification (catches bit rot / silent corruption).
   - Replica count verification (catches under-replication).
   - Cross-AZ consistency checks (catches replication lag).

4. AUTOMATIC RE-REPLICATION: When a disk/server/rack fails:
   - Affected objects are automatically re-replicated to healthy nodes.
   - Thousands of parallel streams across the entire cluster.
   - At S3's scale (trillions of objects), multiple disks fail DAILY.
     Re-replication is not an event — it's a CONTINUOUS operation.

5. DURABILITY MATH:
   With 3 AZs and erasure coding:
   P(losing 1 AZ) = small
   P(losing 2 AZs simultaneously) = tiny
   P(losing enough data in remaining AZ to exceed parity tolerance) = 10⁻¹¹
   
   The 11 nines come from the combination of:
   - Geographic separation (independent failure domains)
   - Erasure coding (tolerates multiple chunk losses)
   - Fast re-replication (reduces the window of vulnerability)
   - Continuous integrity verification (catches corruption early)
```

### Google Spanner

```
Spanner is a globally-distributed, strongly consistent database.

Permanent failure handling:

1. PAXOS GROUPS: Each data range ("split") is managed by a Paxos group 
   of 5 replicas across 5 data centers (typically 3 continents).
   
   Split "users[1-1000]":
     Replica 1: US-East      (leader)
     Replica 2: US-West
     Replica 3: Europe-West
     Replica 4: Asia-East
     Replica 5: Asia-South

2. NODE FAILURE: If a replica fails permanently:
   - Paxos continues with 4/5 replicas (majority = 3, still available).
   - A new replica is provisioned in the same or nearby region.
   - The new replica receives a snapshot from the leader + log replay.
   - Once caught up, it joins the Paxos group.

3. DATACENTER FAILURE: If an entire DC is destroyed:
   - Only 1 out of 5 replicas lost. Paxos continues with 4/5.
   - A new replica is provisioned in an alternate DC in the same region.
   - Data streamed from surviving replicas.
   - The system NEVER becomes unavailable (as long as 3/5 are reachable).

4. TRUETIME: Spanner's TrueTime API (GPS + atomic clocks) ensures 
   consistent timestamps across data centers. This means:
   - Failover to a new leader preserves all committed transactions.
   - No data loss, no inconsistency, even during DC failures.
   - External consistency: transactions appear to execute in a 
     single, global order even across continents.

5. SCALE: Spanner manages billions of Paxos groups. Node failures 
   are handled automatically, continuously, without human intervention. 
   The system is designed so that permanent failures are a normal, 
   expected condition — not an emergency.
```

### CockroachDB

```
CockroachDB is an open-source distributed SQL database inspired by Spanner.

Permanent failure handling:

1. RAFT CONSENSUS: Each data range is a Raft group with 3 replicas.
   
   If a node permanently fails:
   - Raft continues with 2/3 replicas if one is the leader.
   - After a configurable timeout (5 minutes default), the range is 
     considered under-replicated.
   - CockroachDB's "replicate queue" automatically finds a healthy node 
     and adds a new replica.

2. AUTOMATIC REBALANCING:
   $ cockroach node decommission <node-id>
   
   This gracefully moves all replicas off the node before removing it.
   For a permanently-dead node (no graceful decommission possible):
   
   After server.time_until_store_dead (default 5 minutes):
   - Dead node's store is marked as dead.
   - All ranges that had a replica on the dead store are up-replicated 
     to other healthy nodes.

3. ZONE CONFIGURATIONS:
   You can specify replication rules:
   ALTER TABLE users CONFIGURE ZONE USING
     num_replicas = 5,
     constraints = '{+region=us-east: 2, +region=us-west: 2, +region=eu: 1}';
   
   If a node in us-east permanently fails and only 1 us-east replica 
   remains, CockroachDB automatically provisions a new replica in 
   us-east on another node to satisfy the zone constraint.
```

---

## Choosing the Right Strategy

| Scenario | Recommended Strategy | Why |
|---|---|---|
| Single node failure, small data (< 1 TB) | Replica replacement + streaming | Simple, fast enough |
| Single node failure, large data (> 10 TB) | Parallel re-replication | Sequential is too slow |
| Cost-sensitive cold storage (PB scale) | Erasure coding | 1.5x overhead vs. 3x |
| Consensus-based system (etcd, Spanner) | Membership change + log replay | Built into the protocol |
| Entire DC failure | Cross-DC async replication + DNS failover | Already have remote copies |
| Extreme durability requirement (11 nines) | Multi-AZ erasure coding + continuous auditing | Amazon S3 approach |

---

## Key Takeaways

1. **Permanent failure is a race against time.** Once a node dies permanently, the remaining replicas are the *only* copies. Every additional failure before re-replication completes increases the risk of data loss exponentially.

2. **Re-replication speed is the primary durability lever.** Reducing repair time from 24 hours to 1 hour improves durability by 24x. Parallel re-replication across many nodes is essential at scale.

3. **Merkle trees ensure you're replicating correct data.** Before copying data to a new replica, verify source replicas agree with each other. Replicating stale or corrupted data just spreads the problem.

4. **Erasure coding trades compute for storage.** At petabyte scale, 3x replication is prohibitively expensive. Erasure coding provides equal or better durability at 1.5x storage cost, but requires CPU for encoding/decoding and is slower for reads/writes.

5. **Geographic distribution protects against correlated failures.** A single data center can be destroyed by fire, flood, or power loss. Spreading replicas across data centers (or across continents) ensures no single event can destroy all copies.

6. **Automation is non-negotiable at scale.** Systems like S3 and Spanner don't page a human when a disk fails — they handle dozens of failures per day automatically. The system must be designed so that permanent failures are routine, not emergencies.

7. **Layer your defenses.** Use replication for hot data + erasure coding for cold data. Use cross-DC replication for disaster recovery. Use continuous integrity checking (checksums, audits) to catch silent corruption. No single strategy is sufficient alone.

---

*← [Failure Detection](Failure_Detection.md) | [Handling Temporary Failures](Handling_temorary_failures.md)*
