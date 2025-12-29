package mechanics.pieces.movement;

import mechanics.pieces.IPiece;
import mechanics.pieces.Queen;

public class QueenMoveStrategy extends DirectionalMoveStrategy{

  public QueenMoveStrategy(Queen queen) {
    super(queen);
  }

  @Override
  public int[][] getDirections() {
  // Combine diagonals and straight lines
    return new int[][] {
    {1,1}, {-1,1}, {1,-1}, {-1,-1},
    {1,0}, {-1,0}, {0,1}, {0,-1}
    };
  }
  
}
