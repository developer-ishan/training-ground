package behavioral.visitor;

public interface Visitor {
    public void visit(ElectronicsItem item);
    public void visit(GroceryItem item);
    public void visit(ClothingItem   item);
}
