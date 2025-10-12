package structural.composite.devices;

public class AC implements Component {
  @Override
  public void turnOn() {
    System.out.println("AC is turned ON");
  }

  @Override
  public void turnOff() {
    System.out.println("AC is turned OFF");
  }
  
}
