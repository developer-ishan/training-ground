package structural.bridge;

public class DrawingApp {
  public static void main(String[] args) {
    Shape circle = new Circle(5);
    Shape rectangle = new Rectangle(10, 20);

    Renderer vectorRenderer = new VectorRenderer();
    Renderer rasterRenderer = new RasterRenderer();

    circle.draw(vectorRenderer);
    rectangle.draw(rasterRenderer);
  } 
}
