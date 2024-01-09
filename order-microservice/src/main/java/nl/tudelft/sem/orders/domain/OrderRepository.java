package nl.tudelft.sem.orders.domain;

import java.util.List;
import nl.tudelft.sem.orders.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;


@Component
public interface OrderRepository extends JpaRepository<Order, Long> {
    Order findByOrderID(Long orderId);

    Order findTopByOrderByOrderIDDesc();

    List<Order> findByVendorID(long vendorID);

    List<Order> findByCustomerID(long customerID);

    List<Order> findByCourierID(long courierID);

    List<Order> findByVendorIDAndCustomerID(long vendorID, long customerID);
}
