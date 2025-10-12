package structural.adapter.devices;

public class CoffeeMachine {
  public void connectViaUSB() {
    System.out.println("CoffeeMachine connected via USB");
  }

  public void startBrewing() {
    System.out.println("CoffeeMachine is brewing coffee");
  }

  public void stopBrewing() {
    System.out.println("CoffeeMachine stopped brewing");
  }

  public void disconnectUSB() {
    System.out.println("CoffeeMachine disconnected from USB");
  }
}
