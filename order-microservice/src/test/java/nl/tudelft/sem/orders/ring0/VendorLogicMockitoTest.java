package nl.tudelft.sem.orders.ring0;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import nl.tudelft.sem.orders.model.Dish;
import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.ports.output.DeliveryMicroservice;
import nl.tudelft.sem.orders.ports.output.DishDatabase;
import nl.tudelft.sem.orders.ports.output.LocationService;
import nl.tudelft.sem.orders.ports.output.OrderDatabase;
import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import nl.tudelft.sem.orders.result.ForbiddenException;
import nl.tudelft.sem.orders.result.MalformedException;
import nl.tudelft.sem.orders.ring0.distance.GeoDistanceStrategy;
import nl.tudelft.sem.orders.ring0.distance.SimpleWordMatchStrategy;
import nl.tudelft.sem.orders.test.TestConfig;
import nl.tudelft.sem.orders.test.mocks.MockDeliveryMicroservice;
import nl.tudelft.sem.orders.test.mocks.MockDishDatabase;
import nl.tudelft.sem.orders.test.mocks.MockUserMicroservice;
import nl.tudelft.sem.users.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;


@SpringBootTest
@Import(TestConfig.class)
public class VendorLogicMockitoTest {

    private OrderDatabase orderDatabase;
    private UserMicroservice userMicroservice;
    private DeliveryMicroservice deliveryMicroservice;
    private LocationService locationService;
    private DishDatabase dishDatabase;

    private VendorFacade vendorLogic;
    private VendorFacade vendorFacade;

    private MockDeliveryMicroservice mockDeliveryMicroservice;

    private MockDishDatabase mockDishDatabase;

    @Autowired
    VendorLogicMockitoTest(MockDeliveryMicroservice mockDeliveryMicroservice,
                           MockDishDatabase mockDishDatabase,
                           MockUserMicroservice mockUserMicroservice,
                           GeoDistanceStrategy geoDistanceStrategy) {
        this.mockDeliveryMicroservice = mockDeliveryMicroservice;
        this.mockDishDatabase = mockDishDatabase;

        this.vendorFacade = new VendorFacade(mockUserMicroservice, mockDishDatabase,
                geoDistanceStrategy, new SimpleWordMatchStrategy(), null);
    }

    @BeforeEach
    void setup() {
        orderDatabase = mock(OrderDatabase.class);
        userMicroservice = mock(UserMicroservice.class);
        deliveryMicroservice = mock(DeliveryMicroservice.class);
        locationService = mock(LocationService.class);
        dishDatabase = mock(DishDatabase.class);
        vendorLogic = new VendorFacade(
                userMicroservice,
                dishDatabase,
                null,
                null,
                orderDatabase
        );
    }

    @Test
    void deleteDishByIDException() throws ApiException {
        when(userMicroservice.isVendor(1L)).thenThrow(ApiException.class);
        when(dishDatabase.getById(2L)).thenReturn(new Dish());

        assertThrows(MalformedException.class, () -> vendorLogic.deleteDishById(1L, 2L));
    }

