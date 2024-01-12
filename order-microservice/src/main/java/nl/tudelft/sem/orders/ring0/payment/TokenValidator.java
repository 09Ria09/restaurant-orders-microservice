package nl.tudelft.sem.orders.ring0.payment;

import nl.tudelft.sem.orders.ports.output.PaymentService;
import nl.tudelft.sem.orders.result.PaymentException;
import nl.tudelft.sem.orders.result.VerificationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TokenValidator extends BaseValidator {
    private final transient PaymentService paymentService;

    @Autowired
    public TokenValidator(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    public void verify(Payment payment) throws VerificationException {
        if (!paymentService.verifyPaymentConfirmation(payment.getToken())) {
            throw new PaymentException("The payment token is invalid");
        }

        super.checkNext(payment);
    }
}
