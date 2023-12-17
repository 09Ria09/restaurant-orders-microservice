package nl.tudelft.sem.orders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import nl.tudelft.sem.orders.model.Dish;
import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.model.OrderOrderIDDishesPutRequestDishesInner;
import nl.tudelft.sem.orders.ports.output.DishDatabase;
import nl.tudelft.sem.orders.ports.output.OrderDatabase;
import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import nl.tudelft.sem.orders.ring0.OrderFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OrderFacadeTest {

    OrderDatabase orderDatabase;
    DishDatabase dishDatabase;
    private UserMicroservice userMicroservice;
    private OrderFacade orderFacade;

    @BeforeEach
    void setUp() {
        orderDatabase = mock(OrderDatabase.class);
        dishDatabase = mock(DishDatabase.class);
        userMicroservice = mock(UserMicroservice.class);

        orderFacade = new OrderFacade(orderDatabase, dishDatabase, userMicroservice);
    }

    @Test
    void createOrder() {
        long customerId = 1L;
        long vendorId = 2L;
        Location location = new Location();

        when(userMicroservice.getCustomerAddress(customerId)).thenReturn(new Location());

        Order result = orderFacade.createOrder(customerId, vendorId);

        assertNotNull(result);
        assertEquals(customerId, result.getCustomerID());
        assertEquals(vendorId, result.getVendorID());
        assertEquals(Order.StatusEnum.UNPAID, result.getStatus());
        assertEquals(location, result.getLocation());
        verify(orderDatabase, times(1)).save(result);
    }

    @Test
    void updateDishesValidData() {
        long orderId = 2311L;
        long dishId = 413L;
        long customerId = 143L;
        long vendorId = 2123L;

        Dish mockDish = new Dish(dishId, vendorId, "name", "description", new ArrayList<>(), 23.4f);
        Order order = new Order(orderId, customerId, vendorId, new ArrayList<>(), null, Order.StatusEnum.UNPAID);

        OrderOrderIDDishesPutRequestDishesInner dish = new OrderOrderIDDishesPutRequestDishesInner();
        dish.setId(dishId);
        dish.setQuantity(4);

        List<@Valid OrderOrderIDDishesPutRequestDishesInner> dishes = new ArrayList<>();
        dishes.add(dish);

        when(dishDatabase.getById(dishId)).thenReturn(mockDish);
        when(orderDatabase.getById(orderId)).thenReturn(order);

        Float totalPrice = orderFacade.updateDishes(orderId, customerId, dishes);

        assertNotNull(totalPrice);
        assertEquals(4 * mockDish.getPrice(), totalPrice);
        assertEquals(dishes.size(), order.getDishes().size());
        assertEquals(order.getDishes().get(0).getDish(), mockDish);
        assertEquals(order.getDishes().get(0).getAmount(), 4);
        verify(orderDatabase, times(1)).save(order);
    }

    @Test
    void updateDishesMissingOrder() {
        long orderId = 2311L;
        long customerId = 143L;

        List<@Valid OrderOrderIDDishesPutRequestDishesInner> dishes = new ArrayList<>();

        when(orderDatabase.getById(orderId)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> orderFacade.updateDishes(orderId, customerId, dishes));
    }

    @Test
    void updateDishesMissingDish() {
        long orderId = 2311L;
        long dishId = 413L;
        long customerId = 143L;

        OrderOrderIDDishesPutRequestDishesInner dish = new OrderOrderIDDishesPutRequestDishesInner();
        dish.setId(dishId);
        dish.setQuantity(4);

        List<@Valid OrderOrderIDDishesPutRequestDishesInner> dishes = new ArrayList<>();
        dishes.add(dish);

        Order order = new Order(orderId, customerId, 2L, new ArrayList<>(), null, Order.StatusEnum.UNPAID);

        when(orderDatabase.getById(orderId)).thenReturn(order);
        when(dishDatabase.getById(dishId)).thenThrow(EntityNotFoundException.class);

        assertThrows(IllegalStateException.class, () -> orderFacade.updateDishes(orderId, customerId, dishes));
    }

    @Test
    void updateDishesDifferentVendor() {
        long orderId = 2311L;
        long dishId = 413L;
        long customerId = 143L;
        long vendorId = 2123L;
        long anotherVendorId = 3L;
        Order order = new Order(orderId, customerId, vendorId, new ArrayList<>(), null, Order.StatusEnum.UNPAID);

        OrderOrderIDDishesPutRequestDishesInner dish = new OrderOrderIDDishesPutRequestDishesInner();
        dish.setId(dishId);
        dish.setQuantity(4);

        List<@Valid OrderOrderIDDishesPutRequestDishesInner> dishes = new ArrayList<>();
        dishes.add(dish);

        Dish mockDish = new Dish(dishId, anotherVendorId, "name", "description", new ArrayList<>(), 23.4f);

        when(dishDatabase.getById(dishId)).thenReturn(mockDish);
        when(orderDatabase.getById(orderId)).thenReturn(order);

        assertThrows(IllegalStateException.class, () -> orderFacade.updateDishes(orderId, customerId, dishes));
    }
}
