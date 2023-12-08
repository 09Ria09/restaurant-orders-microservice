package nl.tudelft.sem.orders.ring0;

import nl.tudelft.sem.orders.model.Location;

public interface OrdersLogic {
    void payForOrder(long userId, long orderId, String paymentRef);
    void vendorsInRadius(long userId, Location location);
}
