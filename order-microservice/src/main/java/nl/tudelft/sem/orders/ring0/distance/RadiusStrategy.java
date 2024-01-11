package nl.tudelft.sem.orders.ring0.distance;

import java.util.List;
import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.orders.result.MalformedException;

public interface RadiusStrategy {
    List<Long> performRadiusCheck(Long userId, Location loc) throws MalformedException;
}