package nl.tudelft.sem.orders.domain;

import nl.tudelft.sem.orders.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface OrderRepository extends JpaRepository<Order, Long> {
    Order findByOrderID(Long orderId);
    Order findTopByOrderByOrderIDDesc();
}
