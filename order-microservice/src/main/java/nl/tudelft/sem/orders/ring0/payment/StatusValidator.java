package nl.tudelft.sem.orders.ring0.payment;

import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.ports.output.OrderDatabase;
import nl.tudelft.sem.orders.result.VerificationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StatusValidator extends BaseValidator {
    private final transient OrderDatabase orderDatabase;

    @Autowired
    public StatusValidator(OrderDatabase orderDatabase) {
        this.orderDatabase = orderDatabase;
    }

    @Override
    public void verify(Payment payment) throws VerificationException {
        var order = orderDatabase.getById(payment.getOrderId());

        if (order == null) {
            throw new VerificationException("This order does not exist");
        }

        if (!order.getStatus().equals(Order.StatusEnum.UNPAID)) {
            throw new VerificationException(
                "This order has already been paid for");
        }

        super.checkNext(payment);
    }
}
