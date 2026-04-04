# Strategy 7: Operational Transformation / Event Merging

[Back to Vector Clocks Overview](../vector-clocks.md)

---

## Overview

Instead of storing and merging **state** (the current value), this approach stores **operations** (the changes) and merges them. The system replays operations in causal order, transforming concurrent operations so they produce the correct result regardless of the order they're applied.

---

## How It Works

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

---

## OT vs CRDTs for Collaborative Editing

| Aspect | OT | CRDTs (e.g., Yjs, Automerge) |
|---|---|---|
| Coordination | Needs central server for ordering | Fully decentralized |
| Complexity | Transform functions are hard to get right | Data structure design is complex |
| Proven at scale | Google Docs, Google Wave | Figma (partial), local-first apps |
| Undo support | Natural (reverse the operation) | Harder (state-based) |

---

## When to Use It

- Real-time collaborative editing (text, diagrams, spreadsheets).
- You need fine-grained, intent-preserving conflict resolution.
- Operations are well-defined and composable.

## Drawbacks

- **High complexity** — designing correct transform functions for all operation pairs is notoriously error-prone. Google Wave's OT algorithm had subtle bugs for years.
- **Central sequencer often required** — pure peer-to-peer OT is extremely difficult; most implementations use a central server to assign a total order.
- **Operation design** — every user action must be decomposed into well-defined, transformable operations.

---

## Real-World Use Case: Google Docs — Real-Time Collaborative Document Editing

### The System

**Google Docs** is the most widely used OT-based collaborative editing system, supporting hundreds of millions of users editing documents simultaneously. Every keystroke, formatting change, and cursor movement is an operation that must be merged with concurrent edits from other users in real-time.

### How Google Docs Uses OT

```
Document state: "The cat sat on the mat"
Three users editing simultaneously:

User A (position 4): delete "cat", insert "dog"
  Op_A: [delete(4, 3), insert(4, "dog")]

User B (position 19): delete "mat", insert "rug"
  Op_B: [delete(19, 3), insert(19, "rug")]

User C (position 0): insert "Once, "
  Op_C: [insert(0, "Once, ")]

All three are concurrent. Google's OT server:
  1. Receives ops in arrival order (not necessarily causal order).
  2. Assigns a global sequence number to each.
  3. Transforms each op against all previously applied ops.

Transformation chain:
  Apply Op_C first: "Once, The cat sat on the mat"
    → All subsequent positions shift +6

  Transform Op_A against Op_C:
    Original: delete(4, 3) → delete(10, 3)  (shifted +6)
    Apply: "Once, The dog sat on the mat"

  Transform Op_B against Op_C and Op_A:
    Original: delete(19, 3) → delete(25, 3)  (shifted +6, no effect from A)
    Apply: "Once, The dog sat on the rug"

Final result on all clients: "Once, The dog sat on the rug"  ✅
```

### Architecture

```
                    Google OT Server
                    ┌──────────────┐
                    │  Op Log      │
                    │  [op1, op2,  │
                    │   op3, ...]  │
                    │              │
                    │  Transform   │
                    │  Engine      │
                    └──────┬───────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
         User A's      User B's     User C's
         Browser       Browser      Browser
         ┌──────┐     ┌──────┐     ┌──────┐
         │Local │     │Local │     │Local │
         │Copy  │     │Copy  │     │Copy  │
         │+ OT  │     │+ OT  │     │+ OT  │
         └──────┘     └──────┘     └──────┘

Each client:
  1. Applies local ops immediately (optimistic).
  2. Sends ops to server.
  3. Receives transformed ops from server.
  4. Applies remote ops to local copy (after transforming against unacknowledged local ops).
```

### Why OT and Not CRDTs?

When Google Docs was built (2006-2010), CRDTs for text editing were not mature. OT was the established approach from academic research (Ellis & Gibbs, 1989). Google's choice:

- **Central server** is acceptable — Google already runs the infrastructure. The server acts as the sequencer, simplifying OT considerably (no need for peer-to-peer transformation).
- **Undo support** — OT naturally supports undo by reversing operations. CRDTs struggle with undo because they're state-based.
- **Proven correctness** — Google invested heavily in formal verification of their OT transform functions (the Jupiter protocol).

### Scale

- Google Docs handles **~10 million concurrent documents** at peak.
- Each document can have up to **~100 simultaneous editors**.
- Operations are processed in **<100ms** end-to-end (local apply → server transform → broadcast → remote apply).
- The operation log per document is compacted periodically — old ops are folded into snapshots to prevent unbounded growth.

---

## Real-World Use Case: Microsoft Office Online (Fluid Framework)

### The System

**Microsoft Office Online** (Word, Excel, PowerPoint) evolved from OT to a hybrid approach using the **Fluid Framework**, which combines OT-like operation merging with distributed data structures.

