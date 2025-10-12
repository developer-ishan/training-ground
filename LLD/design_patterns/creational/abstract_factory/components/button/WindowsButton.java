package creational.abstract_factory.components.button;

public class WindowsButton extends Button {

    public WindowsButton(String id) {
        super(id);
    }

    @Override
    public void click() {
        System.out.println("Windows button clicked");
    }
}
