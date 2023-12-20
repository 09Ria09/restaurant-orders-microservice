package nl.tudelft.sem.orders.test.mocks;

import nl.tudelft.sem.orders.ports.output.PaymentService;

public class MockPaymentService implements PaymentService {
    @Override
    public boolean verifyPaymentConfirmation(String token) {
        if (token == "fail") {
            return false;
        }

        return true;
    }
}
