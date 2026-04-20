# Splitwise — Low-Level Design: Database Schema

## Overview

Splitwise is an expense-splitting application where users can create groups, add expenses, split them in various ways, and settle debts. The schema below supports equal splits, exact-amount splits, percentage-based splits, and share-based splits.

---

## ER Diagram (Textual)

```
Users ──< GroupMembers >── Groups
  │                          │
  │                          │
  ├──< Expenses >────────────┘
  │       │
  │       └──< ExpenseSplits
  │
  ├──< Settlements
  │
  └──< ActivityLog
```

---

## Tables

### 1. `users`

Stores registered users.

```sql
CREATE TABLE users (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(100)  NOT NULL,
    email           VARCHAR(255)  NOT NULL UNIQUE,
    phone           VARCHAR(20),
    password_hash   VARCHAR(255)  NOT NULL,
    avatar_url      VARCHAR(512),
    default_currency CHAR(3)      NOT NULL DEFAULT 'USD',
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
```

---

### 2. `groups`

A group is a collection of users who share expenses (e.g. "Apartment", "Trip to Goa").

```sql
CREATE TABLE groups (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(200)  NOT NULL,
    description     TEXT,
    cover_image_url VARCHAR(512),
    group_type      ENUM('HOME', 'TRIP', 'COUPLE', 'OTHER') NOT NULL DEFAULT 'OTHER',
    default_currency CHAR(3)      NOT NULL DEFAULT 'USD',
    simplified_debts BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by      BIGINT        NOT NULL,
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id)
);
```

---

### 3. `group_members`

Junction table linking users to groups with their role.

```sql
CREATE TABLE group_members (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id    BIGINT    NOT NULL,
    user_id     BIGINT    NOT NULL,
    role        ENUM('ADMIN', 'MEMBER') NOT NULL DEFAULT 'MEMBER',
    joined_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id)  REFERENCES users(id)  ON DELETE CASCADE,
    UNIQUE KEY uq_group_user (group_id, user_id)
);

CREATE INDEX idx_gm_user ON group_members(user_id);
```

---

### 4. `expenses`

Each expense records who paid, how much, and how it should be split.

```sql
CREATE TABLE expenses (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id        BIGINT,
    description     VARCHAR(500)  NOT NULL,
    amount          DECIMAL(12,2) NOT NULL CHECK (amount > 0),
    currency        CHAR(3)       NOT NULL DEFAULT 'USD',
    category        ENUM('FOOD', 'TRANSPORT', 'SHOPPING', 'ENTERTAINMENT',
                         'UTILITIES', 'RENT', 'MEDICAL', 'OTHER')
                    NOT NULL DEFAULT 'OTHER',
    split_type      ENUM('EQUAL', 'EXACT', 'PERCENTAGE', 'SHARES')
                    NOT NULL DEFAULT 'EQUAL',
    paid_by         BIGINT        NOT NULL,
    expense_date    DATE          NOT NULL,
    notes           TEXT,
    receipt_url     VARCHAR(512),
    is_deleted      BOOLEAN       NOT NULL DEFAULT FALSE,
    created_by      BIGINT        NOT NULL,
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id)   REFERENCES groups(id),
    FOREIGN KEY (paid_by)    REFERENCES users(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE INDEX idx_exp_group   ON expenses(group_id);
CREATE INDEX idx_exp_paid_by ON expenses(paid_by);
CREATE INDEX idx_exp_date    ON expenses(expense_date);
```

> **Note:** `group_id` is nullable to support non-group (1-on-1) expenses between two users.

---

### 5. `expense_payers`

Supports **multiple payers** for a single expense (e.g. two people split the bill at the counter).

```sql
CREATE TABLE expense_payers (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    expense_id  BIGINT        NOT NULL,
    user_id     BIGINT        NOT NULL,
    amount      DECIMAL(12,2) NOT NULL CHECK (amount > 0),
    FOREIGN KEY (expense_id) REFERENCES expenses(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id)    REFERENCES users(id),
    UNIQUE KEY uq_payer (expense_id, user_id)
);
```