    @Test
    void vendorOrderCustomerNotCustomer() throws ApiException, ForbiddenException {
        long vendorID = 1;
        long customerID = 2;
        when(userMicroservice.isVendor(1)).thenReturn(true);
        when(userMicroservice.isCustomer(2)).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> vendorLogic.getPastOrdersForCustomer(1L, 2L));
    }

    @Test
    void vendorOrderCustomerNotVendor() throws ApiException, ForbiddenException {
        long vendorID = 1;
        long customerID = 2;
        when(userMicroservice.isVendor(1)).thenReturn(false);
        when(userMicroservice.isCustomer(2)).thenReturn(true);

        assertThrows(ForbiddenException.class, () -> vendorLogic.getPastOrdersForCustomer(1L, 2L));
    }

    @Test
    void vendorOrderCustomerException() throws ApiException, ForbiddenException {
        long vendorID = 1;
        long customerID = 2;
        when(userMicroservice.isVendor(1)).thenReturn(true);
        when(userMicroservice.isCustomer(2)).thenThrow(new ApiException("L Request"));


        assertThrows(ForbiddenException.class, () -> vendorLogic.getPastOrdersForCustomer(1L, 2L));
    }

    @Test
    void vendorOrderCustomerOk() throws ApiException, ForbiddenException {
        long vendorID = 1;
        long customerID = 2;
        Order order = new Order();
        List<Order> expected = new ArrayList<>();
        expected.add(order);

        when(userMicroservice.isVendor(1)).thenReturn(true);
        when(userMicroservice.isCustomer(2)).thenReturn(true);

        when(orderDatabase.findByVendorIDAndCustomerID(1L, 2L)).thenReturn(expected);
        assertEquals(expected, vendorLogic.getPastOrdersForCustomer(1L, 2L));
    }

    @BeforeEach
    void clean() {
        mockDishDatabase.clean();
    }


    @Test
    void vendorsInRadiusFailRadius() {
        mockDeliveryMicroservice.setFailRadius(true);
        assertThrows(MalformedException.class, () ->
                vendorFacade.vendorsInRadius(1L, "none", null));
        mockDeliveryMicroservice.setFailRadius(false);
    }

    @Test
    void vendorsInRadius() {
        List<Long> retried = assertDoesNotThrow(() ->
                vendorFacade.vendorsInRadius(1L, "none", null));

        assertEquals(retried, new ArrayList<>());
    }

    @Test
    void vendorsInRadiusWithLocation() {
        List<Long> retried = assertDoesNotThrow(() ->
                vendorFacade.vendorsInRadius(1L, "none", new Location().city("a")));

        assertEquals(0L, retried.get(0));
        assertEquals(1, retried.size());
    }

    @Test
    void vendorsInRadiusWrongCustomer() {
        assertThrows(MalformedException.class, () ->
                vendorFacade.vendorsInRadius(100L, "none", null));
    }

    @Test
    void vendorsInRadiusNotACustomer() {
        assertThrows(MalformedException.class, () ->
                vendorFacade.vendorsInRadius(0L, "none", null));
    }

    @Test
    void testDeleteDishByIdNoDish() {
        long userId = 1L;
        long dishId = 100L;

        // Test MalformedException when dish does not exist
        assertThrows(MalformedException.class,
                () -> vendorFacade.deleteDishById(userId, dishId));
    }

    @Test
    void testDeleteDishMalformed() {
        long userId = 100L;
        long dishId = 1L;

        assertThrows(MalformedException.class,
                () -> vendorFacade.deleteDishById(userId, dishId));
    }

    @Test
    void testDeleteDeleteNotMatched() {
        long userId = 3L;
        long dishId = 1L;

        assertThrows(ForbiddenException.class,
                () -> vendorFacade.deleteDishById(userId, dishId));
    }

    @Test
    void testDeleteDishByIdNotVendor() {
        long userId = 1L;
        long dishId = 2L;

        // Test ForbiddenException when user is not a vendor
        assertThrows(ForbiddenException.class,
                () -> vendorFacade.deleteDishById(userId, dishId));
    }

    @Test
    void testDeleteDishByIdNotOwner() {
        long userId = 2L;
        long dishId = 1L;

        // Test ForbiddenException when user is not an owner of the dish
        assertThrows(ForbiddenException.class,
                () -> vendorFacade.deleteDishById(userId, dishId));
    }

    @Test
    void testDeleteDishById() {
        long userId = 0L;
        long dishId = 1L;

        // Test that the dish is deleted when the user is a vendor and the owner of the dish
        assertDoesNotThrow(() -> vendorFacade.deleteDishById(userId, dishId));
        assertNull(mockDishDatabase.getById(dishId));
    }
}

