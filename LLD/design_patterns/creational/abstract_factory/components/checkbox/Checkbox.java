package creational.abstract_factory.components.checkbox;

import creational.abstract_factory.components.Clickable;
import creational.abstract_factory.components.Component;

public abstract class Checkbox extends Component implements Clickable {
    boolean value;
    public Checkbox(String id) {
        super(id);
        this.value = false;
    }
}
