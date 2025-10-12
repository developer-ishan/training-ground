package behavioral.memento.originator;

import behavioral.memento.Memento;

public class TextEditor {
  private String text;

  public void setText(String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }


  public Memento save() {
    return new Memento(text);
  }

  public void restore(Memento memento) {
    this.text = memento.getText();
  }

  public void print() {
    System.out.println("Current Text: " + text);
  }
}
