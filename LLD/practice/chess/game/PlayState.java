package game;

import mechanics.entities.Move;
import players.Player;

public class PlayState implements GameState {
  @Override
  public void continueGame(GameContext context) {
    Player currentPlayer = context.getCurrentPlayer();
    Move move = currentPlayer.getMove(context.getBoard());
    context.getBoard().makeMove(move);

    if(context.getBoard().checkWin()){
      context.setGameState(new WinState());
    } else if(context.getBoard().checkDraw()){
      context.setGameState(new DrawState());
    } else{
      context.switchTurn();
    }
  }
  
}
