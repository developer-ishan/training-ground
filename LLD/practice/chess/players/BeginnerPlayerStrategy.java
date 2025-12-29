package players;

import java.util.List;
import java.util.Scanner;

import mechanics.entities.Board;
import mechanics.entities.Move;
import mechanics.pieces.IPiece;

public class BeginnerPlayerStrategy implements PlayerStrategy{

  @Override
  public Move getMove(Player player, Board board) {
    board.display();
    Scanner scanner = new Scanner(System.in);
    System.out.print(player + ", enter the position of piece your want to move: ");
    String pos = scanner.nextLine();
    IPiece piece = board.getPieceAt(pos);
    System.out.print("Select Your Move: ");
    List<Move> moves = piece.possibleMoves(board);
    System.out.print(moves);
    int x = scanner.nextInt();
    return moves.get(x);
  }
  
}
