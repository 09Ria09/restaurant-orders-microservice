package nl.tudelft.sem.orders.ring0;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
import nl.tudelft.sem.orders.model.OrderDishesInner;
import nl.tudelft.sem.orders.model.OrderOrderIDDishesPutRequestDishesInner;
import nl.tudelft.sem.orders.ports.output.DishDatabase;
import nl.tudelft.sem.orders.ports.output.LocationService;
import nl.tudelft.sem.orders.ports.output.OrderDatabase;
import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import nl.tudelft.sem.orders.result.MalformedException;
import nl.tudelft.sem.orders.result.NotFoundException;
import nl.tudelft.sem.orders.test.mocks.MockPaymentService;
import nl.tudelft.sem.users.ApiException;
import nl.tudelft.sem.users.model.UsersGetUserTypeIdGet200Response.UserTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class OrderLogicMockitoTest {

    OrderDatabase orderDatabase;
    DishDatabase dishDatabase;
    private UserMicroservice userMicroservice;
    private OrderLogic orderLogic;
    private LocationService locationService;

    @BeforeEach
    void setUp() {
        orderDatabase = mock(OrderDatabase.class);
        dishDatabase = mock(DishDatabase.class);
        userMicroservice = mock(UserMicroservice.class);
        locationService = mock(LocationService.class);
        orderLogic = new OrderLogic(
            orderDatabase,
            dishDatabase,
            userMicroservice,
            new MockPaymentService(),
            locationService
        );
    }

    @Test
    void createOrder() throws ApiException {
        final long customerId = 1L;
        final long vendorId = 2L;
        final Location location = new Location();
        Order orderNoId = new Order(null, customerId, vendorId, new ArrayList<>(), location, Order.StatusEnum.UNPAID);

        when(userMicroservice.getCustomerAddress(customerId)).thenReturn(new Location());
        when(orderDatabase.save(orderNoId)).thenReturn(new Order(1L,
            customerId,
            vendorId,
            new ArrayList<>(),
            location,
            Order.StatusEnum.UNPAID));

        Order result = orderLogic.createOrder(customerId, vendorId);

        assertNotNull(result);
        assertEquals(customerId, result.getCustomerID());
        assertEquals(vendorId, result.getVendorID());
        assertEquals(Order.StatusEnum.UNPAID, result.getStatus());
        assertEquals(location, result.getLocation());
        verify(orderDatabase, times(1)).save(orderNoId);
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

        Float totalPrice = orderLogic.updateDishes(orderId, customerId, dishes);

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

        assertThrows(EntityNotFoundException.class, () -> orderLogic.updateDishes(orderId, customerId, dishes));
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

        assertThrows(IllegalStateException.class, () -> orderLogic.updateDishes(orderId, customerId, dishes));
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

        assertThrows(IllegalStateException.class, () -> orderLogic.updateDishes(orderId, customerId, dishes));
    }

    @Test
    void getOrdersAdmin() {
        Long userID = 1L;
        final UserTypeEnum userType = UserTypeEnum.ADMIN;
        final long orderId = 2311L;
        final long dishId = 413L;
        final long vendorId = 2123L;
        final long anotherVendorId = 3L;
        final Order order = new Order(orderId, userID, vendorId,
                new ArrayList<>(), null, Order.StatusEnum.UNPAID);
        final Order order2 = new Order(orderId, userID, anotherVendorId,
                new ArrayList<>(), null, Order.StatusEnum.UNPAID);

        ArrayList<Order> allOrders = new ArrayList<>();
        allOrders.add(order);
        allOrders.add(order2);
        when(orderDatabase.findAllOrders()).thenReturn(allOrders);

        assertEquals(orderLogic.getOrders(userID, userType), allOrders);
        verify(orderDatabase, times(1)).findAllOrders();
    }

    @Test
    void getOrdersVendor() {
        Long userID = 1L;
        final UserTypeEnum userType = UserTypeEnum.VENDOR;
        final long orderId = 2311L;
        final long dishId = 413L;
        final long vendorId = 2123L;
        final long anotherVendorId = 3L;
        final Order order = new Order(orderId, userID, vendorId, new ArrayList<>(),
                null, Order.StatusEnum.UNPAID);
        final Order order2 = new Order(orderId, userID, anotherVendorId, new ArrayList<>(),
                null, Order.StatusEnum.UNPAID);

        ArrayList<Order> expected = new ArrayList<>();
        expected.add(order);
        when(orderDatabase.findByVendorID(vendorId)).thenReturn(expected);

        assertEquals(orderLogic.getOrders(vendorId, userType), expected);
        verify(orderDatabase, times(1)).findByVendorID(vendorId);
    }

    @Test
    void getOrdersCourier() {
        Long userID = 1L;
        final UserTypeEnum userType = UserTypeEnum.COURIER;
        final long orderId = 2311L;
        final long dishId = 413L;
        final long vendorId = 2123L;
        final long anotherVendorId = 3L;
        final Order order = new Order(orderId, userID, vendorId, new ArrayList<>(),
                null, Order.StatusEnum.UNPAID);
        final Order order2 = new Order(orderId, userID, anotherVendorId, new ArrayList<>(),
                null, Order.StatusEnum.UNPAID);

        ArrayList<Order> expected = new ArrayList<>();
        expected.add(order);
        when(orderDatabase.findByCourierID(userID)).thenReturn(expected);

        assertEquals(orderLogic.getOrders(userID, userType), expected);
        verify(orderDatabase, times(1)).findByCourierID(userID);
    }

    @Test
    void getOrdersCustomer() {
        Long userID = 1L;
        final UserTypeEnum userType = UserTypeEnum.CUSTOMER;
        final long orderId = 2311L;
        final long dishId = 413L;
        final long vendorId = 2123L;
        final long anotherVendorId = 3L;
        final Order order = new Order(orderId, userID, vendorId, new ArrayList<>(),
                null, Order.StatusEnum.UNPAID);
        final Order order2 = new Order(orderId, userID, anotherVendorId, new ArrayList<>(),
                null, Order.StatusEnum.UNPAID);

        ArrayList<Order> expected = new ArrayList<>();
        expected.add(order);
        when(orderDatabase.findByCustomerID(userID)).thenReturn(expected);

        assertEquals(orderLogic.getOrders(userID, userType), expected);
        verify(orderDatabase, times(1)).findByCustomerID(userID);
    }

    @Test
    void getOrdersNullFound() {
        Long userID = 1L;
        final UserTypeEnum userType = UserTypeEnum.CUSTOMER;
        final long orderId = 2311L;
        final long dishId = 413L;
        final long vendorId = 2123L;
        final long anotherVendorId = 3L;
        final Order order = new Order(orderId, userID, vendorId, new ArrayList<>(),
                null, Order.StatusEnum.UNPAID);
        final Order order2 = new Order(orderId, userID, anotherVendorId, new ArrayList<>(),
                null, Order.StatusEnum.UNPAID);

        ArrayList<Order> expected = new ArrayList<>();
        expected.add(order);
        when(orderDatabase.findByCustomerID(userID)).thenReturn(null);

        assertThrows(IllegalStateException.class,
                () -> orderLogic.getOrders(userID, userType));
        verify(orderDatabase, times(1)).findByCustomerID(userID);
    }

    @Test
    void getOrdersNullType() {
        Long userID = 1L;
        final UserTypeEnum userType = null;

        assertThrows(IllegalStateException.class,
                () -> orderLogic.getOrders(userID, userType));
        verify(orderDatabase, times(0)).findByCustomerID(userID);
    }

    @Test
    public void testReorder() throws Exception {
        Dish dish = new Dish(1L,
            2L,
            "name",
            "description",
            new ArrayList<>(),
            1.0f);
        OrderDishesInner dishInner = new OrderDishesInner(dish, 1);
        Order order = new Order(1L,
            1L,
            2L,
            new ArrayList<>(List.of(dishInner)),
            null,
            Order.StatusEnum.ACCEPTED);

        when(orderDatabase.getById(1L)).thenReturn(order);
        when(userMicroservice.isVendor(2L)).thenReturn(true);
        when(locationService.isCloseBy(any(), any())).thenReturn(true);
        when(dishDatabase.getById(1L)).thenReturn(dish);
        when(orderDatabase.save(any(Order.class))).thenReturn(new Order(2L,
            1L,
            2L,
            new ArrayList<>(List.of(dishInner)),
            null,
            Order.StatusEnum.UNPAID));

        Order newOrder = orderLogic.reorder(1L, 1L);
        assertEquals(order.getCustomerID(), newOrder.getCustomerID());
        assertEquals(order.getVendorID(), newOrder.getVendorID());
        assertEquals(Order.StatusEnum.UNPAID, newOrder.getStatus());
    }

    @Test
    public void testReorderMalformedExceptionOrderNotExisting() {
        when(orderDatabase.getById(anyLong())).thenReturn(null);

        assertThrows(MalformedException.class, () -> orderLogic.reorder(1L, 1L));
    }

    @Test
    public void testReorderMalformedExceptionOrderNotOwned() {
        Order order = new Order(1L, 1L, 1L, null, null, Order.StatusEnum.ACCEPTED);
        when(orderDatabase.getById(1L)).thenReturn(order);

        assertThrows(MalformedException.class, () -> orderLogic.reorder(2L, 1L));
    }

    @Test
    public void testReorderNotFoundExceptionVendorNotExisting() throws Exception {
        Order order = new Order(1L, 1L, 1L, null, null, Order.StatusEnum.UNPAID);
        when(orderDatabase.getById(1L)).thenReturn(order);
        when(userMicroservice.isVendor(1L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> orderLogic.reorder(1L, 1L));
    }

    @Test
    public void testReorderNotFoundExceptionVendorNotCloseBy() throws Exception {
        Order order = new Order(1L, 1L, 1L, null, null, Order.StatusEnum.UNPAID);
        when(orderDatabase.getById(1L)).thenReturn(order);
        when(userMicroservice.isVendor(1L)).thenReturn(true);
        when(locationService.isCloseBy(any(), any())).thenReturn(false);

        assertThrows(NotFoundException.class, () -> orderLogic.reorder(1L, 1L));
    }

    @Test
    public void testReorderNotFoundExceptionDishNotExisting() throws Exception {
        Dish dish = new Dish(1L,
            2L,
            "name",
            "description",
            new ArrayList<>(),
            1.0f);
        OrderDishesInner dishInner = new OrderDishesInner(dish, 1);
        Order order = new Order(1L,
            1L,
            2L,
            new ArrayList<>(List.of(dishInner)),
            null,
            Order.StatusEnum.ACCEPTED);

        when(orderDatabase.getById(1L)).thenReturn(order);
        when(userMicroservice.isVendor(2L)).thenReturn(true);
        when(locationService.isCloseBy(any(), any())).thenReturn(true);
        when(dishDatabase.getById(1L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> orderLogic.reorder(1L, 1L));
    }

    @Test
    public void testReorderNotFoundExceptionDishNotOwned() throws Exception {
        Dish dish = new Dish(1L,
            2L,
            "name",
            "description",
            new ArrayList<>(),
            1.0f);
        OrderDishesInner dishInner = new OrderDishesInner(dish, 1);
        Order order = new Order(1L,
            1L,
            2L,
            new ArrayList<>(List.of(dishInner)),
            null,
            Order.StatusEnum.ACCEPTED);

        when(orderDatabase.getById(1L)).thenReturn(order);
        when(userMicroservice.isVendor(2L)).thenReturn(true);
        when(locationService.isCloseBy(any(), any())).thenReturn(true);
        when(dishDatabase.getById(1L)).thenReturn(new Dish(1L,
            3L,
            "name",
            "description",
            new ArrayList<>(),
            1.0f));

        assertThrows(NotFoundException.class, () -> orderLogic.reorder(1L, 1L));
    }
}
