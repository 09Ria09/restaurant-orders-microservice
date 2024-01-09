package nl.tudelft.sem.orders.test.mocks;

import java.util.ArrayList;
import java.util.List;
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
        return null;
    }

    @Override
    public List<Order> findByCustomerID(long customerID) {
        return null;
    }

    @Override
    public List<Order> findByCourierID(long courierID) {
        return null;
    }

    @Override
    public List<Order> findAllOrders() {
        return null;
    }

    /**
     * Clean the state of this mock object.
     */
    public void clean() {
        saveRequests.clear();
        this.mocks = new Order[] {
            new Order(1L, 1L, 13L, new ArrayList<>(),
                new Location().city("Kraków").country("PL").postalCode("123ZT"),
                Order.StatusEnum.UNPAID).courierID(3L),
            new Order(2L, 1L, 13L, new ArrayList<>(),
                new Location().city("Kraków").country("PL").postalCode("123ZT"),
                Order.StatusEnum.PENDING).courierID(3L)
        };
    }
}
