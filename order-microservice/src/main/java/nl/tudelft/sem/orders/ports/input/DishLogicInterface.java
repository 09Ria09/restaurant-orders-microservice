package nl.tudelft.sem.orders.ports.input;

import java.util.List;
import nl.tudelft.sem.orders.model.Dish;
import nl.tudelft.sem.orders.result.MalformedException;


public interface DishLogicInterface {
    List<Dish> getDish(Long dishID) throws MalformedException;
}
