User(id, name, email, phone)
- id, name, email, phone
- getters

Group(id, name)
- List<User> members
- List<User> admins
- List<Transaction> transactions
- addUser, removeGroup
- addTransaction()
- getBalance(userA, userB) [calculate on the fly]

Interface SplitStrategy
- Type EQUAL, EXACT, PERCENTAGE
- <Map<User, amount>> getSplit(List<User> participants, Double amount)

class Transaction uniform abstraction over expense
class SettleUp extends Transaction

Expense
Map<User, amount> lenders;
Map<User, amount> split;

Splitwise
List<User> users
List<Group> groups
