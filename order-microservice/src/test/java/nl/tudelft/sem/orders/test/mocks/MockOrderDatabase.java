package nl.tudelft.sem.orders.test.mocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.ports.output.OrderDatabase;

public class MockOrderDatabase implements OrderDatabase {
    private Order[] mocks;

    @Override
    public Order getById(long orderId) {
        if (orderId > mocks.length || orderId < 0) {
            return null;
        }

        return mocks[(int) orderId - 1];
    }

    @Override
    public List<Order> findByVendorIDAndCustomerID(long vendorID, long customerID) {
        return null;
    }

    public ArrayList<Order> getSaveRequests() {
        return saveRequests;
    }

    private ArrayList<Order> saveRequests = new ArrayList<>();

    @Override
    public Order save(Order toSave) {
        toSave.setOrderID((long) saveRequests.size() + 1);
        saveRequests.add(toSave);
        return toSave;
    }

    @Override
    public List<Order> findByVendorID(long vendorID) {
        return Arrays.stream(mocks).filter(u -> u.getVendorID() == vendorID).collect(
            Collectors.toList());
    }

    @Override
    public List<Order> findByCustomerID(long customerID) {
        return Arrays.stream(mocks).filter(u -> u.getCustomerID() == customerID).collect(
            Collectors.toList());
    }

    @Override
    public List<Order> findByCourierID(long courierID) {
        return Arrays.stream(mocks).filter(u -> u.getCourierID() == courierID).collect(
            Collectors.toList());
    }

    @Override
    public List<Order> findAllOrders() {
        return List.of(mocks);
    }

    @Override
    public void delete(Order toDelete) {
        saveRequests.remove(toDelete);
    }

    /**
     * Clean the state of this mock object.
     */
    public void clean() {
        saveRequests.clear();
        this.mocks = new Order[] {
            new Order(1L, 4L, 0L, new ArrayList<>(), 1f,
                new Location().city("a").country("PL").postalCode("123ZT"),
                Order.StatusEnum.UNPAID).courierID(3L),
            new Order(2L, 4L, 3L, new ArrayList<>(), 1f,
                new Location().city("Kraków").country("PL").postalCode("123ZT"),
                Order.StatusEnum.PENDING).courierID(3L),
            new Order(3L, 4L, 3L, new ArrayList<>(), 1f,
                new Location().city("Kraków").country("PL").postalCode("123ZT"),
                Order.StatusEnum.UNPAID).courierID(3L)
        };
    }
}
