package nl.tudelft.sem.orders.ports.input;

import java.util.List;
import javax.validation.Valid;
import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.model.OrderOrderIDDishesPutRequestDishesInner;

public interface OrderLogic {
    void payForOrder(long userId, long orderId, String paymentRef);

    Order createOrder(long customerId, long vendorId);

    Float updateDishes(long orderId, long customerId,
                       @Valid List<@Valid
                           OrderOrderIDDishesPutRequestDishesInner> dishes);
}