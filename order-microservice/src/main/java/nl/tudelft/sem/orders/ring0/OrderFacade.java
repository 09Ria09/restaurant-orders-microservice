package nl.tudelft.sem.orders.ring0;

import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.model.OrderDishesInner;
import nl.tudelft.sem.orders.model.OrderOrderIDDishesPutRequestDishesInner;
import nl.tudelft.sem.orders.ports.input.OrderLogic;
import nl.tudelft.sem.orders.ports.output.DishDatabase;
import nl.tudelft.sem.orders.ports.output.OrderDatabase;
import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class OrderFacade implements OrderLogic {
    private final transient OrderDatabase orderDatabase;
    private final transient DishDatabase dishDatabase;
    private final UserMicroservice userMicroservice;

    @Autowired
    public OrderFacade(OrderDatabase orderDatabase, DishDatabase dishDatabase,
                       UserMicroservice userMicroservice) {
        this.orderDatabase = orderDatabase;
        this.dishDatabase = dishDatabase;
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
        Order order = new Order(0L, customerId, vendorId, new ArrayList<>(),
            userMicroservice.getCustomerAddress(customerId), Order.StatusEnum.UNPAID);
        orderDatabase.save(order);
        return order;
    }

    /**
     * Updates dishes.
     *
     * @param orderId the id of the order
     * @param customerId the id of the customer
     * @param dishes  the list of dishes
     */
    public Float updateDishes(long orderId, long customerId,
                              @Valid List<@Valid OrderOrderIDDishesPutRequestDishesInner> dishes) {
        Order order = orderDatabase.getById(orderId);
        // Check if the order exists and the user owns the order and if the order is unpaid.
        if(order==null || order.getCustomerID() != customerId || order.getStatus() != Order.StatusEnum.UNPAID)
            throw new EntityNotFoundException();

        // Convert the list of IDs and amounts to a list of Dishes and amounts.
        OrderDishesInner[] convertedDishes;
        try {
            convertedDishes = (OrderDishesInner[]) dishes.stream()
                .map((dish) -> new OrderDishesInner(dishDatabase.getById(dish.getId()), dish.getQuantity()))
                .toArray();
        } catch (EntityNotFoundException e) {
            // Dish list is invalid.
            throw new IllegalStateException();
        }

        // Check if the dishes belong to the vendor and calculate the price.
        float totalPrice=0.0f;
        for (OrderDishesInner dish : convertedDishes) {
            if (!Objects.equals(dish.getDish().getVendorID(), order.getVendorID()))
                throw new IllegalStateException();
            totalPrice+=dish.getDish().getPrice()*dish.getAmount();
        }

        // Update the dishes.
        order.setDishes(List.of(convertedDishes));

        // Return the price.
        return totalPrice;
    }
}
