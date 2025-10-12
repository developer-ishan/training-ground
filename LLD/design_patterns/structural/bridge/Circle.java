package structural.bridge;

public class Circle implements Shape {
  private float radius;

  public Circle(float radius) {
    this.radius = radius;
  }

  @Override
  public void draw(Renderer renderer) {
    renderer.render(this);
  }

  public float getRadius() {
    return radius;
  }
  
}
