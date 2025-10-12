package creational.abstract_factory;

import creational.abstract_factory.components.button.Button;
import creational.abstract_factory.components.checkbox.Checkbox;
import creational.abstract_factory.factories.ComponentFactory;

public class Application {
    private final Button button;
    private final Checkbox checkbox;

    public Application(ComponentFactory factory) {
        this.button = factory.createButton("b1");
        this.checkbox = factory.createCheckbox("c1");
    }

    public void renderUI() {
        button.click();
        checkbox.click();
        checkbox.click();
    }
}

