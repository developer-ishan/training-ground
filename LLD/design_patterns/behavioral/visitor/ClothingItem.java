package behavioral.visitor;

public class ClothingItem implements Item {
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
