package nl.tudelft.sem.orders.adapters.remote;

import java.util.List;
import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import nl.tudelft.sem.orders.ring0.distance.LocationMapper;
import nl.tudelft.sem.users.ApiException;
import nl.tudelft.sem.users.api.UserApi;
import nl.tudelft.sem.users.api.VendorApi;
import nl.tudelft.sem.users.model.Customer;
import nl.tudelft.sem.users.model.UsersGetUserTypeIdGet200Response;
import nl.tudelft.sem.users.model.UsersIdGet200Response;
import nl.tudelft.sem.users.model.Vendor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

//@Component
public class UserRemoteProxy implements UserMicroservice {
    private final transient UserApi users;
    private final transient VendorApi vendors;
    private final transient LocationMapper locationMapper;


    /**
     * Create a new remote proxy to the user microservice.
     *
     * @param users The users api.
     * @param vendors The vendors api.
     * @param locationMapper The location mapper.
     */
    @Autowired
    public UserRemoteProxy(UserApi users, VendorApi vendors, LocationMapper locationMapper) {
        this.users = users;
        this.locationMapper = locationMapper;
        this.vendors = vendors;
    }

    public UsersGetUserTypeIdGet200Response.UserTypeEnum getUserType(long userId) throws ApiException {
        return users.usersGetUserTypeIdGet(userId).getUserType();
    }

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
        return locationMapper.mapLocations(address);
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
        return locationMapper.mapLocations(address);
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

    /**
     * Checks if the user is a courier.
     *
     * @param courierId the id of the vendor
     * @return true if the user is a vendor, false otherwise
     * @throws ApiException if the user does not exist
     */
    @Override
    public boolean isCourier(long courierId) throws ApiException {
        UsersGetUserTypeIdGet200Response.UserTypeEnum userType = getUserType(courierId);

        return UsersGetUserTypeIdGet200Response.UserTypeEnum.COURIER.equals(userType);
    }

    /**
     * Checks if the user is an admin.
     *
     * @param adminId the id of the vendor
     * @return true if the user is a vendor, false otherwise
     * @throws ApiException if the user does not exist
     */
    @Override
    public boolean isAdmin(long adminId) throws ApiException {
        UsersGetUserTypeIdGet200Response.UserTypeEnum userType = getUserType(adminId);

        return UsersGetUserTypeIdGet200Response.UserTypeEnum.ADMIN.equals(userType);
    }

    /**
     * Returns true iff the user exists.
     *
     * @param userId The userId to check.
     * @return True iff the user exists.
     */
    @Override
    public boolean doesUserExist(long userId) {
        try {
            getUserById(userId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
