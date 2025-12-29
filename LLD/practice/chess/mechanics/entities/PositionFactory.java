package mechanics.entities;

import java.util.HashMap;
import java.util.Map;

public class PositionFactory {
  private Map<String, Position> positions = new HashMap<>();
  private static PositionFactory instance = null;
  private PositionFactory(){};
  public Position get(char file, int rank){
    String key = "" + file + rank;
    return positions.computeIfAbsent(key, k->new Position(file, rank));
  }

  public Position get(String pos){
    String key = pos;
    char file = pos.charAt(0);
    int rank = Character.getNumericValue(pos.charAt(1));
    return positions.computeIfAbsent(key, k->new Position(file, rank));
  }

  public static PositionFactory getInstance(){
    if(instance == null)
      return instance = new PositionFactory();
    return instance;
  }
}
