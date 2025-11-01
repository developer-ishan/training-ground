package tiktaktoe.game;

import java.util.List;

import tiktaktoe.game.entities.Board;
import tiktaktoe.game.entities.Player;
import tiktaktoe.game.states.DrawState;
import tiktaktoe.game.states.GameState;
import tiktaktoe.game.states.PlayState;
import tiktaktoe.game.states.WinState;

public class GameContext {
  private Board board;
  private List<Player> players;
  private int currentPlayer;
  private GameState gameState;

  public GameContext(Board board, List<Player> players) {
    this.board = board;
    this.players = players;
    this.currentPlayer = 0;
    this.gameState = new PlayState();
  }

  // State pattern methods
  public void continueGame() {
    gameState.continueGame(this);
  }

  // State pattern helper methods
  public boolean isGameOver() {
    return gameState instanceof DrawState || gameState instanceof WinState;
  }

  public void showWinner() {
    if (gameState instanceof WinState) {
      System.out.println("Congratulations " + players.get(currentPlayer) + "! You have won the game.");
    } else if (gameState instanceof DrawState) {
      System.out.println("The game is a draw!");
    } else
      System.out.println("Game is still ongoing.");
  }



  public Player getCurrentPlayer() {
    return players.get(currentPlayer);
  }

  public void switchTurn() {
    currentPlayer = (currentPlayer + 1) % players.size();
  }

  public void setGameState(GameState gameState) {
    this.gameState = gameState;
  }

  public Board getBoard() {
    return board;
  }

  public List<Player> getPlayers() {
    return players;
  }
}
