package nl.tudelft.sem.orders.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import nl.tudelft.sem.orders.model.OrderOrderIDPayPostRequest;
import nl.tudelft.sem.orders.test.TestConfig;
import nl.tudelft.sem.orders.test.mocks.MockOrderDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;


@SpringBootTest
@Import(TestConfig.class)
class OrderControllerTest {
    @Autowired
    private MockOrderDatabase orderDatabase;

    @Autowired
    private OrderController orderController;


    @BeforeEach
    void clean() {
        orderDatabase.clean();
    }

    @Test
    void orderOrderIDPayPostForbidden() {
        assertEquals(HttpStatus.FORBIDDEN,
            orderController.orderOrderIDPayPost(1L, 1L,
                new OrderOrderIDPayPostRequest().paymentConfirmation(
                    "fail")).getStatusCode());
    }

    @Test
    void orderOrderIDPayPostMalformed() {
        assertEquals(HttpStatus.BAD_REQUEST,
            orderController.orderOrderIDPayPost(1L, 4L,
                new OrderOrderIDPayPostRequest().paymentConfirmation(
                    "pass")).getStatusCode());
    }

    @Test
    void orderOrderIDPayPost() {
        assertEquals(HttpStatus.OK,
            orderController.orderOrderIDPayPost(1L, 1L,
                new OrderOrderIDPayPostRequest().paymentConfirmation(
                    "pass")).getStatusCode());

        assertEquals(orderDatabase.getSaveRequests().size(), 1);
    }


}