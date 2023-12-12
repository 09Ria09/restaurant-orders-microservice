package nl.tudelft.sem.orders.controllers;

import nl.tudelft.sem.orders.adapters.LocationMicroserviceAdapter;
import nl.tudelft.sem.orders.adapters.UserMicroserviceAdapter;
import nl.tudelft.sem.orders.api.OrderApi;
import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.model.OrderOrderIDDishesPut200Response;
import nl.tudelft.sem.orders.model.OrderOrderIDDishesPutRequest;
import nl.tudelft.sem.orders.model.OrderOrderIDPayPostRequest;
import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import nl.tudelft.sem.orders.ring0.OrderFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;


@RestController
@RequestMapping("/vendor/")
public class OrderController implements OrderApi {
    private transient OrderFacade ordersFacade;
    private final UserMicroserviceAdapter userMicroservice;
    private final LocationMicroserviceAdapter locationMicroservice;

    /**
     * Creates a new OrderController instance.
     *
     * @param ordersFacade The class providing orders logic.
     */
    @Autowired
    OrderController(OrderFacade ordersFacade, UserMicroserviceAdapter userMicroservice,
                    LocationMicroserviceAdapter locationMicroservice) {
        this.ordersFacade = ordersFacade;
        this.userMicroservice = userMicroservice;
        this.locationMicroservice = locationMicroservice;
    }

    @Override
    public ResponseEntity<Void> orderOrderIDPayPost(
            Long userId,
            Long orderId,
            OrderOrderIDPayPostRequest orderOrderIDPayPostRequest
    ) {
        try {
            ordersFacade.payForOrder(userId, orderId,
                    orderOrderIDPayPostRequest.getPaymentConfirmation());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<Order> orderPost(Long userID, Long vendorID) {
        if(userID==null || vendorID==null ||
            locationMicroservice.isCloseBy(userMicroservice.getHomeAddress(userID), userMicroservice.getLocation(vendorID)))
            return ResponseEntity.badRequest().build();
        try {
            if (userMicroservice.isCustomer(userID)) {
                return ResponseEntity.ok(ordersFacade.createOrder(userID, vendorID));
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (RestClientException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<OrderOrderIDDishesPut200Response> orderOrderIDDishesPut(Long userID,
                                                                                  Long orderID,
                                                                                  OrderOrderIDDishesPutRequest orderOrderIDDishesPutRequest) {
        return OrderApi.super.orderOrderIDDishesPut(userID, orderID, orderOrderIDDishesPutRequest);
    }
}
