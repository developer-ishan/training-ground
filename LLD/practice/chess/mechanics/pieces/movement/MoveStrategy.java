package mechanics.pieces.movement;

import java.util.List;

import mechanics.entities.Board;
import mechanics.entities.PositionFactory;

public abstract class MoveStrategy {
  protected PositionFactory positionFactory = PositionFactory.getInstance();
  public abstract List possibleMoves(Board board);
}
