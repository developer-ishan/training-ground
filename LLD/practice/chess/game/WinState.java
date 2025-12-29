package game;

public class WinState implements GameState {
  @Override
  public void continueGame (GameContext context) {
    context.getBoard().display();
    System.out.println("Player " + context.getCurrentPlayer() + " wins!");
  }
  
}
