package mechanics.entities;

import java.util.HashMap;
import java.util.Map;

import mechanics.pieces.IPiece;
import mechanics.pieces.King;
import mechanics.pieces.PieceFactory;

public class Board {
  private Map<Position, IPiece> grid;
  private PositionFactory positionFactory = PositionFactory.getInstance();

  public Board() {
    grid = new HashMap<>();

    // Initialize all squares to null
    for (char file = 'a'; file <= 'h'; file++) {
      for (int rank = 1; rank <= 8; rank++) {
        grid.put(positionFactory.get(file, rank), null);
      }
    }

    // Place white pawns
    for (char file = 'a'; file <= 'h'; file++) {
      grid.put(positionFactory.get(file, 2), PieceFactory.createPiece("PAWN", Color.WHITE));
    }

    // Place other white pieces
    grid.put(positionFactory.get('a', 1), PieceFactory.createPiece("ROOK", Color.WHITE));
    grid.put(positionFactory.get('b', 1), PieceFactory.createPiece("KNIGHT", Color.WHITE));
    grid.put(positionFactory.get('c', 1), PieceFactory.createPiece("BISHOP", Color.WHITE));
    grid.put(positionFactory.get('d', 1), PieceFactory.createPiece("QUEEN", Color.WHITE));
    grid.put(positionFactory.get('e', 1), PieceFactory.createPiece("KING", Color.WHITE));
    grid.put(positionFactory.get('f', 1), PieceFactory.createPiece("BISHOP", Color.WHITE));
    grid.put(positionFactory.get('g', 1), PieceFactory.createPiece("KNIGHT", Color.WHITE));
    grid.put(positionFactory.get('h', 1), PieceFactory.createPiece("ROOK", Color.WHITE));

    // Place black pawns
    for (char file = 'a'; file <= 'h'; file++) {
      grid.put(positionFactory.get(file, 7), PieceFactory.createPiece("PAWN", Color.BLACK));
    }

    // Place other black pieces
    grid.put(positionFactory.get('a', 8), PieceFactory.createPiece("ROOK", Color.BLACK));
    grid.put(positionFactory.get('b', 8), PieceFactory.createPiece("KNIGHT", Color.BLACK));
    grid.put(positionFactory.get('c', 8), PieceFactory.createPiece("BISHOP", Color.BLACK));
    grid.put(positionFactory.get('d', 8), PieceFactory.createPiece("QUEEN", Color.BLACK));
    grid.put(positionFactory.get('e', 8), PieceFactory.createPiece("KING", Color.BLACK));
    grid.put(positionFactory.get('f', 8), PieceFactory.createPiece("BISHOP", Color.BLACK));
    grid.put(positionFactory.get('g', 8), PieceFactory.createPiece("KNIGHT", Color.BLACK));
    grid.put(positionFactory.get('h', 8), PieceFactory.createPiece("ROOK", Color.BLACK));
  }

  public void display() {
    // 1️⃣ Print the file letters at the top
    System.out.print("  "); // padding for rank numbers on left
    for (char file = 'a'; file <= 'h'; file++) {
        System.out.print(file + " ");
    }
    System.out.println();

    // 2️⃣ Loop through ranks from 8 down to 1 (chess boards display rank 8 at top)
    for (int rank = 8; rank >= 1; rank--) {
        // 3️⃣ Print rank number at the start of the row
        System.out.print(rank + " ");

        for (char file = 'a'; file <= 'h'; file++) {
            Position pos = positionFactory.get(file, rank);
            IPiece piece = grid.get(pos);

            // 4️⃣ Print piece symbol or "." if square is empty
            System.out.print((piece == null ? ". " : piece + " "));
        }

        // 5️⃣ Print rank number at the end of the row
        System.out.println(rank);
    }

    // 6️⃣ Print the file letters at the bottom
    System.out.print("  "); // padding for rank numbers on left
    for (char file = 'a'; file <= 'h'; file++) {
        System.out.print(file + " ");
    }
    System.out.println();
}


  public IPiece getPieceAt(Position position) {
    return grid.get(position);
  }

  public IPiece getPieceAt(char file, int rank){
    return grid.get(positionFactory.get(file, rank));
  }

  public IPiece getPieceAt(String position){
    return grid.get(positionFactory.get(position));
  }

  public Position getPosition(IPiece piece) {
    for (Map.Entry<Position, IPiece> entry : grid.entrySet()) {
      if (entry.getValue() == piece) {
        return entry.getKey();
      }
    }
    return null;
  }

  //move the pieces around or capture
public void makeMove(Move move) {
    Position from = move.getFrom();
    Position to = move.getTo();
    IPiece piece = move.getPiece();

    // 1️⃣ Remove the piece from the original square
    grid.put(from, null);

    // 2️⃣ Place the piece on the destination square (capture happens automatically)
    grid.put(to, piece);
}

//check if you have captured opponent's king
public boolean checkWin() {
    boolean whiteKingPresent = false;
    boolean blackKingPresent = false;

    for (IPiece piece : grid.values()) {
        if (piece != null) {
            if (piece instanceof King) {
                if (piece.getColor() == Color.WHITE) whiteKingPresent = true;
                if (piece.getColor() == Color.BLACK) blackKingPresent = true;
            }
        }
    }

    // if either king is missing, opponent wins
    return !whiteKingPresent || !blackKingPresent;
}

//check if there are only 2 pieces left both being the kings
public boolean checkDraw() {
    int pieceCount = 0;

    for (IPiece piece : grid.values()) {
        if (piece != null) pieceCount++;
    }

    if (pieceCount != 2) return false;

    // check if the remaining 2 pieces are both kings
    boolean whiteKing = false;
    boolean blackKing = false;
    for (IPiece piece : grid.values()) {
        if (piece instanceof King) {
            if (piece.getColor() == Color.WHITE) whiteKing = true;
            if (piece.getColor() == Color.BLACK) blackKing = true;
        }
    }

    return whiteKing && blackKing;
}

}
