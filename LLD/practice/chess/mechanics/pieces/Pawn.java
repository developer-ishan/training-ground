package mechanics.pieces;

import java.util.ArrayList;
import java.util.List;

import mechanics.entities.Board;
import mechanics.entities.Color;
import mechanics.entities.Move;
import mechanics.entities.Position;

public class Pawn extends IPiece {
  public Pawn(Color color) {
    super(color);
  }

  @Override
  public List<Move> possibleMoves(Board board) {
    List<Move> moves = new ArrayList<>();

    // 1️⃣ Get the current position of the pawn
    Position currentPos = board.getPosition(this);
    if (currentPos == null) return moves; // safety check

    char file = currentPos.getFile(); // 'a' to 'h'
    int rank = currentPos.getRank();  // 1 to 8

    // 2️⃣ Determine the direction the pawn moves
    // White pawns move up the board (rank+1), black pawns move down (rank-1)
    int direction = (color == Color.WHITE) ? 1 : -1;

    // --- Forward moves ---

    // 3️⃣ One step forward
    Position oneStepForward = positionFactory.get(file, rank + direction);
    if (board.getPieceAt(oneStepForward) == null) {
      moves.add(new Move(this, currentPos, oneStepForward));

      // 4️⃣ Two steps forward if at starting rank
      boolean atStart = (color == Color.WHITE && rank == 2) || (color == Color.BLACK && rank == 7);
      if (atStart) {
        Position twoStepForward = positionFactory.get(file, rank + 2 * direction);
        if (board.getPieceAt(twoStepForward) == null) {
          moves.add(new Move(this, currentPos, twoStepForward));
        }
      }
    }

    // --- Diagonal captures ---

    // 5️⃣ Check both diagonals (left and right) for opponent pieces
    char[] diagonals = new char[]{ (char)(file - 1), (char)(file + 1) };
    for (char diagFile : diagonals) {
      if (diagFile < 'a' || diagFile > 'h') continue; // stay inside board
      Position diagPos = positionFactory.get(diagFile, rank + direction);
      IPiece target = board.getPieceAt(diagPos);

      // 6️⃣ Capture if the square has an opponent piece
      if (target != null && target.getColor() != this.color) {
        moves.add(new Move(this, currentPos, diagPos));
      }
    }

    /*
     * Example scenario:
     * - White pawn at d2: 
     *   - Forward moves: d3, d4 (if d3 is empty)
     *   - Captures: c3, e3 if opponent pieces are there
     * - Black pawn at e7:
     *   - Forward moves: e6, e5
     *   - Captures: d6, f6
     */

    return moves;
  }

  @Override
  public String toString() {
    return color == Color.WHITE ? "♙" : "♟";
  }
}
