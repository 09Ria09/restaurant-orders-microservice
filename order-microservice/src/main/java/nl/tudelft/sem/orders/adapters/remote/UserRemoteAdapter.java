package nl.tudelft.sem.orders.adapters.remote;

import java.util.List;
import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import nl.tudelft.sem.users.ApiException;
import nl.tudelft.sem.users.api.UserApi;
import nl.tudelft.sem.users.api.VendorApi;
import nl.tudelft.sem.users.model.Customer;
import nl.tudelft.sem.users.model.UsersGetUserTypeIdGet200Response;
import nl.tudelft.sem.users.model.UsersIdGet200Response;
import nl.tudelft.sem.users.model.Vendor;

public class UserRemoteAdapter implements UserMicroservice {
    private final transient UserApi users;
    private final transient VendorApi vendors;


    public UserRemoteAdapter(UserApi users, VendorApi vendors) {
        this.users = users;
        this.vendors = vendors;
    }

    @Override
    public UsersGetUserTypeIdGet200Response.UserTypeEnum getUserType(long userId) throws ApiException {
        return users.usersGetUserTypeIdGet(userId).getUserType();
    }

    @Override
    public UsersIdGet200Response getUserById(long userId) throws ApiException {
        return users.usersIdGet(userId);
    }

    @Override
    public List<Vendor> getAllVendors() throws ApiException {
        return vendors.vendorsGet();
    }

    /**
     * Gets the home address of the customer.
     *
     * @param customerId the id of the customer
     */
    @Override
    public Location getCustomerAddress(long customerId) throws ApiException {
        Customer user = getUserById(customerId).getCustomer();
        nl.tudelft.sem.users.model.Location address = user.getAddress();
        return locationConverter(address);
    }

    /**
     * Gets the address of the vendor.
     *
     * @param vendorId the id of the vendor
     */
    @Override
    public Location getVendorAddress(long vendorId) throws ApiException {
        Vendor user = getUserById(vendorId).getVendor();
        nl.tudelft.sem.users.model.Location address = user.getLocation();
        return locationConverter(address);
    }

    private Location locationConverter(nl.tudelft.sem.users.model.Location address) {
        Location location = new Location();
        location.setCountry(address.getCountry());
        location.setCity(address.getCity());
        location.setAddress(address.getStreet() + ' ' + address.getStreetNumber());
        // location.setPostalCode(address.getPostalCode());
        location.setAdditionalRemarks(address.getAdditionalRemarks());
        return location;
    }

    /**
     * Gets the home address of the customer.
     *
     * @param customerId the id of the customer
     */
    @Override
    public boolean isCustomer(long customerId) throws ApiException {
        UsersGetUserTypeIdGet200Response.UserTypeEnum userType = getUserType(customerId);

        return UsersGetUserTypeIdGet200Response.UserTypeEnum.CUSTOMER.equals(userType);
    }

    /**
     * Checks if the user is a vendor.
     *
     * @param vendorId the id of the vendor
     * @return true if the user is a vendor, false otherwise
     * @throws ApiException if the user does not exist
     */
    @Override
    public boolean isVendor(long vendorId) throws ApiException {
        UsersGetUserTypeIdGet200Response.UserTypeEnum userType = getUserType(vendorId);

        return UsersGetUserTypeIdGet200Response.UserTypeEnum.VENDOR.equals(userType);
    }

}
