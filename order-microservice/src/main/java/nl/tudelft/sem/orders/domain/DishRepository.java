package nl.tudelft.sem.orders.domain;

import java.util.List;
import nl.tudelft.sem.orders.model.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface DishRepository extends JpaRepository<Dish, Long> {
    Dish findByDishID(Long dishId);

    List<Dish> findDishesByVendorID(Long vendorId);

}
