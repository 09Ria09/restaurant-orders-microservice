package nl.tudelft.sem.orders.ring0.distance;

import nl.tudelft.sem.orders.model.Location;
import org.springframework.stereotype.Component;

@Component
public class LocationMapper {
    /**
     * This adapts the location type from the one used by group C to the
     * one used by us.
     *
     * @param preLocation The location in the user microservice type.
     * @return The location in the local type.
     */
    public Location mapLocations(nl.tudelft.sem.users.model.Location preLocation) {
        // TODO: definitely contact group c.
        Location location = new Location();
        location.setCity(preLocation.getCity());
        location.setCountry(preLocation.getCountry());
        location.setAdditionalRemarks(preLocation.getAdditionalRemarks());
        location.setPostalCode(preLocation.getStreetNumber());
        location.setAddress(preLocation.getStreet() + ' ' + preLocation.getStreetNumber());

        return location;
    }
}
