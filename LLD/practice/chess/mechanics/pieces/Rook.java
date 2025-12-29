package mechanics.pieces;

import java.util.List;

import mechanics.entities.Board;
import mechanics.entities.Color;
import mechanics.pieces.movement.MoveStrategy;
import mechanics.pieces.movement.RookMoveStrategy;

/**
 * Rook moves vertically and horizontally until blocked.
 * Uses DirectionalPiece template for movement logic.
 */
public class Rook extends IPiece {

  private MoveStrategy moveStrategy;

  public Rook(Color color) {
    super(color);
    this.moveStrategy = new RookMoveStrategy(this);
  }

  @Override
  public String toString() {
    return color == Color.WHITE ? "♖" : "♜";
  }

  @Override
  public List possibleMoves(Board board) {
    return moveStrategy.possibleMoves(board);
  }
}
