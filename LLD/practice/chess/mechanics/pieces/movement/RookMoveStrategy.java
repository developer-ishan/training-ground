package mechanics.pieces.movement;

import mechanics.pieces.IPiece;
import mechanics.pieces.Rook;

public class RookMoveStrategy extends DirectionalMoveStrategy{

  public RookMoveStrategy(Rook rook) {
    super(rook);
  }

  @Override
  public int[][] getDirections() {
    // Straight lines only
    return new int[][] { {1,0}, {-1,0}, {0,1}, {0,-1} };
  }
  
}
