package nl.tudelft.sem.orders.adapters.mocks;

import java.util.Random;
import nl.tudelft.sem.orders.domain.GeoLocation;
import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.orders.ports.output.LocationService;

public class MockLocationAdapter implements LocationService {
    @Override
    public GeoLocation getGeoLocation(Location location) {
        return new GeoLocation(1, 2);
    }
}
