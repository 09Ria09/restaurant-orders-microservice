package nl.tudelft.sem.orders.controllers;

import java.util.List;
import javax.persistence.EntityNotFoundException;
import nl.tudelft.sem.orders.api.OrderApi;
import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.model.OrderOrderIDDishesPut200Response;
import nl.tudelft.sem.orders.model.OrderOrderIDDishesPutRequest;
import nl.tudelft.sem.orders.model.OrderOrderIDPayPostRequest;
import nl.tudelft.sem.orders.ports.output.LocationService;
import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import nl.tudelft.sem.orders.result.ForbiddenException;
import nl.tudelft.sem.orders.result.MalformedException;
import nl.tudelft.sem.orders.ring0.OrderLogic;
import nl.tudelft.sem.users.ApiException;
import nl.tudelft.sem.users.model.UsersGetUserTypeIdGet200Response;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class OrderController implements OrderApi {
    private final transient UserMicroservice userMicroservice;
    private final transient LocationService locationService;
    private final transient OrderLogic orderLogic;

    /**
     * Creates a new OrderController instance.
     *
     * @param orderLogic The class providing orders logic.
     */
    @Autowired
    public OrderController(OrderLogic orderLogic,
                           UserMicroservice userMicroservice,
                           LocationService locationService) {
        this.orderLogic = orderLogic;
        this.userMicroservice = userMicroservice;
        this.locationService = locationService;
    }

    @Override
    public ResponseEntity<Void> orderOrderIDPayPost(
        Long userId,
        Long orderId,
        @NotNull OrderOrderIDPayPostRequest orderOrderIDPayPostRequest
    ) {
        try {
            orderLogic.payForOrder(userId, orderId,
                orderOrderIDPayPostRequest.getPaymentConfirmation());
            return ResponseEntity.ok().build();
        } catch (ForbiddenException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (MalformedException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Override
    public ResponseEntity<Order> orderPost(Long userID, Long vendorID) {
        try {
            if (!userMicroservice.isCustomer(userID)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            if (!locationService.isCloseBy(
                userMicroservice.getCustomerAddress(userID),
                userMicroservice.getVendorAddress(vendorID))) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.ok(
                orderLogic.createOrder(userID, vendorID));
        } catch (ApiException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<OrderOrderIDDishesPut200Response>
        orderOrderIDDishesPut(
        Long userID, Long orderID,
        OrderOrderIDDishesPutRequest orderOrderIDDishesPutRequest) {
        try {
            if (userMicroservice.isCustomer(userID)) {
                try {
                    Float newTotalPrice = orderLogic.updateDishes(orderID, userID,
                        orderOrderIDDishesPutRequest.getDishes());

                    OrderOrderIDDishesPut200Response response =
                        new OrderOrderIDDishesPut200Response();
                    response.setPrice(newTotalPrice);

                    return new ResponseEntity<>(response, HttpStatus.OK);
                } catch (IllegalStateException e) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                } catch (EntityNotFoundException e) {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (ApiException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @Override
    public ResponseEntity<List<Order>> orderGet(Long userID) {
        // The null check is here instead of in the orderLogic class as
        // I do not want to propagate this problem to another microservice,
        // but communication should be in the controller and not logic,
        // therefore I put this null check here
        if (userID == null) {
            return ResponseEntity.badRequest().build();
        }
        UsersGetUserTypeIdGet200Response.UserTypeEnum userType;
        try {
            userType = userMicroservice.getUserType(userID);
        } catch (ApiException e) {
            return ResponseEntity.badRequest().build();
        }
        List<Order> retrievedOrders;
        try {
            retrievedOrders  = orderLogic.getOrders(userID, userType);
        } catch(IllegalStateException ise) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(retrievedOrders);
    }
}
