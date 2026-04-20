# EmTest — LLM gateway policy (Java)

Small demo of a **Claude-primary / OpenAI-fallback** gateway with **failure (`x`)** and **recovery (`y`)** windows, request **tracking**, and **weighted routing**.

All sources for this exercise live in the **`interview/`** directory (run commands from there, or `cd interview` first).

## Requirements

- **Java 8+** (tested with `javac` / `java` on the classpath; no build tool).

## Project layout

| File | Role |
|------|------|
| `Main.java` | `Model`, `ModelHealth`, `ModelPolicyConfig`, `AIStrategy` + Claude/OpenAI stubs, `Req`, `Tracker`, `PolicyProvider`, `LlmGateway`, `Main` entry |
| `REQ.md` | Original design notes |
| `GatewayHealthyTrafficTest.java` | Tests: all Claude successes → policy UP, gateway sends traffic to Claude |
| `GatewayFailureTrafficTest.java` | Tests: Claude DOWN (all fails); both Claude + OpenAI DOWN |
| `GatewayRecoveryTrafficTest.java` | Tests: 7 fail + 3 succeed on Claude → UP again, gateway 100% Claude |

## Build and run the sample app

```bash
cd interview
javac Main.java
java Main
```

## Run tests

Compile the main sources with all test classes, then run each test’s `main`:

```bash
cd interview
javac Main.java \
  GatewayHealthyTrafficTest.java \
  GatewayFailureTrafficTest.java \
  GatewayRecoveryTrafficTest.java

java GatewayHealthyTrafficTest
java GatewayFailureTrafficTest
java GatewayRecoveryTrafficTest
```

### Test summary

| Test class | What it checks |
|------------|----------------|
| **GatewayHealthyTrafficTest** | Ten successful Claude requests: `healthForRouting(CLAUDE)` is UP, OpenAI UNKNOWN; `LlmGateway.askPrompt` uses Claude. |
| **GatewayFailureTrafficTest** | Ten Claude failures: Claude DOWN. Then five OpenAI failures + five Claude failures: both models DOWN (each model’s last five attempts all fail). |
| **GatewayRecoveryTrafficTest** | Seven Claude failures then three successes: recovery-first policy marks Claude UP; next `askPrompt` goes to Claude only. |

Policy defaults in tests use **x = 5**, **y = 3**, and **5%** “when down” share where applicable (see `ModelPolicyConfig` in `Main.java`).

## Last verification

All three test mains completed successfully:

- `GatewayHealthyTrafficTest: all passed`
- `GatewayFailureTrafficTest: all passed`
- `GatewayRecoveryTrafficTest: all passed`

Re-run the commands above after changes to confirm.
