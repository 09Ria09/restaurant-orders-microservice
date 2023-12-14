package nl.tudelft.sem.orders.ports.input;

import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.orders.result.MalformedException;

import java.util.List;

public interface VendorLogic {
    List<Long> vendorsInRadius(Long userId, String search, Location location) throws MalformedException;
}