> The sum of `expense_payers.amount` for an expense must equal `expenses.amount`. When there is a single payer, this table has one row matching `expenses.paid_by`.

---

### 6. `expense_splits`

How the expense is divided among participants. Each row represents one user's share.

```sql
CREATE TABLE expense_splits (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    expense_id  BIGINT        NOT NULL,
    user_id     BIGINT        NOT NULL,
    amount      DECIMAL(12,2) NOT NULL,  -- resolved split amount (always stored)
    percentage  DECIMAL(5,2),            -- populated when split_type = PERCENTAGE
    shares      INT,                     -- populated when split_type = SHARES
    FOREIGN KEY (expense_id) REFERENCES expenses(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id)    REFERENCES users(id),
    UNIQUE KEY uq_split (expense_id, user_id)
);

CREATE INDEX idx_split_user ON expense_splits(user_id);
```

**Split type semantics:**

| `split_type` | `amount` | `percentage` | `shares` |
|---|---|---|---|
| EQUAL | auto-calculated equally | NULL | NULL |
| EXACT | user-provided exact amount | NULL | NULL |
| PERCENTAGE | calculated from % of total | user-provided (sum = 100) | NULL |
| SHARES | calculated from share ratio | NULL | user-provided (e.g. 2, 1, 1) |

**Invariant:** `SUM(expense_splits.amount) = expenses.amount` for every expense.

---

### 7. `settlements`

Records payments made to settle debts between users.

```sql
CREATE TABLE settlements (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id      BIGINT,
    payer_id      BIGINT        NOT NULL,  -- user who pays
    payee_id      BIGINT        NOT NULL,  -- user who receives
    amount        DECIMAL(12,2) NOT NULL CHECK (amount > 0),
    currency      CHAR(3)       NOT NULL DEFAULT 'USD',
    payment_mode  ENUM('CASH', 'UPI', 'BANK_TRANSFER', 'VENMO', 'PAYPAL', 'OTHER')
                  NOT NULL DEFAULT 'CASH',
    notes         TEXT,
    settled_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES groups(id),
    FOREIGN KEY (payer_id) REFERENCES users(id),
    FOREIGN KEY (payee_id) REFERENCES users(id),
    CHECK (payer_id <> payee_id)
);

CREATE INDEX idx_settle_payer ON settlements(payer_id);
CREATE INDEX idx_settle_payee ON settlements(payee_id);
CREATE INDEX idx_settle_group ON settlements(group_id);
```

---

### 8. `balances` (Materialized / Denormalized)

Pre-computed net balances between user pairs per group for fast reads. Updated transactionally when expenses or settlements are created/modified.

```sql
CREATE TABLE balances (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id    BIGINT,
    user_id     BIGINT        NOT NULL,  -- the user who owes or is owed
    owes_to     BIGINT        NOT NULL,  -- the counterparty
    amount      DECIMAL(12,2) NOT NULL,  -- positive = user_id owes owes_to
    currency    CHAR(3)       NOT NULL DEFAULT 'USD',
    updated_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES groups(id),
    FOREIGN KEY (user_id)  REFERENCES users(id),
    FOREIGN KEY (owes_to)  REFERENCES users(id),
    UNIQUE KEY uq_balance (group_id, user_id, owes_to, currency),
    CHECK (user_id <> owes_to)
);

CREATE INDEX idx_bal_user ON balances(user_id);
```

> This table is **derived data**. The source of truth is `expenses` + `expense_splits` + `settlements`. A background job or trigger keeps it in sync.

---

### 9. `activity_log`

Audit trail for all mutations (expense added, settlement recorded, member joined, etc.).

