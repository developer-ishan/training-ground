package creational.abstract_factory.factories;

import creational.abstract_factory.components.button.Button;
import creational.abstract_factory.components.checkbox.Checkbox;

public interface ComponentFactory {
    public Button createButton(String id);
    public Checkbox createCheckbox(String id);
}
