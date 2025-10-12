package creational.abstract_factory.components.checkbox;

public class WindowsCheckbox extends Checkbox {
    boolean value;
    public WindowsCheckbox(String id) {
        super(id);
        this.value = false;
    }

    @Override
    public void click() {
        value = !value;
        System.out.println("Windows Checkbox clicked: " + value);
    }
}
