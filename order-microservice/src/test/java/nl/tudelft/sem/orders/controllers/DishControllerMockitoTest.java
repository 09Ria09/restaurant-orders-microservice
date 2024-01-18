package nl.tudelft.sem.orders.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import nl.tudelft.sem.orders.model.Dish;
import nl.tudelft.sem.orders.result.MalformedException;
import nl.tudelft.sem.orders.ring0.DishFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


public class DishControllerMockitoTest {
    private DishFacade dishFacade;
    private DishController dishController;

    @BeforeEach
    public void setUp() {
        dishFacade = mock(DishFacade.class);
        dishController = new DishController(dishFacade);
    }

    @Test
    public void testDishDishIDGetMalformed() throws MalformedException {
        when(dishFacade.getDish(1L)).thenThrow(MalformedException.class);
        assertEquals(HttpStatus.BAD_REQUEST,
                dishController.dishDishIDGet(1L)
                        .getStatusCode());
    }

    @Test
    public void testDishDishIDGetOk() throws MalformedException {
        Dish dish = new Dish();
        List<Dish> list = new ArrayList<>();
        list.add(dish);

        when(dishFacade.getDish(1L)).thenReturn(list);

        ResponseEntity<List<Dish>> response = dishController.dishDishIDGet(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(list, response.getBody());
    }

}
