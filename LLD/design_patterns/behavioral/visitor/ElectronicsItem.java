package behavioral.visitor;

public class ElectronicsItem implements Item {

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
