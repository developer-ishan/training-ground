package behavioral.strategy;

public class PayPal implements PaymentStrategy{


    @Override
    public void processPayment(Long amount) {
        System.out.println("PayPal Payment of amount " + amount);
    }
}
