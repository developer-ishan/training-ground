# Synchronized, Wait, Notify, NotifyAll Mini Project

This mini project demonstrates Java's thread synchronization mechanisms through practical examples.

## Files Overview

### 1. ProducerConsumerDemo.java
**Concepts:** `synchronized`, `wait()`, `notifyAll()`

A classic Producer-Consumer pattern with:
- Multiple producers adding items to a shared buffer
- Multiple consumers removing items from the buffer
- Bounded buffer capacity (max 5 items)
- Producers wait when buffer is full
- Consumers wait when buffer is empty

**Key Learning Points:**
- `synchronized` methods ensure thread-safe access
- `wait()` releases the lock and pauses the thread
- `notifyAll()` wakes up all waiting threads
- Demonstrates why we use `while` instead of `if` for wait conditions

### 2. WaitNotifyDemo.java
**Concepts:** `notify()` vs `notifyAll()`

Side-by-side comparison showing:
- `notify()` wakes only ONE waiting thread
- `notifyAll()` wakes ALL waiting threads
- Practical implications of choosing one over the other

**Key Learning Points:**
- When to use `notify()` vs `notifyAll()`
- Visual demonstration of thread wake-up behavior

### 3. BankAccountDemo.java
**Concepts:** `synchronized`, `wait()`, `notifyAll()`, race conditions

Realistic banking scenario with:
- Multiple threads depositing money
- Multiple threads withdrawing money
- Withdrawals wait if insufficient balance
- All operations are thread-safe

**Key Learning Points:**
- Preventing race conditions in shared resources
- Using `wait()` for conditional waiting
- Practical use of synchronization in real-world scenarios

## Running the Examples

### Compile all files:
```bash
javac org/example/mini_project/*.java
```

### Run individual demos:

```bash
# Producer-Consumer Demo
java org.example.mini_project.ProducerConsumerDemo

# Wait/Notify Comparison Demo
java org.example.mini_project.WaitNotifyDemo

# Bank Account Demo
java org.example.mini_project.BankAccountDemo
```

## Key Concepts Explained

### synchronized
- Ensures only one thread can execute a synchronized method/block at a time
- Prevents race conditions and maintains data consistency
- Acquires an intrinsic lock on the object

### wait()
- Must be called from within a synchronized context
- Releases the lock and pauses the thread
- Thread remains paused until another thread calls `notify()` or `notifyAll()`
- Always use in a `while` loop to handle spurious wake-ups

### notify()
- Wakes up ONE arbitrary waiting thread
- Use when only one thread needs to proceed
- More efficient than `notifyAll()` when applicable

### notifyAll()
- Wakes up ALL waiting threads
- Safer choice when multiple threads might need to check conditions
- Recommended when in doubt

## Common Pitfalls to Avoid

1. **Not using synchronized with wait/notify**
   ```java
   // WRONG - throws IllegalMonitorStateException
   object.wait();
   
   // CORRECT
   synchronized(object) {
       object.wait();
   }
   ```

2. **Using if instead of while for wait**
   ```java
   // WRONG - vulnerable to spurious wake-ups
   if (condition) {
       wait();
   }
   
   // CORRECT
   while (condition) {
       wait();
   }
   ```

3. **Forgetting to call notify/notifyAll**
   - Threads will wait forever if never notified

4. **Using notify() when notifyAll() is needed**
   - Can cause deadlocks if multiple threads need to wake up

## Expected Output Patterns

Each demo will show:
- Thread names and their actions
- Wait/notify events
- Buffer/balance state changes
- Synchronization in action

Try running each demo multiple times to see different thread interleavings!
