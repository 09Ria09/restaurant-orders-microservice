package nl.tudelft.sem.orders.ports.input;

import java.util.List;
import javax.validation.Valid;
import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.model.OrderOrderIDDishesPutRequestDishesInner;
import nl.tudelft.sem.orders.result.ForbiddenException;
import nl.tudelft.sem.orders.result.MalformedException;

public interface OrderLogic {
    void payForOrder(long userId, long orderId, String paymentRef)
        throws MalformedException, ForbiddenException;

    Order createOrder(long customerId, long vendorId) throws MalformedException;

    Float updateDishes(long orderId, long customerId,
                       @Valid List<@Valid
                           OrderOrderIDDishesPutRequestDishesInner> dishes) throws MalformedException;
}