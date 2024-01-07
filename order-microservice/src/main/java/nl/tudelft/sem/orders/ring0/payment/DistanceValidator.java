package nl.tudelft.sem.orders.ring0.payment;

import nl.tudelft.sem.orders.domain.GeoLocation;
import nl.tudelft.sem.orders.ports.output.DeliveryMicroservice;
import nl.tudelft.sem.orders.ports.output.LocationService;
import nl.tudelft.sem.orders.ports.output.OrderDatabase;
import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import nl.tudelft.sem.orders.result.VerificationException;
import nl.tudelft.sem.users.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DistanceValidator extends BaseHandler {
    private final transient LocationService locationService;
    private final transient UserMicroservice userMicroservice;
    private final transient OrderDatabase orderDatabase;
    private final transient DeliveryMicroservice deliveryMicroservice;

    @Autowired
    DistanceValidator(LocationService locationService,
                             UserMicroservice userMicroservice,
                             OrderDatabase orderDatabase,
                             DeliveryMicroservice deliveryMicroservice) {
        this.locationService = locationService;
        this.userMicroservice = userMicroservice;
        this.orderDatabase = orderDatabase;
        this.deliveryMicroservice = deliveryMicroservice;
    }

    @Override
    public void verify(Payment payment) throws VerificationException {
        try {
            var order = orderDatabase.getById(payment.getOrderId());

            GeoLocation customer =
                locationService.getGeoLocation(order.getLocation());

            long vendorId = order.getVendorID();

            GeoLocation vendor = locationService.getGeoLocation(
                userMicroservice.getVendorAddress(
                    vendorId));

            long maxDistance = deliveryMicroservice.getDeliveryRadius(vendorId,
                payment.getUserId());

            if (vendor.distanceTo(customer) > maxDistance) {
                throw new VerificationException(
                    "Cannot place order this customer is too far from the vendor.");
            }
        } catch (Exception e) {
            throw new VerificationException("Could not check distances, delivery microservice returned error");
        }

        super.checkNext(payment);
    }
}
