package nl.tudelft.sem.orders.ring0;

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
import nl.tudelft.sem.orders.test.mocks.MockPaymentService;
import nl.tudelft.sem.users.ApiException;
import nl.tudelft.sem.users.model.UsersGetUserTypeIdGet200Response.UserTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class OrderLogicMockitoTest {

    OrderDatabase orderDatabase;
    DishDatabase dishDatabase;
    private UserMicroservice userMicroservice;
    private OrderLogic orderFacade;

    @BeforeEach
    void setUp() {
        orderDatabase = mock(OrderDatabase.class);
        dishDatabase = mock(DishDatabase.class);
        userMicroservice = mock(UserMicroservice.class);

        orderFacade = new OrderLogic(orderDatabase, dishDatabase, userMicroservice, new MockPaymentService());
    }

    @Test
    void createOrder() throws ApiException {
        final long customerId = 1L;
        final long vendorId = 2L;
        final Location location = new Location();

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
        final long orderId = 2311L;
        final long dishId = 413L;
        final long customerId = 143L;
        final long vendorId = 2123L;

        final Dish mockDish = new Dish(dishId, vendorId, "name", "description", new ArrayList<>(), 23.4f);
        final Order order = new Order(orderId, customerId, vendorId, new ArrayList<>(), null, Order.StatusEnum.UNPAID);

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
        final long orderId = 2311L;
        final long customerId = 143L;

        List<@Valid OrderOrderIDDishesPutRequestDishesInner> dishes = new ArrayList<>();

        when(orderDatabase.getById(orderId)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> orderFacade.updateDishes(orderId, customerId, dishes));
    }

    @Test
    void updateDishesMissingDish() {
        final long orderId = 2311L;
        final long dishId = 413L;
        final long customerId = 143L;

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
        final long orderId = 2311L;
        final long dishId = 413L;
        final long customerId = 143L;
        final long vendorId = 2123L;
        final long anotherVendorId = 3L;
        final Order order = new Order(orderId, customerId, vendorId, new ArrayList<>(), null, Order.StatusEnum.UNPAID);

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

    void getOrdersAdmin(){
        Long userID = 1L;
        UserTypeEnum userType = UserTypeEnum.ADMIN;
        final long orderId = 2311L;
        final long dishId = 413L;
        final long vendorId = 2123L;
        final long anotherVendorId = 3L;
        final Order order = new Order(orderId, userID, vendorId, new ArrayList<>(), null, Order.StatusEnum.UNPAID);
        final Order order2 = new Order(orderId, userID, anotherVendorId, new ArrayList<>(), null, Order.StatusEnum.UNPAID);

        ArrayList<Order> allOrders = new ArrayList<>();
        allOrders.add(order);
        allOrders.add(order2);
        when(orderDatabase.findAllOrders()).thenReturn(allOrders);

        assertEquals(orderFacade.getOrders(userID, userType), allOrders);
    }
}
