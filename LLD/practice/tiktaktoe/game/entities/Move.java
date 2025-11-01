package tiktaktoe.game.entities;

import tiktaktoe.game.Cell;

public class Move {
  private Cell cell;
  private int row;
  private int col;
  public Move(Cell cell, int row, int col) {
    this.cell = cell;
    this.row = row;
    this.col = col;
  }
  public Cell getCell() {
    return cell;
  }
  public int getRow() {
    return row;
  }
  public int getCol() {
    return col;
  }
}
