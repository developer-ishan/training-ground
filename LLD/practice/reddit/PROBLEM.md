# LLD: Reddit-like Discussion Platform

## Product Overview

Design a Reddit-like discussion platform where users can create posts, and other users can engage through a threaded commenting system. Comments can be nested arbitrarily deep — users can reply to a post directly or reply to any existing comment, forming conversation threads.

---

## Core Entities

- **User** — A registered member of the platform.
- **Post** — A piece of content (text/link) created by a user within a community.
- **Community (Subreddit)** — A topic-based group where posts are published.
- **Comment** — A response to a post or to another comment, forming a recursive tree.
- **Vote** — An upvote or downvote on a post or comment.

---

## Functional Requirements

### Users
1. Users can register with a unique username and email.
2. Users can view their own profile (karma, post/comment history).

### Communities
3. Users can create a community with a unique name and description.
4. Users can join or leave a community.

### Posts
5. A user can create a text or link post within a community they have joined.
6. Posts can be upvoted or downvoted by any user (one vote per user per post).
7. Posts display a **score** (upvotes − downvotes).
8. Users can delete their own posts.

### Comments & Threads
9. A user can add a **top-level comment** on a post (parent_comment = NULL).
10. A user can **reply to any existing comment**, creating a nested thread.
11. Comments can be nested to **arbitrary depth**.
12. Comments can be upvoted or downvoted (one vote per user per comment).
13. Comments display a score (upvotes − downvotes).
14. Users can delete their own comments (soft delete — content replaced with `[deleted]`).

### Feed & Retrieval
15. Retrieve the **top posts** in a community sorted by score (descending), with pagination.
16. Retrieve the **newest posts** in a community sorted by creation time, with pagination.
17. Retrieve the **full comment tree** for a post, ordered by score at each level.
18. Retrieve all posts by a specific user.
19. Retrieve all comments by a specific user.

---

## Step I — DB Schema & SQL Queries

### Part A: Design the Schema

Design the relational database schema (tables, columns, types, constraints, indexes) for the entities above. Consider:

- How to model the **recursive comment tree** (adjacency list, materialized path, or nested sets — pick one and justify).
- How to efficiently store and compute **vote scores**.
- How to enforce **one vote per user per entity**.
- Appropriate **indexes** for the query patterns listed below.

### Part B: Write SQL Queries for All Business Logic

Write production-quality SQL for each of the following operations:

| #  | Operation | Notes |
|----|-----------|-------|
| 1  | Register a new user | Insert with uniqueness check |
| 2  | Create a community | Unique name constraint |
| 3  | User joins a community | Idempotent |
| 4  | User leaves a community | |
| 5  | Create a post in a community | Validate membership |
| 6  | Delete a post (soft delete) | Only the author |
| 7  | Upvote / downvote a post | Upsert — change vote or insert new |
| 8  | Remove vote from a post | |
| 9  | Get top posts in a community | Paginated, sorted by score desc |
| 10 | Get newest posts in a community | Paginated, sorted by created_at desc |
| 11 | Add a top-level comment to a post | parent_comment_id = NULL |
| 12 | Reply to an existing comment | parent_comment_id = <id> |
| 13 | Soft-delete a comment | Mark deleted, preserve tree structure |
| 14 | Upvote / downvote a comment | Upsert semantics |
| 15 | Remove vote from a comment | |
| 16 | Get full comment tree for a post | Recursive, ordered by score at each level |
| 17 | Get all posts by a user | Paginated |
| 18 | Get all comments by a user | Paginated |
| 19 | Get user profile with karma | Total post karma + comment karma |
| 20 | Get members of a community | With count |

---

## Constraints & Assumptions

- Use **PostgreSQL** syntax (CTEs, `ON CONFLICT`, recursive queries are all fair game).
- Assume moderate scale: millions of posts, tens of millions of comments.
- Optimize for **read-heavy** workloads (feeds and comment trees are read far more often than written).
- Vote scores may be **denormalized** for performance — justify if you do.
- Timestamps should use `TIMESTAMPTZ`.
- All deletes are **soft deletes** (use `is_deleted` or `deleted_at`).
- Use **UUIDs** or **BIGSERIAL** for primary keys — pick one and be consistent.

---

## Evaluation Criteria

| Criterion | What to look for |
|-----------|-----------------|
| **Schema correctness** | Proper normalization, FK relationships, NOT NULL constraints |
| **Comment tree model** | Clean recursive model with efficient retrieval |
| **Vote integrity** | Unique constraint preventing double votes, correct score computation |
| **Query correctness** | Queries return the right data for each operation |
| **Index strategy** | Indexes match the query patterns (no missing, no unnecessary) |
| **Edge cases** | Handling deleted content, vote flips, empty trees, pagination boundaries |
| **Readability** | Clean SQL, meaningful aliases, comments where non-obvious |

---

## Getting Started

Create your solution in `solution.sql` in this directory. Structure it as:

```
-- ============================================================
-- PART A: SCHEMA
-- ============================================================

-- (tables, indexes, constraints)

-- ============================================================
-- PART B: QUERIES
-- ============================================================

-- Q1: Register a new user
-- Q2: Create a community
-- ...
```

Good luck!
