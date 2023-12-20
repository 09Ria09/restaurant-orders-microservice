package nl.tudelft.sem.orders.controllers;

import nl.tudelft.sem.orders.api.OrderApi;
import nl.tudelft.sem.orders.model.OrderOrderIDPayPostRequest;
import nl.tudelft.sem.orders.ports.input.OrderLogic;
import nl.tudelft.sem.orders.result.ForbiddenException;
import nl.tudelft.sem.orders.result.MalformedException;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class OrderController implements OrderApi {
    private transient OrderLogic orderLogic;

    /**
     * Creates a new OrderController instance.
     *
     * @param orderLogic The class providing orders logic.
     */
    @Autowired
    OrderController(OrderLogic orderLogic) {
        this.orderLogic = orderLogic;
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
}
