package nl.tudelft.sem.orders.ports.output;

import nl.tudelft.sem.orders.model.Location;

public interface UserMicroservice {
    Location getHomeAddress(long userId);
    boolean isCustomer(long userId);
}
