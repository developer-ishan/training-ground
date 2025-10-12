package structural.bridge;

public class Rectangle implements Shape {
  private int width, height;

  public Rectangle(int width, int height) {
    this.width = width;
    this.height = height;
  }

  @Override
  public void draw(Renderer renderer) {
    renderer.render(this);
  }

  public int getHeight() {
    return height;
  }

  public int getWidth() {
    return width;
  }
  
}
