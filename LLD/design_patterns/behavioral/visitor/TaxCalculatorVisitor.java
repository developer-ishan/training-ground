package behavioral.visitor;

public class TaxCalculatorVisitor implements Visitor {
    @Override
    public void visit(ElectronicsItem item) {
        System.out.println("TaxCalculatorVisitor: visiting ElectronicsItem");
    }

    @Override
    public void visit(GroceryItem item) {
        System.out.println("TaxCalculatorVisitor: visiting GroceryItem");
    }

    @Override
    public void visit(ClothingItem item) {
        System.out.println("TaxCalculatorVisitor: visiting ClothingItem");
    }
}
