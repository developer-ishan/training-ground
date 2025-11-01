package tiktaktoe.game.states;

import tiktaktoe.game.GameContext;
import tiktaktoe.game.entities.Move;
import tiktaktoe.game.entities.Player;

public class PlayState implements GameState {
  @Override
  public void continueGame(GameContext context) {
    context.getBoard().display();
    Player currentPlayer = context.getCurrentPlayer();

    while (true) {
      try {
        Move move = currentPlayer.getMove();
        context.getBoard().makeMove(move);
        break; // Exit loop if move is successful
      } catch (IllegalArgumentException e) {
        System.out.println("Please try again.");
      }
    }

    if(context.getBoard().getWinner() != tiktaktoe.game.Cell.E){
      System.out.println("Winner is: " + currentPlayer);
      context.setGameState(new WinState());
      return;
    }

    if (context.getBoard().isFull()) {
      System.out.println("The game is a draw!");
      context.setGameState(new DrawState());
      return;
    }

    context.switchTurn();
  }
  
}
