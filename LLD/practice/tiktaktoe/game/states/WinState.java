package tiktaktoe.game.states;

import tiktaktoe.game.GameContext;

public class WinState implements GameState {
  @Override
  public void continueGame (GameContext context) {
    context.getBoard().display();
    System.out.println("Player " + context.getCurrentPlayer() + " wins!");
  }
  
}
