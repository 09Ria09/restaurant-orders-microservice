package nl.tudelft.sem.orders.ring0;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.model.OrderDishesInner;
import nl.tudelft.sem.orders.model.OrderOrderIDDishesPutRequestDishesInner;
import nl.tudelft.sem.orders.ports.input.OrderLogic;
import nl.tudelft.sem.orders.ports.output.DishDatabase;
import nl.tudelft.sem.orders.ports.output.OrderDatabase;
import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.ports.input.OrderLogic;
import nl.tudelft.sem.orders.ports.output.OrderDatabase;
import nl.tudelft.sem.orders.ports.output.PaymentService;
import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import nl.tudelft.sem.orders.result.ForbiddenException;
import nl.tudelft.sem.orders.result.MalformedException;
import nl.tudelft.sem.users.model.UsersGetUserTypeIdGet200Response;
import nl.tudelft.sem.users.model.UsersGetUserTypeIdGet200Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderFacade implements OrderLogic {
    private final transient OrderDatabase orderDatabase;
    private final transient DishDatabase dishDatabase;
    private final transient UserMicroservice userMicroservice;
    private transient PaymentService paymentService;

    /**
     * Creates a new order facade.
     *
     * @param orderDatabase The database output port.
     * @param dishDatabase The dish database.
     * @param userMicroservice The output port for the user microservice.
     * @param paymentService The output port for the payment service.
     */
    @Autowired
    public OrderFacade(OrderDatabase orderDatabase, DishDatabase dishDatabase, UserMicroservice userMicroservice) {
        this.orderDatabase = orderDatabase;
        this.dishDatabase = dishDatabase;
        this.userMicroservice = userMicroservice;
        this.paymentService = paymentService;
    }

    @Override
    public void payForOrder(long userId, long orderId,
                            String paymentConfirmation)
        throws MalformedException, ForbiddenException {
        Order order = orderDatabase.getById(orderId);

        if (order == null || order.getStatus() != Order.StatusEnum.UNPAID) {
            throw new MalformedException();
        }

        UsersGetUserTypeIdGet200Response.UserTypeEnum userType;

        try {
            userType = userMicroservice.getUserType(userId);
        } catch (Exception e) {
            throw new MalformedException();
        }

        // userType should never be null now
        // therefore there is no need to check for that.

        if (order.getCustomerID() != userId
            || !paymentService.verifyPaymentConfirmation(paymentConfirmation)) {
            throw new ForbiddenException();
        }

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
        Order order = new Order(0L, customerId, vendorId, new ArrayList<>(), userMicroservice.getCustomerAddress(customerId),
            Order.StatusEnum.UNPAID);
        orderDatabase.save(order);
        return order;
    }

    /**
     * Updates dishes.
     *
     * @param orderId    the id of the order
     * @param customerId the id of the customer
     * @param dishes     the list of dishes
     */
    public Float updateDishes(long orderId, long customerId,
                              @Valid List<@Valid OrderOrderIDDishesPutRequestDishesInner> dishes) {
        Order order = orderDatabase.getById(orderId);
        // Check if the order exists and the user
        // owns the order and if the order is unpaid.
        if (order == null || order.getCustomerID() != customerId || order.getStatus() != Order.StatusEnum.UNPAID) {
            throw new EntityNotFoundException();
        }

        // Convert the list of IDs and amounts to a list of Dishes and amounts.
        try {
            OrderDishesInner[] convertedDishes =
                dishes.stream().map((dish) -> new OrderDishesInner(dishDatabase.getById(dish.getId()), dish.getQuantity()))
                    .toArray(OrderDishesInner[]::new);

            // Check if the dishes belong to the vendor.
            for (OrderDishesInner dish : convertedDishes) {
                if (!Objects.equals(dish.getDish().getVendorID(), order.getVendorID())) {
                    throw new IllegalStateException();
                }
            }

            // Update the dishes.
            order.setDishes(List.of(convertedDishes));
            orderDatabase.save(order);

            // Return the price.
            return Arrays.stream(convertedDishes).map(dish -> dish.getDish().getPrice() * dish.getAmount())
                .reduce(0.0f, Float::sum);
            // I had to do this because of a silly PMD warning...
            // Found 'DU'-anomaly for variable 'totalPrice' (lines '90'-'111')

        } catch (EntityNotFoundException e) {
            // Dish list is invalid.
            throw new IllegalStateException();
        }
    }
}
