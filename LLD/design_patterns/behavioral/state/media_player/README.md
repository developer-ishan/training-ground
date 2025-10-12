Sure! Here's a clear and copyable **Low-Level Design (LLD) Question** on the **State Design Pattern** written in markdown:

---

## 💡 LLD Question: State Design Pattern

### 🎯 Problem Statement:

You are designing a **Media Player** application that supports different playback states. The player can be in one of the following states:

* **Stopped**
* **Playing**
* **Paused**

Depending on the current state, the behavior of the following actions changes:

* `play()`
* `pause()`
* `stop()`

Your task is to use the **State Design Pattern** to model the behavior of the media player such that:

1. Each state encapsulates its own behavior and knows how to transition to other states.
2. The `MediaPlayer` class delegates behavior to its current state.
3. Transitions between states should be handled **internally by the state classes** based on the invoked actions.
4. Invalid transitions (e.g., pausing when stopped) should be **handled gracefully** with appropriate feedback.

### 🔧 Requirements:

* Implement `State` interface with `play()`, `pause()`, and `stop()` methods.
* Create `PlayingState`, `PausedState`, and `StoppedState` classes implementing the interface.
* The `MediaPlayer` class should maintain the current state and delegate the actions to it.
* Demonstrate the behavior transitions through a small `main()` simulation.

### 🔍 Example Behavior:

```java
MediaPlayer player = new MediaPlayer();

player.play();   // Output: "Playing media..."
player.pause();  // Output: "Media paused."
player.stop();   // Output: "Media stopped."
player.pause();  // Output: "Cannot pause. Media is already stopped."
```

---

Let me know if you'd like the **full code implementation**, **unit tests**, or an **extension with additional states** like `Buffering`, `Rewinding`, etc.
