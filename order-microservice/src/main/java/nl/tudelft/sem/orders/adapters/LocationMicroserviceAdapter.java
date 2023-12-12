package nl.tudelft.sem.orders.adapters;

import nl.tudelft.sem.orders.ports.output.LocationMicroservice;
import nl.tudelft.sem.users.model.Location;

public class LocationMicroserviceAdapter implements LocationMicroservice {
    @Override
    public boolean isCloseBy(Location a, Location b){
        return true;
    }
}
