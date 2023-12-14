package nl.tudelft.sem.orders.ports.output;

import nl.tudelft.sem.delivery.ApiException;
import nl.tudelft.sem.delivery.model.GetDeliveryRadiuses200ResponseInner;
import nl.tudelft.sem.users.model.UsersGetUserTypeIdGet200Response;

import java.util.List;

public interface DeliveryMicroservice {
    List<GetDeliveryRadiuses200ResponseInner> getRadii(long userId) throws ApiException;
}
