package nl.tudelft.sem.orders.ring0;

import nl.tudelft.sem.delivery.model.Delivery;
import nl.tudelft.sem.delivery.model.DeliveryTimes;
import nl.tudelft.sem.orders.model.AnalyticOrderVolumeInner;
import nl.tudelft.sem.orders.model.Dish;
import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.ports.output.DeliveryMicroservice;
import nl.tudelft.sem.orders.ports.output.OrderDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class VendorAnalyticsTest {

    DeliveryMicroservice deliveryMicroservice;
    OrderDatabase orderDatabase;
    VendorAnalytics vendorAnalytics;
    DeliveryTimes sunday = new DeliveryTimes();
    DeliveryTimes tuesday = new DeliveryTimes();
    Delivery sundayDelivery;

    Delivery tuesdayDelivery;

    @BeforeEach
    void setUp() {
        deliveryMicroservice = mock(DeliveryMicroservice.class);
        orderDatabase = mock(OrderDatabase.class);
        vendorAnalytics = new VendorAnalytics(orderDatabase, deliveryMicroservice);
    }

    @Test
    void analyseOrders() {
    }

    @Test
    void getCustomerPreferences() {
    }

    @Test
    void getPopularDishes() {
        Dish dish1 = new Dish();
        Dish dish2 = new Dish();
        Dish dish3 = new Dish();
    }

    @Test
    void getPeakHours() {
    }

    @Test
    void calculateOrderVolume() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        Date sundayDate = calendar.getTime();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        Date tuesdayDate = calendar.getTime();
        tuesday.setActualPickupTime(tuesdayDate);
        sunday.setActualPickupTime(sundayDate);

        sundayDelivery = new Delivery();
        sundayDelivery.setTimes(sunday);

        tuesdayDelivery = new Delivery();
        tuesdayDelivery.setTimes(tuesday);

        List<Delivery> deliveries = new ArrayList<>();
        deliveries.add(tuesdayDelivery);
        deliveries.add(sundayDelivery);

        List<AnalyticOrderVolumeInner> expected = new ArrayList<>();
        expected.add(new AnalyticOrderVolumeInner("Sunday", new BigDecimal(1)));
        expected.add(new AnalyticOrderVolumeInner("Monday", new BigDecimal(0)));
        expected.add(new AnalyticOrderVolumeInner("Tuesday", new BigDecimal(1)));
        expected.add(new AnalyticOrderVolumeInner("Wednesday", new BigDecimal(0)));
        expected.add(new AnalyticOrderVolumeInner("Thursday", new BigDecimal(0)));
        expected.add(new AnalyticOrderVolumeInner("Friday", new BigDecimal(0)));
        expected.add(new AnalyticOrderVolumeInner("Saturday", new BigDecimal(0)));

        assertEquals(expected, vendorAnalytics.calculateOrderVolume(deliveries));
    }
}