package structural.adapter.devices;

public class AirConditioner {
  public void connectViaBluetooth() {
    System.out.println("AirConditioner connected via Bluetooth");
  }

  public void startCooling() {
    System.out.println("AirConditioner is cooling");
  }

  public void stopCooling() {
    System.out.println("AirConditioner stopped cooling");
  }

  public void disconnectBluetooth() {
    System.out.println("AirConditioner disconnected from bluetooth");
  }
}
