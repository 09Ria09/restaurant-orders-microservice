package nl.tudelft.sem.orders.ring0;

import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.model.OrderDishesInner;
import nl.tudelft.sem.orders.ports.input.OrderLogic;
import nl.tudelft.sem.orders.ports.output.OrderDatabase;
import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class OrderFacade implements OrderLogic {
    private transient OrderDatabase orderDatabase;
    private final UserMicroservice userMicroservice;

    @Autowired
    public OrderFacade(OrderDatabase orderDatabase, UserMicroservice userMicroservice) {
        this.orderDatabase = orderDatabase;
        this.userMicroservice = userMicroservice;
    }

    @Override
    public void payForOrder(long userId, long orderId, String paymentRef) {
        Order order = orderDatabase.getById(orderId);

        order.setStatus(Order.StatusEnum.PENDING);

        orderDatabase.save(order);
    }

    /**
     * Creates a new order.
     *
     * @param customerId the id of the customer
     * @param vendorId   the id of the vendor
     */
    @Override
    public Order createOrder(long customerId, long vendorId) {
        return new Order(1L, customerId, vendorId, new ArrayList<>(),
            userMicroservice.getHomeAddress(customerId), Order.StatusEnum.UNPAID);
    }
}
