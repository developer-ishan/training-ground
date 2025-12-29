package mechanics.pieces.movement;

import java.util.ArrayList;
import java.util.List;

import mechanics.entities.Board;
import mechanics.entities.Move;
import mechanics.entities.Position;
import mechanics.pieces.IPiece;

public abstract class DirectionalMoveStrategy extends MoveStrategy{
  
  private final IPiece piece;
  public DirectionalMoveStrategy(IPiece piece){
    this.piece = piece;
  }  

  @Override
    public List<Move> possibleMoves(Board board) {
        List<Move> moves = new ArrayList<>();

        // 1️⃣ Get the current position of this piece
        Position currentPos = board.getPosition(piece);
        if (currentPos == null) return moves; // safety check

        char file = currentPos.getFile();
        int rank = currentPos.getRank();

        // 2️⃣ Loop over each allowed direction
        for (int[] dir : getDirections()) {
            int dx = dir[0];
            int dy = dir[1];

            char f = file;
            int r = rank;

            // 3️⃣ Move step by step in this direction until blocked
            while (true) {
                f += dx;
                r += dy;

                // 4️⃣ Stop if we go outside the board boundaries
                if (f < 'a' || f > 'h' || r < 1 || r > 8) break;

                Position nextPos = positionFactory.get(f, r);
                IPiece target = board.getPieceAt(nextPos);

                if (target == null) {
                    // ✅ Empty square: add as valid move
                    moves.add(new Move(piece, currentPos, nextPos));
                } else {
                    // 🔶 Occupied square
                    if (target.getColor() != piece.getColor()) {
                        // ✅ Opponent piece: can capture
                        moves.add(new Move(piece, currentPos, nextPos));
                    }
                    // ❌ Cannot move past any piece (friend or foe)
                    break;
                }
            }
        }

        return moves;
    }


  public abstract int[][] getDirections();

}
