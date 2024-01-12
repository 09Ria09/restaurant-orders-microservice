package nl.tudelft.sem.orders.ports.input;

import java.util.List;
import javax.validation.Valid;
import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.model.OrderOrderIDDishesPutRequestDishesInner;
import nl.tudelft.sem.orders.result.ForbiddenException;
import nl.tudelft.sem.orders.result.MalformedException;
import nl.tudelft.sem.orders.result.NotFoundException;
import nl.tudelft.sem.users.ApiException;

public interface OrderFacadeInterface {
    void payForOrder(long userId, long orderId, String paymentRef)
        throws MalformedException, ForbiddenException;

    Order createOrder(long customerId, long vendorId) throws MalformedException, ApiException;

    Float updateDishes(long orderId, long customerId,
                       @Valid List<@Valid
                           OrderOrderIDDishesPutRequestDishesInner> dishes) throws MalformedException;

    List<Order> getOrders(Long userID) throws ApiException;

    Order reorder(Long userID, Long orderID) throws MalformedException, NotFoundException;

    void rateOrder(Long userID, Long orderID, Integer rating)
            throws MalformedException, ForbiddenException, ApiException;

    Order changeOrder(Long userID, Order order) throws MalformedException, ApiException, ForbiddenException;

    List<Order> getOrder(Long orderID) throws MalformedException;
}