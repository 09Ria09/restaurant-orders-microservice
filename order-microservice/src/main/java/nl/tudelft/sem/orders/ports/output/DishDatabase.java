package nl.tudelft.sem.orders.ports.output;

import nl.tudelft.sem.orders.model.Dish;

public interface DishDatabase {
    Dish getById(long dishId);

    void save(Dish toSave);
}
