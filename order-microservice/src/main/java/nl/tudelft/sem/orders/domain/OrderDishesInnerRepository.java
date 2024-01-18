package nl.tudelft.sem.orders.domain;

import nl.tudelft.sem.orders.model.OrderDishesInner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderDishesInnerRepository extends JpaRepository<OrderDishesInner, Long> {
}
