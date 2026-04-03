# Strategy 5: Sibling Version Retention (Multi-Version Storage)

[Back to Vector Clocks Overview](../vector-clocks.md)

---

## Overview

Rather than resolving conflicts immediately, the system **stores all conflicting versions** (called siblings) and defers reconciliation to a later time — either on the next read or via an explicit merge process.

---

## How It Works

```
1. Node A writes: value="v1",  clock=[A:1]
2. Node B writes: value="v2",  clock=[B:1]   (concurrent with v1)

3. System stores BOTH as siblings under the same key:
     Sibling 1: "v1", clock=[A:1]
     Sibling 2: "v2", clock=[B:1]

4. On next read, client receives both siblings.
5. Reconciliation happens at read time (or via background job).
```

---

## When to Use It

- You cannot afford data loss under any circumstance.
- Reconciliation logic isn't available at write time (e.g., only certain services know how to merge).
- You want flexibility to choose a merge strategy later.

## Drawbacks

- **Storage overhead** — multiple versions per key consume disk and memory.
- **Read complexity** — clients must be prepared to handle multiple values for a single key.
- **Sibling explosion** — without timely reconciliation, sustained concurrent writes accumulate unbounded siblings.

---

## Real-World Use Case: Riak — Multi-Version Storage with `allow_mult`

### The System

**Riak** is a distributed key-value store heavily inspired by Amazon Dynamo. It's the most prominent production system that made sibling retention a **first-class, configurable feature** via the `allow_mult` bucket property.

### How It Works in Practice

```
Riak bucket configuration:
  {
    "props": {
      "allow_mult": true,     ← enable sibling retention
      "last_write_wins": false,
      "n_val": 3              ← replication factor
    }
  }

Scenario: Two clients write to the same key concurrently.

Client 1 (via Node A):
  PUT /buckets/users/keys/user42
  Body: {"name": "Alice", "email": "alice@new.com"}
  Causal context: [A:3, B:2]

Client 2 (via Node B):
  PUT /buckets/users/keys/user42
  Body: {"name": "Alice", "email": "alice@work.com"}
  Causal context: [A:2, B:3]

Vector clocks are concurrent → Riak stores BOTH as siblings.
```

### Reading Siblings

```
GET /buckets/users/keys/user42

Response (HTTP 300 Multiple Choices):
Content-Type: multipart/mixed; boundary=ABC123

--ABC123
Content-Type: application/json
X-Riak-Vclock: [A:3, B:2]

{"name": "Alice", "email": "alice@new.com"}

--ABC123
Content-Type: application/json
X-Riak-Vclock: [A:2, B:3]

{"name": "Alice", "email": "alice@work.com"}
--ABC123--
```

The client receives **both versions** and must decide how to merge.

### The Sibling Explosion Problem

Riak encountered this in production at scale. The scenario:

```
High-write, low-read key (e.g., a logging endpoint):

T=0:   Write from Node A → 1 version
T=1:   Concurrent write from Node B → 2 siblings
T=2:   Concurrent write from Node C → 3 siblings
...
T=100: 100 siblings, nobody has read this key yet.

Each read now returns 100 versions → slow, memory-intensive.
If a client reads and writes back just ONE sibling → data loss.
```

**Riak's safeguards:**
- `max_siblings` property: caps the number of siblings (default: 100). Beyond this, Riak falls back to LWW.
- `sibling_resolution` callbacks: server-side hooks to auto-resolve when sibling count exceeds a threshold.

### Real Production Impact: Bet365

**Bet365** (online gambling platform) used Riak for user session and betting slip storage:

- Sibling retention ensured that concurrent bets placed from multiple devices were **never lost** — critical when real money is involved.
- Sessions could be updated from mobile and desktop simultaneously during live events. Sibling retention preserved both updates.
- Reconciliation happened in the application layer: bet slips were merged via union, session data via field-level merge (latest timestamp per field).

### Before vs After `allow_mult` (Riak's History)

| Riak Version | Default Behavior | Problem |
|---|---|---|
| Pre-2.0 | `allow_mult=false` (LWW) | Silent data loss on concurrent writes |
| 2.0+ | `allow_mult=true` (siblings) | Clients must handle siblings; sibling explosion risk |

The switch to `allow_mult=true` as the default was a deliberate decision: Riak's maintainers decided that **surprising the client with multiple values** was better than **silently losing data**.

---

## Real-World Use Case: Amazon Dynamo — The Origin of Siblings

### The System

The Dynamo paper (2007) formalized sibling retention as a core design principle:

> *"Dynamo is designed to be an eventually consistent data store; that is, all updates reach all replicas eventually. Certain failure modes can potentially result in the system having... multiple versions of the same data."*

### How Dynamo Uses Siblings in Practice

```
Amazon shopping cart service during peak (Prime Day):

1. User adds "laptop" from phone → Dynamo write to DC-East
   Version: {items: ["laptop"]}, clock=[East:1]

2. User adds "headphones" from laptop → Dynamo write to DC-West (partition)
   Version: {items: ["headphones"]}, clock=[West:1]

3. Both versions stored as siblings. Neither is discarded.

4. User opens cart page → Dynamo returns both siblings:
   [
     {items: ["laptop"],      clock: [East:1]},
     {items: ["headphones"],  clock: [West:1]}
   ]

5. Cart service merges: union → {items: ["laptop", "headphones"]}
   Writes back with merged clock: [East:1, West:1, Coordinator:1]
```

### Why Not Resolve at Write Time?

Dynamo's core design principle: **always accept writes**. During a partition, the system cannot know what other writes are happening on the other side. Resolving at write time would require coordination, which sacrifices availability.

By storing siblings, Dynamo guarantees:
- **Zero write rejections** — every write succeeds, even during partitions.
- **Zero data loss** — conflicting writes are preserved, not discarded.
- **Deferred resolution** — reconciliation happens when it's convenient (next read), not when it's urgent (during a partition).

---

## Systems That Use Sibling Version Retention

| System | Details |
|---|---|
| **Amazon Dynamo** | Core design; siblings returned to application for merge |
| **Riak** | `allow_mult=true`; HTTP 300 with multipart response |
| **Voldemort** (LinkedIn) | Dynamo-inspired; vector clock versioning with sibling return |
| **CouchDB** | Conflicting revisions stored; accessible via `_conflicts` |
| **Git** | Conflicting branches / merge conflicts are "siblings" |

---

## Key Takeaway

Sibling version retention is not a reconciliation strategy per se — it's a **conflict preservation** strategy. It guarantees zero data loss by keeping all conflicting versions, deferring the actual merge decision to the reader or a background process. The trade-off is storage cost, read complexity, and the risk of sibling explosion. It works best when paired with a clear application-level merge policy and when write availability is non-negotiable.
