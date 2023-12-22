package nl.tudelft.sem.orders.test.mocks;

import java.util.ArrayList;
import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.ports.output.OrderDatabase;
import org.aspectj.weaver.ast.Or;

public class MockOrderDatabase implements OrderDatabase {
    private Order[] mocks;

    @Override
    public Order getById(long orderId) {
        if (orderId > mocks.length || orderId < 0) {
            return null;
        }

        return mocks[(int) orderId - 1];
    }

    public ArrayList<Order> getSaveRequests() {
        return saveRequests;
    }

    private ArrayList<Order> saveRequests = new ArrayList<>();

    @Override
    public void save(Order toSave) {
        saveRequests.add(toSave);
    }

    @Override
    public Long getLastId() {
        return 4L;
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
