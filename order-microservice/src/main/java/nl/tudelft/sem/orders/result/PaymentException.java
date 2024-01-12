package nl.tudelft.sem.orders.result;

public class PaymentException extends VerificationException {
    public static final long serialVersionUID = 1;

    public PaymentException(String s) {
        super(s);
    }
}
