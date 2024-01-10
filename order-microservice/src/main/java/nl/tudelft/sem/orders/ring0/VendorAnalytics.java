package nl.tudelft.sem.orders.ring0;

import nl.tudelft.sem.delivery.model.Delivery;
import nl.tudelft.sem.orders.model.*;
import nl.tudelft.sem.orders.ports.output.DeliveryMicroservice;
import nl.tudelft.sem.orders.ports.output.OrderDatabase;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class VendorAnalytics {

    private transient OrderDatabase orderDatabase;
    private transient DeliveryMicroservice deliveryMicroservice;

    // This number allows us to filter the popular dishes in a restaurant
    // based on how much they get ordered in comparison to the most ordered dish.
    private static double popularThreshold = 0.8;

    private class hourDeliveries {
        public int hour;
        public int deliveries;

        public hourDeliveries(int hour, int deliveries) {
            this.hour = hour;
            this.deliveries = deliveries;
        }
    }

    public VendorAnalytics(OrderDatabase orderDatabase,
                           DeliveryMicroservice deliveryMicroservice) {
        this.orderDatabase = orderDatabase;
        this.deliveryMicroservice = deliveryMicroservice;
    }

    public List<Analytic> analyseOrders(Long vendorID) {
        List<Order> allOrders = orderDatabase.findByVendorID(vendorID);
        List<Delivery> deliveries = new ArrayList<>();
        for (Order order : allOrders){
            deliveries.add(deliveryMicroservice.getDelivery(vendorID,order.getOrderID()));
        }
        List<Analytic> analytics = new ArrayList<>();
        Analytic analytic = new Analytic();
        analytic.setOrderVolume(calculateOrderVolume(deliveries));
        analytic.setPeakOrderingHours(getPeakHours(deliveries));
        analytic.setPopularItems(getPopularDishes(allOrders));
        analytic.setCustomerPreferences(getCustomerPreferences(allOrders));
        return analytics;
    }

    public List<AnalyticCustomerPreferencesInner> getCustomerPreferences(List<Order> allOrders) {
        HashMap<Long, HashMap<Long, Integer>> dishesPerCustomer = new HashMap<>();
        for(Order o : allOrders){
            Long customerID = o.getCustomerID();
            List<OrderDishesInner> dishesInners = o.getDishes();

            if(!dishesPerCustomer.containsKey(customerID)){
                dishesPerCustomer.put(customerID, new HashMap<>());
                for(OrderDishesInner dish : dishesInners){
                    dishesPerCustomer.get(customerID).put(dish.getDish().getDishID(), dish.getAmount());
                }
            } else{

                for(OrderDishesInner dish : dishesInners){
                    Long dishID = dish.getDish().getDishID();
                    if(dishesPerCustomer.get(customerID).containsKey(dishID)){
                        Integer curAmount = dishesPerCustomer.get(customerID).get(dishID);
                        dishesPerCustomer.get(customerID).replace(dishID, dish.getAmount() + curAmount);
                    } else {
                        dishesPerCustomer.get(customerID).put(dish.getDish().getDishID(), dish.getAmount());
                    }
                }
            }
        }
        List<AnalyticCustomerPreferencesInner> customerPreferences = new ArrayList<>();
        for(Long cID : dishesPerCustomer.keySet()) {
            HashMap<Long, Integer> customerDishes = dishesPerCustomer.get(cID);
            int maxAmount = Collections.max(customerDishes.values());
            for(Map.Entry<Long, Integer> dishAmount : customerDishes.entrySet()) {
                if(dishAmount.getValue() == maxAmount){
                    customerPreferences.add(new AnalyticCustomerPreferencesInner(cID,dishAmount.getKey()));
                    break;
                }
            }
        }
        return customerPreferences;
    }

    public List<Dish> getPopularDishes(List<Order> allOrders) {
        List<OrderDishesInner> dishesInners = allOrders.stream()
                .flatMap(x -> x.getDishes().stream())
                .collect(Collectors.toList());

        HashMap<Dish, Integer> dishAmounts = new HashMap<>();

        for(OrderDishesInner d : dishesInners){
            Dish dish = d.getDish();
            Integer amount = d.getAmount();
            if(!dishAmounts.containsKey(dish)) {
                dishAmounts.put(dish, amount);
            } else {
                Integer oldAmount = dishAmounts.get(dish);
                dishAmounts.replace(dish, oldAmount + amount);
            }
        }

        double maxAmount = (double) Collections.max(dishAmounts.values());
        List<Dish> popularDishes = new ArrayList<>();

        for (Map.Entry<Dish, Integer> entry : dishAmounts.entrySet()) {
            if(entry.getValue() >= Math.floor(popularThreshold * maxAmount)) {
                popularDishes.add(entry.getKey());
            }
        }

        return popularDishes;
    }

    public List<Integer> getPeakHours(List<Delivery> deliveries) {
        List<Integer> peakHours = new ArrayList<>();
        hourDeliveries[] hours = new hourDeliveries[24];

        for(int i = 0; i < 24; i++) {
            hours[i] = new hourDeliveries(i, 0);
        }

        for(Delivery d : deliveries) {
            hours[d.getTimes().getActualPickupTime().getHours()].deliveries++;
        }
        Arrays.sort(hours, (h1, h2) -> {return h1.deliveries-h2.deliveries;});

        for(int i = 0; i < 24; i++){
            peakHours.add(i, hours[i].hour);
        }

        return peakHours;
    }

    public List<AnalyticOrderVolumeInner> calculateOrderVolume(List<Delivery> deliveries){
        List<AnalyticOrderVolumeInner> orderVolumes = new ArrayList<>();
        int[] days = new int[7];

        for(Delivery d : deliveries) {
            if(d.getTimes() == null) {
                continue;
            }
            days[d.getTimes().getActualPickupTime().getDay()]++;
        }
        for(int i = 0; i < 7; i++) {
            orderVolumes.add(new AnalyticOrderVolumeInner(intToDay(i), new BigDecimal(days[i])));
        }
        return orderVolumes;
    }

    private String intToDay(int i) {
        switch (i){
            case 0 -> {return "Sunday";}
            case 1 -> {return "Monday";}
            case 2 -> {return "Tuesday";}
            case 3 -> {return "Wednesday";}
            case 4 -> {return "Thursday";}
            case 5 -> {return "Friday";}
            default -> {return "Saturday";}
        }
    }
}
