package nl.tudelft.sem.orders.ring0;

import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.ports.input.OrderLogic;
import nl.tudelft.sem.orders.ports.output.OrderDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class OrderFacade implements OrderLogic {
    private transient OrderDatabase orderDatabase;

    @Autowired
    public OrderFacade(OrderDatabase orderDatabase) {
        this.orderDatabase = orderDatabase;
    }

    @Override
    public void payForOrder(long userId, long orderId, String paymentRef) {
        Order order = orderDatabase.getById(orderId);

        order.setStatus(Order.StatusEnum.PENDING);

        orderDatabase.save(order);
    }
}
