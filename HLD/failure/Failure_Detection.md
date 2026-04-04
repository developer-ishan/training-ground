# Failure Detection in Distributed Systems

[Handling Temporary Failures →](Handling_temorary_failures.md) | [Handling Permanent Failures →](Handling_permanent_failuers.md)

---

## The Problem

In a single-machine system, failure is binary — the process is either running or it's not. The OS tells you immediately. In a distributed system, things are profoundly harder:

- **No shared memory** — nodes communicate only over the network.
- **No global clock** — you can't agree on "when" something happened.
- **Network ≠ node** — if Node B doesn't respond to Node A, is B dead? Is the network broken? Is B just slow?

This ambiguity is the core challenge. In a distributed system, **you can never be 100% certain a remote node has failed** — you can only *suspect* it. The design of failure detection systems is about making that suspicion accurate, fast, and actionable.

---

## Why It Matters

Every distributed system decision depends on knowing who's alive:

| System Concern | Why Failure Detection Matters |
|---|---|
| **Replication** | If a replica is dead, writes must be redirected to healthy replicas |
| **Leader Election** | If the leader dies, a new one must be elected — but only if it's *actually* dead |
| **Load Balancing** | Requests shouldn't be routed to dead or slow nodes |
| **Data Consistency** | Quorum calculations change when nodes are unavailable |
| **Self-Healing** | The system can't repair itself if it doesn't know something is broken |

**The cost of getting it wrong:**

- **Too slow to detect** → requests pile up on dead nodes, users see errors, data goes unreplicated.
- **Too aggressive (false positive)** → healthy nodes get marked dead, triggering unnecessary failovers, data rebalancing, and "thundering herd" problems that *cause* real failures.

---

## Types of Failures

Before detecting failures, you need to understand what kinds exist. Not all failures look the same.

### 1. Crash Failures

The node stops working completely — process crash, power loss, kernel panic.

```
Timeline:
  Node B:  [running] [running] [running] [CRASH] [............nothing............]
  
  Node A sends request ──────────────────► (no response, ever)
```

**Characteristics:**
- Permanent (until restart) — the node doesn't come back by itself.
- Clean — the node doesn't send corrupted or wrong data, it just *stops*.
- Easiest to model and handle.

**Real example:** A Java service hits an OutOfMemoryError, the JVM crashes, the process exits. The container orchestrator (Kubernetes) eventually restarts it, but for 30–90 seconds the node is gone.

### 2. Omission Failures

The node is running but **drops some messages** — it fails to send or receive certain communications while handling others fine.

```
Timeline:
  Node A ── request 1 ──► Node B  ✓ (responds)
  Node A ── request 2 ──► Node B  ✗ (dropped — network buffer overflow)
  Node A ── request 3 ──► Node B  ✓ (responds)
  Node A ── request 4 ──► Node B  ✗ (dropped — GC pause caused timeout)
```

**Characteristics:**
- Intermittent — the node works *sometimes*.
- Harder to detect — a single missed heartbeat doesn't mean the node is dead.
- Can be caused by: network congestion, packet loss, full message queues, garbage collection pauses.

**Real example:** A node under heavy GC pressure (a 5-second stop-the-world pause) misses 2 heartbeats but responds to the 3rd. Is it dead? No. Should you worry? Yes.

### 3. Timing Failures

The node responds, but **too slowly** — it exceeds the expected timing bounds. In a system with real-time guarantees, this *is* a failure.

```
Expected response time: < 200ms

  Request 1 → Response in 50ms   ✓
  Request 2 → Response in 180ms  ✓
  Request 3 → Response in 3200ms ✗ (timing failure — SLA violated)
  Request 4 → Response in 90ms   ✓
```

**Characteristics:**
- The response is *correct*, just *late*.
- In async systems (most real-world systems), there are no strict timing bounds — so technically "timing failure" is a spectrum.
- A node getting consistently slower may be a precursor to a crash failure.

**Real example:** A database replica falls behind on replication. It can still serve reads, but the data is 30 seconds stale. For a stock trading system, this *is* a failure. For a social media feed, it might be acceptable.

### 4. Byzantine Failures

The node behaves **arbitrarily** — it may send wrong data, lie about its state, or actively try to sabotage the system.

```
Node B receives "What is the balance of user 42?"

  Correct answer: $1,000

  To Node A, B says: "$1,000"   (correct)
  To Node C, B says: "$5,000"   (wrong!)
  To Node D, B says: "$0"       (wrong!)
```

