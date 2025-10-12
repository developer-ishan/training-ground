package structural.decorator;

public class Cappuccino implements Coffee {
    @Override
    public String getDescription() {
        return "Cappuccino";
    }
    @Override
    public double getCost() {
        return 3.00;
    }
}
