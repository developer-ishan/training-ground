import java.util.List;
import java.util.Scanner;

import game.GameContext;
import mechanics.entities.Board;
import mechanics.entities.Color;
import mechanics.entities.Position;
import mechanics.entities.PositionFactory;
import mechanics.pieces.IPiece;
import players.BeginnerPlayerStrategy;
import players.Player;
import players.User;

public class Main {
    public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    System.out.println("Welcome to Chess!");
    
    System.out.print("Enter name for Player 1; ");
    String name1 = scanner.nextLine();
    User user1 = new User(name1);

    System.out.print("Enter name for Player 2: ");
    String name2 = scanner.nextLine();
    User user2 = new User(name2);

    // Player Cell type selection
    System.out.print(name1 + ", choose your Colour: ");
    String cellType1 = scanner.nextLine().toUpperCase();
    while (!cellType1.equals("W") && !cellType1.equals("B")) {
      System.out.print("Invalid choice. Please choose W or B: ");
      cellType1 = scanner.nextLine().toUpperCase();
    }
    Color c1 = cellType1.equals("W") ? Color.WHITE : Color.BLACK;
    Color c2 = (c1 == Color.WHITE) ? Color.BLACK : Color.WHITE;


    //players initialization
    List<Player> players = List.of(
      new Player(user1, c1, new BeginnerPlayerStrategy()),
      new Player(user2, c2, new BeginnerPlayerStrategy())
    );

    Board board = new Board();

    GameContext gameContext = new GameContext(board, players);

    while (!gameContext.isGameOver()) {
      gameContext.continueGame();
    }

    gameContext.showWinner();

  }
}
