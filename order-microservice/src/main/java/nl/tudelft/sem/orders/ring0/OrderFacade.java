package nl.tudelft.sem.orders.ring0;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import nl.tudelft.sem.orders.model.Dish;
import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.model.OrderDishesInner;
import nl.tudelft.sem.orders.model.OrderOrderIDDishesPutRequestDishesInner;
import nl.tudelft.sem.orders.ports.input.OrderLogicInterface;
import nl.tudelft.sem.orders.ports.output.DeliveryMicroservice;
import nl.tudelft.sem.orders.ports.output.DishDatabase;
import nl.tudelft.sem.orders.ports.output.LocationService;
import nl.tudelft.sem.orders.ports.output.OrderDatabase;
import nl.tudelft.sem.orders.ports.output.PaymentService;
import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import nl.tudelft.sem.orders.result.ForbiddenException;
import nl.tudelft.sem.orders.result.MalformedException;
import nl.tudelft.sem.orders.result.NotFoundException;
import nl.tudelft.sem.orders.result.PaymentException;
import nl.tudelft.sem.orders.result.VerificationException;
import nl.tudelft.sem.orders.ring0.payment.DistanceValidator;
import nl.tudelft.sem.orders.ring0.payment.Payment;
import nl.tudelft.sem.orders.ring0.payment.StatusValidator;
import nl.tudelft.sem.orders.ring0.payment.TokenValidator;
import nl.tudelft.sem.orders.ring0.payment.UserOwnershipValidator;
import nl.tudelft.sem.users.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderFacade implements OrderLogicInterface {
    private final transient OrderDatabase orderDatabase;
    private final transient DishDatabase dishDatabase;
    private final transient UserMicroservice userMicroservice;
    private final transient PaymentService paymentService;
    private final transient LocationService locationService;
    private final transient DistanceValidator distanceValidator;
    private final transient StatusValidator statusValidator;
    private final transient TokenValidator tokenValidator;
    private final transient UserOwnershipValidator userOwnershipValidator;
    private final transient DeliveryMicroservice deliveryMicroservice;

    /**
     * Creates a new order facade.
     *
     * @param orderDatabase    The database output port.
     * @param dishDatabase     The dish database.
     * @param userMicroservice The output port for the user microservice.
     * @param paymentService   The output port for the payment service.
     */
    //CHECKSTYLE:OFF
    @Autowired
    public OrderFacade(OrderDatabase orderDatabase,
                       DishDatabase dishDatabase,
                       UserMicroservice userMicroservice,
                       PaymentService paymentService,
                       LocationService locationService,
                       UserOwnershipValidator userOwnershipValidator,
                       DistanceValidator distanceValidator,
                       TokenValidator tokenValidator,
                       StatusValidator statusValidator,
                       DeliveryMicroservice deliveryMicroservice) {
        //CHECKSTYLE:ON
        this.orderDatabase = orderDatabase;
        this.dishDatabase = dishDatabase;
        this.userMicroservice = userMicroservice;
        this.paymentService = paymentService;
        this.locationService = locationService;
        this.distanceValidator = distanceValidator;
        this.statusValidator = statusValidator;
        this.tokenValidator = tokenValidator;
        this.userOwnershipValidator = userOwnershipValidator;
        this.deliveryMicroservice = deliveryMicroservice;
    }

    @Override
    public void payForOrder(long userId, long orderId,
                            String paymentConfirmation)
            throws MalformedException, ForbiddenException {
        // create the validation chain

        var handler = tokenValidator;
        handler.setNext(statusValidator);
        statusValidator.setNext(userOwnershipValidator);

        // make sure the distance validator is last since it is most costly

        userOwnershipValidator.setNext(distanceValidator);


        try {
            handler.verify(
                    new Payment(userId, orderId, paymentConfirmation));
        } catch (PaymentException e) {
            throw new ForbiddenException();
        } catch (VerificationException e) {
            throw new MalformedException();
        }

        // now try debiting the amount from the card

        Order order = orderDatabase.getById(orderId);
        if (!paymentService.finalizePayment(paymentConfirmation)) {
            // just give up
            throw new MalformedException();
        }

        // otherwise first set the order to pend

        order.setStatus(Order.StatusEnum.PENDING);
        orderDatabase.save(order);

        // and inform the vendor of the new order

        try {
            deliveryMicroservice.newDelivery(order.getVendorID(),
                    order.getOrderID(), order.getCustomerID());
        } catch (nl.tudelft.sem.delivery.ApiException e) {
            throw new MalformedException();
        }
    }

    /**
     * Creates a new order.
     *
     * @param customerId the id of the customer
     * @param vendorId   the id of the vendor
     */
    @Override
    public Order createOrder(long customerId, long vendorId)
            throws ApiException {
        Order order = new Order();
        order.setCustomerID(customerId);
        order.setVendorID(vendorId);
        order.setDishes(new ArrayList<>());
        order.setLocation(userMicroservice.getCustomerAddress(customerId));
        order.setPrice(0f);
        order.setStatus(Order.StatusEnum.UNPAID);

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
        if (order == null
                || order.getCustomerID() != customerId
                || order.getStatus() != Order.StatusEnum.UNPAID) {
            throw new EntityNotFoundException();
        }

        // Convert the list of IDs and amounts to a list of Dishes and amounts.
        try {
            OrderDishesInner[] convertedDishes = dishes.stream()
                    .map((dish) -> new OrderDishesInner(
                            dishDatabase.getById(dish.getId()), dish.getQuantity()))
                    .toArray(OrderDishesInner[]::new);

            // Check if the dishes belong to the vendor and calculate the total price.
            float totalPrice = 0;
            for (OrderDishesInner dish : convertedDishes) {
                if (dish.getDish() == null || dish.getAmount() == null
                        || !Objects.equals(dish.getDish().getVendorID(),
                        order.getVendorID())) {
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
    public List<Order> getOrders(Long userID) throws ApiException {
        List<Order> foundOrders = null;

        if (userMicroservice.isCustomer(userID)) {
            foundOrders = orderDatabase.findByCustomerID(userID);
        } else if (userMicroservice.isVendor(userID)) {
            foundOrders = orderDatabase.findByVendorID(userID);
        } else if (userMicroservice.isAdmin(userID)) {
            foundOrders = orderDatabase.findAllOrders();
        } else if (userMicroservice.isCourier(userID)) {
            foundOrders = orderDatabase.findByCourierID(userID);
        }

        if (foundOrders == null) {
            throw new IllegalStateException("The database query went wrong");
        }

        return foundOrders;
    }

    /**
     * Reorders an existing order. The method first checks if the order exists,
     * and if the user is the owner of the order.
     * Then it checks if the vendor still exists and is close by. It also checks if all dishes still exist.
     * If all checks pass, a new order is created with the same details as the old order,
     * and the status is set to UNPAID.
     *
     * @param userID  the ID of the user who wants to reorder
     * @param orderID the ID of the order to be reordered
     * @return the newly created order
     * @throws MalformedException if the order does not exist or the user is not the owner of the order
     * @throws NotFoundException  if the vendor does not exist, is not close by, or if any of the dishes do not exist
     */
    @Override
    public Order reorder(Long userID, Long orderID)
            throws MalformedException, NotFoundException {
        Order order = orderDatabase.getById(orderID);

        if (order == null || !Objects.equals(order.getCustomerID(), userID)) {
            throw new MalformedException();
        }

        long vendorID = order.getVendorID();

        // Check if the vendor still exists and is close by
        Location userAddress;

        try {
            userAddress = userMicroservice.getCustomerAddress(userID);

            if (!userMicroservice.isVendor(vendorID)) {
                throw new NotFoundException();
            }
        } catch (Exception e) {
            throw new NotFoundException();
        }

        // Check if all dishes still exist
        for (OrderDishesInner orderDish : order.getDishes()) {
            Long dishID = orderDish.getDish().getDishID();
            Dish dish = dishDatabase.getById(dishID);

            if (dish == null || dish.getVendorID() != vendorID) {
                throw new NotFoundException();
            }
        }

        Order newOrder = new Order();
        newOrder.setCustomerID(userID);
        newOrder.setVendorID(vendorID);
        newOrder.setDishes(order.getDishes());
        newOrder.setLocation(userAddress);
        newOrder.setStatus(Order.StatusEnum.UNPAID);

        return orderDatabase.save(newOrder);
    }

    /**
     * Adds given rating to the order.
     *
     * @param userID  id of user who rates.
     * @param orderID id of order to be rated.
     * @param rating  integer between 0 and 10.
     * @throws MalformedException Invalid rating value or user id or missing/invalid order id/
     * @throws ForbiddenException If trying to change a rating not as a customer
     *                            or admin or of not own order (in case of customer).
     * @throws ApiException       .
     */
    public void rateOrder(Long userID, Long orderID, Integer rating)
            throws MalformedException, ForbiddenException, ApiException {
        if (orderID == null || userID == null) {
            throw new MalformedException();
        }

        Order order = orderDatabase.getById(orderID);

        if (order == null || rating < 0 || rating > 10) {
            throw new MalformedException();
        }

        if (!userMicroservice.isAdmin(userID) && !order.getCustomerID().equals(userID)) {
            throw new ForbiddenException();
        }

        order.setRating(rating);
        orderDatabase.save(order);
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
    public Order changeOrder(Long userId, Order order) throws MalformedException, ApiException, ForbiddenException {
        if (userId == null || order == null) {
            throw new MalformedException();
        }

        Order orderRepo = orderDatabase.getById(order.getOrderID());
        if (orderRepo == null) {
            throw new MalformedException();
        }
        checkModifyForCustomer(userId, order, orderRepo);
        orderRepo = orderDatabase.getById(order.getOrderID());
        checkModifyForVendorAndCourier(userId, order, orderRepo);
        orderDatabase.save(order);
        return order;
    }

    private void checkModifyForVendorAndCourier(Long userId, Order order, Order orderRepo)
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

    private void checkModifyForCustomer(Long userId, Order order, Order orderRepo)
            throws ApiException, ForbiddenException {
        if (userMicroservice.isCustomer(userId)) {
            orderRepo.setLocation(order.getLocation());
            if (!userId.equals(orderRepo.getCustomerID())
                    || orderRepo.getStatus() != Order.StatusEnum.UNPAID
                    || !orderRepo.equals(order)) {
                throw new ForbiddenException();
            }
        }
    }

    /**
     * Retrieves order by given Id.
     *
     * @param orderID Id of the order to retrieve.
     * @return order.
     * @throws MalformedException if invalid orderId or missing Order
     */
    public List<Order> getOrder(Long orderID) throws MalformedException {
        if (orderID == null) {
            throw new MalformedException();
        }

        Order order = orderDatabase.getById(orderID);

        if (order == null) {
            throw new MalformedException();
        }

        List<Order> result = new ArrayList<>();
        result.add(order);
        return result;
    }
}
