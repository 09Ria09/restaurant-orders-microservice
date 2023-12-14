package nl.tudelft.sem.orders.ports.input;

import nl.tudelft.sem.orders.result.ForbiddenException;
import nl.tudelft.sem.orders.result.MalformedException;
import org.springframework.http.HttpStatus;

public interface OrderLogic {
    void payForOrder(long userId, long orderId, String paymentRef) throws MalformedException, ForbiddenException;
}