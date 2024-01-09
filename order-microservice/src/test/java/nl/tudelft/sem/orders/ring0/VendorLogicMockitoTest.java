package nl.tudelft.sem.orders.ring0;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import nl.tudelft.sem.orders.model.Dish;
import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.ports.output.DeliveryMicroservice;
import nl.tudelft.sem.orders.ports.output.DishDatabase;
import nl.tudelft.sem.orders.ports.output.LocationService;
import nl.tudelft.sem.orders.ports.output.OrderDatabase;
import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import nl.tudelft.sem.orders.result.ForbiddenException;
import nl.tudelft.sem.orders.result.MalformedException;
import nl.tudelft.sem.users.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;



public class VendorLogicMockitoTest {

    private OrderDatabase orderDatabase;
    private UserMicroservice userMicroservice;
    private DeliveryMicroservice deliveryMicroservice;
    private LocationService locationService;
    private DishDatabase dishDatabase;

    private VendorLogic vendorLogic;

    @BeforeEach
    void setup() {
        orderDatabase = mock(OrderDatabase.class);
        userMicroservice = mock(UserMicroservice.class);
        deliveryMicroservice = mock(DeliveryMicroservice.class);
        locationService = mock(LocationService.class);
        dishDatabase = mock(DishDatabase.class);
        vendorLogic = new VendorLogic(
                orderDatabase,
                userMicroservice,
                deliveryMicroservice,
                locationService,
                dishDatabase
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
}
