package nl.tudelft.sem.orders.ports.output;

import nl.tudelft.sem.orders.model.Location;

public interface UserMicroservice {
    Location getCustomerAddress(long customerId);

    Location getVendorAddress(long vendorId);

    boolean isCustomer(long userId);
}
