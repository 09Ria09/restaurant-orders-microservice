package nl.tudelft.sem.orders.test.mocks;

import java.util.ArrayList;
import java.util.List;
import nl.tudelft.sem.delivery.ApiException;
import nl.tudelft.sem.delivery.model.Delivery;
import nl.tudelft.sem.delivery.model.GetDeliveryRadiuses200ResponseInner;
import nl.tudelft.sem.orders.ports.output.DeliveryMicroservice;

public class MockDeliveryMicroservice implements DeliveryMicroservice {
    private boolean failRadius;

    public boolean isFailNew() {
        return failNew;
    }

    public void setFailNew(boolean failNew) {
        this.failNew = failNew;
    }

    private boolean failNew;


    @Override
    public List<GetDeliveryRadiuses200ResponseInner> getRadii(long userId)
        throws ApiException {
        List<GetDeliveryRadiuses200ResponseInner> res = new ArrayList<>();
        res.add(
            new GetDeliveryRadiuses200ResponseInner().radius(4).vendorID(3L));

        return res;
    }

    @Override
    public Integer getAdminRadius(long userId) throws ApiException {
        if (failRadius) {
            throw new ApiException();
        }

        return 0;
    }

    @Override
    public Delivery getDelivery(long userID, long orderID) {
        return null;
    }

    /**
     * Get the global delivery radius.
     */
    public long getDeliveryRadius(long vendorId, long userId)
        throws ApiException {
        if (failRadius) {
            throw new ApiException();
        }

        return 5;
    }

    @Override
    public void newDelivery(long vendorId, long orderId, long userId)
        throws ApiException {
        if (failNew) {
            throw new ApiException();
        }
    }

    public boolean isFailRadius() {
        return failRadius;
    }

    public void setFailRadius(boolean failRadius) {
        this.failRadius = failRadius;
    }
}
