package creational.abstract_factory.components.button;

import creational.abstract_factory.components.Clickable;
import creational.abstract_factory.components.Component;

public abstract class Button extends Component implements Clickable {
    public Button(String id) {
        super(id);
    }
}
