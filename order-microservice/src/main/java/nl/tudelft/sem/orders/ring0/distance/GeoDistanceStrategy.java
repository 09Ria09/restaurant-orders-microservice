package nl.tudelft.sem.orders.ring0.distance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import nl.tudelft.sem.orders.domain.GeoLocation;
import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.orders.ports.output.DeliveryMicroservice;
import nl.tudelft.sem.orders.ports.output.LocationService;
import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import nl.tudelft.sem.orders.result.MalformedException;
import nl.tudelft.sem.users.model.Vendor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GeoDistanceStrategy implements RadiusStrategy {
    private transient DeliveryMicroservice deliveryMicroservice;
    private transient UserMicroservice userMicroservice;
    private transient LocationService locationService;
    private transient LocationMapper locationMapper;

    /**
     * Create a new GeoDistance strategy.
     *
     * @param deliveryMicroservice The delivery microservice.
     * @param userMicroservice     The user microservice.
     * @param locationService      The location service.
     * @param locationMapper       The location mapper.
     */
    @Autowired
    public GeoDistanceStrategy(DeliveryMicroservice deliveryMicroservice,
                               UserMicroservice userMicroservice,
                               LocationService locationService,
                               LocationMapper locationMapper) {
        this.deliveryMicroservice = deliveryMicroservice;
        this.userMicroservice = userMicroservice;
        this.locationService = locationService;
        this.locationMapper = locationMapper;
    }

    /**
     * This performs a radius check based on distances between earth coordinates.
     *
     * @param userId The userid of the user.
     * @param loc    The location to which the user wants the order, cannot be null.
     * @return The list of restaurants in range of that location.
     * @throws MalformedException Thrown if the user doesn't exist or some other error with microservices arises.
     */
    @Override
    public List<Vendor> performRadiusCheck(Long userId, Location loc)
        throws MalformedException {
        try {
            var distances = deliveryMicroservice.getRadii(userId);
            var allVendors = userMicroservice.getAllVendors();
            var defaultRad = deliveryMicroservice.getAdminRadius(userId);

            HashMap<Long, Integer> distanceMap = new HashMap<>();
            HashMap<Long, Location> locationMap = new HashMap<>();

            for (var d : allVendors) {
                distanceMap.put(d.getId(), defaultRad);
                locationMap.put(d.getId(), locationMapper.mapLocations(d.getLocation()));
            }

            for (var d : distances) {
                distanceMap.put(d.getVendorID(), d.getRadius());
            }

            // This algorithm is very simple, and it takes a linear pass over all vendors
            // we could make this more efficient.

            GeoLocation userGeoLocation = locationService.getGeoLocation(loc);
            List<Long> result = new ArrayList<>();

            for (var pair : distanceMap.entrySet()) {
                Location vendorLocation = locationMap.get(pair.getKey());
                GeoLocation vendorGeoLocation =
                    locationService.getGeoLocation(vendorLocation);

                if (userGeoLocation.distanceTo(vendorGeoLocation)
                    <= pair.getValue()) {
                    result.add(pair.getKey());
                }
            }

            return allVendors.stream()
                .filter(vendor -> result.contains(vendor.getId()))
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new MalformedException();
        }
    }
}
