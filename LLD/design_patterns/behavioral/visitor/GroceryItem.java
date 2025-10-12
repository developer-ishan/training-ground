package behavioral.visitor;

public class GroceryItem implements Item {
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
