package nl.tudelft.sem.orders.ring0;

import nl.tudelft.sem.delivery.ApiException;
import nl.tudelft.sem.orders.domain.GeoLocation;
import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.ports.input.OrderLogic;
import nl.tudelft.sem.orders.ports.input.VendorLogic;
import nl.tudelft.sem.orders.ports.output.*;
import nl.tudelft.sem.orders.result.ForbiddenException;
import nl.tudelft.sem.orders.result.MalformedException;
import nl.tudelft.sem.users.model.UsersGetUserTypeIdGet200Response;
import nl.tudelft.sem.users.model.Vendor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class VendorFacade implements VendorLogic {
    private transient OrderDatabase orderDatabase;
    private transient UserMicroservice userMicroservice;
    private transient PaymentService paymentService;
    private transient DeliveryMicroservice deliveryMicroservice;
    private transient LocationService locationService;


    @Autowired
    public VendorFacade(OrderDatabase orderDatabase, UserMicroservice userMicroservice,
                        PaymentService paymentService, DeliveryMicroservice deliveryMicroservice,
                        LocationService locationService) {
        this.orderDatabase = orderDatabase;
        this.userMicroservice = userMicroservice;
        this.paymentService = paymentService;
        this.deliveryMicroservice = deliveryMicroservice;
        this.locationService = locationService;
    }

    private Location mapLocations(nl.tudelft.sem.users.model.Location preLocation) {
        // TODO: definitely contact group c.
        Location location = new Location();
        location.setCity(preLocation.getCity());
        location.setCountry(preLocation.getCountry());
        location.setAdditionalRemarks(preLocation.getAdditionalRemarks());
        location.setPostalCode(preLocation.getStreetNumber());
        location.setAddress(preLocation.getStreet());

        return location;
    }

    private List<Long> performRadiusCheck(Long userId, Location loc) throws MalformedException {
        try {
            var distances = deliveryMicroservice.getRadii(userId);

            // This algorithm is very simple and it takes a linear pass over all vendors
            // we could make this more efficient.

            GeoLocation userGeoLocation = locationService.getGeoLocation(loc);
            List<Long> result = new ArrayList<>();

            for(var distance : distances) {
                Location vendorLocation =
                        mapLocations(userMicroservice.getUserById(distance.getVendorID()).getVendor().getLocation());

                GeoLocation vendorGeoLocation = locationService.getGeoLocation(vendorLocation);

                if(userGeoLocation.distanceTo(vendorGeoLocation) <= distance.getRadius()) {
                    result.add(distance.getVendorID());
                }
            }

            return result;
        } catch (Exception _e) {
            throw new MalformedException();
        }
    }

    @Override
    public List<Long> vendorsInRadius(Long userId, String search, Location location) throws MalformedException {
        try {
            UsersGetUserTypeIdGet200Response.UserTypeEnum userType = userMicroservice.getUserType(userId);

            if(location == null) {
                // I have to use this ugly hack because people from group c did not fix their specification
                // TODO: contact group c?

                location = mapLocations(userMicroservice.getUserById(userId).getCustomer().getAddress());
            }

            List<Long> vendors =
        } catch (Exception _e) {
            throw new MalformedException();
        }
    }
}
