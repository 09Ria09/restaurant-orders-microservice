package nl.tudelft.sem.orders.ports.output;

import java.util.List;
import nl.tudelft.sem.orders.model.Dish;

public interface DishDatabase {
    Dish getById(long dishId);

    void save(Dish toSave);

    void delete(Dish dish);

    List<Dish> findDishesByVendorID(Long vendorId);
}
