package nl.tudelft.sem.orders.adapters.mocks;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import nl.tudelft.sem.delivery.ApiException;
import nl.tudelft.sem.delivery.model.Delivery;
import nl.tudelft.sem.delivery.model.DeliveryTimes;
import nl.tudelft.sem.delivery.model.GetDeliveryRadiuses200ResponseInner;
import nl.tudelft.sem.orders.ports.output.DeliveryMicroservice;

public class MockDeliveryMicroservice implements DeliveryMicroservice {
    @Override
    public List<GetDeliveryRadiuses200ResponseInner> getRadii(long userId)
        throws ApiException {
        GetDeliveryRadiuses200ResponseInner radius = new GetDeliveryRadiuses200ResponseInner();
        radius.setVendorID(2L);
        radius.setRadius(5);
        return new ArrayList<>(List.of(radius));
    }

    @Override
    public Integer getAdminRadius(long userId) throws ApiException {
        return 5;
    }

    @Override
    public long getDeliveryRadius(long vendorId, long userId)
        throws ApiException {
        return 5;
    }

    @Override
    public void newDelivery(long vendorId, long orderId, long userId)
        throws ApiException {
        return;
    }

    @Override
    public Delivery getDelivery(long userId, long orderId) {
        Delivery delivery = new Delivery();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        Date twelve = calendar.getTime();
        DeliveryTimes twelveTime = new DeliveryTimes();
        twelveTime.setActualPickupTime(twelve);

        delivery.setDeliveryId(orderId);
        delivery.setTimes(twelveTime);
        return delivery;
    }

}
