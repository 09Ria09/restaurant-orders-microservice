package nl.tudelft.sem.orders.ports.output;

public interface PaymentService {
    boolean verifyPaymentConfirmation(String token);

    boolean finalizePayment(String token);
}
