package nl.tudelft.sem.orders.ring0;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.orders.result.MalformedException;
import nl.tudelft.sem.orders.test.TestConfig;
import nl.tudelft.sem.orders.test.mocks.MockDeliveryMicroservice;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestConfig.class)
class VendorLogicTest {
    @Autowired
    private VendorLogic vendorLogic;

    @Autowired
    private MockDeliveryMicroservice deliveryMicroservice;

    @Test
    void vendorsInRadiusFailRadius() {
        deliveryMicroservice.setFailRadius(true);
        assertThrows(MalformedException.class, () ->
            vendorLogic.vendorsInRadius(1L, "none", null));
        deliveryMicroservice.setFailRadius(false);
    }

    @Test
    void vendorsInRadius() {
        List<Long> retried = assertDoesNotThrow(() ->
            vendorLogic.vendorsInRadius(1L, "none", null));

        assertEquals(retried, new ArrayList<>());
    }

    @Test
    void vendorsInRadiusWithLocation() {
        List<Long> retried = assertDoesNotThrow(() ->
            vendorLogic.vendorsInRadius(1L, "none", new Location().city("a")));

        assertEquals(100L, retried.get(0));
        assertEquals(1, retried.size());
    }

    @Test
    void vendorsInRadiusWrongCustomer() {
        assertThrows(MalformedException.class, () ->
            vendorLogic.vendorsInRadius(4L, "none", null));
    }


}