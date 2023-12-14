package nl.tudelft.sem.orders.controllers;

import nl.tudelft.sem.orders.api.OrderApi;
import nl.tudelft.sem.orders.model.OrderOrderIDPayPostRequest;
import nl.tudelft.sem.orders.ports.input.OrderLogic;
import nl.tudelft.sem.orders.result.ForbiddenException;
import nl.tudelft.sem.orders.result.MalformedException;
import nl.tudelft.sem.orders.ring0.OrderFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/vendor/")
public class OrderController implements OrderApi {
    private transient OrderLogic orderLogic;

    /**
     * Creates a new OrderController instance.
     *
     * @param orderLogic The class providing orders logic.
     */
    @Autowired
    OrderController(OrderFacade orderLogic) {
        this.orderLogic = orderLogic;
    }

    @Override
    public ResponseEntity<Void> orderOrderIDPayPost(
            Long userId,
            Long orderId,
            OrderOrderIDPayPostRequest orderOrderIDPayPostRequest
    ) {
        try {
            orderLogic.payForOrder(userId, orderId,
                    orderOrderIDPayPostRequest.getPaymentConfirmation());
            return ResponseEntity.ok().build();
        } catch (ForbiddenException _e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (MalformedException _e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
