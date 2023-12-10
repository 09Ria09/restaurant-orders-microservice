package nl.tudelft.sem.orders.controllers;

import nl.tudelft.sem.orders.api.OrderApi;
import nl.tudelft.sem.orders.model.OrderOrderIDPayPostRequest;
import nl.tudelft.sem.orders.ring0.OrderFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/vendor/")
public class OrderController implements OrderApi {
    private transient OrderFacade ordersFacade;

    /**
     * Creates a new OrderController instance.
     *
     * @param ordersFacade The class providing orders logic.
     */
    @Autowired
    OrderController(OrderFacade ordersFacade) {
        this.ordersFacade = ordersFacade;
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
}
