package nl.tudelft.sem.orders.ports.input;

import org.springframework.http.HttpStatus;

public interface OrderLogic {
    void payForOrder(long userId, long orderId, String paymentRef);
}