## 🚧 Problem Statement

You are tasked with designing a system to create complex `UserProfile` objects for a social networking platform.

Each `UserProfile` consists of the following fields:

- `String name` (required)
- `int age` (optional)
- `String email` (optional)
- `String phoneNumber` (optional)
- `String address` (optional)
- `String bio` (optional)
- `List<String> interests` (optional)

Because the profile creation can be flexible (some fields might be omitted), the client should be able to construct `UserProfile` objects in a readable and controlled manner.

---

## 🎯 Requirements

1. Use the **Builder Design Pattern** to allow step-by-step construction of `UserProfile` objects.
2. Ensure that `name` is a **required field**.
3. The builder should support method chaining like:
   ```java
   UserProfile profile = new UserProfile.Builder("Alice")
                                .age(28)
                                .email("alice@example.com")
                                .bio("Loves photography")
                                .build();
