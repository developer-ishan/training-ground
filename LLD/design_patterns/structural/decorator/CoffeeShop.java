package structural.decorator;

public class CoffeeShop {
    public static void main(String[] args) {
        Coffee coffee = new Espresso();
        coffee = new MilkDecorator(coffee);
        coffee = new SugarDecorator(coffee);
        System.out.println("Order: " + coffee.getDescription());
        System.out.println("Total Cost: $" + coffee.getCost());
        Coffee anotherCoffee = new Cappuccino();
        anotherCoffee = new VanillaDecorator(anotherCoffee);
        System.out.println("nOrder: " + anotherCoffee.getDescription());
        System.out.println("Total Cost: $" + anotherCoffee.getCost());
    }
}
