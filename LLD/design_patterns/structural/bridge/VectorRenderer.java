package structural.bridge;

public class VectorRenderer implements Renderer {
  @Override
  public void render(Circle circle) {
    System.out.println("VectorRenderer: Drawing Circle of radius " + circle.getRadius());
  }

  @Override
  public void render(Rectangle rectangle) {
    System.out.println("VectorRenderer: Drawing Rectangle of width " + rectangle.getWidth() + " and height " + rectangle.getHeight());
  }

}
