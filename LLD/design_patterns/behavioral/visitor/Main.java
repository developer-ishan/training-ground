package behavioral.visitor;

public class Main {
    public static void main(String[] args) {
        TaxCalculatorVisitor taxCalculatorVisitor = new TaxCalculatorVisitor();

        GroceryItem groceryItem = new GroceryItem();

        groceryItem.accept(taxCalculatorVisitor);
    }
}
