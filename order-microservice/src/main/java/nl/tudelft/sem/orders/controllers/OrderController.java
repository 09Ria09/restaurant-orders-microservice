package nl.tudelft.sem.orders.controllers;

import javax.persistence.EntityNotFoundException;
import nl.tudelft.sem.orders.adapters.LocationMicroserviceAdapter;
import nl.tudelft.sem.orders.adapters.UserMicroserviceAdapter;
import nl.tudelft.sem.orders.api.OrderApi;
import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.model.OrderOrderIDDishesPut200Response;
import nl.tudelft.sem.orders.model.OrderOrderIDDishesPutRequest;
import nl.tudelft.sem.orders.model.OrderOrderIDPayPostRequest;
import nl.tudelft.sem.orders.ring0.OrderFacade;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;


@RestController
@RequestMapping("/order/")
public class OrderController implements OrderApi {
    private final transient UserMicroserviceAdapter userMicroservice;
    private final transient LocationMicroserviceAdapter locationMicroservice;
    private final transient OrderFacade ordersFacade;

    /**
     * Creates a new OrderController instance.
     *
     * @param orderLogic The class providing orders logic.
     */
    @Autowired
    public OrderController(OrderFacade ordersFacade,
                    UserMicroserviceAdapter userMicroservice,
                    LocationMicroserviceAdapter locationMicroservice) {
        this.ordersFacade = ordersFacade;
        this.userMicroservice = userMicroservice;
        this.locationMicroservice = locationMicroservice;
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
        if (userID == null
            || vendorID == null
            || !locationMicroservice.isCloseBy(
                userMicroservice.getCustomerAddress(userID),
                userMicroservice.getVendorAddress(vendorID))) {
            return ResponseEntity.badRequest().build();
        }
        try {
            if (userMicroservice.isCustomer(userID)) {
                return ResponseEntity.ok(
                    ordersFacade.createOrder(userID, vendorID));
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (RestClientException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<OrderOrderIDDishesPut200Response>
        orderOrderIDDishesPut(
        Long userID, Long orderID,
        OrderOrderIDDishesPutRequest orderOrderIDDishesPutRequest) {
        if (userMicroservice.isCustomer(userID)) {
            try {
                Float newTotalPrice = ordersFacade.updateDishes(orderID, userID,
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
    }
}
