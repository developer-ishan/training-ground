package creational.abstract_factory;

import creational.abstract_factory.factories.ComponentFactory;
import creational.abstract_factory.factories.MacFactory;
import creational.abstract_factory.factories.WindowsFactory;

public class Main {
    public static void main(String[] args) {
        String os = System.getProperty("os.name").toLowerCase();
        ComponentFactory factory;

        if (os.contains("win")) {
            factory = new WindowsFactory();
        } else if (os.contains("mac")) {
            factory = new MacFactory();
        } else {
            throw new UnsupportedOperationException("Unsupported OS: " + os);
        }

        Application app = new Application(factory);
        app.renderUI();
    }
}
