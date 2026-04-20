package src.models;

import java.math.BigDecimal;

import src.models.abstractions.Transaction;
import src.models.enums.TransactionType;

public class Settlement extends Transaction{

  private final User fromUser;
  private final User toUser;

  public Settlement(String id, BigDecimal amount, String description, User fromUser, User toUser) {
    super(id, amount, description);
    this.fromUser = fromUser;
    this.toUser = toUser;
  }

  @Override
  public TransactionType getType() {
    return TransactionType.SETTLEMENT;
  }

  public User getFromUser() { return fromUser; }
  public User getToUser() { return toUser; }
}