### How Fluid Framework Works

```
Fluid uses "Distributed Data Structures" (DDS) — shared objects like:
  - SharedString: collaborative text (OT-based)
  - SharedMap: collaborative key-value store
  - SharedTree: collaborative hierarchical data

Each DDS operation is:
  1. Applied locally (optimistic).
  2. Sent to the Fluid Relay Service (central sequencer).
  3. Relay assigns a global sequence number.
  4. All clients receive ops in the same order.
  5. Each client transforms its pending local ops against incoming remote ops.

This is essentially OT, but with a richer set of data types beyond text.
```

### Excel Example: Concurrent Cell Edits

```
Spreadsheet with cell A1 = 100

User A: Set A1 = 200 (direct edit)
User B: Set A1 = A1 + 50 (formula-based increment)

Without OT:
  If A applies first, then B: A1 = 200 + 50 = 250
  If B applies first, then A: A1 = 200 (A's set overwrites B's increment)
  → ORDER MATTERS → inconsistent results across clients

With OT (Fluid):
  Server receives both ops, assigns order: [Op_A, Op_B]
  Op_A: Set A1 = 200 (absolute assignment)
  Op_B: transformed against Op_A:
    Original: A1 = A1 + 50
    After transform: A1 = 200 + 50 = 250 (rebase against new value)
  
  All clients converge to: A1 = 250  ✅
```

---

## Real-World Use Case: ShareDB — Open-Source OT for Web Apps

### The System

**ShareDB** is an open-source Node.js library that provides real-time OT-based collaboration. It powers several production applications and demonstrates how OT works in practice for developers who aren't Google-scale.

### How ShareDB Works

```javascript
// Server setup
const ShareDB = require('sharedb');
const db = require('sharedb-mongo')('mongodb://localhost/myapp');
const backend = ShareDB({ db });

// Client connects and subscribes to a document
const doc = connection.get('documents', 'doc-42');
doc.subscribe(() => {
  // Local state is synchronized automatically
  console.log(doc.data); // { text: "Hello World" }
});

// User A types (inserts at position 5)
doc.submitOp([{ p: ['text', 5], si: ' Beautiful' }]);
// Local state immediately: "Hello Beautiful World"

// User B concurrently deletes "World"
doc.submitOp([{ p: ['text', 6], sd: 'World' }]);

// Server transforms B's op against A's:
//   B's delete at position 6 → shifted to position 16
//   (because A inserted 10 chars before position 6)
// 
// All clients converge: "Hello Beautiful "
```

### Production Users of ShareDB

| Application | Use Case |
|---|---|
| **CodeSandbox** | Collaborative code editing in the browser |
| **Quill-based editors** | Real-time rich text editing |
| **Operational dashboards** | Multiple operators editing shared configs |

---

## The Evolution: From OT to CRDTs

The industry is gradually shifting from OT to CRDTs for new collaborative systems:

```
2006: Google Docs → OT (Jupiter protocol)
2010: Google Wave → OT (Wave OT, complex, buggy)
2015: Atom Teletype → CRDT (peer-to-peer)
2018: Figma → Custom CRDT
2019: Yjs → CRDT library (powers many editors)
2020: Automerge → CRDT for JSON documents
2022: Apple Notes → CRDT (WWDC 2022)
2023: Notion → Hybrid (OT-like with CRDT concepts)
```

**Why the shift?**
- CRDTs work peer-to-peer (no central server requirement).
- CRDTs handle offline editing more naturally.
- Modern CRDT libraries (Yjs) have caught up in performance and correctness.
- OT's central server is a single point of failure and a scaling bottleneck.

**Why OT persists:**
- Google Docs has billions of documents — migrating to CRDTs would be a massive rewrite.
- OT's undo model is more natural for document editing.
- The central server isn't a problem if you're Google, Microsoft, or any cloud provider.

---

## Systems That Use OT / Event Merging

| System | Details |
|---|---|
| **Google Docs / Sheets / Slides** | Jupiter OT protocol; central server |
| **Microsoft Office Online** | Fluid Framework (OT-based DDS) |
| **ShareDB** | Open-source Node.js OT library |
| **Apache Wave** | Google Wave's open-sourced OT engine |
| **CKEditor 5** | Real-time collaboration plugin using OT |
| **CodeSandbox** | Live collaborative coding via ShareDB |
| **Overleaf** | Collaborative LaTeX editing via ShareDB-based OT |

---

## Key Takeaway

OT is the proven approach for **real-time collaborative editing** at scale — Google Docs is the definitive success story. It preserves user intent (inserts, deletes, formatting changes) by transforming concurrent operations so they compose correctly. The cost is high complexity (transform functions are hard to get right) and a typical dependency on a central sequencer. For new projects, CRDTs are increasingly preferred, but OT remains dominant in existing large-scale systems where a central server is acceptable.
