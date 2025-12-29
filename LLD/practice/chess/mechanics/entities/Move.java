package mechanics.entities;

import mechanics.pieces.IPiece;

public class Move {
  private IPiece piece;
  private Position from;
  private Position to;

  public Move(IPiece piece, Position from, Position to) {
    this.piece = piece;
    this.from = from;
    this.to = to;
  }

  public IPiece getPiece() {
    return piece;
  }

  public Position getFrom() {
    return from;
  }

  public Position getTo() {
    return to;
  }

  @Override
  public String toString() {
    return piece + ": " + from + " → " + to;
  }
}
