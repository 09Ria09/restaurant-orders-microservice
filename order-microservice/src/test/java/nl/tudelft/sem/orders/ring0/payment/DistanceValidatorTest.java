package nl.tudelft.sem.orders.ring0.payment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import nl.tudelft.sem.orders.domain.GeoLocation;
import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.ports.output.DeliveryMicroservice;
import nl.tudelft.sem.orders.ports.output.LocationService;
import nl.tudelft.sem.orders.ports.output.OrderDatabase;
import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import nl.tudelft.sem.orders.result.VerificationException;
import nl.tudelft.sem.users.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DistanceValidatorTest {
    private transient DistanceValidator distanceValidator;
    private transient LocationService locationService;
    private transient UserMicroservice userMicroservice;
    private transient OrderDatabase orderDatabase;
    private transient DeliveryMicroservice deliveryMicroservice;
    private transient Validator validator;

    @BeforeEach
    void prep() {
        locationService = mock(LocationService.class);
        userMicroservice = mock(UserMicroservice.class);
        orderDatabase = mock(OrderDatabase.class);
        deliveryMicroservice = mock(DeliveryMicroservice.class);

        validator = mock(Validator.class);

        distanceValidator =
            new DistanceValidator(locationService, userMicroservice,
                orderDatabase, deliveryMicroservice);
        distanceValidator.setNext(validator);
    }

    @Test
    void verifyNotExact() throws VerificationException, ApiException,
        nl.tudelft.sem.delivery.ApiException {

        when(orderDatabase.getById(2L)).thenReturn(
            new Order().location(new Location()).vendorID(13L));
        when(userMicroservice.getVendorAddress(13L)).thenReturn(
            new Location().address("aba"));

        when(locationService.getGeoLocation(new Location())).thenReturn(
            new GeoLocation(1, 1));
        when(locationService.getGeoLocation(
            new Location().address("aba"))).thenReturn(new GeoLocation(1, 13));

        when(deliveryMicroservice.getDeliveryRadius(13L, 1L)).thenReturn(2L);

        assertThrows(VerificationException.class,
            () -> distanceValidator.verify(new Payment(1L, 2L, "test")));
    }

    @Test
    void verifyExact() throws VerificationException, ApiException,
        nl.tudelft.sem.delivery.ApiException {

        when(orderDatabase.getById(2L)).thenReturn(
            new Order().location(new Location()).vendorID(13L));
        when(userMicroservice.getVendorAddress(13L)).thenReturn(
            new Location().address("aba"));

        when(locationService.getGeoLocation(new Location())).thenReturn(
            new GeoLocation(1, 1));
        when(locationService.getGeoLocation(
            new Location().address("aba"))).thenReturn(new GeoLocation(1, 1));

        when(deliveryMicroservice.getDeliveryRadius(13L, 1L)).thenReturn(0L);

        assertDoesNotThrow(
            () -> distanceValidator.verify(new Payment(1L, 2L, "test")));

        verify(validator, times(1)).verify(any());
    }
}