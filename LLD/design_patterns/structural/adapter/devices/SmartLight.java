package structural.adapter.devices;

public class SmartLight {
  public void connectViaWiFi() {
    System.out.println("SmartLight connected via WiFi");
  }

  public void turnOnLight() {
    System.out.println("SmartLight is ON");
  }

  public void turnOffLight() {
    System.out.println("SmartLight is OFF");
  }

  public void disconnectWiFi() {
    System.out.println("SmartLight disconnected from WiFi");
  }
}
