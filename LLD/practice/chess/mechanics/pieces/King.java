package mechanics.pieces;

import java.util.ArrayList;
import java.util.List;

import mechanics.entities.Board;
import mechanics.entities.Color;
import mechanics.entities.Move;
import mechanics.entities.Position;

public class King extends IPiece {
  public King(Color color) {
    super(color);
  }

  @Override
  public List<Move> possibleMoves(Board board) {
    List<Move> moves = new ArrayList<>();

    // 1️⃣ Get current position of the king
    Position currentPos = board.getPosition(this);
    if (currentPos == null) return moves; // safety check

    char file = currentPos.getFile(); // 'a' to 'h'
    int rank = currentPos.getRank();  // 1 to 8

    // 2️⃣ Define all 8 possible directions for the king (one step)
    int[][] directions = {
      {1, 0},   // right
      {-1, 0},  // left
      {0, 1},   // up
      {0, -1},  // down
      {1, 1},   // up-right
      {-1, 1},  // up-left
      {1, -1},  // down-right
      {-1, -1}  // down-left
    };

    // 3️⃣ Loop over each direction
    for (int[] dir : directions) {
      char f = (char) (file + dir[0]);
      int r = rank + dir[1];

      // 4️⃣ Check board boundaries
      if (f < 'a' || f > 'h' || r < 1 || r > 8) continue;

      Position nextPos = positionFactory.get(f, r);
      IPiece target = board.getPieceAt(nextPos);

      // 5️⃣ If the square is empty or has an opponent piece, the king can move there
      if (target == null || target.getColor() != this.color) {
        moves.add(new Move(this, currentPos, nextPos));
      }

      // 6️⃣ King cannot move past one step, so no loop required
    }

    /*
     * Example scenario:
     * King at e4:
     * - Can move to d3, d4, d5, e3, e5, f3, f4, f5
     * - Cannot move to squares with friendly pieces
     * - Can capture opponent pieces in adjacent squares
     */

    return moves;
  }

  @Override
  public String toString() {
    return color == Color.WHITE ? "♔" : "♚";
  }
}
