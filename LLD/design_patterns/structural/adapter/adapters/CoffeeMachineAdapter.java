package structural.adapter.adapters;

import structural.adapter.devices.CoffeeMachine;

public class CoffeeMachineAdapter implements SmartDevice {
  // Assume CoffeeMachine is a third-party class we cannot modify
  private CoffeeMachine coffeeMachine;

  public CoffeeMachineAdapter(CoffeeMachine coffeeMachine) {
    this.coffeeMachine = coffeeMachine;
  }

  @Override
  public void turnOn() {
    coffeeMachine.connectViaUSB();
    coffeeMachine.startBrewing();
  }

  @Override
  public void turnOff() {
    coffeeMachine.stopBrewing();
    coffeeMachine.disconnectUSB();
  }
  
}