```sql
CREATE TABLE activity_log (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id      BIGINT,
    user_id       BIGINT        NOT NULL,
    action        ENUM('EXPENSE_CREATED', 'EXPENSE_UPDATED', 'EXPENSE_DELETED',
                       'SETTLEMENT_CREATED', 'MEMBER_ADDED', 'MEMBER_REMOVED',
                       'GROUP_CREATED', 'GROUP_UPDATED')
                  NOT NULL,
    entity_type   VARCHAR(50)   NOT NULL,  -- 'expense', 'settlement', 'group', etc.
    entity_id     BIGINT        NOT NULL,
    metadata      JSON,                    -- old/new values for auditing
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES groups(id),
    FOREIGN KEY (user_id)  REFERENCES users(id)
);

CREATE INDEX idx_activity_group ON activity_log(group_id, created_at);
CREATE INDEX idx_activity_user  ON activity_log(user_id, created_at);
```

---

### 10. `friendships`

Tracks direct friend connections between users (outside of groups).

```sql
CREATE TABLE friendships (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT    NOT NULL,
    friend_id   BIGINT    NOT NULL,
    status      ENUM('PENDING', 'ACCEPTED', 'BLOCKED') NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id)   REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (friend_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uq_friendship (user_id, friend_id),
    CHECK (user_id <> friend_id)
);
```

---

## Key Relationships Summary

| Relationship | Type | Description |
|---|---|---|
| `users` → `groups` | Many-to-Many | via `group_members` |
| `groups` → `expenses` | One-to-Many | a group has many expenses |
| `expenses` → `expense_payers` | One-to-Many | who paid the expense |
| `expenses` → `expense_splits` | One-to-Many | how the expense is divided |
| `users` → `settlements` | One-to-Many | a user can make/receive many settlements |
| `users` → `friendships` | Many-to-Many | self-referencing via `friendships` |
| `balances` | Derived | net amount between two users per group |

---

## Balance Computation Logic

For any user pair (A, B) in a group:

```
net_balance(A→B) =
    SUM(splits where A is split participant AND B is payer)
  − SUM(splits where B is split participant AND A is payer)
  − SUM(settlements where A paid B)
  + SUM(settlements where B paid A)
```

If `net_balance > 0`, A owes B. If `< 0`, B owes A.

**Simplified debts** (enabled per group) reduces the number of transactions needed to settle all debts using a min-cash-flow algorithm (greedy or graph-based).

---

## Design Decisions

1. **Soft deletes on expenses** — `is_deleted` flag instead of hard delete preserves audit history.
2. **Denormalized `balances` table** — avoids expensive aggregation queries on every dashboard load; kept consistent via application-level transactions.
3. **Multiple split types** — `expense_splits.amount` is always populated regardless of split type, so balance queries never need to understand split semantics.
4. **Nullable `group_id`** — supports both group expenses and direct 1-on-1 expenses.
5. **`expense_payers` table** — handles the edge case where multiple people pay for one expense (e.g. splitting the bill at checkout).
6. **Currency per expense** — supports multi-currency groups (e.g. international trips).

---

## Sample Queries

### Get all balances for a user across all groups

```sql
SELECT g.name AS group_name, u2.name AS owes_to, b.amount, b.currency
FROM balances b
JOIN groups g ON g.id = b.group_id
JOIN users u2 ON u2.id = b.owes_to
WHERE b.user_id = ? AND b.amount > 0
ORDER BY b.amount DESC;
```

### Get expense history for a group

```sql
SELECT e.id, e.description, e.amount, e.currency, u.name AS paid_by,
       e.expense_date, e.split_type
FROM expenses e
JOIN users u ON u.id = e.paid_by
WHERE e.group_id = ? AND e.is_deleted = FALSE
ORDER BY e.expense_date DESC;
```

### Get what a specific user owes/is owed in a group

```sql
-- What I owe others
SELECT u.name, b.amount, b.currency
FROM balances b JOIN users u ON u.id = b.owes_to
WHERE b.group_id = ? AND b.user_id = ? AND b.amount > 0;

-- What others owe me
SELECT u.name, b.amount, b.currency
FROM balances b JOIN users u ON u.id = b.user_id
WHERE b.group_id = ? AND b.owes_to = ? AND b.amount > 0;
```
