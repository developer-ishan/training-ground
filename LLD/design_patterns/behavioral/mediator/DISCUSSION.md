| Component              | Pattern Used | Description                                          |
| ---------------------- | ------------ | ---------------------------------------------------- |
| `ChatMediator`         | Mediator     | Central hub for all user communication               |
| `User`                 | Observer     | Users observe updates/messages                       |
| `SendMessageCommand`   | Command      | Encapsulates a message send as a command             |
| `CommandHistory`       | Command      | Maintains undo/redo stack                            |
| `NotificationStrategy` | Strategy     | Different notification strategies (Email, SMS, Push) |
| `NotificationFactory`  | Factory      | Chooses correct `NotificationStrategy` based on type |
| `LoggedUserDecorator`  | Decorator    | Adds logging capabilities to users                   |
| `UserSessionManager`   | Singleton    | Ensures only one instance manages user sessions      |
| `MessageServiceProxy`  | Proxy        | Controls access to the actual message service        |
