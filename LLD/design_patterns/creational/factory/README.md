### 💡 LLD Question: Factory Design Pattern

You are tasked with designing a system for a **Notification Service** that supports sending different types of notifications to users such as:

- **Email**
- **SMS**
- **Push Notification**

Each notification type has different implementations for sending messages.

#### Requirements:

1. The client code should not be responsible for instantiating the specific notification classes.
2. Add support for new notification types in the future with minimal changes to existing code.
3. Each notification should implement a common interface `Notification` with a method `send(String message)`.

#### Tasks:

1. Design the class structure using the **Factory Design Pattern**.
2. Write the interfaces and classes involved.
3. Implement a `NotificationFactory` that returns the appropriate notification object based on the input.
4. Provide a simple client code example that uses the factory to send a notification.
5. Extend the design to add a new type of notification: **WhatsApp**.

> Use your preferred programming language (Java, Python, etc.) to demonstrate the implementation.
