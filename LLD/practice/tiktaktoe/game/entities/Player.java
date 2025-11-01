package tiktaktoe.game.entities;

import tiktaktoe.game.Cell;
import tiktaktoe.game.PlayerStrategy;

public class Player {
  private User user;
  private Cell type;
  private PlayerStrategy playerStrategy;

  public Player(User user, Cell type, PlayerStrategy playerStrategy) {
    this.user = user;
    this.type = type;
    this.playerStrategy = playerStrategy;
  }

  public User getUser() {
    return user;
  }

  public Cell getCellType() {
    return type;
  }

  @Override
  public String toString() {
    return user + "(" + type + ")";
  }

  public Move getMove() {
    return playerStrategy.getMove(this);
  }
}
