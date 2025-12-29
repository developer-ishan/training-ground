package mechanics.pieces;

import java.util.List;

import mechanics.entities.Board;
import mechanics.entities.Color;
import mechanics.pieces.movement.MoveStrategy;
import mechanics.pieces.movement.QueenMoveStrategy;

/**
 * Queen combines the movement of Rook and Bishop:
 * can move diagonally, vertically, and horizontally until blocked.
 */
public class Queen extends IPiece {

  private final MoveStrategy moveStrategy;

  public Queen(Color color) {
      super(color);
      this.moveStrategy = new QueenMoveStrategy(this);
  }

  @Override
  public String toString() {
      return color == Color.WHITE ? "♕" : "♛";
  }

  @Override
  public List possibleMoves(Board board) {
    return moveStrategy.possibleMoves(board);
  }
}
