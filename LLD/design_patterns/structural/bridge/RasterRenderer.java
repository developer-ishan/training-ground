package structural.bridge;

public class RasterRenderer implements Renderer{

  @Override
  public void render(Circle circle) {
    System.out.println("RasterRenderer: Drawing Circle of radius " + circle.getRadius());
  }

  @Override
  public void render(Rectangle rectangle) {
    System.out.println("RasterRenderer: Drawing Rectangle of width " + rectangle.getWidth() + " and height " + rectangle.getHeight());
  }
  
}
