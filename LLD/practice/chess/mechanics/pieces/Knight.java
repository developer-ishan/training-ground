package mechanics.pieces;

import java.util.ArrayList;
import java.util.List;

import mechanics.entities.Board;
import mechanics.entities.Color;
import mechanics.entities.Move;
import mechanics.entities.Position;

public class Knight extends IPiece {
  public Knight(Color color) {
    super(color);
  }

  @Override
  public List<Move> possibleMoves(Board board) {
    List<Move> moves = new ArrayList<>();

    // 1️⃣ Get the current position of the knight
    Position currentPos = board.getPosition(this);
    if (currentPos == null) return moves; // safety check

    char file = currentPos.getFile();
    int rank = currentPos.getRank();

    // 2️⃣ Define all 8 possible L-shaped moves
    // Each pair {dx, dy} indicates how file and rank change
    int[][] directions = {
      {1, 2},   // right 1, up 2
      {2, 1},   // right 2, up 1
      {2, -1},  // right 2, down 1
      {1, -2},  // right 1, down 2
      {-1, -2}, // left 1, down 2
      {-2, -1}, // left 2, down 1
      {-2, 1},  // left 2, up 1
      {-1, 2}   // left 1, up 2
    };

    // 3️⃣ Loop over each possible move
    for (int[] dir : directions) {
      char f = (char) (file + dir[0]);
      int r = rank + dir[1];

      // 4️⃣ Check if the target square is within board boundaries
      if (f < 'a' || f > 'h' || r < 1 || r > 8) continue;

      Position nextPos = positionFactory.get(f, r);
      IPiece target = board.getPieceAt(nextPos);

      // 5️⃣ Add move if square is empty or has an opponent piece
      if (target == null || target.getColor() != this.color) {
        moves.add(new Move(this, currentPos, nextPos));
      }

      /* Example scenario:
       * Knight at d4:
       * - Can move to: c6, e6, b5, f5, b3, f3, c2, e2
       * - Can jump over any pieces in between
       * - Cannot move to squares with friendly pieces
       */
    }

    return moves;
  }

  @Override
  public String toString() {
    return color == Color.WHITE ? "♘" : "♞";
  }
}
