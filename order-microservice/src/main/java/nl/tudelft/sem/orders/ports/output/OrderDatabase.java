package nl.tudelft.sem.orders.ports.output;

import java.util.List;
import nl.tudelft.sem.orders.model.Order;
import org.aspectj.weaver.ast.Or;


public interface OrderDatabase {
    Order getById(long orderId);

    List<Order> findByVendorIDAndCustomerID(long vendorID, long customerID);

    List<Order> findByVendorID(long vendorID);

    List<Order> findByCustomerID(long customerID);

    List<Order> findByCourierID(long courierID);

    List<Order> findAllOrders();
    
    Order save(Order toSave);
}
