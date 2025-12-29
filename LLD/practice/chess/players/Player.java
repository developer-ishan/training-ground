package players;

import mechanics.entities.Board;
import mechanics.entities.Color;
import mechanics.entities.Move;

public class Player {
  private User user;
  private Color color;
  private PlayerStrategy playerStrategy;

  public Player(User user, Color color, PlayerStrategy playerStrategy){
    this.color = color;
    this.playerStrategy = playerStrategy;
    this.user = user;
  }

  public User getUser() {
    return user;
  }

  public Color getColor() {
    return color;
  }

  @Override
  public String toString() {
    return user + "(" + color + ")";
  }

  public Move getMove(Board board){
    return playerStrategy.getMove(this, board);
  }
}
