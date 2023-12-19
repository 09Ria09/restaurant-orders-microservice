package nl.tudelft.sem.orders.adapters.remote;

import java.util.List;
import nl.tudelft.sem.delivery.ApiException;
import nl.tudelft.sem.delivery.api.AdminApi;
import nl.tudelft.sem.delivery.api.VendorApi;
import nl.tudelft.sem.delivery.model.GetDeliveryRadiuses200ResponseInner;
import nl.tudelft.sem.orders.ports.output.DeliveryMicroservice;
import org.springframework.beans.factory.annotation.Autowired;

public class DeliveryRemoteAdapter implements DeliveryMicroservice {
    private transient VendorApi vendorApi;
    private transient AdminApi adminApi;

    public DeliveryRemoteAdapter(VendorApi deliveryApi, AdminApi adminApi) {
        this.vendorApi = deliveryApi;
        this.adminApi = adminApi;
    }

    @Override
    public List<GetDeliveryRadiuses200ResponseInner> getRadii(long userId)
        throws ApiException {
        // TODO: ask group b why this endpoint requires a user id??
        return vendorApi.getDeliveryRadiuses(userId);
    }

    @Override
    public Integer getAdminRadius(long userId) throws ApiException {
        // TODO: ask group b why this endpoint requires a user id??
        return adminApi.getCurrentDefaultRadius(userId).getRadius();
    }
}
