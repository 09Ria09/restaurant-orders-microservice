package nl.tudelft.sem.orders.adapters;

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
    public Long getLastId() {
        Dish dish = dishRepository.findTopByDishByDishIDDesc();
        if(dish==null) return 0L;
        return dish.getDishID();
    }

}
