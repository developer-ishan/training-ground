package game;

public class DrawState implements GameState {
  @Override
  public void continueGame(GameContext context) {
    context.getBoard().display();
    System.out.println("The game is a draw!");
  }
  
}
