# Problem statement

Build an **LLM gateway** that sends each prompt to **Claude** (primary) or **OpenAI** (fallback). After every attempt it records **which model ran** and **whether it succeeded or failed**, in order. Routing for the **next** request depends only on that **history** and the rules below. Real APIs are not required—stub providers are fine.

**Per-model history.** From the global log, take only entries for a given model. The **most recent** attempts for that model are the last entries in **that model’s** subsequence (scan global history from newest to oldest and keep matches).

**x and y (consecutive streaks).** For each model, **x** and **y** are **counts of consecutive** outcomes at the **tail** of that model’s history—failures or successes **in a row**, with no opposite result inside the streak. Informal wording like “x requests failed concurrently” means **x consecutive failures**, not x parallel in-flight failures.

- **Failure condition:** The **x** most recent attempts for this model **all failed** (and there are at least **x** such attempts). Default **x = 5** unless specified.
- **Recovery condition:** The **y** most recent attempts for this model **all succeeded** (at least **y** attempts). Default **y = 3** unless specified.

If there are **fewer than x** (or **y**) attempts for that model, the corresponding streak condition does **not** hold.

**Routing health (recovery before failure).** For **each** model, when choosing where to send the **next** request, assign **UP**, **DOWN**, or **UNKNOWN**:

1. If the recovery condition holds (**y** consecutive successes at the tail) → **UP**.
2. Else if the failure condition holds (**x** consecutive failures at the tail) → **DOWN**.
3. Else → **UNKNOWN**. **UNKNOWN** is not **DOWN**.

**Important:** Apply step 1 before step 2 so a fresh run of successes can mark the model **UP** even when an older failure streak would matter if you looked at **x** first.

**Next-hop routing.** Let Claude’s label be **C** and OpenAI’s **O** (each UP, DOWN, or UNKNOWN).

- If Claude is **not** **DOWN** → send **100%** of requests to Claude (always Claude next).
- If Claude is **DOWN** and OpenAI is **not** **DOWN** → **5%** Claude, **95%** OpenAI: draw uniformly from **0…99**; if the draw is **in 0–4** use Claude, otherwise OpenAI.
- If Claude is **DOWN** and OpenAI is **DOWN** → **5%** Claude, **5%** OpenAI, **90%** rejected: draw **0…99**; **0–4** Claude, **5–9** OpenAI, **10–99** do **not** call either provider—return a clear error or sentinel.

Claude is preferred whenever it is not **DOWN**. When a model is **DOWN** it may still receive its small share above; use **5%** for those “still try this backend” paths unless you generalize.

**What to build.** A **tracker** (append each completed attempt with model + success), a **policy** that computes **UP / DOWN / UNKNOWN** per model from the tracker using the recovery-first rules, and a **gateway** entry point (e.g. `askPrompt`) that reads policy, applies the next-hop rules (including random choice and reject), calls a stub when not rejected, then records the outcome. You may assume a **single-threaded** caller unless asked otherwise. Whether **rejected** requests are logged is up to you.
