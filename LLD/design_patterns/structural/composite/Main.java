package structural.composite;

import structural.composite.devices.Bulb;

public class Main {
  public static void main(String[] args) {
    CompositeController homeController = new CompositeController();
    Bulb livingRoomBulb = new Bulb();

    homeController.addDevice(livingRoomBulb);

    homeController.turnOn();
    homeController.turnOff();
  }
}
