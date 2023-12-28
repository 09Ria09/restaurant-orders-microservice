package nl.tudelft.sem.orders.ring0;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.orders.result.ForbiddenException;
import nl.tudelft.sem.orders.result.MalformedException;
import nl.tudelft.sem.orders.test.TestConfig;
import nl.tudelft.sem.orders.test.mocks.MockDeliveryMicroservice;
import nl.tudelft.sem.orders.test.mocks.MockDishDatabase;
import org.junit.jupiter.api.BeforeEach;
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

    @Autowired
    private MockDishDatabase dishDatabase;

    @BeforeEach
    void clean() {
        dishDatabase.clean();
    }


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

    @Test
    void testDeleteDishByIdNoDish() {
        long userId = 1L;
        long dishId = 3L;

        // Test MalformedException when dish does not exist
        assertThrows(MalformedException.class, () -> vendorLogic.deleteDishById(userId, dishId));
    }

    @Test
    void testDeleteDishByIdNotVendor() {
        long userId = 100L;
        long dishId = 2L;

        // Test ForbiddenException when user is not a vendor
        assertThrows(ForbiddenException.class, () -> vendorLogic.deleteDishById(userId, dishId));
    }

    @Test
    void testDeleteDishByIdNotOwner() {
        long userId = 2L;
        long dishId = 1L;

        // Test ForbiddenException when user is not an owner of the dish
        assertThrows(ForbiddenException.class, () -> vendorLogic.deleteDishById(userId, dishId));
    }

    @Test
    void testDeleteDishById() {
        long userId = 1L;
        long dishId = 1L;

        // Test that the dish is deleted when the user is a vendor and the owner of the dish
        assertDoesNotThrow(() -> vendorLogic.deleteDishById(userId, dishId));
        assertNull(dishDatabase.getById(dishId));
    }

}