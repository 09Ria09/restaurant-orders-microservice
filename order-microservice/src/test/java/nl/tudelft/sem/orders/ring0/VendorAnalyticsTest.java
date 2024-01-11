package nl.tudelft.sem.orders.ring0;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import nl.tudelft.sem.delivery.model.Delivery;
import nl.tudelft.sem.delivery.model.DeliveryTimes;
import nl.tudelft.sem.orders.model.AnalyticCustomerPreferencesInner;
import nl.tudelft.sem.orders.model.AnalyticOrderVolumeInner;
import nl.tudelft.sem.orders.model.Dish;
import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.model.OrderDishesInner;
import nl.tudelft.sem.orders.ports.output.DeliveryMicroservice;
import nl.tudelft.sem.orders.ports.output.OrderDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class VendorAnalyticsTest {

    DeliveryMicroservice deliveryMicroservice;
    OrderDatabase orderDatabase;
    VendorAnalytics vendorAnalytics;

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
        Dish dish1 = new Dish();
        dish1.setDishID(1L);
        Dish dish2 = new Dish();
        dish2.setDishID(2L);
        Dish dish3 = new Dish();
        dish3.setDishID(3L);
        Dish dish4 = new Dish();
        dish4.setDishID(4L);


        List<OrderDishesInner> dishList1 = new ArrayList<>();
        List<OrderDishesInner> dishList2 = new ArrayList<>();
        OrderDishesInner dishes2 = new OrderDishesInner(dish2, 1);
        dishList1.add(dishes2);
        dishList2.add(dishes2);
        OrderDishesInner dishes4 = new OrderDishesInner(dish3, 3);
        dishList2.add(dishes4);
        List<OrderDishesInner> dishList3 = new ArrayList<>();
        OrderDishesInner dishes3 = new OrderDishesInner(dish3, 2);
        dishList3.add(dishes3);
        dishList3.add(dishes2);
        List<OrderDishesInner> dishList4 = new ArrayList<>();
        OrderDishesInner dishes1 = new OrderDishesInner(dish3, 1);
        dishList4.add(dishes1);
        OrderDishesInner dishes5 = new OrderDishesInner(dish4, 1);
        dishList4.add(dishes5);


        Order order1 = new Order();
        order1.setCustomerID(1L);
        order1.setDishes(dishList1);
        Order order2 = new Order();
        order2.setCustomerID(2L);
        order2.setDishes(dishList2);
        Order order3 = new Order();
        order3.setCustomerID(3L);
        order3.setDishes(dishList3);
        Order order4 = new Order();
        order4.setCustomerID(3L);
        order4.setDishes(dishList4);

        List<Order> input = new ArrayList<>();
        input.add(order1);
        input.add(order2);
        input.add(order3);
        input.add(order4);

        List<AnalyticCustomerPreferencesInner> expected = new ArrayList<>();
        expected.add(new AnalyticCustomerPreferencesInner(1L, 2L));
        expected.add(new AnalyticCustomerPreferencesInner(2L, 3L));
        expected.add(new AnalyticCustomerPreferencesInner(3L, 3L));

        assertEquals(expected, vendorAnalytics.getCustomerPreferences(input));
    }

    @Test
    void getPopularDishes() {
        Dish dish1 = new Dish();
        dish1.setDishID(1L);
        Dish dish2 = new Dish();
        dish2.setDishID(2L);
        Dish dish3 = new Dish();
        dish3.setDishID(3L);

        OrderDishesInner dishes1 = new OrderDishesInner(dish1, 10);
        List<OrderDishesInner> dishList1 = new ArrayList<>();
        dishList1.add(dishes1);
        OrderDishesInner dishes2 = new OrderDishesInner(dish2, 6);
        List<OrderDishesInner> dishList2 = new ArrayList<>();
        dishList2.add(dishes2);
        OrderDishesInner dishes3 = new OrderDishesInner(dish3, 6);
        List<OrderDishesInner> dishList3 = new ArrayList<>();
        dishList3.add(dishes3);
        OrderDishesInner dishes4 = new OrderDishesInner(dish3, 1);
        dishList2.add(dishes4);
        OrderDishesInner dishes5 = new OrderDishesInner(dish2, 2);
        dishList3.add(dishes5);

        Order order1 = new Order();
        order1.setDishes(dishList1);
        Order order2 = new Order();
        order2.setDishes(dishList2);
        Order order3 = new Order();
        order3.setDishes(dishList3);

        List<Order> input = new ArrayList<>();
        input.add(order1);
        input.add(order2);
        input.add(order3);

        List<Dish> expected = new ArrayList<>();
        expected.add(dish1);
        expected.add(dish2);

        assertEquals(expected, vendorAnalytics.getPopularDishes(input));
    }

    @Test
    void getPeakHours() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        Date twelve = calendar.getTime();
        DeliveryTimes twelveTime = new DeliveryTimes();
        twelveTime.setActualPickupTime(twelve);
        calendar.set(Calendar.HOUR_OF_DAY, 11);
        Date eleven = calendar.getTime();
        DeliveryTimes elevenTime = new DeliveryTimes();
        elevenTime.setActualPickupTime(eleven);
        calendar.set(Calendar.HOUR_OF_DAY, 19);
        Date nineteen = calendar.getTime();
        DeliveryTimes nineteenTime = new DeliveryTimes();
        nineteenTime.setActualPickupTime(nineteen);
        calendar.set(Calendar.HOUR_OF_DAY, 20);
        Date twenty = calendar.getTime();
        DeliveryTimes twentyTime = new DeliveryTimes();
        twentyTime.setActualPickupTime(twenty);

        List<Delivery> deliveries = new ArrayList<>();
        Delivery del11 = new Delivery();
        del11.setTimes(elevenTime);
        deliveries.add(del11);
        Delivery del12 = new Delivery();
        del12.setTimes(twelveTime);
        deliveries.add(del12);
        Delivery del122 = new Delivery();
        del122.setTimes(twelveTime);
        deliveries.add(del122);
        Delivery del19 = new Delivery();
        del19.setTimes(nineteenTime);
        deliveries.add(del19);
        Delivery del192 = new Delivery();
        del192.setTimes(nineteenTime);
        deliveries.add(del192);
        Delivery del193 = new Delivery();
        del193.setTimes(nineteenTime);
        deliveries.add(del193);
        Delivery del20 = new Delivery();
        del20.setTimes(twentyTime);
        deliveries.add(del20);
        Delivery del202 = new Delivery();
        del202.setTimes(twentyTime);
        deliveries.add(del202);
        Delivery del203 = new Delivery();
        del203.setTimes(twentyTime);
        deliveries.add(del203);
        Delivery del204 = new Delivery();
        del204.setTimes(twentyTime);
        deliveries.add(del204);

        List<Integer> actual = vendorAnalytics.getPeakHours(deliveries);
        assertEquals(20, actual.get(0));
        assertEquals(19, actual.get(1));
        assertEquals(12, actual.get(2));
        assertEquals(11, actual.get(3));
        assertEquals(23, actual.get(4));
        assertEquals(0, actual.get(23));
    }

    @Test
    void calculateOrderVolume() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        Date sundayDate = calendar.getTime();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        Date tuesdayDate = calendar.getTime();
        DeliveryTimes tuesday = new DeliveryTimes();
        tuesday.setActualPickupTime(tuesdayDate);
        DeliveryTimes sunday = new DeliveryTimes();
        sunday.setActualPickupTime(sundayDate);

        Delivery sundayDelivery;
        sundayDelivery = new Delivery();
        sundayDelivery.setTimes(sunday);

        Delivery tuesdayDelivery;
        tuesdayDelivery = new Delivery();
        tuesdayDelivery.setTimes(tuesday);

        List<Delivery> deliveries = new ArrayList<>();
        deliveries.add(tuesdayDelivery);
        deliveries.add(sundayDelivery);
        Delivery nullDelivery = new Delivery();
        deliveries.add(nullDelivery);

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