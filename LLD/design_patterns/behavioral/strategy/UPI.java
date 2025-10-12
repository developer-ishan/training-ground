package behavioral.strategy;

public class UPI implements PaymentStrategy{

    @Override
    public void processPayment(Long amount) {
        System.out.println("UPI Payment of amount " + amount);
    }
}
