User(id, name, email, phone)
- id, name, email, phone
- getters

Group(id, name)
- List<User> members
- List<User> admins
- List<Transaction> transactions
- addUser, removeGroup
- addTransaction()

Interface SplitStrategy
- Type EQUAL, EXACT, PERCENTAGE
- <Map<User, amount>> getSplit(List<User> participants, List<Map<User, amount>> lenders)

abstract class Transaction

class ExpenseTransaction extends Transaction
class SettleUp extends Transaction

Expense
List<Map<User, amount>> lenders;
List<Map<User, amount>> split;

