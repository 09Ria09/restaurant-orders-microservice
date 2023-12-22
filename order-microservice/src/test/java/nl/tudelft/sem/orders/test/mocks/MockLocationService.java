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

        return new GeoLocation(1, 2);
    }

    @Override
    public boolean isCloseBy(Location a, Location b) {
        return true;
    }
}
