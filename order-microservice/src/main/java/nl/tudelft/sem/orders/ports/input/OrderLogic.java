package nl.tudelft.sem.orders.ports.input;

import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.model.OrderDishesInner;
import nl.tudelft.sem.orders.model.OrderOrderIDDishesPutRequestDishesInner;
import org.springframework.http.HttpStatus;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

public interface OrderLogic {
    void payForOrder(long userId, long orderId, String paymentRef);
    Order createOrder(long customerId, long vendorId);
    Float updateDishes(long orderId, long customerId, @Valid List<@Valid OrderOrderIDDishesPutRequestDishesInner> dishes);
}