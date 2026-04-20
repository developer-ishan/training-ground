package src.models.abstractions;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import src.models.enums.TransactionType;

public abstract class Transaction {
  private final String id;
  private final BigDecimal amount;
  private final String description;
  private final LocalDateTime timestamp;

  public Transaction(String id, BigDecimal amount, String description){
    this.id = id;
    this.amount = amount;
    this.description = description;
    this.timestamp = LocalDateTime.now();
  }

  public abstract TransactionType getType();

  public BigDecimal getAmount() {
    return amount;
  }

  public String getDescription() {
    return description;
  }

  public String getId() {
    return id;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }
}