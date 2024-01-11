package nl.tudelft.sem.orders.ports.input;

import java.util.List;
import javax.validation.Valid;
import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.model.OrderOrderIDDishesPutRequestDishesInner;
import nl.tudelft.sem.orders.result.ForbiddenException;
import nl.tudelft.sem.orders.result.MalformedException;
import nl.tudelft.sem.orders.result.NotFoundException;
import nl.tudelft.sem.users.ApiException;
import nl.tudelft.sem.users.model.UsersGetUserTypeIdGet200Response;

public interface OrderLogicInterface {
    void payForOrder(long userId, long orderId, String paymentRef)
        throws MalformedException, ForbiddenException;

    Order createOrder(long customerId, long vendorId) throws MalformedException, ApiException, ForbiddenException;

    Float updateDishes(long orderId, long customerId,
                       @Valid List<@Valid
                           OrderOrderIDDishesPutRequestDishesInner> dishes) throws MalformedException, ApiException;

    List<Order> getOrders(Long userID) throws ApiException;

    Order reorder(Long userID, Long orderID) throws MalformedException, NotFoundException;
}