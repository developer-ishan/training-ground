package behavioral.strategy;

public class CreditCard implements PaymentStrategy{

    @Override
    public void processPayment(Long amount) {
        System.out.println("Credit Card Payment of amount " + amount);
    }
}

