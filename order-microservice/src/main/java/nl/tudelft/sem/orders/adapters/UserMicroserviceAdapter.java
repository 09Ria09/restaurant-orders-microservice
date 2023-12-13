package nl.tudelft.sem.orders.adapters;

import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import nl.tudelft.sem.users.model.Customer;
import nl.tudelft.sem.users.model.UsersGetUserTypeIdGet200Response;
import nl.tudelft.sem.users.model.Vendor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class UserMicroserviceAdapter implements UserMicroservice {

    private final transient RestTemplate restTemplate;
    private static final String apiUrl = "http://localhost:8081";

    public UserMicroserviceAdapter() {
        restTemplate = new RestTemplate();
    }

    /**
     * Gets the home address of the customer.
     *
     * @param customerId the id of the customer
     */
    public Location getCustomerAddress(long customerId) {
        Customer user =
            restTemplate.getForObject(apiUrl + "/users/" + customerId,
                Customer.class);
        Location location = new Location();
        if (user != null) {
            nl.tudelft.sem.users.model.Location address = user.getAddress();
            if (address != null) {
                location.setCountry(address.getCountry());
                location.setCity(address.getCity());
                location.setAddress(
                    address.getStreet() + ' ' + address.getStreetNumber());
                // location.setPostalCode(address.getPostalCode());
                location.setAdditionalRemarks(address.getAdditionalRemarks());
            }
        }
        return location;
    }


    /**
     * Gets the address of the vendor.
     *
     * @param vendorId the id of the vendor
     */
    public Location getVendorAddress(long vendorId) {
        Vendor user = restTemplate.getForObject(apiUrl + "/users/" + vendorId,
            Vendor.class);
        Location location = new Location();
        if (user != null) {
            nl.tudelft.sem.users.model.Location address = user.getLocation();
            location.setCountry(address.getCountry());
            location.setCity(address.getCity());
            location.setAddress(
                address.getStreet() + ' ' + address.getStreetNumber());
            // location.setPostalCode(address.getPostalCode());
            location.setAdditionalRemarks(address.getAdditionalRemarks());
        }
        return location;
    }


    /**
     * Gets the home address of the customer.
     *
     * @param customerId the id of the customer
     */
    public boolean isCustomer(long customerId) throws RestClientException {
        UsersGetUserTypeIdGet200Response userType = restTemplate.getForObject(
            apiUrl + "/users/getUserType/" + customerId,
            UsersGetUserTypeIdGet200Response.class);
        if (userType == null || userType.getUserType() == null) {
            throw new IllegalStateException();
        }
        return userType.getUserType()
            .equals(UsersGetUserTypeIdGet200Response.UserTypeEnum.CUSTOMER);
    }
}
