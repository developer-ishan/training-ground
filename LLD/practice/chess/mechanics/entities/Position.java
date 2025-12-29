package mechanics.entities;

public class Position {
  private int rank;
  private char file;
  public Position(char file, int rank){
    this.file = file;
    this.rank = rank;
  }
  public int getRank(){
    return this.rank;
  }
  public char getFile(){
    return this.file;
  }
  @Override
  public String toString() {
    return "" + file + rank;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null || getClass() != obj.getClass())
      return false;
    Position other = (Position) obj;
    return rank == other.rank && file == other.file;
  }
  @Override
  public int hashCode() {
    int result = Character.hashCode(file);
    result = 31 * result + Integer.hashCode(rank);
    return result;
  }
}
