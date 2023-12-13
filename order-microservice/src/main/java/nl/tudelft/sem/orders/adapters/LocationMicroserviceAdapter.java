package nl.tudelft.sem.orders.adapters;

import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.orders.ports.output.LocationMicroservice;

public class LocationMicroserviceAdapter implements LocationMicroservice {
    @Override
    public boolean isCloseBy(Location a, Location b) {
        return true;
    }
}
