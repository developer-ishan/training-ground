## Problem Statement

Design a Logging System for a multi-threaded application using the Singleton Design Pattern.

### Requirements:
1. Only one instance of the `Logger` class should exist throughout the application lifecycle.
2. The logger should be thread-safe — multiple threads can access it without creating multiple instances.
3. The logger should expose a method `log(String message)` that appends messages to a central in-memory log.
4. Provide a method `getLogHistory()` that returns all the logged messages in order.
5. Demonstrate the usage of the singleton `Logger` by simulating multiple threads logging messages.

### Constraints:
- Do not use external logging libraries (like SLF4J or Log4J).
- The implementation must be thread-safe.
- Optimize for performance — avoid unnecessary synchronization after the instance is created.

### Deliverables:
- Java implementation of the `Logger` class using the Singleton pattern.
- A sample main class that spawns multiple threads to log messages.
- Output showing that all logs were written by the same logger instance.
