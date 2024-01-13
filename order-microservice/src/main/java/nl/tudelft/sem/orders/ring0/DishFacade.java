package nl.tudelft.sem.orders.ring0;

import java.util.ArrayList;
import java.util.List;
import nl.tudelft.sem.orders.model.Dish;
import nl.tudelft.sem.orders.ports.input.DishLogicInterface;
import nl.tudelft.sem.orders.ports.output.DishDatabase;
import nl.tudelft.sem.orders.result.MalformedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class DishFacade implements DishLogicInterface {
    private final transient DishDatabase dishDatabase;

    @Autowired
    public DishFacade(DishDatabase dishDatabase) {
        this.dishDatabase = dishDatabase;
    }

    @Override
    public List<Dish> getDish(Long dishID) throws MalformedException {
        if (dishID == null) {
            throw new MalformedException();
        }

        Dish dish = dishDatabase.getById(dishID);

        if (dish == null) {
            throw new MalformedException();
        }

        List<Dish> result = new ArrayList<>();
        result.add(dish);
        return result;
    }

}
