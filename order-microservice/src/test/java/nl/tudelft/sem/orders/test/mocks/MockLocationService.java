package nl.tudelft.sem.orders.test.mocks;

import nl.tudelft.sem.orders.domain.GeoLocation;
import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.orders.ports.output.LocationService;

public class MockLocationService implements LocationService {
    @Override
    public GeoLocation getGeoLocation(Location location) {
        if (location.getCity().equals("a")) {
            return new GeoLocation(89, 40);
        }

        if (location.getCity().equals("c")) {
            return new GeoLocation(12, -40);
        }

        return new GeoLocation(1, 2);
    }
}
