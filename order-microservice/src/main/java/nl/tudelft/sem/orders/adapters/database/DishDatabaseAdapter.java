package nl.tudelft.sem.orders.adapters.database;

import java.util.List;
import nl.tudelft.sem.orders.domain.DishRepository;
import nl.tudelft.sem.orders.model.Dish;
import nl.tudelft.sem.orders.ports.output.DishDatabase;
import org.springframework.beans.factory.annotation.Autowired;

public class DishDatabaseAdapter implements DishDatabase {
    @Autowired
    private transient DishRepository dishRepository;

    @Override
    public Dish getById(long orderId) {
        return dishRepository.findByDishID(orderId);
    }

    @Override
    public void save(Dish toSave) {
        dishRepository.saveAndFlush(toSave);
    }

    @Override
    public void delete(Dish dish) {
        dishRepository.delete(dish);
    }

    @Override
    public List<Dish> findDishesByVendorID(Long vendorId) {
        return dishRepository.findDishesByVendorID(vendorId);
    }

}
