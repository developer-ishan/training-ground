package src.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import src.models.abstractions.Transaction;

public class Group {
  private final String id;
  private final String name;
  private final List<User> members;
  private final List<Transaction> transactions;

  public Group(String id, String name) {
    this.id = id;
    this.name = name;
    this.members = new ArrayList<>();
    this.transactions = new ArrayList<>();
  }

  public void addMember(User user) {
    if (isMember(user)) {
      throw new IllegalArgumentException("User " + user.getName() + " is already a member of group " + name);
    }
    members.add(user);
  }

  public boolean isMember(User user) {
    return members.contains(user);
  }

  public void addTransaction(Transaction t) {
    transactions.add(t);
  }

  public String getId() { return id; }
  public String getName() { return name; }

  public List<User> getMembers() {
    return Collections.unmodifiableList(members);
  }

  public List<Transaction> getTransactions() {
    return Collections.unmodifiableList(transactions);
  }
}
