package nl.tudelft.sem.orders.ports.output;

import nl.tudelft.sem.users.model.Location;

public interface LocationMicroservice {
    public boolean isCloseBy(Location a, Location b);
}
