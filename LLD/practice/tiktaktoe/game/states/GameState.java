package tiktaktoe.game.states;

import tiktaktoe.game.GameContext;
import tiktaktoe.game.entities.Move;

public interface GameState {
  void continueGame(GameContext context);
}
