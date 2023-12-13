package nl.tudelft.sem.orders.ports.output;

import nl.tudelft.sem.orders.model.Location;

public interface LocationMicroservice {
    public boolean isCloseBy(Location a, Location b);
}
