package nl.tudelft.sem.orders.ring0;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.model.OrderDishesInner;
import nl.tudelft.sem.orders.model.OrderOrderIDDishesPutRequestDishesInner;
import nl.tudelft.sem.orders.ports.input.OrderLogicInterface;
import nl.tudelft.sem.orders.ports.output.DishDatabase;
import nl.tudelft.sem.orders.ports.output.OrderDatabase;
import nl.tudelft.sem.orders.ports.output.PaymentService;
import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import nl.tudelft.sem.orders.result.ForbiddenException;
import nl.tudelft.sem.orders.result.MalformedException;
import nl.tudelft.sem.users.ApiException;
import nl.tudelft.sem.users.model.UsersGetUserTypeIdGet200Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderLogic implements OrderLogicInterface {
    private final transient OrderDatabase orderDatabase;
    private final transient DishDatabase dishDatabase;
    private final transient UserMicroservice userMicroservice;
    private final transient PaymentService paymentService;

    /**
     * Creates a new order facade.
     *
     * @param orderDatabase    The database output port.
     * @param dishDatabase     The dish database.
     * @param userMicroservice The output port for the user microservice.
     * @param paymentService   The output port for the payment service.
     */
    @Autowired
    public OrderLogic(OrderDatabase orderDatabase, DishDatabase dishDatabase, UserMicroservice userMicroservice,
                      PaymentService paymentService) {
        this.orderDatabase = orderDatabase;
        this.dishDatabase = dishDatabase;
        this.userMicroservice = userMicroservice;
        this.paymentService = paymentService;
    }

    @Override
    public void payForOrder(long userId, long orderId, String paymentConfirmation)
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

        if (order.getCustomerID() != userId || !paymentService.verifyPaymentConfirmation(paymentConfirmation)) {
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
    public Order createOrder(long customerId, long vendorId) throws ApiException {
        Order order = new Order(orderDatabase.getLastId() + 1, customerId, vendorId, new ArrayList<>(),
            userMicroservice.getCustomerAddress(customerId), Order.StatusEnum.UNPAID);
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
                              @Valid List<@Valid OrderOrderIDDishesPutRequestDishesInner> dishes)
        throws EntityNotFoundException, IllegalStateException {
        Order order = orderDatabase.getById(orderId);
        // Check if the order exists and the user
        // owns the order and if the order is unpaid.
        if (order == null || order.getCustomerID() != customerId || order.getStatus() != Order.StatusEnum.UNPAID) {
            throw new EntityNotFoundException();
        }

        // Convert the list of IDs and amounts to a list of Dishes and amounts.
        try {
            OrderDishesInner[] convertedDishes = dishes.stream()
                .map((dish) -> new OrderDishesInner(dishDatabase.getById(dish.getId()), dish.getQuantity()))
                .toArray(OrderDishesInner[]::new);

            // Check if the dishes belong to the vendor and calculate the total price.
            float totalPrice = 0;
            for (OrderDishesInner dish : convertedDishes) {
                if (dish.getDish() == null || dish.getAmount() == null
                    || !Objects.equals(dish.getDish().getVendorID(), order.getVendorID())) {
                    throw new IllegalStateException();
                }
                totalPrice += dish.getDish().getPrice() * dish.getAmount();
            }

            // Update the dishes.
            order.setDishes(List.of(convertedDishes));
            orderDatabase.save(order);

            // Return the price.
            return totalPrice;
        } catch (EntityNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public List<Order> getOrders(Long userID, UsersGetUserTypeIdGet200Response.UserTypeEnum userType) {
        List<Order> foundOrders;
        switch (userType) {
            case ADMIN -> foundOrders = orderDatabase.findAllOrders();
            case VENDOR -> foundOrders = orderDatabase.findByVendorID(userID);
            case COURIER -> foundOrders = orderDatabase.findByCourierID(userID);
            case CUSTOMER -> foundOrders = orderDatabase.findByCustomerID(userID);
            default -> {
                throw new IllegalStateException("An account without a correct usertype has appeared");
            }
        }
        if (foundOrders == null) {
            throw new IllegalStateException("The database query went wrong");
        }
        return foundOrders;
    }
}
