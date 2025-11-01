package tiktaktoe.game;

import tiktaktoe.game.entities.Move;
import tiktaktoe.game.entities.Player;

public interface PlayerStrategy {
  Move getMove(Player player);
}
