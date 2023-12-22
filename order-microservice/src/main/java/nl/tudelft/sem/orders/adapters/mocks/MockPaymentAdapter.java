package nl.tudelft.sem.orders.adapters.mocks;

import nl.tudelft.sem.orders.ports.output.PaymentService;

public class MockPaymentAdapter implements PaymentService {
    @Override
    public boolean verifyPaymentConfirmation(String token) {
        return true;
    }
}
