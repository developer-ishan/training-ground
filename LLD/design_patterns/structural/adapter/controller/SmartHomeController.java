package structural.adapter.controller;

import structural.adapter.adapters.AirConditionerAdapter;
import structural.adapter.devices.AirConditioner;
import structural.adapter.adapters.SmartDevice;

public class SmartHomeController {
  public static void main(String[] args) {
    SmartDevice airConditioner = new AirConditionerAdapter(new AirConditioner());
    airConditioner.turnOn();
  }
}
