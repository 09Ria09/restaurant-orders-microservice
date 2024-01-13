package nl.tudelft.sem.orders.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import nl.tudelft.sem.delivery.ApiException;
import nl.tudelft.sem.delivery.api.AdminApi;
import nl.tudelft.sem.delivery.api.DeliveryApi;
import nl.tudelft.sem.delivery.api.VendorApi;
import nl.tudelft.sem.delivery.model.CreateDeliveryRequest;
import nl.tudelft.sem.delivery.model.Delivery;
import nl.tudelft.sem.delivery.model.GetCurrentDefaultRadius200Response;
import nl.tudelft.sem.delivery.model.GetVendorDeliveryRadius200Response;
import nl.tudelft.sem.orders.adapters.remote.DeliveryRemoteProxy;
import nl.tudelft.sem.orders.adapters.remote.UserRemoteProxy;
import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.orders.ring0.distance.LocationMapper;
import nl.tudelft.sem.users.api.UserApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class DeliveryProxyTest {

    @Spy
    @InjectMocks
    private DeliveryRemoteProxy deliveryRemoteProxy;

    private transient DeliveryApi mockDeliveryApi;

    private transient VendorApi mockVendorApi;

    private transient AdminApi mockAdminApi;

    /**
     * Sets up the mocks.
     */
    @BeforeEach
    public void setUp() {
        mockDeliveryApi = mock(DeliveryApi.class);
        mockVendorApi = mock(VendorApi.class);
        mockAdminApi = mock(AdminApi.class);
        deliveryRemoteProxy =
            new DeliveryRemoteProxy(mockVendorApi, mockAdminApi,
                mockDeliveryApi);
    }

    @Test
    void testGetRadii() throws ApiException {
        when(mockVendorApi.getDeliveryRadiuses(1L)).thenReturn(
            new ArrayList<>());

        assertTrue(deliveryRemoteProxy.getRadii(1L).isEmpty());
    }

    @Test
    void testGetAdminRad() throws ApiException {
        when(mockAdminApi.getCurrentDefaultRadius(21L)).thenReturn(
            new GetCurrentDefaultRadius200Response().radius(3331));

        assertEquals(3331, deliveryRemoteProxy.getAdminRadius(21L));
    }

    @Test
    void testGetForVendor() throws ApiException {
        when(mockVendorApi.getVendorDeliveryRadius(2L, 1L)).thenReturn(
            new GetVendorDeliveryRadius200Response().radius(3));
        when(mockVendorApi.getVendorDeliveryRadius(2L, 12L)).thenReturn(
            new GetVendorDeliveryRadius200Response().radius(13));

        assertEquals(3, deliveryRemoteProxy.getDeliveryRadius(1L, 2L));
        assertEquals(13, deliveryRemoteProxy.getDeliveryRadius(12L, 2L));
    }

    @Test
    void testGetDelivery() throws ApiException {
        when(mockDeliveryApi.getDeliveryFromOrder(2L, 13L)).thenReturn(
            new Delivery().deliveryId(222L));

        assertEquals(new Delivery().deliveryId(222L),
            deliveryRemoteProxy.getDelivery(2L, 13L));
    }

    @Test
    void testCreate() throws ApiException {
        deliveryRemoteProxy.newDelivery(1L, 2L, 3L);

        verify(mockDeliveryApi, times(1)).createDelivery(3L,
            new CreateDeliveryRequest().vendorId(1L).orderId(2L).customerId(3L));
    }

}
