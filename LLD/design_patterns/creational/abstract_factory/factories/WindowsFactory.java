package creational.abstract_factory.factories;

import creational.abstract_factory.components.button.Button;
import creational.abstract_factory.components.button.WindowsButton;
import creational.abstract_factory.components.checkbox.Checkbox;
import creational.abstract_factory.components.checkbox.WindowsCheckbox;

public class WindowsFactory implements ComponentFactory{

    @Override
    public Button createButton(String id) {
        return new WindowsButton(id);
    }

    @Override
    public Checkbox createCheckbox(String id) {
        return new WindowsCheckbox(id);
    }
}
