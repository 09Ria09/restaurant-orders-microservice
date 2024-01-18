package nl.tudelft.sem.orders.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import nl.tudelft.sem.orders.adapters.remote.UserRemoteProxy;
import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.orders.ring0.distance.LocationMapper;
import nl.tudelft.sem.users.ApiException;
import nl.tudelft.sem.users.api.UserApi;
import nl.tudelft.sem.users.api.VendorApi;
import nl.tudelft.sem.users.model.Courier;
import nl.tudelft.sem.users.model.Customer;
import nl.tudelft.sem.users.model.UsersGetUserTypeIdGet200Response;
import nl.tudelft.sem.users.model.UsersIdGet200Response;
import nl.tudelft.sem.users.model.Vendor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class UserProxyTest {

    @Spy
    @InjectMocks
    private UserRemoteProxy userRemoteAdapter;

    private transient UserApi mockUserApi;

    private transient VendorApi mockVendorApi;

    /**
     * Sets up the mocks.
     */
    @BeforeEach
    public void setUp() {
        mockUserApi = mock(UserApi.class);
        mockVendorApi = mock(VendorApi.class);
        userRemoteAdapter = new UserRemoteProxy(mockUserApi, mockVendorApi, new LocationMapper());
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetCustomerAddress() throws ApiException {
        long customerId = 324L;
        Customer mockCustomer = new Customer();
        mockCustomer.setId(customerId);
        mockCustomer.setAddress(createMockLocation());

        doReturn(new UsersIdGet200Response(mockCustomer)).when(userRemoteAdapter).getUserById(customerId);

        Location result = userRemoteAdapter.getCustomerAddress(customerId);

        assertLocationsEquals(mockCustomer.getAddress(), result);
    }

    @Test
    public void testGetCustomerAddressThrows() throws ApiException {
        long customerId = 324L;

        doThrow(new ApiException()).when(userRemoteAdapter).getUserById(customerId);

        assertThrows(ApiException.class, () -> userRemoteAdapter.getCustomerAddress(customerId));
    }

    @Test
    public void testGetVendorAddress() throws ApiException {
        long vendorId = 324L;
        Vendor mockVendor = new Vendor();
        mockVendor.setId(vendorId);
        mockVendor.setLocation(createMockLocation());

        doReturn(new UsersIdGet200Response(mockVendor)).when(userRemoteAdapter).getUserById(vendorId);

        Location result = userRemoteAdapter.getVendorAddress(vendorId);

        assertLocationsEquals(mockVendor.getLocation(), result);
    }

    @Test
    public void testGetVendorAddressThrows() throws ApiException {
        long vendorId = 324L;

        doThrow(new ApiException()).when(userRemoteAdapter).getUserById(vendorId);

        assertThrows(ApiException.class, () -> userRemoteAdapter.getVendorAddress(vendorId));
    }

    @Test
    public void testIsCustomer() throws ApiException {
        long customerId = 324L;

        doReturn(UsersGetUserTypeIdGet200Response.UserTypeEnum.CUSTOMER)
            .when(userRemoteAdapter).getUserType(customerId);

        boolean result = userRemoteAdapter.isCustomer(customerId);

        assertTrue(result);
    }

    @Test
    public void testIsNotCustomer() throws ApiException {
        long customerId = 324L;

        doReturn(UsersGetUserTypeIdGet200Response.UserTypeEnum.VENDOR).when(userRemoteAdapter).getUserType(customerId);

        boolean result = userRemoteAdapter.isCustomer(customerId);

        assertFalse(result);
    }

    @Test
    public void testIsCustomerThrowsException() throws ApiException {
        long customerId = 324L;

        doThrow(new ApiException()).when(userRemoteAdapter).getUserType(customerId);

        assertThrows(ApiException.class, () -> userRemoteAdapter.isCustomer(customerId));
    }

    private nl.tudelft.sem.users.model.Location createMockLocation() {
        nl.tudelft.sem.users.model.Location location = new nl.tudelft.sem.users.model.Location();
        location.setCountry("the Netherlands");
        location.setCity("Delft");
        location.setStreet("Mekelweg");
        location.setStreetNumber("5");
        location.setAdditionalRemarks("Inside the building.");
        return location;
    }

    private void assertLocationsEquals(nl.tudelft.sem.users.model.Location userLocation, Location orderLocation) {
        assertEquals(userLocation.getCountry(), orderLocation.getCountry());
        assertEquals(userLocation.getCity(), orderLocation.getCity());
        assertEquals(userLocation.getStreet() + ' ' + userLocation.getStreetNumber(), orderLocation.getAddress());
        assertEquals(userLocation.getAdditionalRemarks(), orderLocation.getAdditionalRemarks());
    }

    @Test
    void testGetCustomerAllergies() throws ApiException {
        long userId = 1L;
        List<String> expectedAllergies = Arrays.asList("allergy1", "allergy2");
        Customer customer = new Customer();
        customer.setAllergens(expectedAllergies);
        doReturn(new UsersIdGet200Response(customer)).when(userRemoteAdapter).getUserById(userId);

        List<String> result = userRemoteAdapter.getCustomerAllergies(userId);

        assertEquals(expectedAllergies, result);
    }

    @Test
    void testGetCustomerAllergiesThrowApiException() throws ApiException {
        long userId = 1L;
        doThrow(new ApiException()).when(userRemoteAdapter).getUserById(userId);

        assertThrows(ApiException.class, () -> userRemoteAdapter.getCustomerAllergies(userId));
    }

    @Test
    void testDoesUserExist() throws ApiException {
        when(mockUserApi.usersIdGet(37L)).thenReturn(new UsersIdGet200Response());
        when(mockUserApi.usersIdGet(38L)).thenThrow(new ApiException());

        assertTrue(userRemoteAdapter.doesUserExist(37L));
        assertFalse(userRemoteAdapter.doesUserExist(38L));
    }

    @Test
    void testGetAllVendors() throws ApiException {
        var vv = new ArrayList<Vendor>();
        vv.add(new Vendor().name("test"));

        when(mockVendorApi.vendorsGet()).thenReturn(vv);

        assertEquals(new Vendor().name("test"), userRemoteAdapter.getAllVendors().get(0));
    }

    @Test
    void testGetUserById() throws ApiException {
        var dis = new UsersIdGet200Response();
        dis.setActualInstance(new Customer().name("test"));

        when(mockUserApi.usersIdGet(13L)).thenReturn(dis);

        assertEquals(new Customer().name("test"), userRemoteAdapter.getUserById(13L).getActualInstance());
    }

    @Test
    void testIsVendor() throws ApiException {
        when(mockUserApi.usersGetUserTypeIdGet(22L)).thenReturn(new UsersGetUserTypeIdGet200Response().userType(
            UsersGetUserTypeIdGet200Response.UserTypeEnum.VENDOR));

        when(mockUserApi.usersGetUserTypeIdGet(23L)).thenReturn(new UsersGetUserTypeIdGet200Response().userType(
            UsersGetUserTypeIdGet200Response.UserTypeEnum.CUSTOMER));

        when(mockUserApi.usersGetUserTypeIdGet(11L)).thenReturn(new UsersGetUserTypeIdGet200Response().userType(
            UsersGetUserTypeIdGet200Response.UserTypeEnum.ADMIN));

        when(mockUserApi.usersGetUserTypeIdGet(13L)).thenReturn(new UsersGetUserTypeIdGet200Response().userType(
            UsersGetUserTypeIdGet200Response.UserTypeEnum.COURIER));

        assertTrue(userRemoteAdapter.isVendor(22L));
        assertFalse(userRemoteAdapter.isVendor(23L));
        assertTrue(userRemoteAdapter.isCourier(13L));
        assertFalse(userRemoteAdapter.isCourier(23L));
        assertTrue(userRemoteAdapter.isAdmin(11L));
        assertFalse(userRemoteAdapter.isAdmin(13L));
        assertTrue(userRemoteAdapter.isCustomer(23L));
        assertFalse(userRemoteAdapter.isCustomer(13L));
    }

}
