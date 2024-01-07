package nl.tudelft.sem.orders.ring0;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Objects;
import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.result.ForbiddenException;
import nl.tudelft.sem.orders.result.MalformedException;
import nl.tudelft.sem.orders.test.TestConfig;
import nl.tudelft.sem.orders.test.mocks.MockDeliveryMicroservice;
import nl.tudelft.sem.orders.test.mocks.MockOrderDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestConfig.class)
class OrderLogicTest {
    @Autowired
    private MockOrderDatabase orderDatabase;
    @Autowired
    private MockDeliveryMicroservice mockDeliveryMicroservice;

    @Autowired
    private OrderLogic orderFacade;

    @BeforeEach
    void clean() {
        orderDatabase.clean();
        mockDeliveryMicroservice.setFailNew(false);
        mockDeliveryMicroservice.setFailRadius(false);
    }

    @Test
    void payForOrderSuccess() {
        assertDoesNotThrow(() -> orderFacade.payForOrder(4L, 1L, "auth"));

        Order expected = orderDatabase.getById(1).status(Order.StatusEnum.PENDING);

        assertEquals(1, orderDatabase.getSaveRequests().size());
        assertEquals(expected, orderDatabase.getSaveRequests().get(0));
    }

    @Test
    void payForOrderForbid() {
        assertThrows(ForbiddenException.class,
            () -> orderFacade.payForOrder(2L, 1L, "auth"));
        assertEquals(0, orderDatabase.getSaveRequests().size());
    }

    @Test
    void payForOrderForbidPayment() {
        assertThrows(ForbiddenException.class,
            () -> orderFacade.payForOrder(1L, 1L, "fail"));
        assertEquals(0, orderDatabase.getSaveRequests().size());
    }

    @Test
    void payForOrderNoSuchOrder() {
        assertThrows(MalformedException.class,
            () -> orderFacade.payForOrder(1L, 400L, "pass"));
        assertEquals(0, orderDatabase.getSaveRequests().size());
    }

    @Test
    void payForOrderNoSuchUser() {
        assertThrows(ForbiddenException.class,
            () -> orderFacade.payForOrder(100L, 1L, "pass"));
        assertEquals(0, orderDatabase.getSaveRequests().size());
    }

    @Test
    void payForOrderNoDistance() {
        mockDeliveryMicroservice.setFailRadius(true);

        assertThrows(MalformedException.class,
            () -> orderFacade.payForOrder(4L, 1L, "pass"));
        assertEquals(0, orderDatabase.getSaveRequests().size());
    }

    @Test
    void payForOrderCannotFinalize() {
        assertThrows(MalformedException.class,
            () -> orderFacade.payForOrder(4L, 1L, "finfail"));
        assertEquals(0, orderDatabase.getSaveRequests().size());
    }

    @Test
    void payForOrderTooFar() {
        assertThrows(MalformedException.class,
            () -> orderFacade.payForOrder(4L, 3L, "pass"));
        assertEquals(0, orderDatabase.getSaveRequests().size());
    }

    @Test
    void payForOrderNoNew() {
        mockDeliveryMicroservice.setFailNew(true);

        assertThrows(MalformedException.class,
            () -> orderFacade.payForOrder(4L, 1L, "pass"));
        assertEquals(1, orderDatabase.getSaveRequests().size());
    }

    @Test
    void payForOrderAlreadyPaid() {
        assertThrows(MalformedException.class,
            () -> orderFacade.payForOrder(1L, 2L, "pass"));
        assertEquals(0, orderDatabase.getSaveRequests().size());
    }
}