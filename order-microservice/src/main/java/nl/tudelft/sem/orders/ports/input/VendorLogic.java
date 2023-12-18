package nl.tudelft.sem.orders.ports.input;

import java.util.List;
import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.orders.result.MalformedException;

public interface VendorLogic {
    List<Long> vendorsInRadius(Long userId, String search, Location location)
        throws MalformedException;
}