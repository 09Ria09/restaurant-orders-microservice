package nl.tudelft.sem.orders.ports.input;

import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.model.OrderDishesInner;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;

public interface OrderLogic {
    void payForOrder(long userId, long orderId, String paymentRef);
    Order createOrder(long customerId, long vendorId);
}