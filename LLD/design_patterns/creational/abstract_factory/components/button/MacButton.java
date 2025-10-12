package creational.abstract_factory.components.button;

public class MacButton extends Button {

    public MacButton(String id) {
        super(id);
    }

    @Override
    public void click() {
        System.out.println("Mac button clicked");
    }
}
