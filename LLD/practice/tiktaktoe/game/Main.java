package tiktaktoe.game;

import java.util.List;
import java.util.Scanner;

import tiktaktoe.game.entities.Board;
import tiktaktoe.game.entities.Player;
import tiktaktoe.game.entities.User;

public class Main {
  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    System.out.println("Welcome to Tic Tac Toe!");
    
    System.out.print("Enter name for Player 1; ");
    String name1 = scanner.nextLine();
    User user1 = new User(name1);

    System.out.print("Enter name for Player 2: ");
    String name2 = scanner.nextLine();
    User user2 = new User(name2);

    // Player Cell type selection
    System.out.print(name1 + ", choose your cell type (X/O): ");
    String cellType1 = scanner.nextLine().toUpperCase();
    while (!cellType1.equals("X") && !cellType1.equals("O")) {
      System.out.print("Invalid choice. Please choose X or O: ");
      cellType1 = scanner.nextLine().toUpperCase();
    }
    Cell cell1 = cellType1.equals("X") ? Cell.X : Cell.O;
    Cell cell2 = (cell1 == Cell.X) ? Cell.O : Cell.X;


    //players initialization
    List<Player> players = List.of(
      new Player(user1, cell1, new HumanPlayerStrategy()),
      new Player(user2, cell2, new HumanPlayerStrategy())
    );

    //board initialization
    //Select Board Size
    System.out.print("Enter board size (e.g., enter 3 for a 3x3 board): ");
    int size = scanner.nextInt();
    Board board = new Board(3,3);

    GameContext gameContext = new GameContext(board, players);

    while (!gameContext.isGameOver()) {
      gameContext.continueGame();
    }

    gameContext.showWinner();

  }
}
