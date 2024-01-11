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
import nl.tudelft.sem.orders.result.NotFoundException;
import nl.tudelft.sem.orders.ring0.OrderFacade;
import nl.tudelft.sem.users.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class OrderController implements OrderApi {
    private final transient OrderFacade orderFacade;

    /**
     * Creates a new OrderController instance.
     *
     * @param orderFacade The class providing orders logic.
     */
    @Autowired
    public OrderController(OrderFacade orderFacade) {
        this.orderFacade = orderFacade;
    }

    @Override
    public ResponseEntity<Void> orderOrderIDPayPost(
        Long userId,
        Long orderId,
        @NotNull OrderOrderIDPayPostRequest orderOrderIDPayPostRequest
    ) {
        try {
            orderFacade.payForOrder(userId, orderId,
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
            return ResponseEntity.ok(
                orderFacade.createOrder(userID, vendorID));
        } catch (ApiException | MalformedException e) {
            return ResponseEntity.badRequest().build();
        } catch (ForbiddenException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @Override
    public ResponseEntity<OrderOrderIDDishesPut200Response>
        orderOrderIDDishesPut(
        Long userID, Long orderID,
        OrderOrderIDDishesPutRequest orderOrderIDDishesPutRequest) {
        try {
            Float newTotalPrice = orderFacade.updateDishes(orderID, userID, orderOrderIDDishesPutRequest.getDishes());

            OrderOrderIDDishesPut200Response response = new OrderOrderIDDishesPut200Response();
            response.setPrice(newTotalPrice);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
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

        List<Order> retrievedOrders;
        try {
            retrievedOrders  = orderFacade.getOrders(userID);
        } catch (IllegalStateException ise) {
            return ResponseEntity.badRequest().build();
        } catch (ApiException api) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(retrievedOrders);
    }
    
    @Override
    public ResponseEntity<Order> orderOrderIDReorderPost(Long userID, Long orderID) {
        try {
            return ResponseEntity.ok(orderFacade.reorder(userID, orderID));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (MalformedException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
