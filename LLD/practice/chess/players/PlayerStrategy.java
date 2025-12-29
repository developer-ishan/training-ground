package players;

import mechanics.entities.Board;
import mechanics.entities.Move;

public interface PlayerStrategy {
  Move getMove(Player player, Board board);
}
