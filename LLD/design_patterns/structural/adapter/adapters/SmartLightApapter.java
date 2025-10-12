package structural.adapter.adapters;

import structural.adapter.devices.SmartLight;

public class SmartLightApapter implements SmartDevice {
  private SmartLight smartLight;

  public SmartLightApapter(SmartLight smartLight) {
    this.smartLight = smartLight;
  }

  @Override
  public void turnOn() {
    smartLight.connectViaWiFi();
    smartLight.turnOnLight();
  }

  @Override
  public void turnOff() {
    smartLight.turnOffLight();
    smartLight.disconnectWiFi();
  }
  
}
