package mechanics.pieces;

import java.util.List;

import mechanics.entities.Board;
import mechanics.entities.Color;
import mechanics.entities.Move;
import mechanics.entities.PositionFactory;

public abstract class IPiece {
  protected PositionFactory positionFactory = PositionFactory.getInstance();
  protected Color color;
  public IPiece(Color color){
    this.color = color;
  }

  public abstract List possibleMoves(Board board);

  public Color getColor() {
    return color;
  }

  abstract public String toString();
}
