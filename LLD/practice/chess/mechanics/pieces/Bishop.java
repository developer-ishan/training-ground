package mechanics.pieces;

import java.util.List;

import mechanics.entities.Board;
import mechanics.entities.Color;
import mechanics.pieces.movement.BishopMoveStrategy;
import mechanics.pieces.movement.MoveStrategy;

/**
 * Bishop moves diagonally in all four directions until blocked.
 * Uses DirectionalPiece template for movement logic.
 */
public class Bishop extends IPiece {

  private final MoveStrategy moveStrategy;
  public Bishop(Color color) {
     super(color);
     this.moveStrategy = new BishopMoveStrategy(this);
  }

  @Override
  public String toString() {
      return color == Color.WHITE ? "♗" : "♝";
  }

  @Override
  public List possibleMoves(Board board) {
    return moveStrategy.possibleMoves(board);
  }
}
