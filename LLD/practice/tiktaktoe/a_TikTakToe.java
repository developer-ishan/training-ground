package tiktaktoe;

import java.util.Scanner;

public class a_TikTakToe {

  public enum CellType {
    X,
    O,
    E
  }

  public class User {
    private String name;
    public User(String name) {
      this.name = name;
    }
    public String getName() {
      return name;
    }
  }

  public class Player {
    private User user;
    private CellType cellType;

    public Player(User user, CellType cellType) {
      this.user = user;
      this.cellType = cellType;
    }

    public User getUser() {
      return user;
    }

    public CellType getCellType() {
      return cellType;
    }

    @Override
    public String toString() {
      return user.getName() + "(" + cellType + ")";
    }
  }

  public class Board {
    private CellType[][] grid;
    public Board(){
      grid  = new CellType[3][3];
      for(int i=0; i<3; i++) {
        for(int j=0; j<3; j++) {
          grid[i][j] = CellType.E;
        }
      }
    }

    public boolean playTurn(Player player ,int row, int col){
      if(row < 0 || row >= 3 || col < 0 || col >= 3) {
        System.out.println("Invalid move");
        return false;
      }
      if(grid[row][col] != CellType.E) {
        System.out.println("Cell already occupied");
        return false;
      }
      grid[row][col] = player.getCellType();
      return true;
    }

    public boolean isFull() {
      for(int i=0; i<3; i++) {
        for(int j=0; j<3; j++) {
          if(grid[i][j] == CellType.E) {
            return false;
          }
        }
      }
      return true;
    }

    public CellType getWinner() {
      // Check rows and columns
      for(int i=0; i<3; i++) {
        if(grid[i][0] != CellType.E && grid[i][0] == grid[i][1] && grid[i][1] == grid[i][2]) {
          return grid[i][0];
        }
        if(grid[0][i] != CellType.E && grid[0][i] == grid[1][i] && grid[1][i] == grid[2][i]) {
          return grid[0][i];
        }
      }
      // Check diagonals
      if(grid[0][0] != CellType.E && grid[0][0] == grid[1][1] && grid[1][1] == grid[2][2]) {
        return grid[0][0];
      }
      if(grid[0][2] != CellType.E && grid[0][2] == grid[1][1] && grid[1][1] == grid[2][0]) {
        return grid[0][2];
      }
      return CellType.E; // No winner
    }

    public void display() {
      for(int i=0; i<3; i++) {
        for(int j=0; j<3; j++) {
          System.out.print(grid[i][j] + " ");
        }
        System.out.println();
      }
    }
  }

  public class Game {
    private Player[] players;
    private Board board;
    private int currentPlayer;
  
    public Game(Player player1, Player player2) {
      players = new Player[]{player1, player2};
      board = new Board();
      currentPlayer = 0;
    }

    
    public void startGame() {
      Scanner scanner = new Scanner(System.in);
      while (true) {

        if(board.getWinner() != CellType.E) {
            System.out.println("Game Over! " + board.getWinner() + " wins!");
            break;
        }
        
        if (board.isFull()) {
          System.out.println("Game Over! It's a draw.");
          break;
        }

        board.display();
        System.out.print(players[currentPlayer] + ", enter your move (row and column): ");
        int row = scanner.nextInt();
        int col = scanner.nextInt();
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
