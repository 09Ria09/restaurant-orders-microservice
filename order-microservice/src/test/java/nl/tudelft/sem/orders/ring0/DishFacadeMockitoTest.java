package nl.tudelft.sem.orders.ring0;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import nl.tudelft.sem.orders.model.Dish;
import nl.tudelft.sem.orders.ports.output.DishDatabase;
import nl.tudelft.sem.orders.result.MalformedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;



public class DishFacadeMockitoTest {
    DishDatabase dishDatabase;
    private DishFacade dishFacade;

    @BeforeEach
    void setUp() {
        dishDatabase = mock(DishDatabase.class);
        dishFacade = new DishFacade(dishDatabase);
    }

    @Test
    void testGetDishInvalidId() {
        assertThrows(MalformedException.class,
                () -> dishFacade.getDish(null));
    }

    @Test
    void testGetDishMissingDish() {
        when(dishDatabase.getById(1L)).thenReturn(null);
        assertThrows(MalformedException.class,
                () -> dishFacade.getDish(1L));
    }

    @Test
    void testGetDishOk() {
        Dish dish = new Dish();
        when(dishDatabase.getById(1L)).thenReturn(dish);
        List<Dish> list = new ArrayList<>();
        list.add(dish);
        assertDoesNotThrow(() -> {
            assertEquals(dishFacade.getDish(1L), list);
        });
        verify(dishDatabase, times(1)).getById(1L);
    }

}
