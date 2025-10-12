package structural.composite.devices;

public class Bulb implements Component {
  @Override
  public void turnOn() {
    System.out.println("Bulb is turned ON");
  }

  @Override
  public void turnOff() {
    System.out.println("Bulb is turned OFF");
  }
  
}
