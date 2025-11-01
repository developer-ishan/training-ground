# a_TikTakToe
- simple implementation of the tik_tok_game

# Implementation 1

```java
package tiktaktoe;

import java.util.Scanner;

public class a_TikTakToe {

  public enum CellType {
    X,O,E
  }

  public class User {
    private String name;
  }

  public class Player {
    private User user;
    private CellType cellType;
  }

  public class Board {
    private CellType[][] grid;
    public Board(){
      // grid init
    }

    public boolean playTurn(Player player ,int row, int col){
      // check turn validity
      // check if the cell is already occupied
      //write in the grid
    }

    public boolean isFull() {}

    public CellType getWinner() {}

    public void display() {}
  }

  public class Game {
    private Player[] players;
    private Board board;
    private int currentPlayer;
  
    public Game(Player player1, Player player2) {
      // init the properties
    }

    
    public void startGame() {
      Scanner scanner = new Scanner(System.in);
      while (true) {

        if(board.getWinner() != CellType.E) {
            break;
        }
        
        if (board.isFull()) {
          break;
        }

        board.display();
        int row = scanner.nextInt(), col = scanner.nextInt();
        if(board.playTurn(players[currentPlayer], row, col)) {
          currentPlayer = (currentPlayer + 1) % 2;
        } else {
          System.out.println("Try again!");
          continue;
        }
      }
    }
  }

  public static void main(String[] args) {
    a_TikTakToe ttt = new a_TikTakToe();
    Scanner scanner = new Scanner(System.in);

    System.out.print("Enter name for Player 1 (X): ");
    String name1 = scanner.nextLine();
    User user1 = ttt.new User(name1);
    Player player1 = ttt.new Player(user1, CellType.X);

    System.out.print("Enter name for Player 2 (O): ");
    String name2 = scanner.nextLine();
    User user2 = ttt.new User(name2);
    Player player2 = ttt.new Player(user2, CellType.O);

    Game game = ttt.new Game(player1, player2);
    game.startGame();
  }
}

```
  ## Issues with the implementation
  - No way of knowing the game state after game has finished, no state management
  - Difficult to add new type of players, eg: AI players of different difficulty
    - The human player input is tightly coupled to the game
  - No communication b/w players, eg for the moves
  - Handle different player creation via a factory
