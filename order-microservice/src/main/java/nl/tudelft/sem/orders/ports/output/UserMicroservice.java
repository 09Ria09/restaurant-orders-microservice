package nl.tudelft.sem.orders.ports.output;

import java.util.List;
import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.users.ApiException;
import nl.tudelft.sem.users.model.Vendor;

public interface UserMicroservice {
    List<Vendor> getAllVendors() throws ApiException;

    Location getCustomerAddress(long customerId) throws ApiException;

    Location getVendorAddress(long vendorId) throws ApiException;

    List<String> getCustomerAllergies(long userId) throws ApiException;

    boolean isCustomer(long userId) throws ApiException;

    boolean isVendor(long userId) throws ApiException;

    boolean isCourier(long userId) throws ApiException;

    boolean isAdmin(long userId) throws ApiException;

    boolean doesUserExist(long userId);
}
