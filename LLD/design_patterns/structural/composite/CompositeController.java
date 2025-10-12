package structural.composite;

import java.util.ArrayList;
import java.util.List;

import structural.composite.devices.Component;

public class CompositeController implements Component {
  private List<Component> components;

  public CompositeController() {
    this.components = new ArrayList<>();
  }

  public void addDevice(Component device) {
    components.add(device);
  }

  public void removeDevice(Component device) {
    components.remove(device);
  }

  @Override
  public void turnOn() {
    for (Component device : components) {
      device.turnOn();
    }
  }

  @Override
  public void turnOff() {
    for (Component device : components) {
      device.turnOff();
    }
  }

  
}