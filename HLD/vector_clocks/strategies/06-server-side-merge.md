# Strategy 6: Server-side Custom Merge Logic

[Back to Vector Clocks Overview](../vector-clocks.md)

---

## Overview

Instead of pushing conflict resolution to the client, the **database itself** applies predefined merge rules automatically when concurrent versions are detected. Clients are unaware that conflicts ever existed.

---

## How It Works

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

---

## When to Use It

- Your data model has well-defined, deterministic merge semantics.
- You want clients to be unaware of conflicts entirely.
- Merge logic is simple enough to express as generic rules.

## Drawbacks

- **Cannot handle all edge cases** — domain-specific conflicts (e.g., two users editing different fields of the same profile) may need richer logic than a generic rule.
- **Risk of incorrect merges** — if the rule doesn't match the actual intent (e.g., summing a field that should have been overwritten), data corruption occurs silently.
- **Tight coupling** — merge rules are baked into the database layer, making them harder to evolve as business logic changes.

---

## Real-World Use Case: CouchDB — Custom Merge Functions for Multi-Master Replication

### The System

**Apache CouchDB** is a document database built for multi-master replication. Any node can accept writes, and CouchDB replicates changes between nodes. When the same document is modified on two nodes, CouchDB detects the conflict (via revision trees, analogous to vector clocks) and offers two resolution paths: automatic winner selection and custom merge via **update handlers** or **view-based conflict resolution**.

### How CouchDB Handles Conflicts

```
Document "doc:invoice-42" exists on Node A and Node B.

Node A updates: { "total": 150, "status": "paid", "_rev": "2-aaa" }
Node B updates: { "total": 175, "status": "pending", "_rev": "2-bbb" }

CouchDB detects conflict (divergent revision trees).

Default behavior:
  CouchDB picks a "winning" revision deterministically
  (lexicographic comparison of revision hashes).
  The "losing" revision is stored but marked as a conflict.

Custom merge (via design document):
  A conflict resolution function runs server-side:

  function merge(winner, loser) {
    return {
      total: Math.max(winner.total, loser.total),
      status: winner.status === "paid" ? "paid" : loser.status,
      items: union(winner.items, loser.items)
    };
  }
```

### Real Production Use: IBM Cloudant

**IBM Cloudant** (CouchDB-as-a-service) powers multi-region applications for enterprises:

- **Retail inventory**: Two stores update the same product's stock count. Server-side merge sums the deltas from a common ancestor. The client POS system never sees siblings — it just gets the merged count.
- **Healthcare records**: Patient records replicate across hospitals. Merge rules: union for medication lists, max for last-visit timestamp, LWW for address. All handled server-side so clinical apps don't need conflict logic.

```
Hospital A updates patient record:
  medications: ["aspirin", "metformin"]
  last_visit: "2024-03-15"
  address: "123 Main St"

Hospital B updates same patient record (concurrent):
  medications: ["aspirin", "lisinopril"]
  last_visit: "2024-03-18"
  address: "456 Oak Ave"

Server-side merge:
  medications: ["aspirin", "metformin", "lisinopril"]  ← union
  last_visit: "2024-03-18"                              ← max
  address: "456 Oak Ave"                                ← LWW
```

### Why Server-Side?

- Clinical applications are built by many different teams. Requiring every app to implement sibling resolution correctly is a recipe for bugs.
- Merge rules for healthcare data are **standardized** (HL7 FHIR spec defines merge semantics) — they belong in the data layer, not scattered across apps.
- The database can merge during replication (before any client reads), so clients always see a single, consistent document.

---

## Real-World Use Case: Firebase Realtime Database — Automatic Server-Side Rules

### The System

**Firebase Realtime Database** allows multiple clients (mobile apps, web apps) to write to the same JSON path simultaneously. Firebase's servers apply **transaction handlers** and **security rules** that act as server-side merge logic.

### How It Works

```
Firebase path: /counters/page_views

Client A: transaction { current += 5 }
Client B: transaction { current += 3 }

Both arrive at the server near-simultaneously.

Firebase server:
  1. Client A's transaction reads current value (1000), writes 1005.
  2. Client B's transaction reads stale value (1000), writes 1003.
  3. Server detects conflict (B's write was based on stale state).
  4. Server re-runs B's transaction with updated value:
     current = 1005 → 1005 + 3 = 1008.
  5. Final value: 1008  ✅

Client B's app receives the retry automatically — transparent to the developer.
```

### Server-Side Rules for Merge

```javascript
// Firebase security rules that enforce merge semantics:
{
  "rules": {
    "inventory": {
      "$item": {
        ".write": "true",
        "quantity": {
          // Only allow writes that decrement (no negative stock)
          ".validate": "newData.val() >= 0"
        },
        "tags": {
          // Tags can only be added, never removed (grow-only set)
          ".validate": "newData.val().contains(data.val())"
        }
      }
    }
  }
}
```

### Why Firebase Uses Server-Side Merge

- **Mobile clients** have unreliable networks. Offline writes queue up and sync when connectivity returns. Server-side transactions ensure correctness without requiring the mobile app to handle conflicts.
- **Simple API**: developers write `ref.transaction(currentVal => currentVal + 1)` — the server handles retries and merge. No sibling handling, no vector clocks exposed to the app.
- **Scale**: Firebase processes millions of concurrent writes. Pushing merge logic to clients would create a thundering herd of retry storms.

---

## Real-World Use Case: PostgreSQL — Bi-Directional Replication (BDR)

### The System

**PostgreSQL BDR** (Bi-Directional Replication, by 2ndQuadrant/EDB) enables multi-master PostgreSQL clusters. When the same row is updated on two nodes, BDR applies configurable **conflict resolution handlers**:

```sql
-- Configure server-side merge for the 'orders' table:
SELECT bdr.alter_table_conflict_detection(
  'orders',
  'column_modify_timestamp'  -- track last-modified timestamp per column
);

-- Conflict resolution: per-column LWW
-- Each column independently resolves to the latest writer.
-- Two concurrent updates to different columns both survive.

Node A: UPDATE orders SET status='shipped' WHERE id=42;  (T=100)
Node B: UPDATE orders SET tracking='UPS123' WHERE id=42; (T=102)

Per-column LWW merge:
  status  → 'shipped'  (from Node A, T=100 — only writer)
  tracking → 'UPS123'  (from Node B, T=102 — only writer)

Both updates preserved — no conflict.
```

---

## Systems That Use Server-Side Merge

| System | Details |
|---|---|
| **CouchDB / Cloudant** | Custom merge functions via design documents |
| **Firebase Realtime DB** | Server-side transactions with automatic retry |
| **PostgreSQL BDR** | Configurable per-table conflict handlers (LWW, column-level, custom) |
| **Oracle GoldenGate** | CDR (Conflict Detection and Resolution) with built-in rules |
| **MySQL Group Replication** | Certification-based conflict detection with auto-rollback |
| **Cosmos DB** | LWW by default; custom merge via stored procedures |

---

## Key Takeaway

Server-side merge is ideal when merge semantics are **simple, well-defined, and stable** — counters that sum, sets that union, timestamps that max. It keeps clients simple and ensures consistent resolution across all readers. But it struggles with complex domain logic (you can't encode "merge two user profiles intelligently" as a generic rule) and creates tight coupling between business logic and the data layer. In practice, it works best for infrastructure-level data (counters, flags, timestamps) while complex domain objects are better served by application-level resolution.
