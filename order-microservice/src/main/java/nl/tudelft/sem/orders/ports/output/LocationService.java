package nl.tudelft.sem.orders.ports.output;

import nl.tudelft.sem.orders.domain.GeoLocation;
import nl.tudelft.sem.orders.model.Location;

public interface LocationService {
    GeoLocation getGeoLocation(Location location);
}
