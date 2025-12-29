package players;

import java.util.List;
import java.util.Scanner;

import mechanics.entities.Board;
import mechanics.entities.Move;
import mechanics.entities.Position;
import mechanics.entities.PositionFactory;
import mechanics.pieces.IPiece;

public class AdvancePlayerStrategy implements PlayerStrategy {

  PositionFactory positionFactory = PositionFactory.getInstance();

  @Override
  public Move getMove(Player player, Board board) {
    board.display();
    Move move = null;
    Scanner scanner = new Scanner(System.in);

    while (true) {
      System.out.println(player + ", enter your move (e.g., e2e4): ");
      String input = scanner.nextLine().trim();

      // --- Basic input validation ---
      if (input.length() != 4) {
        System.out.println("Invalid input format. Use format like e2e4.");
        continue;
      }

      String fromStr = input.substring(0, 2);
      String toStr = input.substring(2, 4);
      Position from = positionFactory.get(fromStr);
      Position to = positionFactory.get(toStr);

      IPiece piece = board.getPieceAt(from);
      if (piece == null) {
        System.out.println("No piece at " + from + ". Try again.");
        continue;
      }

      if (piece.getColor() != player.getColor()) {
        System.out.println("That is not your piece. Try again.");
        continue;
      }

      // --- Check if move is valid ---
      List<Move> possibleMoves = piece.possibleMoves(board);
      boolean valid = false;
      for (Move m : possibleMoves) {
        if (m.getTo().equals(to)) {
          move = m;
          valid = true;
          break;
        }
      }

      if (!valid) {
        System.out.println("Invalid move for " + piece + ". Try again.");
        continue;
      }

      // If valid, break the loop and return the move
      break;
    }

    return move;
  }
}
