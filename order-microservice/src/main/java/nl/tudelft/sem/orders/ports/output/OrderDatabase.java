package nl.tudelft.sem.orders.ports.output;

import java.util.List;
import nl.tudelft.sem.orders.model.Order;



public interface OrderDatabase {
    Order getById(long orderId);

    void save(Order toSave);

    Long getLastId();

    List<Order> findByVendorID(long vendorID);

    List<Order> findByCustomerID(long customerID);

    List<Order> findByCourierID(long courierID);

    List<Order> findAllOrders();
}
