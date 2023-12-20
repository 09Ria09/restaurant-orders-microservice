package nl.tudelft.sem.orders.ring0;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.result.ForbiddenException;
import nl.tudelft.sem.orders.result.MalformedException;
import nl.tudelft.sem.orders.test.TestConfig;
import nl.tudelft.sem.orders.test.mocks.MockOrderDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestConfig.class)
class OrderLogicInterfaceTest {
    @Autowired
    private MockOrderDatabase orderDatabase;

    @Autowired
    private OrderLogic orderFacade;

    @BeforeEach
    void clean() {
        orderDatabase.clean();
    }

    @Test
    void payForOrderSuccess() {
        assertDoesNotThrow(() -> orderFacade.payForOrder(1L, 1L, "auth"));

        Order expected = new Order(1L, 1L, 13L, new ArrayList<>(),
            new Location().city("Kraków").country("PL").postalCode("123ZT"),
            nl.tudelft.sem.orders.model.Order.StatusEnum.PENDING).courierID(3L);

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
            () -> orderFacade.payForOrder(1L, 4L, "pass"));
        assertEquals(0, orderDatabase.getSaveRequests().size());
    }

    @Test
    void payForOrderNoSuchUser() {
        assertThrows(MalformedException.class,
            () -> orderFacade.payForOrder(4L, 1L, "pass"));
        assertEquals(0, orderDatabase.getSaveRequests().size());
    }

    @Test
    void payForOrderAlreadyPaid() {
        assertThrows(MalformedException.class,
            () -> orderFacade.payForOrder(1L, 2L, "pass"));
        assertEquals(0, orderDatabase.getSaveRequests().size());
    }
}