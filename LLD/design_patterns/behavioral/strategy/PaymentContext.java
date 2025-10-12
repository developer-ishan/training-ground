package behavioral.strategy;

public class PaymentContext{
    private final PaymentStrategy paymentStrategy;
    public PaymentContext(PaymentStrategy paymentStrategy){
        this.paymentStrategy = paymentStrategy;
    }

    public boolean initiatePayment(Long amount){
        paymentStrategy.processPayment(amount);
        return true;
    }
}
