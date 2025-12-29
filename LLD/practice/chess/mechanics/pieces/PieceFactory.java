package mechanics.pieces;

import mechanics.entities.Color;

public class PieceFactory {
  public static IPiece createPiece(String type, Color color){
    switch(type.toUpperCase()){
      case "KING":
        return new King(color);
      case "QUEEN":
        return new Queen(color);
      case "BISHOP":
        return new Bishop(color);
      case "KNIGHT":
        return new Knight(color);
      case "ROOK":
        return new Rook(color);
      case "PAWN":
        return new Pawn(color);
      default:
        throw new IllegalArgumentException("Invalid piece type: " + type);
    }
  }
}
