package structural.composite.devices;

public class Fan implements Component {
  @Override
  public void turnOn() {
    System.out.println("Fan is turned ON");
  }

  @Override
  public void turnOff() {
    System.out.println("Fan is turned OFF");
  }
  
}
