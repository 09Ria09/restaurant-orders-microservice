package nl.tudelft.sem.orders.ports.output;

import java.util.List;
import nl.tudelft.sem.delivery.ApiException;
import nl.tudelft.sem.delivery.model.Delivery;
import nl.tudelft.sem.delivery.model.GetDeliveryRadiuses200ResponseInner;

public interface DeliveryMicroservice {
    List<GetDeliveryRadiuses200ResponseInner> getRadii(long userId)
        throws ApiException;

    Integer getAdminRadius(long userId) throws ApiException;

    Delivery getDelivery(long userId, long orderId) throws ApiException;

    long getDeliveryRadius(long vendorId, long userId) throws ApiException;

    void newDelivery(long vendorId, long orderId, long userId) throws ApiException;
}
