package nl.tudelft.sem.orders.controllers;

import java.util.List;
import nl.tudelft.sem.orders.api.DishApi;
import nl.tudelft.sem.orders.model.Dish;
import nl.tudelft.sem.orders.result.MalformedException;
import nl.tudelft.sem.orders.ring0.DishFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class DishController implements DishApi {
    private final transient DishFacade dishFacade;

    @Autowired
    public DishController(DishFacade dishFacade) {
        this.dishFacade = dishFacade;
    }

    @Override
    public ResponseEntity<List<Dish>> dishDishIDGet(Long dishID) {
        try {
            return ResponseEntity.ok(
                   dishFacade.getDish(dishID));
        } catch (MalformedException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
