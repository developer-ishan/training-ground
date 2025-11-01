package tiktaktoe.game.entities;

import tiktaktoe.game.Cell;

public class Board {
  private Cell[][] board;
  private final int N_ROWS;
  private final int N_COLS;

  public Board(int N_ROWS, int N_COLS) {
    this.N_ROWS = N_ROWS;
    this.N_COLS = N_COLS;
    board = new Cell[N_ROWS][N_COLS];
    for (int i = 0; i < N_ROWS; i++) {
      for (int j = 0; j < N_COLS; j++) {
        board[i][j] = Cell.E;
      }
    }
  }

  public Cell getWinner() {
    // check rows
    for (int i = 0; i < N_ROWS; i++) {
      if (board[i][0] != Cell.E && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
        return board[i][0];
      }
    }

    // check cols
    for (int j = 0; j < N_COLS; j++) {
      if (board[0][j] != Cell.E && board[0][j] == board[1][j] && board[1][j] == board[2][j]) {
        return board[0][j];
      }
    }

    // check diagonals
    if (board[0][0] != Cell.E && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
      return board[0][0];
    }
    if (board[0][2] != Cell.E && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
      return board[0][2];
    }

    return Cell.E; // No winner
  }

  public void makeMove(Move move) throws IllegalArgumentException {
    int row = move.getRow();
    int col = move.getCol();
    Cell cell = move.getCell();
    if (row < 0 || row >= N_ROWS || col < 0 || col >= N_COLS) {
      throw new IllegalArgumentException("Invalid move");
    }
    if (board[row][col] != Cell.E) {
      throw new IllegalArgumentException("Cell already occupied");
    }
    board[row][col] = cell;
  }

  public void display(){
    for (int i = 0; i < N_ROWS; i++) {
      for (int j = 0; j < N_COLS; j++) {
        System.out.print(board[i][j] + " ");
      }
      System.out.println();
    }
  }

  public boolean isFull() {
    for (int i = 0; i < N_ROWS; i++) {
      for (int j = 0; j < N_COLS; j++) {
        if (board[i][j] == Cell.E) {
          return false;
        }
      }
    }
    return true;
  }
  
}
