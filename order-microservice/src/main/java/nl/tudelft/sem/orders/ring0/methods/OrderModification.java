package nl.tudelft.sem.orders.ring0.methods;

import java.util.List;
import java.util.Objects;
import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.model.OrderDishesInner;
import nl.tudelft.sem.orders.model.OrderOrderIDDishesPutRequestDishesInner;
import nl.tudelft.sem.orders.ports.output.DishDatabase;
import nl.tudelft.sem.orders.ports.output.OrderDatabase;
import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import nl.tudelft.sem.orders.result.ForbiddenException;
import nl.tudelft.sem.orders.result.MalformedException;
import nl.tudelft.sem.users.ApiException;
import org.springframework.stereotype.Component;


@Component
public class OrderModification {

    private final transient OrderDatabase orderDatabase;
    private final transient DishDatabase dishDatabase;
    private final transient UserMicroservice userMicroservice;

    /**
     * Constructor for OrderModification Class.
     *
     * @param orderDatabase The database of orders
     * @param dishDatabase The database of dishes
     * @param userMicroservice The user microservice
     */
    public OrderModification(OrderDatabase orderDatabase,
                             DishDatabase dishDatabase,
                             UserMicroservice userMicroservice) {
        this.orderDatabase = orderDatabase;
        this.dishDatabase = dishDatabase;
        this.userMicroservice = userMicroservice;
    }

    /**
     * Updates dishes.
     *
     * @param orderId    the id of the order
     * @param customerId the id of the customer
     * @param dishes     the list of dishes
     */
    public float updateDishesProcess(long orderId, long customerId,
                                     List<@Valid OrderOrderIDDishesPutRequestDishesInner> dishes)
            throws ApiException {
        Order order = orderDatabase.getById(orderId);
        if (!userMicroservice.isCustomer(customerId)) {
            throw new ApiException();
        }

        // Check if the order exists and the user
        // owns the order and if the order is unpaid.
        if (order == null || order.getCustomerID() != customerId
                || order.getStatus() != Order.StatusEnum.UNPAID) {
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
                if (dish.getDish() == null
                        || dish.getAmount() == null
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

    /**
     * Update order according to permissions.
     *
     * @param userId id of user asking for the update
     * @param order Order to be updated
     * @return The updated order
     * @throws MalformedException thrown if invalid or missing order or userId
     * @throws ApiException thrown if userMicroservice error
     * @throws ForbiddenException thrown if user doesn't have the permission to do the requested update.
     */
    public Order updateOrderProcess(Long userId, Order order)
            throws MalformedException, ApiException, ForbiddenException {
        if (userId == null || order == null) {
            throw new MalformedException();
        }

        Order orderRepo = orderDatabase.getById(order.getOrderID());
        if (orderRepo == null) {
            throw new MalformedException();
        }
        if (userMicroservice.isCustomer(userId)) {
            orderRepo.setLocation(order.getLocation());
            if (!userId.equals(orderRepo.getCustomerID())
                    || orderRepo.getStatus() != Order.StatusEnum.UNPAID
                    || !orderRepo.equals(order)) {
                throw new ForbiddenException();
            }
        }
        orderRepo = orderDatabase.getById(order.getOrderID());
        checkModifyVendorOrCourier(userId, order, orderRepo);
        orderDatabase.save(order);
        return order;
    }

    private void checkModifyVendorOrCourier(Long userId, Order order, Order orderRepo)
            throws ApiException, ForbiddenException {
        if (userMicroservice.isVendor(userId) || userMicroservice.isCourier(userId)) {
            orderRepo.setStatus(order.getStatus());
            orderRepo.setCourierID(order.getCourierID());
            orderRepo.setPrice(order.getPrice());
            if (userMicroservice.isCourier(userId)) {
                orderRepo.setCourierRating(order.getCourierRating());
            }
            float price = 0;
            for (OrderDishesInner d : order.getDishes()) {
                price = d.getDish().getPrice() * d.getAmount();
            }
            if (!orderRepo.equals(order) || order.getPrice() < price) {
                throw new ForbiddenException();
            }
        }
    }
}
