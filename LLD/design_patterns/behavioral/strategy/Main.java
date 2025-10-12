package behavioral.strategy;

public class Main{
    public static void main(String[] args) {
        PaymentStrategy creditCard = new CreditCard();
        PaymentStrategy payPal = new PayPal();
        PaymentContext paymentContext = new PaymentContext(creditCard);
        paymentContext.initiatePayment(100L);
    }
}