**Characteristics:**
- Hardest to handle — you need 3f+1 nodes to tolerate f Byzantine failures.
- Caused by: bugs, hardware corruption, malicious actors (hacked nodes).
- Most distributed systems **don't** handle Byzantine failures (too expensive). They assume nodes are honest but fallible.

**Real example:** Blockchain networks (Bitcoin, Ethereum) are designed to handle Byzantine failures because participants are untrusted. Traditional backend systems (Cassandra, DynamoDB) assume trusted nodes and only handle crash/omission failures.

### Summary Table

| Failure Type | Behavior | Detection Difficulty | Handling Cost |
|---|---|---|---|
| **Crash** | Node stops completely | Easy (no response at all) | Low |
| **Omission** | Drops some messages | Medium (intermittent) | Medium |
| **Timing** | Responds too slowly | Medium (what's "too slow"?) | Medium |
| **Byzantine** | Arbitrary/malicious behavior | Very Hard | Very High (3f+1 redundancy) |

---

## Failure Detection Mechanisms

### 1. Direct Heartbeat (Ping-Ack)

The simplest approach: every node periodically pings every other node and waits for an acknowledgment.

```
Every 2 seconds:

  Node A ── PING ──► Node B
  Node A ◄── ACK ─── Node B   ✓  B is alive

  Node A ── PING ──► Node C
  (no ACK within 5 seconds)     ✗  C is suspected dead
  
  Node A ── PING ──► Node D
  Node A ◄── ACK ─── Node D   ✓  D is alive
```

**Algorithm:**
1. Every T seconds (the **heartbeat interval**), send a PING to each monitored node.
2. If no ACK is received within timeout `t`, increment a **miss counter**.
3. If the miss counter exceeds threshold `k`, mark the node as **suspected failed**.

**Tuning the parameters:**

| Parameter | Too Low | Too High |
|---|---|---|
| Heartbeat interval (T) | High network overhead, wastes bandwidth | Slow to detect failures |
| Timeout (t) | False positives during GC pauses or network blips | Slow to detect failures |
| Miss threshold (k) | False positives (one dropped packet = "dead") | Slow to detect real failures |

**Typical values:** T = 1–5 seconds, t = 2–10 seconds, k = 3–5 misses.

**Problems with direct heartbeat:**
- **O(n²) messages** — every node pings every other node. 100 nodes = 9,900 pings per interval.
- **Single observer** — if the *network between A and B* is broken but B is fine, A incorrectly thinks B is dead. Other nodes may still reach B just fine.
- **Symmetry assumption** — assumes if A can't reach B, then B can't reach A. Not always true (asymmetric network partitions).

**Used by:** Simple two-node setups, leader-follower replication (follower pings leader).

---

### 2. Centralized Heartbeat (with a Coordinator)

One designated node collects heartbeats from everyone. All failure decisions are centralized.

```
                    ┌──────────────┐
                    │  Coordinator │
                    │   (Leader)   │
                    └──────┬───────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
              ▼            ▼            ▼
           Node A       Node B       Node C
           
  Every 2 seconds:
    Node A ── heartbeat ──► Coordinator  ✓
    Node B ── heartbeat ──► Coordinator  ✓
    Node C ── (nothing)  ──► Coordinator  ✗ suspected
```

**Algorithm:**
1. Every node sends a heartbeat to the coordinator every T seconds.
2. The coordinator tracks the **last heartbeat time** for each node.
3. If `now - last_heartbeat > timeout`, the coordinator marks the node as suspected.
4. The coordinator broadcasts the updated **membership view** to all nodes.

**Advantages:**
- **O(n) messages** per interval — much cheaper than O(n²).
- **Single consistent view** — one node makes all decisions, so no disagreements.
- Easy to implement and reason about.

**Problems:**
- **Single point of failure** — if the coordinator dies, failure detection stops entirely.
- **False accusations** — if the network between Node C and the coordinator is broken (but C is healthy), C gets falsely accused. Meanwhile, if Node A can still reach C, A knows C is fine — but nobody asked A.

**Mitigation:** Use a consensus protocol (like Raft) to elect a new coordinator if the current one fails. But now you need failure detection *for the failure detector* — turtles all the way down.

**Used by:** ZooKeeper (leader tracks session heartbeats), HDFS NameNode (tracks DataNode heartbeats), Kubernetes API server (tracks kubelet heartbeats).

---

### 3. Gossip-Based Failure Detection

Instead of O(n²) pinging or relying on a single coordinator, nodes **gossip** about each other's health. This is the approach used by most modern large-scale distributed systems.

**Core Idea:** Each node maintains a list of all nodes and their **heartbeat counters**. Periodically, each node increments its own counter and gossips its list to a randomly chosen peer. Through repeated random exchanges, information about every node's liveness propagates through the entire cluster — like a rumor spreading through a crowd.

```
Each node maintains a table:

  Node A's view:
  ┌─────────┬───────────┬─────────────────┐
  │  Node   │ Heartbeat │ Last Updated    │
  ├─────────┼───────────┼─────────────────┤
  │  A      │    47     │ (self — always  │
  │         │           │  current)       │
  │  B      │    45     │ 2 sec ago       │
  │  C      │    44     │ 5 sec ago       │
  │  D      │    38     │ 12 sec ago ⚠️   │
  │  E      │    46     │ 1 sec ago       │
  └─────────┴───────────┴─────────────────┘
```

**Algorithm:**

```
Every T seconds (e.g., 1 second):

1. Increment own heartbeat counter.
2. Pick a random peer (e.g., Node C).
3. Send your entire heartbeat table to Node C.
4. Node C merges the tables:
   - For each node in the received table:
     - If received heartbeat > local heartbeat → update local entry.
     - If received heartbeat ≤ local heartbeat → keep local entry.
5. If any node's heartbeat hasn't increased in > T_fail seconds → mark as SUSPECTED.
6. If any node has been SUSPECTED for > T_cleanup seconds → mark as DEAD and remove.
```

**Concrete Example:**

```
Time T=0:
  Node A's table: {A:10, B:8, C:9, D:7}
  Node B's table: {A:9,  B:9, C:8, D:7}

  A gossips to B → B receives {A:10, B:8, C:9, D:7}

  B merges (takes max of each):
    A: max(9, 10)  = 10  ← updated from A's gossip
    B: max(9, 8)   = 9   ← B's local is newer
    C: max(8, 9)   = 9   ← updated from A's gossip
    D: max(7, 7)   = 7   ← unchanged

  B's table after merge: {A:10, B:9, C:9, D:7}

Time T=1:
  A increments: A:11. B increments: B:10.
  But D hasn't incremented in a while...

Time T=10:
  D's heartbeat is still 7. It's been 10 seconds since D's counter changed.
  If T_fail = 8 seconds → D is now SUSPECTED.

Time T=20:
  D's heartbeat is still 7. It's been SUSPECTED for 10 seconds.
  If T_cleanup = 15 seconds → D is still SUSPECTED, not yet removed.

Time T=25:
  D still at 7. SUSPECTED for 15 seconds → D is marked DEAD and removed.
```

**Why gossip works well:**

| Property | Benefit |
|---|---|
| **O(n) messages per interval** | Each node contacts only 1 peer per round |
| **No single point of failure** | No coordinator — every node participates equally |
| **Convergence** | Information reaches all nodes in O(log n) rounds |
| **Tolerates network partitions** | If A can't reach B directly, the info about B still reaches A through C, D, etc. |
| **Scalable** | Works for clusters of thousands of nodes |

**Convergence proof intuition:** If you have n nodes and each gossips to 1 random peer per round, after `log₂(n)` rounds, every node has heard the news. With 1,000 nodes, information propagates in ~10 rounds (10 seconds if T=1s). This is the same math behind how rumors spread exponentially.

**Used by:** Cassandra, CockroachDB, Consul, Serf, Amazon S3 (internal), DynamoDB (internal).

---

### 4. The Phi Accrual Failure Detector

Traditional failure detectors output a **binary** decision: alive or dead. The Phi (Φ) Accrual Failure Detector instead outputs a **continuous suspicion level** — a number that represents "how suspicious we are that this node has failed."

**The Insight:** Different situations require different sensitivity. A payment system should react faster to failures than a log aggregator. Instead of hardcoding a timeout, the Phi detector lets the *application* choose its suspicion threshold.

**How it works:**

```
1. Track the ARRIVAL TIMES of heartbeats from each node.
   
   Heartbeats from Node B arrived at:
   T=0.0, T=1.1, T=2.0, T=3.1, T=4.0, T=5.2, T=6.0

2. Compute the INTER-ARRIVAL TIMES (gaps between consecutive heartbeats):
   1.1, 0.9, 1.1, 0.9, 1.2, 0.8

3. Model these as a NORMAL DISTRIBUTION:
   Mean (μ) = 1.0 seconds
   Std Dev (σ) = 0.15 seconds

4. When the current time is T and the last heartbeat was at T_last:
   Time since last heartbeat = T - T_last

5. Compute Phi (Φ):
   Φ = -log₁₀(1 - CDF(T - T_last))
   
   Where CDF is the cumulative distribution function of the 
   normal distribution with the computed μ and σ.
```

**Intuition with numbers:**

```
Node B heartbeats arrive every ~1.0 seconds (σ = 0.15s).

  Time since last heartbeat │ Φ value │ Interpretation
  ─────────────────────────┼─────────┼──────────────────────────
  0.5 seconds               │  0.1    │ Totally normal
  1.0 seconds               │  1.0    │ Expected — nothing unusual
  1.5 seconds               │  3.0    │ Getting suspicious
  2.0 seconds               │  5.8    │ Very likely failed
  3.0 seconds               │ 12.0    │ Almost certainly dead
  5.0 seconds               │ 28.0    │ Definitely dead
```

**The application decides the threshold:**

```
Payment processing system:   Φ_threshold = 3   (react at 1.5s — fast!)
Log shipping pipeline:       Φ_threshold = 8   (react at 2.5s — more tolerant)
Batch analytics cluster:     Φ_threshold = 12  (react at 3.0s — very tolerant)
```

**Why this is better than fixed timeouts:**

| Fixed Timeout | Phi Accrual |
|---|---|
| Same timeout for all nodes | Adapts to each node's individual pattern |
| Can't handle variable network latency | Models network jitter naturally |
| Binary: alive/dead, nothing in between | Continuous suspicion level enables nuanced decisions |
| Must manually tune timeout per environment | Self-tunes from observed heartbeat arrivals |
| A node with jittery heartbeats (1s, 3s, 1s, 4s) gets falsely accused | The high σ makes Phi rise slowly — fewer false positives |

**Concrete scenario:**

```
Node B is in the same data center — heartbeats arrive like clockwork:
  μ = 1.0s, σ = 0.05s
  → If 1.5s passes with no heartbeat: Φ = 10.0 (very suspicious — this node never takes this long)

Node C is cross-datacenter — heartbeats are jittery:
  μ = 1.0s, σ = 0.8s
  → If 1.5s passes with no heartbeat: Φ = 0.8 (totally normal — C is often this slow)

Fixed timeout of 2s would treat both the same. Phi gives a nuanced answer.
```

**Used by:** Cassandra (primary failure detector), Akka (actor framework), Hazelcast.

---

### 5. SWIM (Scalable Weakly-consistent Infection-style Membership)

SWIM is a protocol specifically designed for **membership management** — knowing who's in the cluster and who's not. It combines failure detection with membership dissemination.

**Key Innovations:**
1. Instead of pinging everyone, each node probes **one random node per round**.
2. If the probe fails, it doesn't immediately accuse — it asks **other nodes to probe on its behalf** (indirect probing).
3. Membership changes (joins/leaves/failures) are piggybacked on the probe messages — no extra bandwidth cost.

**Algorithm:**

```
Every T seconds (the "protocol period"), Node A does:

1. DIRECT PROBE: Pick a random node (say Node D). Send PING.
   
   Node A ── PING ──► Node D
   
   If Node D responds with ACK within timeout → D is alive. Done.

2. If no ACK (timeout):
   INDIRECT PROBE: Pick k random OTHER nodes (say B and C). 
   Ask them to ping D on A's behalf.
   
   Node A ── "Please ping D" ──► Node B
   Node A ── "Please ping D" ──► Node C
   
   Node B ── PING ──► Node D ── ACK ──► Node B ── "D is alive" ──► Node A
   Node C ── PING ──► Node D ── (no response) ──► Node C ── "D didn't respond" ──► Node A

3. If ANY indirect probe succeeds → D is alive (the problem was A↔D network, not D).
   If ALL indirect probes fail → D is SUSPECTED.

4. SUSPECTED nodes get a grace period. If D responds to any probe during this time,
   the suspicion is lifted. Otherwise → D is marked DEAD.
```

**Why indirect probing is brilliant:**

```
Scenario: Network between A and D is broken (asymmetric partition).

  Without indirect probing:
    A can't reach D → "D is dead!" → WRONG. D is perfectly healthy.

  With indirect probing:
    A can't reach D → asks B to try → B reaches D → "D is alive."
    A learns that D is fine, the problem is A's network to D.
    
This dramatically reduces false positives.
```

**Piggybacking membership updates:**

```
Instead of separate messages for failure detection and membership changes,
SWIM piggybacks membership updates onto probe messages:

  PING message from A to B:
  {
    type: "PING",
    membership_updates: [
      { node: "E", status: "JOINED",     seq: 42 },
      { node: "F", status: "SUSPECTED",  seq: 43 },
      { node: "G", status: "DEAD",       seq: 44 }
    ]
  }

  B processes these updates and includes them in its future pings.
  Updates propagate through gossip — free of extra bandwidth.
```

**Scalability analysis:**

| Metric | SWIM | Full Gossip | Direct Heartbeat |
|---|---|---|---|
| Messages per round per node | 1 + k (indirect) | 1 | n-1 |
| Total messages per round | O(n) | O(n) | O(n²) |
| False positive rate | Very low (indirect probing) | Low | Higher (single observer) |
| Detection time | O(log n) protocol periods | O(log n) rounds | 1 period (but higher false positive) |

**Used by:** HashiCorp Serf, HashiCorp Consul (memberlist library), Uber's Ringpop.

---

## Failure Detection vs. Failure Suspicion

This is a critical conceptual distinction that often gets glossed over.

### The FLP Impossibility Result

In 1985, Fischer, Lynch, and Paterson proved that in an asynchronous distributed system (one where there is no bound on message delivery time), **it is impossible to distinguish between a crashed node and a very slow node**. This is the FLP impossibility result.

```
Scenario: Node A sends a request to Node B. No response after 10 seconds.

Two equally valid explanations:

  Explanation 1: Node B crashed.
    Node A ── request ──► [B is dead] ──► (nothing happens)

  Explanation 2: Node B is alive but slow (GC pause, disk I/O, CPU overload).
    Node A ── request ──► [B is processing...still processing...] ──► ACK (at T=15s)

Node A CANNOT distinguish these two cases by waiting.
If A waits longer, maybe B responds at T=20s, T=60s, T=600s...
There is no finite timeout that guarantees a correct answer.
```

### Practical Implication

Because of FLP, every real-world failure detector works with **suspicion**, not certainty:

```
                    ┌──────────────────────────────────────────────┐
                    │        Failure Detector Output Spectrum      │
                    │                                              │
  ◄────────────────┼──────────────────────────────────────────────┤
  "Definitely       │                                              "Definitely
   alive"           │                                              dead"
                    │                                              │
                    │    In reality, you're always somewhere       │
                    │    in the middle. The goal is to be          │
                    │    RIGHT ENOUGH, FAST ENOUGH.                │
                    └──────────────────────────────────────────────┘
```

**Two types of mistakes:**

| Mistake | Name | Consequence |
|---|---|---|
| Declare a **healthy** node as dead | **False positive** | Unnecessary failover, wasted resources, potential split-brain |
| Declare a **dead** node as alive | **False negative** | Requests sent to dead node, timeouts, user-facing errors |

Every failure detector trades these off. You can never eliminate both.

---

## Real-World Failure Detection Systems

### Apache Cassandra

```
Mechanism: Gossip + Phi Accrual Failure Detector

  1. Every second, each node gossips with 1–3 random peers.
  2. Gossip messages include: heartbeat generation, heartbeat version, 
     application state (load, schema version, tokens).
  3. Phi Accrual detector with configurable threshold (default Φ = 8).
  4. Node states: NORMAL → SUSPECTED → DOWN → removed from ring.

Configuration:
  phi_convict_threshold: 8        # Phi threshold for marking down
  
  Key insight: In cloud environments (AWS), Cassandra recommends
  raising this to 10–12 because cross-AZ network jitter is higher.
```

### Apache ZooKeeper

```
Mechanism: Centralized Session-Based Heartbeat

  1. Clients maintain a SESSION with the ZooKeeper ensemble.
  2. Client sends heartbeats every tickTime/2 (default: 1 second).
  3. If the server doesn't hear from a client within sessionTimeout 
     (default: 2–20 ticks), the session expires.
  4. Session expiration triggers: ephemeral nodes deleted, watches fired,
     locks released.
  
  Key insight: ZooKeeper uses session semantics rather than 
  node-level failure detection. This is important for distributed 
  locks — if the lock holder's session expires, the lock is 
  automatically released, preventing deadlocks from crashed holders.

  Example flow:
    Client C holds lock /locks/resource-1 (ephemeral node)
    C crashes → no heartbeats → session expires after 10s
    /locks/resource-1 deleted automatically
    Other clients watching the lock are notified → next in queue acquires lock
```

### Kubernetes

```
Mechanism: Kubelet Heartbeats + Node Lease Objects

  1. Each kubelet sends TWO types of heartbeats to the API server:
     a. NodeStatus updates (heavy — includes capacity, conditions, addresses)
        Frequency: every 10 seconds (or when status changes)
     b. Lease objects (lightweight — just a timestamp)
        Frequency: every 10 seconds (default)
  
  2. Node Controller checks lease freshness:
     If lease not renewed in 40 seconds → node marked as "Unknown"
     If still Unknown after 5 minutes → pods evicted and rescheduled
  
  3. The two-tier approach (heavy NodeStatus + lightweight Lease) reduces
     API server load. Before Lease objects (pre-1.14), NodeStatus updates
     from 5,000 nodes could overwhelm the API server.

  Timeline of a node failure:
    T=0     Last successful kubelet heartbeat
    T=10    Missed heartbeat (expected)
    T=20    Missed heartbeat
    T=30    Missed heartbeat  
    T=40    Node Controller marks node as "Unknown" (node-monitor-grace-period)
    T=340   Pod eviction begins (pod-eviction-timeout = 5 minutes)
    
  Total time to recovery: ~5.5 minutes (conservative by design —
  aggressive eviction in cloud environments can cause cascading failures
  during transient network partitions).
```

### etcd (Raft-based)

```
Mechanism: Leader-driven Heartbeat within Raft consensus

  1. The Raft leader sends heartbeats to all followers every 
     heartbeat-interval (default: 100ms).
  2. If a follower doesn't receive a heartbeat within 
     election-timeout (10 * heartbeat-interval = 1s), it starts 
     a new election.
  3. The leader tracks which followers are responsive. If a follower 
     hasn't responded to AppendEntries RPCs, it's considered unreachable.
  
  Key insight: In Raft, failure detection IS the consensus protocol.
  A new leader can only be elected if a majority of nodes are alive.
  This means the system automatically stops accepting writes if too 
  many nodes fail — safety is guaranteed.

  etcd recommended tuning:
    heartbeat-interval: 100ms  (datacenter)
    election-timeout:   1000ms (10x heartbeat)
    
    For cross-datacenter:
    heartbeat-interval: 500ms
    election-timeout:   5000ms
```

### Amazon DynamoDB (Internal)

```
Mechanism: Gossip-based (based on the Dynamo paper)

  1. Every node gossips membership information to a random peer 
     every second.
  2. If a node's heartbeat counter hasn't incremented in 
     T_fail seconds, it's marked as temporarily unavailable.
  3. DynamoDB uses "virtual nodes" (vnodes) — each physical node 
     owns multiple token ranges. When a node fails, its token 
     ranges are distributed across many other nodes, spreading 
     the load evenly.
  
  Key insight: DynamoDB's failure detection is tightly coupled 
  with its routing layer. When a node is suspected, the routing 
  table is updated so that requests to that node's key ranges 
  are redirected to replica nodes — this is the "sloppy quorum" 
  approach (covered in Handling Temporary Failures).
```

---

## Choosing a Failure Detection Strategy

| Scenario | Recommended Approach | Why |
|---|---|---|
| Small cluster (3–10 nodes) | Direct heartbeat or Raft-built-in | Simple, effective at small scale |
| Medium cluster (10–100 nodes) | Gossip + Phi Accrual | Good balance of accuracy and scalability |
| Large cluster (100–10,000+ nodes) | SWIM or Gossip | O(n) message complexity essential |
| Need consensus (leader election, locks) | Raft/Paxos built-in detection | Failure detection and consensus in one protocol |
| Untrusted nodes (blockchain) | Byzantine fault tolerance protocols | Must tolerate lying/malicious nodes |
| Cloud/multi-datacenter | Phi Accrual with high threshold | Adapts to variable network latency |

---

## Key Takeaways

1. **You can never be certain a node is dead** — only suspicious. Design for suspicion, not certainty.
2. **False positives are expensive** — marking a healthy node dead triggers unnecessary work (failover, replication, rebalancing). Tune conservatively.
3. **Gossip scales** — O(n) messages, no single point of failure, information propagates in O(log n) rounds.
4. **Phi Accrual adapts** — instead of hardcoded timeouts, it learns each node's normal heartbeat pattern and adjusts automatically.
5. **Indirect probing prevents false accusations** — if you can't reach a node, ask others to try before declaring it dead (SWIM).
6. **Failure detection is the foundation** — every other distributed systems concern (replication, consistency, availability) depends on knowing who's alive.

---

*Next: [Handling Temporary Failures →](Handling_temorary_failures.md)*
