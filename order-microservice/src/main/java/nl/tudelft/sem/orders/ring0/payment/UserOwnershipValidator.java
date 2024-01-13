package nl.tudelft.sem.orders.ring0.payment;

import nl.tudelft.sem.orders.ports.output.OrderDatabase;
import nl.tudelft.sem.orders.result.PaymentException;
import nl.tudelft.sem.orders.result.VerificationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserOwnershipValidator extends BaseValidator {
    private final transient OrderDatabase orderDatabase;

    @Autowired
    public UserOwnershipValidator(OrderDatabase orderDatabase) {
        this.orderDatabase = orderDatabase;
    }

    @Override
    public void verify(Payment payment) throws VerificationException {
        if (orderDatabase.getById(payment.getOrderId()).getCustomerID()
            != payment.getUserId()) {
            throw new PaymentException(
                "This customer is not the owner of this order");
        }

        super.checkNext(payment);
    }
}
