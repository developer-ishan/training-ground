package src.models;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

import src.models.abstractions.Transaction;
import src.models.enums.SplitType;
import src.models.enums.TransactionType;

public class Expense extends Transaction{

  private final User paidBy;
  private final Map<User, BigDecimal> splits;
  private final SplitType splitType;

  public Expense(String id, BigDecimal amount, String description, User paidBy, Map<User, BigDecimal> splits, SplitType splitType) {
    super(id, amount, description);
    this.paidBy = paidBy;
    this.splits = Collections.unmodifiableMap(splits);
    this.splitType = splitType;
  }
  
  @Override
  public TransactionType getType() {
    return TransactionType.EXPENSE;
  }

  public User getPaidBy() { return paidBy; }
  public Map<User, BigDecimal> getSplits() { return splits; }
  public SplitType getSplitType() { return splitType; }
}
