package nl.tudelft.sem.orders.adapters;

import nl.tudelft.sem.delivery.ApiException;
import nl.tudelft.sem.delivery.api.VendorApi;
import nl.tudelft.sem.delivery.model.GetDeliveryRadiuses200ResponseInner;
import nl.tudelft.sem.orders.ports.output.DeliveryMicroservice;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class DeliveryRemoteAdapter implements DeliveryMicroservice {
    VendorApi vendorApi;

    @Autowired
    DeliveryRemoteAdapter(VendorApi deliveryApi) {this.vendorApi = deliveryApi;}

    @Override
    public List<GetDeliveryRadiuses200ResponseInner> getRadii(long userId) throws ApiException {
        // TODO: ask group b why this endpoint requires a user id??
        return vendorApi.getDeliveryRadiuses(userId);
    }
}
