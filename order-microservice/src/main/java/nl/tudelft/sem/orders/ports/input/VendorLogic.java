package nl.tudelft.sem.orders.ports.input;

import nl.tudelft.sem.orders.model.Location;

public interface VendorLogic {
    void vendorsInRadius(long userId, Location location);
}