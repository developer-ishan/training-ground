package creational.abstract_factory.factories;

import creational.abstract_factory.components.button.Button;
import creational.abstract_factory.components.button.MacButton;
import creational.abstract_factory.components.checkbox.Checkbox;
import creational.abstract_factory.components.checkbox.MacCheckbox;

public class MacFactory implements ComponentFactory {

    @Override
    public Button createButton(String id) {
        return new MacButton(id);
    }

    @Override
    public Checkbox createCheckbox(String id) {
        return new MacCheckbox(id);
    }
}
