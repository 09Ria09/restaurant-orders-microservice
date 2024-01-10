package nl.tudelft.sem.orders.ring0;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import nl.tudelft.sem.delivery.model.Delivery;
import nl.tudelft.sem.orders.model.Analytic;
import nl.tudelft.sem.orders.model.AnalyticCustomerPreferencesInner;
import nl.tudelft.sem.orders.model.AnalyticOrderVolumeInner;
import nl.tudelft.sem.orders.model.Dish;
import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.model.OrderDishesInner;
import nl.tudelft.sem.orders.ports.output.DeliveryMicroservice;
import nl.tudelft.sem.orders.ports.output.OrderDatabase;
import org.springframework.stereotype.Component;

@Component
public class VendorAnalytics {

    private transient OrderDatabase orderDatabase;
    private transient DeliveryMicroservice deliveryMicroservice;

    // This number allows us to filter the popular dishes in a restaurant
    // based on how much they get ordered in comparison to the most ordered dish.
    private static double popularThreshold = 0.8;

    private class HourDeliveries {
        public int hour;
        public int deliveries;

        public HourDeliveries(int hour, int deliveries) {
            this.hour = hour;
            this.deliveries = deliveries;
        }
    }

    public VendorAnalytics(OrderDatabase orderDatabase,
                           DeliveryMicroservice deliveryMicroservice) {
        this.orderDatabase = orderDatabase;
        this.deliveryMicroservice = deliveryMicroservice;
    }

    /**
     * General analysis method.
     *
     * @param vendorID ID of the vendor.
     * @return The analytic of the vendor.
     */
    public List<Analytic> analyseOrders(Long vendorID) {
        List<Order> allOrders = orderDatabase.findByVendorID(vendorID);
        List<Delivery> deliveries = new ArrayList<>();
        for (Order order : allOrders) {
            deliveries.add(deliveryMicroservice.getDelivery(vendorID, order.getOrderID()));
        }

        Analytic analytic = new Analytic();
        analytic.setOrderVolume(calculateOrderVolume(deliveries));
        analytic.setPeakOrderingHours(getPeakHours(deliveries));
        analytic.setPopularItems(getPopularDishes(allOrders));
        analytic.setCustomerPreferences(getCustomerPreferences(allOrders));
        List<Analytic> analytics = new ArrayList<>();
        analytics.add(analytic);
        return analytics;
    }

    /**
     * Getting customer preferences.
     *
     * @param allOrders The orders
     * @return The most liked dish of each customer
     */
    public List<AnalyticCustomerPreferencesInner> getCustomerPreferences(List<Order> allOrders) {
        HashMap<Long, HashMap<Long, Integer>> dishesPerCustomer = new HashMap<>();
        for (Order o : allOrders) {
            putOrderInHashMap(dishesPerCustomer, o);
        }
        List<AnalyticCustomerPreferencesInner> customerPreferences = new ArrayList<>();
        for (Long customerID : dishesPerCustomer.keySet()) {
            HashMap<Long, Integer> customerDishes = dishesPerCustomer.get(customerID);
            int maxAmount = Collections.max(customerDishes.values());
            for (Map.Entry<Long, Integer> dishAmount : customerDishes.entrySet()) {
                if (dishAmount.getValue() == maxAmount) {
                    customerPreferences.add(new AnalyticCustomerPreferencesInner(customerID, dishAmount.getKey()));
                    break;
                }
            }
        }
        return customerPreferences;
    }

    private static void putOrderInHashMap(HashMap<Long, HashMap<Long, Integer>> dishesPerCustomer, Order o) {
        Long customerID = o.getCustomerID();
        List<OrderDishesInner> dishesInners = o.getDishes();

        if (!dishesPerCustomer.containsKey(customerID)) {
            dishesPerCustomer.put(customerID, new HashMap<>());
            for (OrderDishesInner dish : dishesInners) {
                dishesPerCustomer.get(customerID).put(dish.getDish().getDishID(), dish.getAmount());
            }
        } else {

            for (OrderDishesInner dish : dishesInners) {
                Long dishID = dish.getDish().getDishID();
                if (dishesPerCustomer.get(customerID).containsKey(dishID)) {
                    Integer curAmount = dishesPerCustomer.get(customerID).get(dishID);
                    dishesPerCustomer.get(customerID).replace(dishID, dish.getAmount() + curAmount);
                } else {
                    dishesPerCustomer.get(customerID).put(dish.getDish().getDishID(), dish.getAmount());
                }
            }
        }
    }

    /**
     * Get the most popular dishes at this restaurant.
     *
     * @param allOrders The orders at this restaurant
     * @return List of popular distance
     */
    public List<Dish> getPopularDishes(List<Order> allOrders) {
        List<OrderDishesInner> dishesInners = allOrders.stream()
                .flatMap(x -> x.getDishes().stream())
                .collect(Collectors.toList());

        HashMap<Dish, Integer> dishAmounts = new HashMap<>();

        for (OrderDishesInner d : dishesInners) {
            Dish dish = d.getDish();
            Integer amount = d.getAmount();
            if (!dishAmounts.containsKey(dish)) {
                dishAmounts.put(dish, amount);
            } else {
                Integer oldAmount = dishAmounts.get(dish);
                dishAmounts.replace(dish, oldAmount + amount);
            }
        }

        double maxAmount = (double) Collections.max(dishAmounts.values());
        List<Dish> popularDishes = new ArrayList<>();

        for (Map.Entry<Dish, Integer> entry : dishAmounts.entrySet()) {
            if (entry.getValue() >= popularThreshold * maxAmount) {
                popularDishes.add(entry.getKey());
            }
        }

        return popularDishes;
    }

    /**
     * Get the peak hours for this retaurant.
     *
     * @param deliveries The deliveries from this restaurant
     * @return List of hours sorted by intensity of orders
     */
    public List<Integer> getPeakHours(List<Delivery> deliveries) {
        List<Integer> peakHours = new ArrayList<>();
        HourDeliveries[] hours = new HourDeliveries[24];

        for (int i = 0; i < 24; i++) {
            hours[i] = new HourDeliveries(i, 0);
        }

        for (Delivery d : deliveries) {
            hours[d.getTimes().getActualPickupTime().getHours()].deliveries++;
        }
        Arrays.sort(hours, (h1, h2) -> {
            return h1.deliveries - h2.deliveries;
        });

        for (int i = 0; i < 24; i++) {
            peakHours.add(i, hours[i].hour);
        }

        return peakHours;
    }

    /**
     * Calculate the orders per day.
     *
     * @param deliveries Deliveries made
     * @return Volume of orders
     */
    public List<AnalyticOrderVolumeInner> calculateOrderVolume(List<Delivery> deliveries) {
        List<AnalyticOrderVolumeInner> orderVolumes = new ArrayList<>();
        int[] days = new int[7];

        for (Delivery d : deliveries) {
            if (d.getTimes() == null) {
                continue;
            }
            days[d.getTimes().getActualPickupTime().getDay()]++;
        }
        for (int i = 0; i < 7; i++) {
            orderVolumes.add(new AnalyticOrderVolumeInner(intToDay(i), new BigDecimal(days[i])));
        }
        return orderVolumes;
    }

    private String intToDay(int i) {
        switch (i) {
            case 0 -> {
                return "Sunday";
            }
            case 1 -> {
                return "Monday";
            }
            case 2 -> {
                return "Tuesday";
            }
            case 3 -> {
                return "Wednesday";
            }
            case 4 -> {
                return "Thursday";
            }
            case 5 -> {
                return "Friday";
            }
            default -> {
                return "Saturday";
            }
        }
    }
}
