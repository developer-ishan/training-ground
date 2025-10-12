package creational.abstract_factory.components.checkbox;

public class MacCheckbox extends Checkbox {
    public MacCheckbox(String id) {
        super(id);
    }


    @Override
    public void click() {
        value = !value;
        System.out.println("Mac Checkbox clicked: " + value);
    }
}
