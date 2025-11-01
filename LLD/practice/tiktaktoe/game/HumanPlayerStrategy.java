package tiktaktoe.game;

import java.util.Scanner;

import tiktaktoe.game.entities.Move;
import tiktaktoe.game.entities.Player;

public class HumanPlayerStrategy implements PlayerStrategy {
  @Override
  public Move getMove(Player player) {
    Scanner scanner = new Scanner(System.in);
    System.out.println(player + ": Enter your move (row and column): ");
    int row = scanner.nextInt();
    int col = scanner.nextInt();
    return new Move(player.getCellType(), row, col);
  }
  
}
