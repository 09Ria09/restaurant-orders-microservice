package nl.tudelft.sem.orders.test.mocks;

import java.util.ArrayList;
import nl.tudelft.sem.orders.model.Dish;
import nl.tudelft.sem.orders.ports.output.DishDatabase;

public class MockDishDatabase implements DishDatabase {

    private ArrayList<Dish> mocks = new ArrayList<>();

    @Override
    public Dish getById(long dishId) {
        return mocks.stream()
            .filter(d -> d.getDishID().equals(dishId))
            .findAny()
            .orElse(null);
    }

    @Override
    public void save(Dish dish) {
        mocks.add(dish);
    }

    @Override
    public void delete(Dish dish) {
        mocks.remove(dish);
    }

    /**
     * Clean the state of this mock object.
     */
    public void clean() {
        mocks.clear();
        mocks.add(new Dish().dishID(1L).name("a").price(1.0f).vendorID(1L));
        mocks.add(new Dish().dishID(2L).name("a").price(1.0f).vendorID(100L));
    }
}
