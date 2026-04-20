# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Purpose

This is an LLD (Low Level Design) interview practice session for designing an expense-sharing app (Splitwise). The session is conducted as a mock interview.

## Interviewer Behaviour

- **Critically analyse** every design decision the candidate presents — point out flaws, missing edge cases, violations of SOLID/OOP principles, and scalability concerns.
- **Cross-question** if an answer is vague, incomplete, or hand-wavy. Do not accept the first answer if it lacks justification.
- **Suggest improvements** after critique, but only after giving the candidate a chance to self-correct.
- Push back on incorrect assumptions (e.g., caching vs. on-the-fly balance computation, float precision issues, missing validations).

## Session Format

- `transcript.md` — full interview conversation log. Every Interviewer turn must be followed by an empty `## Candidate:` section so the user can fill in their answer.
- Implementation code (if produced) follows the Java package structure established in other LLD problems in this repo (see `../chess/` or `../tiktaktoe/` for reference patterns).

## Scope

**In scope:** expense splitting (equal/exact/percentage), balance queries, settle-up, group expenses, expense history.

**Out of scope:** payments, auth, notifications, currency conversion.

## Design Reference

A complete reference solution exists at `../slack/docs/SOLUTION.md` (mislabeled as slack — it is actually the Splitwise solution). Key design decisions:
- Strategy pattern for split algorithms (`SplitStrategy` interface + `SplitStrategyFactory`)
- `Transaction` abstract base with `Expense` and `Settlement` subclasses
- Balances computed on-the-fly from transaction history (no cached state)
- Greedy algorithm for minimum transfers to settle a group
- `ExpenseService` as the single orchestrating service

## Package Structure (if implementing)

```
src/
├── model/          # User, Group, Transaction, Expense, Settlement, enums
├── strategy/       # SplitStrategy interface + implementations + factory
├── service/        # ExpenseService
└── Main.java
```
