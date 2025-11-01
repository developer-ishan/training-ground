package tiktaktoe.game.states;

public class DrawState implements GameState {
  @Override
  public void continueGame(tiktaktoe.game.GameContext context) {
    context.getBoard().display();
    System.out.println("The game is a draw!");
  }
  
}
