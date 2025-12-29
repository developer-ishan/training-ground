package mechanics.pieces.movement;

import mechanics.pieces.Bishop;

public class BishopMoveStrategy extends DirectionalMoveStrategy{

  public BishopMoveStrategy(Bishop bishop) {
    super(bishop);
  }
  @Override
  public int[][] getDirections() {
    return new int[][] { {1,1}, {-1,1}, {1,-1}, {-1,-1} };
  }
}
