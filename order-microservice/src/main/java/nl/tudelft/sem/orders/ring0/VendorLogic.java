package nl.tudelft.sem.orders.ring0;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import nl.tudelft.sem.orders.domain.GeoLocation;
import nl.tudelft.sem.orders.model.Dish;
import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.ports.input.VendorLogicInterface;
import nl.tudelft.sem.orders.ports.output.DeliveryMicroservice;
import nl.tudelft.sem.orders.ports.output.DishDatabase;
import nl.tudelft.sem.orders.ports.output.LocationService;
import nl.tudelft.sem.orders.ports.output.OrderDatabase;
import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import nl.tudelft.sem.orders.result.ForbiddenException;
import nl.tudelft.sem.orders.result.MalformedException;
import nl.tudelft.sem.users.ApiException;
import nl.tudelft.sem.users.model.UsersGetUserTypeIdGet200Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class VendorLogic implements VendorLogicInterface {
    private transient OrderDatabase orderDatabase;
    private transient UserMicroservice userMicroservice;
    private transient DeliveryMicroservice deliveryMicroservice;
    private transient LocationService locationService;
    private transient DishDatabase dishDatabase;


    /**
     * Creates a new vendor logic.
     *
     * @param orderDatabase        The database output port.
     * @param userMicroservice     The output port for the user microservice.
     * @param deliveryMicroservice The output port for the delivery microservice.
     * @param locationService      The output port for the location service.
     */
    @Autowired
    public VendorLogic(OrderDatabase orderDatabase,
                       UserMicroservice userMicroservice,
                       DeliveryMicroservice deliveryMicroservice,
                       LocationService locationService,
                       DishDatabase dishDatabase) {
        this.orderDatabase = orderDatabase;
        this.userMicroservice = userMicroservice;
        this.deliveryMicroservice = deliveryMicroservice;
        this.locationService = locationService;
        this.dishDatabase = dishDatabase;
    }

    private Location mapLocations(
        nl.tudelft.sem.users.model.Location preLocation) {
        // TODO: definitely contact group c.
        Location location = new Location();
        location.setCity(preLocation.getCity());
        location.setCountry(preLocation.getCountry());
        location.setAdditionalRemarks(preLocation.getAdditionalRemarks());
        location.setPostalCode(preLocation.getStreetNumber());
        location.setAddress(preLocation.getStreet());

        return location;
    }

    private List<Long> performRadiusCheck(Long userId, Location loc)
        throws MalformedException {
        try {
            var distances = deliveryMicroservice.getRadii(userId);
            var allVendors = userMicroservice.getAllVendors();
            var defaultRad = deliveryMicroservice.getAdminRadius(userId);

            HashMap<Long, Integer> distanceMap = new HashMap<>();
            HashMap<Long, Location> locationMap = new HashMap<>();

            for (var d : allVendors) {
                distanceMap.put(d.getId(), defaultRad);
                locationMap.put(d.getId(), mapLocations(d.getLocation()));
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

            return result;
        } catch (Exception e) {
            throw new MalformedException();
        }
    }

    @Override
    public List<Long> vendorsInRadius(Long userId, String search,
                                      Location location)
        throws MalformedException {
        try {
            UsersGetUserTypeIdGet200Response.UserTypeEnum userType =
                userMicroservice.getUserType(userId);

            if (location == null) {
                // I have to use this ugly hack because people from
                // group c did not fix their specification
                // TODO: contact group c?

                location = mapLocations(
                    userMicroservice.getUserById(userId).getCustomer()
                        .getAddress());
            }

            // For now we will ignore search
            // TODO: implement fuzzy finding based on 'search'

            return performRadiusCheck(userId, location);
        } catch (Exception e) {
            throw new MalformedException();
        }
    }

    /**
     * Deletes a dish by its ID.
     *
     * @param userId The ID of the user who is attempting to delete the dish.
     * @param dishId The ID of the dish to be deleted.
     * @throws MalformedException If the dish or user does not exist.
     * @throws ForbiddenException If the user is not a vendor or does not own the dish.
     */
    @Override
    public void deleteDishById(Long userId, Long dishId)
        throws MalformedException, ForbiddenException {
        Dish dish = dishDatabase.getById(dishId);

        if (dish == null) {
            throw new MalformedException();
        }

        try {
            if (userMicroservice.isVendor(userId) && dish.getVendorID().equals(userId)) {
                dishDatabase.delete(dish);
            } else {
                throw new ForbiddenException();
            }
        } catch (ApiException e) {
            throw new MalformedException();
        }
    }

    /**
     * Gets all the orders at this vendor from the specific customer.
     *
     * @param userID The vendor's ID.
     * @param customerID The customer's ID.
     * @return List of the orders at this vendor by the specific customer.
     */
    @Override
    public List<Order> getPastOrdersForCustomer(Long userID, Long customerID) {
        List<Order> foundOrders = orderDatabase.findByVendorIDAndCustomerID(userID, customerID);
        return foundOrders;
    }
}
