### 🧩 Problem Statement

Design a **logging framework** using the **Chain of Responsibility** design pattern.

The framework should support multiple levels of logging:
- `INFO`
- `DEBUG`
- `ERROR`

Each logger should decide whether to handle the message or pass it along the chain based on the severity level.

#### 🎯 Requirements:
1. **Logger Interface / Abstract Class**:
    - Should define a method to handle the log message.
    - Should maintain a reference to the next logger in the chain.

2. **Concrete Loggers**:
    - `InfoLogger` should only log INFO level messages.
    - `DebugLogger` should log DEBUG and higher.
    - `ErrorLogger` should log all types of messages (INFO, DEBUG, ERROR).

3. **Severity Levels**:
    - Messages should have a `LogLevel` (e.g., enum).

4. **Chain Configuration**:
    - You should be able to construct a chain like:
      ```java
      ErrorLogger -> DebugLogger -> InfoLogger
      ```

5. **Test Case**:
    - When logging a message of level `DEBUG`, only the `ErrorLogger` and `DebugLogger` should log it.

#### 🔍 Optional Extension:
- Add a `FileLogger` or `DatabaseLogger` to demonstrate extensibility of the chain.
- Make the system thread-safe for concurrent logging.

#### Example:
```java
Logger loggerChain = getChain();
loggerChain.logMessage(LogLevel.DEBUG, "This is a debug message");
