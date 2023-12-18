package nl.tudelft.sem.orders.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import nl.tudelft.sem.orders.adapters.UserMicroserviceAdapter;
import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.users.model.Customer;
import nl.tudelft.sem.users.model.UsersGetUserTypeIdGet200Response;
import nl.tudelft.sem.users.model.Vendor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

public class TestUserMicroserviceAdapter {

    private static final String API_URL = "http://localhost:8081";
    private UserMicroserviceAdapter userMicroserviceAdapter;
    private RestTemplate restTemplate;

    @BeforeEach
    public void setUp() {
        restTemplate = mock(RestTemplate.class);
        userMicroserviceAdapter = new UserMicroserviceAdapter(restTemplate);
    }

    @Test
    public void testGetCustomerAddress() {
        long customerId = 324L;
        Customer mockCustomer = new Customer();
        mockCustomer.setId(customerId);
        mockCustomer.setAddress(createMockLocation());

        when(restTemplate.getForObject(API_URL + "/users/" + customerId, Customer.class)).thenReturn(mockCustomer);

        Location result = userMicroserviceAdapter.getCustomerAddress(customerId);

        assertNotNull(mockCustomer.getAddress());
        assertLocationsEquals(mockCustomer.getAddress(), result);
    }

    @Test
    public void testGetVendorAddress() {
        long vendorId = 2L;
        Vendor mockVendor = new Vendor();
        mockVendor.setLocation(createMockLocation());

        when(restTemplate.getForObject(API_URL + "/users/" + vendorId, Vendor.class)).thenReturn(mockVendor);

        Location result = userMicroserviceAdapter.getVendorAddress(vendorId);

        assertLocationsEquals(mockVendor.getLocation(), result);
    }

    @Test
    public void testIsCustomer() {
        long customerId = 324L;
        UsersGetUserTypeIdGet200Response mockResponse = new UsersGetUserTypeIdGet200Response();
        mockResponse.setUserType(UsersGetUserTypeIdGet200Response.UserTypeEnum.CUSTOMER);

        when(restTemplate.getForObject(API_URL + "/users/getUserType/" + customerId,
            UsersGetUserTypeIdGet200Response.class)).thenReturn(mockResponse);

        boolean result = userMicroserviceAdapter.isCustomer(customerId);

        assertTrue(result);
    }

    @Test
    public void testIsCustomerThrowsException() {
        long customerId = 324L;

        when(restTemplate.getForObject(API_URL + "/users/getUserType/" + customerId,
            UsersGetUserTypeIdGet200Response.class)).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> userMicroserviceAdapter.isCustomer(customerId));
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
}
