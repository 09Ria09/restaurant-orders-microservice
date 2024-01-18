package nl.tudelft.sem.orders.ring0.distance;

import java.util.List;
import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.orders.result.MalformedException;
import nl.tudelft.sem.users.model.Vendor;

public interface RadiusStrategy {
    List<Vendor> performRadiusCheck(Long userId, Location loc) throws MalformedException;
}