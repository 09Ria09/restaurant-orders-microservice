package nl.tudelft.sem.orders.ring0;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import nl.tudelft.sem.orders.result.ForbiddenException;
import nl.tudelft.sem.orders.result.MalformedException;
import nl.tudelft.sem.orders.result.NotFoundException;
import nl.tudelft.sem.orders.ring0.methods.OrderModification;
import nl.tudelft.sem.orders.ring0.payment.PaymentProcess;
import nl.tudelft.sem.users.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class OrderFacadeMockitoTest {

    OrderDatabase orderDatabase;
    DishDatabase dishDatabase;
    private UserMicroservice userMicroservice;
    private OrderFacade orderFacade;
    private LocationService locationService;
    private OrderModification orderModification;

    @BeforeEach
    void setUp() throws ApiException {
        orderDatabase = mock(OrderDatabase.class);
        dishDatabase = mock(DishDatabase.class);
        userMicroservice = mock(UserMicroservice.class);
        locationService = mock(LocationService.class);

        when(userMicroservice.isCustomer(anyLong())).thenReturn(true);
        orderModification = new OrderModification(orderDatabase, dishDatabase, userMicroservice);

        orderFacade = new OrderFacade(
            orderDatabase,
            dishDatabase,
            userMicroservice,
            locationService,
            mock(PaymentProcess.class),
            orderModification
        );
    }

    @Test
    void createOrder()
        throws ApiException, ForbiddenException, MalformedException {
        final long customerId = 1L;
        final long vendorId = 2L;
        final Location location = new Location();
        Order orderNoId =
            new Order(null, customerId, vendorId, new ArrayList<>(), 0F,
                location, Order.StatusEnum.UNPAID);

        when(userMicroservice.getCustomerAddress(customerId)).thenReturn(
            new Location());
        when(orderDatabase.save(orderNoId)).thenReturn(new Order(1L,
            customerId,
            vendorId,
            new ArrayList<>(),
            0F,
            location,
            Order.StatusEnum.UNPAID));

        Order result = orderFacade.createOrder(customerId, vendorId);

        assertNotNull(result);
        assertEquals(customerId, result.getCustomerID());
        assertEquals(vendorId, result.getVendorID());
        assertEquals(Order.StatusEnum.UNPAID, result.getStatus());
        assertEquals(location, result.getLocation());
        assertTrue(result.getDishes().isEmpty());
        verify(orderDatabase, times(1)).save(orderNoId);
    }


    @Test
    void createOrderNotACustomer() throws ApiException {
        final long customerId = 1L;
        final long vendorId = 2L;

        when(userMicroservice.isCustomer(anyLong())).thenReturn(false);

        assertThrows(ForbiddenException.class,
            () -> orderFacade.createOrder(customerId, vendorId));

        verify(orderDatabase, times(0)).save(any());
    }

    @Test
    void updateDishesValidData() throws ApiException {
        final long orderId = 2311L;
        final long dishId = 413L;
        final long customerId = 143L;
        final long vendorId = 2123L;

        final Dish mockDish =
            new Dish(dishId, vendorId, "name", "description", new ArrayList<>(),
                23.4f);
        final Order order =
            new Order(orderId, customerId, vendorId, new ArrayList<>(), 1F,
                null, Order.StatusEnum.UNPAID);

        OrderOrderIDDishesPutRequestDishesInner dish =
            new OrderOrderIDDishesPutRequestDishesInner();
        dish.setId(dishId);
        dish.setQuantity(4);

        List<@Valid OrderOrderIDDishesPutRequestDishesInner> dishes =
            new ArrayList<>();
        dishes.add(dish);

        when(dishDatabase.getById(dishId)).thenReturn(mockDish);
        when(orderDatabase.getById(orderId)).thenReturn(order);

        Float totalPrice =
            orderFacade.updateDishes(orderId, customerId, dishes);

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

        List<@Valid OrderOrderIDDishesPutRequestDishesInner> dishes =
            new ArrayList<>();

        when(orderDatabase.getById(orderId)).thenReturn(null);

        assertThrows(EntityNotFoundException.class,
            () -> orderFacade.updateDishes(orderId, customerId, dishes));
    }

    @Test
    void updateDishesNotACustomer() throws ApiException {
        final long orderId = 2311L;
        final long customerId = 143L;

        when(userMicroservice.isCustomer(anyLong())).thenReturn(false);

        List<@Valid OrderOrderIDDishesPutRequestDishesInner> dishes =
            new ArrayList<>();

        when(orderDatabase.getById(orderId)).thenReturn(null);

        assertThrows(ApiException.class,
            () -> orderFacade.updateDishes(orderId, customerId, dishes));
    }

    @Test
    void updateDishesInvalidCustomer() {
        final long orderId = 2311L;
        final long dishId = 413L;
        final long vendorId = 221L;
        final long customerId = 143L;

        OrderOrderIDDishesPutRequestDishesInner dish =
            new OrderOrderIDDishesPutRequestDishesInner();
        dish.setId(dishId);
        dish.setQuantity(4);

        List<@Valid OrderOrderIDDishesPutRequestDishesInner> dishes =
            new ArrayList<>();
        dishes.add(dish);

        Dish mockDish =
            new Dish(dishId, vendorId, "name", "description", new ArrayList<>(),
                23.4f);
        Order order =
            new Order(orderId, customerId, 2L, new ArrayList<>(), 1F, null,
                Order.StatusEnum.UNPAID);

        when(orderDatabase.getById(orderId)).thenReturn(order);
        when(dishDatabase.getById(dishId)).thenReturn(mockDish);

        assertThrows(EntityNotFoundException.class,
            () -> orderFacade.updateDishes(orderId, customerId + 1, dishes));
    }

    @Test
    void updateDishesInvalidStatus() {
        final long orderId = 2311L;
        final long dishId = 413L;
        final long vendorId = 221L;
        final long customerId = 143L;

        OrderOrderIDDishesPutRequestDishesInner dish =
            new OrderOrderIDDishesPutRequestDishesInner();
        dish.setId(dishId);
        dish.setQuantity(4);

        List<@Valid OrderOrderIDDishesPutRequestDishesInner> dishes =
            new ArrayList<>();
        dishes.add(dish);

        Dish mockDish =
            new Dish(dishId, vendorId, "name", "description", new ArrayList<>(),
                23.4f);
        Order order =
            new Order(orderId, customerId, 2L, new ArrayList<>(), 1F, null,
                Order.StatusEnum.PENDING);

        when(orderDatabase.getById(orderId)).thenReturn(order);
        when(dishDatabase.getById(dishId)).thenReturn(mockDish);

        assertThrows(EntityNotFoundException.class,
            () -> orderFacade.updateDishes(orderId, customerId, dishes));
    }

    @Test
    void updateDishesNullPrice() {
        final long orderId = 2311L;
        final long dishId = 413L;
        final long vendorId = 221L;
        final long customerId = 143L;

        OrderOrderIDDishesPutRequestDishesInner dish =
            new OrderOrderIDDishesPutRequestDishesInner();
        dish.setId(dishId);
        dish.setQuantity(4);

        List<@Valid OrderOrderIDDishesPutRequestDishesInner> dishes =
            new ArrayList<>();
        dishes.add(dish);

        Dish mockDish =
            new Dish(dishId, vendorId, "name", "description", new ArrayList<>(),
                23.4f);
        Order order =
            new Order(orderId, customerId, 2L, new ArrayList<>(), 1F, null,
                Order.StatusEnum.UNPAID);

        when(orderDatabase.getById(orderId)).thenReturn(order);
        when(dishDatabase.getById(dishId)).thenReturn(null);

        assertThrows(IllegalStateException.class,
            () -> orderFacade.updateDishes(orderId, customerId, dishes));
    }

    @Test
    void updateDishesNullQuant() {
        final long orderId = 2311L;
        final long dishId = 413L;
        final long vendorId = 221L;
        final long customerId = 143L;

        OrderOrderIDDishesPutRequestDishesInner dish =
            new OrderOrderIDDishesPutRequestDishesInner();
        dish.setId(dishId);
        dish.setQuantity(null);

        List<@Valid OrderOrderIDDishesPutRequestDishesInner> dishes =
            new ArrayList<>();
        dishes.add(dish);

        Dish mockDish =
            new Dish(dishId, vendorId, "name", "description", new ArrayList<>(),
                23.4f);
        Order order =
            new Order(orderId, customerId, 2L, new ArrayList<>(), 1F, null,
                Order.StatusEnum.UNPAID);

        when(orderDatabase.getById(orderId)).thenReturn(order);
        when(dishDatabase.getById(dishId)).thenReturn(mockDish);

        assertThrows(IllegalStateException.class,
            () -> orderFacade.updateDishes(orderId, customerId, dishes));
    }

    @Test
    void updateDishesMissingDish() {
        final long orderId = 2311L;
        final long dishId = 413L;
        final long customerId = 143L;

        OrderOrderIDDishesPutRequestDishesInner dish =
            new OrderOrderIDDishesPutRequestDishesInner();
        dish.setId(dishId);
        dish.setQuantity(4);

        List<@Valid OrderOrderIDDishesPutRequestDishesInner> dishes =
            new ArrayList<>();
        dishes.add(dish);

        Order order =
            new Order(orderId, customerId, 2L, new ArrayList<>(), 1F, null,
                Order.StatusEnum.UNPAID);

        when(orderDatabase.getById(orderId)).thenReturn(order);
        when(dishDatabase.getById(dishId)).thenThrow(
            EntityNotFoundException.class);

        assertThrows(IllegalStateException.class,
            () -> orderFacade.updateDishes(orderId, customerId, dishes));
    }

    @Test
    void updateDishesDifferentVendor() {
        final long orderId = 2311L;
        final long dishId = 413L;
        final long customerId = 143L;
        final long vendorId = 2123L;
        final long anotherVendorId = 3L;
        final Order order =
            new Order(orderId, customerId, vendorId, new ArrayList<>(), 1F,
                null, Order.StatusEnum.UNPAID);

        OrderOrderIDDishesPutRequestDishesInner dish =
            new OrderOrderIDDishesPutRequestDishesInner();
        dish.setId(dishId);
        dish.setQuantity(4);

        List<@Valid OrderOrderIDDishesPutRequestDishesInner> dishes =
            new ArrayList<>();
        dishes.add(dish);

        Dish mockDish = new Dish(dishId, anotherVendorId, "name", "description",
            new ArrayList<>(), 23.4f);

        when(dishDatabase.getById(dishId)).thenReturn(mockDish);
        when(orderDatabase.getById(orderId)).thenReturn(order);

        assertThrows(IllegalStateException.class,
            () -> orderFacade.updateDishes(orderId, customerId, dishes));
    }

    @Test
    void getOrdersAdmin() throws ApiException {
        Long userID = 1L;

        when(userMicroservice.isCustomer(userID)).thenReturn(false);
        when(userMicroservice.isVendor(userID)).thenReturn(false);
        when(userMicroservice.isAdmin(userID)).thenReturn(true);
        when(userMicroservice.isCourier(userID)).thenReturn(false);

        final long orderId = 2311L;
        final long dishId = 413L;
        final long vendorId = 2123L;
        final long anotherVendorId = 3L;
        final Order order = new Order(orderId, userID, vendorId,
            new ArrayList<>(), 1F, null, Order.StatusEnum.UNPAID);
        final Order order2 = new Order(orderId, userID, anotherVendorId,
            new ArrayList<>(), 2F, null, Order.StatusEnum.UNPAID);

        ArrayList<Order> allOrders = new ArrayList<>();
        allOrders.add(order);
        allOrders.add(order2);
        when(orderDatabase.findAllOrders()).thenReturn(allOrders);

        assertEquals(assertDoesNotThrow(() -> orderFacade.getOrders(userID)),
            allOrders);
        verify(orderDatabase, times(1)).findAllOrders();
    }

    @Test
    void getOrdersVendor() throws ApiException {
        final long vendorId = 2123L;

        when(userMicroservice.isCustomer(2123L)).thenReturn(false);
        when(userMicroservice.isVendor(2123L)).thenReturn(true);
        when(userMicroservice.isAdmin(2123L)).thenReturn(false);
        when(userMicroservice.isCourier(2123L)).thenReturn(false);

        final long orderId = 2311L;
        final long dishId = 413L;

        final long anotherVendorId = 3L;
        final Order order =
            new Order(orderId, 1L, vendorId, new ArrayList<>(), 1F,
                null, Order.StatusEnum.UNPAID);
        final Order order2 =
            new Order(orderId, 1L, anotherVendorId, new ArrayList<>(), 2F,
                null, Order.StatusEnum.UNPAID);

        ArrayList<Order> expected = new ArrayList<>();
        expected.add(order);
        when(orderDatabase.findByVendorID(vendorId)).thenReturn(expected);

        assertEquals(assertDoesNotThrow(() -> orderFacade.getOrders(vendorId)),
            expected);
        verify(orderDatabase, times(1)).findByVendorID(vendorId);
    }

    @Test
    void getOrdersCourier() throws ApiException {
        Long userID = 1L;

        when(userMicroservice.isCustomer(1L)).thenReturn(false);
        when(userMicroservice.isVendor(1L)).thenReturn(false);
        when(userMicroservice.isAdmin(1L)).thenReturn(false);
        when(userMicroservice.isCourier(1L)).thenReturn(true);

        final long orderId = 2311L;
        final long dishId = 413L;
        final long vendorId = 2123L;
        final long anotherVendorId = 3L;
        final Order order =
            new Order(orderId, userID, vendorId, new ArrayList<>(), 1f,
                null, Order.StatusEnum.UNPAID);
        final Order order2 =
            new Order(orderId, userID, anotherVendorId, new ArrayList<>(), 2f,
                null, Order.StatusEnum.UNPAID);

        ArrayList<Order> expected = new ArrayList<>();
        expected.add(order);
        when(orderDatabase.findByCourierID(userID)).thenReturn(expected);

        assertEquals(assertDoesNotThrow(() -> orderFacade.getOrders(userID)),
            expected);
        verify(orderDatabase, times(1)).findByCourierID(userID);
    }

    @Test
    void getOrdersCustomer() throws ApiException {
        Long userID = 1L;

        when(userMicroservice.isCustomer(1L)).thenReturn(true);
        when(userMicroservice.isVendor(1L)).thenReturn(false);
        when(userMicroservice.isAdmin(1L)).thenReturn(false);
        when(userMicroservice.isCourier(1L)).thenReturn(false);

        final long orderId = 2311L;
        final long dishId = 413L;
        final long vendorId = 2123L;
        final long anotherVendorId = 3L;
        final Order order =
            new Order(orderId, userID, vendorId, new ArrayList<>(), 1f,
                null, Order.StatusEnum.UNPAID);
        final Order order2 =
            new Order(orderId, userID, anotherVendorId, new ArrayList<>(), 2f,
                null, Order.StatusEnum.UNPAID);

        ArrayList<Order> expected = new ArrayList<>();
        expected.add(order);
        when(orderDatabase.findByCustomerID(userID)).thenReturn(expected);

        assertEquals(assertDoesNotThrow(() -> orderFacade.getOrders(userID)),
            expected);
        verify(orderDatabase, times(1)).findByCustomerID(userID);
    }

    @Test
    void getOrdersNoSuchUser() throws ApiException {
        Long userID = 21L;

        when(userMicroservice.isCustomer(userID)).thenThrow(new ApiException());
        when(userMicroservice.isVendor(userID)).thenThrow(new ApiException());
        when(userMicroservice.isAdmin(userID)).thenThrow(new ApiException());
        when(userMicroservice.isCourier(userID)).thenThrow(new ApiException());

        assertThrows(ApiException.class,
            () -> orderFacade.getOrders(userID));
        verify(orderDatabase, times(0)).findByCustomerID(userID);
    }

    @Test
    void getOrdersNone() throws ApiException {
        when(userMicroservice.isCustomer(1L)).thenReturn(false);
        when(userMicroservice.isVendor(1L)).thenReturn(false);
        when(userMicroservice.isAdmin(1L)).thenReturn(false);
        when(userMicroservice.isCourier(1L)).thenReturn(false);

        assertThrows(IllegalStateException.class,
            () -> orderFacade.getOrders(1L));
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
            1f,
            null,
            Order.StatusEnum.ACCEPTED);

        when(orderDatabase.getById(1L)).thenReturn(order);
        when(userMicroservice.isVendor(2L)).thenReturn(true);
        when(userMicroservice.getCustomerAddress(1L)).thenReturn(
            new Location().city("a"));
        when(dishDatabase.getById(1L)).thenReturn(dish);
        when(orderDatabase.save(new Order().customerID(1L).vendorID(2L)
            .dishes(new ArrayList<>(List.of(dishInner)))
            .location(new Location().city("a")).status(
                Order.StatusEnum.UNPAID))).thenReturn(new Order(2L,
            1L,
            2L,
            new ArrayList<>(List.of(dishInner)),
            1f,
            new Location().city("a"),
            Order.StatusEnum.UNPAID));

        Order newOrder = orderFacade.reorder(1L, 1L);
        assertEquals(order.getCustomerID(), newOrder.getCustomerID());
        assertEquals(order.getVendorID(), newOrder.getVendorID());
        assertEquals(new Location().city("a"), newOrder.getLocation());
        assertEquals(Order.StatusEnum.UNPAID, newOrder.getStatus());
    }

    @Test
    public void testReorderMalformedExceptionOrderNotExisting() {
        when(orderDatabase.getById(anyLong())).thenReturn(null);

        assertThrows(MalformedException.class,
            () -> orderFacade.reorder(1L, 1L));
    }

    @Test
    public void testReorderMalformedExceptionOrderNotOwned() {
        Order order =
            new Order(1L, 1L, 1L, null, 1f, null, Order.StatusEnum.ACCEPTED);
        when(orderDatabase.getById(1L)).thenReturn(order);

        assertThrows(MalformedException.class,
            () -> orderFacade.reorder(2L, 1L));
    }

    @Test
    public void testReorderNotFoundExceptionVendorNotExisting()
        throws Exception {
        Order order =
            new Order(1L, 1L, 1L, null, 1f, null, Order.StatusEnum.UNPAID);
        when(orderDatabase.getById(1L)).thenReturn(order);
        when(userMicroservice.isVendor(1L)).thenReturn(false);

        assertThrows(NotFoundException.class,
            () -> orderFacade.reorder(1L, 1L));
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
            1f,
            null,
            Order.StatusEnum.ACCEPTED);

        when(orderDatabase.getById(1L)).thenReturn(order);
        when(userMicroservice.isVendor(2L)).thenReturn(true);
        when(dishDatabase.getById(1L)).thenReturn(null);

        assertThrows(NotFoundException.class,
            () -> orderFacade.reorder(1L, 1L));
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
            1f,
            null,
            Order.StatusEnum.ACCEPTED);

        when(orderDatabase.getById(1L)).thenReturn(order);
        when(userMicroservice.isVendor(2L)).thenReturn(true);
        when(dishDatabase.getById(1L)).thenReturn(new Dish(1L,
            3L,
            "name",
            "description",
            new ArrayList<>(),
            1.0f));

        assertThrows(NotFoundException.class,
            () -> orderFacade.reorder(1L, 1L));
    }

    @Test
    void testRateOrderInvalidUserID() {
        assertThrows(MalformedException.class,
            () -> orderFacade.rateOrder(null, 2L, 7));
    }

    @Test
    void testRateOrderInvalidOrderID() {
        assertThrows(MalformedException.class,
            () -> orderFacade.rateOrder(1L, null, 7));
    }

    @Test
    void testRateOrderNoOrder() {
        final Location location = new Location();
        Order order = new Order(1L, 2L, 3L, new ArrayList<>(), 0F,
            location, Order.StatusEnum.ACCEPTED);
        when(orderDatabase.getById(1L)).thenReturn(null);
        assertThrows(MalformedException.class,
            () -> orderFacade.rateOrder(1L, 1L, 7));

    }

    @Test
    void testRateOrderWrongRating() {
        final Location location = new Location();
        Order order = new Order(1L, 2L, 3L, new ArrayList<>(), 0F,
            location, Order.StatusEnum.ACCEPTED);
        when(orderDatabase.getById(1L)).thenReturn(order);
        assertThrows(MalformedException.class,
            () -> orderFacade.rateOrder(2L, 1L, 11));
        assertThrows(MalformedException.class,
            () -> orderFacade.rateOrder(2L, 1L, -2));
        assertDoesNotThrow(
            () -> orderFacade.rateOrder(2L, 1L, 10));

        verify(orderDatabase, times(1)).save(order.rating(10));

        assertDoesNotThrow(
            () -> orderFacade.rateOrder(2L, 1L, 0));


    }

    @Test
    void testRateOrderDifferentCustomer() throws ApiException {
        final Location location = new Location();
        Order order = new Order(1L, 2L, 3L, new ArrayList<>(), 0F,
            location, Order.StatusEnum.ACCEPTED);
        when(orderDatabase.getById(1L)).thenReturn(order);
        when(userMicroservice.isAdmin(2L)).thenReturn(false);
        assertThrows(ForbiddenException.class,
            () -> orderFacade.rateOrder(999L, 1L, 7));

    }

    @Test
    void testRateOrderAdmin() throws ApiException {
        final Location location = new Location();
        Order order = new Order(1L, 99999L, 3L, new ArrayList<>(), 0F,
            location, Order.StatusEnum.ACCEPTED);
        when(orderDatabase.getById(1L)).thenReturn(order);
        when(userMicroservice.isAdmin(2L)).thenReturn(true);
        assertDoesNotThrow(() -> orderFacade.rateOrder(2L, 1L, 7));
    }

    @Test
    void testRateOrderAllGood() throws ApiException {
        final Location location = new Location();
        Order order = new Order(1L, 2L, 3L, new ArrayList<>(), 0F,
            location, Order.StatusEnum.ACCEPTED);
        when(orderDatabase.getById(1L)).thenReturn(order);
        when(userMicroservice.isAdmin(2L)).thenReturn(false);
        assertDoesNotThrow(() -> orderFacade.rateOrder(2L, 1L, 7));
        Order order2 = new Order(1L, 2L, 3L, new ArrayList<>(), 0F,
            location, Order.StatusEnum.ACCEPTED);
        order2.setRating(7);
        verify(orderDatabase, times(1)).save(order2);
    }

    @Test
    void testDeleteOrderById() throws ApiException {
        long userId = 1L;
        long orderId = 1L;

        var order = new Order(1L,
            1L,
            2L,
            new ArrayList<>(),
            1f,
            null,
            Order.StatusEnum.UNPAID);

        when(userMicroservice.isCustomer(userId)).thenReturn(true);
        when(orderDatabase.getById(orderId)).thenReturn(order);

        assertDoesNotThrow(() -> orderFacade.deleteOrder(userId, orderId));

        verify(orderDatabase, times(1)).delete(order);
    }

    @Test
    void testDeleteOrderByIdNotCustomer() throws ApiException {
        long userId = 1L;
        long orderId = 1L;

        var order = new Order(1L,
            1L,
            2L,
            new ArrayList<>(),
            1f,
            null,
            Order.StatusEnum.UNPAID);

        when(userMicroservice.isCustomer(userId)).thenReturn(false);
        when(orderDatabase.getById(orderId)).thenReturn(order);

        assertThrows(ForbiddenException.class,
            () -> orderFacade.deleteOrder(userId, orderId));

        verify(orderDatabase, times(0)).delete(order);
    }

    @Test
    void testDeleteOrderByIdAdmin() throws ApiException {
        long userId = 1L;
        long orderId = 1L;

        var order = new Order(1L,
            100L,
            2L,
            new ArrayList<>(),
            1f,
            null,
            Order.StatusEnum.DELIVERED);

        when(userMicroservice.isCustomer(userId)).thenReturn(false);
        when(userMicroservice.isAdmin(userId)).thenReturn(true);
        when(orderDatabase.getById(orderId)).thenReturn(order);

        assertDoesNotThrow(() -> orderFacade.deleteOrder(userId, orderId));

        verify(orderDatabase, times(1)).delete(order);
    }

    @Test
    void testDeleteOrderByIdOrderPaid() throws ApiException {
        long userId = 1L;
        long orderId = 1L;

        var order = new Order(1L,
            1L,
            2L,
            new ArrayList<>(),
            1f,
            null,
            Order.StatusEnum.ACCEPTED);

        when(userMicroservice.isCustomer(userId)).thenReturn(true);
        when(orderDatabase.getById(orderId)).thenReturn(order);

        assertThrows(ForbiddenException.class,
            () -> orderFacade.deleteOrder(userId, orderId));

        verify(orderDatabase, times(0)).delete(order);
    }

    @Test
    void testChangeOrderInvalidUserOrOrder() {
        Location location = new Location();
        Order order = new Order(1L, 2L, 3L, new ArrayList<>(),
            20F, location, Order.StatusEnum.UNPAID);

        assertThrows(MalformedException.class,
            () -> orderFacade.changeOrder(null, order));
        assertThrows(MalformedException.class,
            () -> orderFacade.changeOrder(1L, null));
    }

    @Test
    void testChangeOrderNoOrderFound() {
        Location location = new Location();
        Order order = new Order(1L, 2L, 3L, new ArrayList<>(),
            20F, location, Order.StatusEnum.UNPAID);

        when(orderDatabase.getById(1L)).thenReturn(null);

        assertThrows(MalformedException.class,
            () -> orderFacade.changeOrder(1L, order));

    }

    @Test
    void testChangeOrderCustomerPaidOrder() throws ApiException {
        Location location = new Location();
        Order order = new Order(1L, 2L, 3L, new ArrayList<>(),
            20F, location, Order.StatusEnum.UNPAID);
        Order orderRepo = new Order(1L, 2L, 3L, new ArrayList<>(),
            20F, location, Order.StatusEnum.ACCEPTED);

        when(userMicroservice.isCustomer(2L)).thenReturn(true);
        when(orderDatabase.getById(1L)).thenReturn(orderRepo);

        assertThrows(ForbiddenException.class,
            () -> orderFacade.changeOrder(2L, order));
    }

    @Test
    void testChangeOrderCustomerNotOwnOrder() throws ApiException {
        Location location = new Location();
        Order order = new Order(1L, 2L, 3L, new ArrayList<>(),
            20F, location, Order.StatusEnum.UNPAID);
        Order orderRepo = new Order(1L, 999L, 3L, new ArrayList<>(),
            20F, location, Order.StatusEnum.UNPAID);

        when(userMicroservice.isCustomer(2L)).thenReturn(true);
        when(orderDatabase.getById(1L)).thenReturn(orderRepo);

        assertThrows(ForbiddenException.class,
            () -> orderFacade.changeOrder(2L, order));
    }

    @Test
    void testChangeOrderCustomerUnauthorizedChange() throws ApiException {
        Location location = new Location();
        Order order = new Order(1L, 2L, 3L, new ArrayList<>(),
            20F, location, Order.StatusEnum.UNPAID);
        Order orderRepo = new Order(1L, 2L, 3L, new ArrayList<>(),
            200000F, location, Order.StatusEnum.UNPAID);

        when(userMicroservice.isCustomer(2L)).thenReturn(true);
        when(orderDatabase.getById(1L)).thenReturn(orderRepo);

        assertThrows(ForbiddenException.class,
            () -> orderFacade.changeOrder(2L, order));
    }

    @Test
    void testChangeOrderCustomerOk() throws ApiException {
        Location location = new Location();
        Location locationChanged =
            new Location("NL", "Delft", "Kanalweg", "9023PL");
        Order order = new Order(1L, 2L, 3L, new ArrayList<>(),
            20F, locationChanged, Order.StatusEnum.UNPAID);
        Order orderRepo = new Order(1L, 2L, 3L, new ArrayList<>(),
            20F, location, Order.StatusEnum.UNPAID);

        when(userMicroservice.isCustomer(2L)).thenReturn(true);
        when(orderDatabase.getById(1L)).thenReturn(orderRepo);

        assertDoesNotThrow(() -> {
            assertEquals(orderFacade.changeOrder(2L, order), order);
        });
        verify(orderDatabase, times(1)).save(order);
    }

    @Test
    void testChangeOrderVendorUnauthorizedChange() throws ApiException {
        Location location = new Location();
        Location locationChanged =
            new Location("NL", "Delft", "Kanalweg", "9023PL");
        Order order = new Order(1L, 2L, 3L, new ArrayList<>(),
            20F, locationChanged, Order.StatusEnum.UNPAID);
        order.setCourierRating(8);
        Order orderRepo = new Order(1L, 2L, 3L, new ArrayList<>(),
            20F, location, Order.StatusEnum.UNPAID);
        orderRepo.setCourierRating(10);

        when(userMicroservice.isVendor(3L)).thenReturn(true);
        when(orderDatabase.getById(1L)).thenReturn(orderRepo);

        assertThrows(ForbiddenException.class,
            () -> orderFacade.changeOrder(3L, order));
    }

    @Test
    void testChangeOrderVendorUnauthorizedCourierRatingChange()
        throws ApiException {
        Location location = new Location();
        Order order = new Order(1L, 2L, 3L, new ArrayList<>(),
            20F, location, Order.StatusEnum.UNPAID);
        order.setCourierRating(8);
        Order orderRepo = new Order(1L, 2L, 3L, new ArrayList<>(),
            20F, location, Order.StatusEnum.UNPAID);
        orderRepo.setCourierRating(10);

        when(userMicroservice.isVendor(3L)).thenReturn(true);
        when(orderDatabase.getById(1L)).thenReturn(orderRepo);

        assertThrows(ForbiddenException.class,
            () -> orderFacade.changeOrder(3L, order));
    }

    @Test
    void testChangeOrderVendorWrongFee() throws ApiException {
        Location location = new Location();
        Dish dish = new Dish(1L,
            2L,
            "name",
            "description",
            new ArrayList<>(),
            8.0f);
        OrderDishesInner dishInner = new OrderDishesInner(dish, 2);
        ArrayList<OrderDishesInner> dishList = new ArrayList<>();
        dishList.add(dishInner);

        Order orderRepo = new Order(1L, 2L, 3L, dishList,
            20F, location, Order.StatusEnum.UNPAID);

        when(userMicroservice.isVendor(3L)).thenReturn(true);
        when(userMicroservice.isCustomer(3L)).thenReturn(false);
        when(orderDatabase.getById(1L)).thenReturn(orderRepo);

        Order order = new Order(1L, 2L, 3L, dishList,
            15.9F, location, Order.StatusEnum.UNPAID);
        Order order2 = new Order(1L, 2L, 3L, dishList,
            16F, location, Order.StatusEnum.UNPAID);

        assertThrows(ForbiddenException.class,
            () -> orderFacade.changeOrder(3L, order));
        assertDoesNotThrow(() -> orderFacade.changeOrder(3L, order2));
    }

    @Test
    void testChangeOrderVendorOk() throws ApiException {
        Location location = new Location();
        Dish dish = new Dish(1L,
            2L,
            "name",
            "description",
            new ArrayList<>(),
            8.0f);
        OrderDishesInner dishInner = new OrderDishesInner(dish, 2);
        ArrayList<OrderDishesInner> dishList = new ArrayList<>();
        dishList.add(dishInner);

        Order order = new Order(1L, 2L, 3L, dishList,
            16F, location, Order.StatusEnum.ACCEPTED);
        order.setCourierID(99L);
        Order orderRepo = new Order(1L, 2L, 3L, dishList,
            20F, location, Order.StatusEnum.UNPAID);
        orderRepo.setCourierID(44L);

        when(userMicroservice.isCustomer(3L)).thenReturn(false);
        when(userMicroservice.isVendor(3L)).thenReturn(true);
        when(orderDatabase.getById(1L)).thenReturn(orderRepo);

        assertDoesNotThrow(() -> {
            assertEquals(orderFacade.changeOrder(3L, order), order);
        });
        verify(orderDatabase, times(1)).save(order);
    }

    @Test
    void testChangeOrderCourierChangeCourierRating() throws ApiException {
        Location location = new Location();
        Order order = new Order(1L, 2L, 3L, new ArrayList<>(),
            20F, location, Order.StatusEnum.UNPAID);
        order.setCourierRating(8);
        Order orderRepo = new Order(1L, 2L, 3L, new ArrayList<>(),
            20F, location, Order.StatusEnum.UNPAID);
        orderRepo.setCourierRating(10);

        when(userMicroservice.isCustomer(4L)).thenReturn(false);
        when(userMicroservice.isCourier(4L)).thenReturn(true);
        when(orderDatabase.getById(1L)).thenReturn(orderRepo);

        assertDoesNotThrow(() -> {
            assertEquals(orderFacade.changeOrder(4L, order), order);
        });
        verify(orderDatabase, times(1)).save(order);
    }

    @Test
    void testChangeOrderCourierOk() throws ApiException {
        Location location = new Location();
        Dish dish = new Dish(1L,
            2L,
            "name",
            "description",
            new ArrayList<>(),
            8.0f);
        OrderDishesInner dishInner = new OrderDishesInner(dish, 2);
        ArrayList<OrderDishesInner> dishList = new ArrayList<>();
        dishList.add(dishInner);

        Order order = new Order(1L, 2L, 3L, dishList,
            16F, location, Order.StatusEnum.ACCEPTED);
        order.setCourierID(99L);
        order.setCourierRating(10);
        Order orderRepo = new Order(1L, 2L, 3L, dishList,
            20F, location, Order.StatusEnum.UNPAID);
        orderRepo.setCourierID(44L);
        orderRepo.setCourierRating(8);

        when(userMicroservice.isCustomer(4L)).thenReturn(false);
        when(userMicroservice.isCourier(4L)).thenReturn(true);
        when(orderDatabase.getById(1L)).thenReturn(orderRepo);

        assertDoesNotThrow(() -> {
            assertEquals(orderFacade.changeOrder(4L, order), order);
        });
        verify(orderDatabase, times(1)).save(order);
    }

    @Test
    void testChangeOrderAdminOk() throws ApiException {

        Dish dish = new Dish(1L,
            2L,
            "name",
            "description",
            new ArrayList<>(),
            8.0f);
        OrderDishesInner dishInner = new OrderDishesInner(dish, 2);
        ArrayList<OrderDishesInner> dishList = new ArrayList<>();
        dishList.add(dishInner);

        Location location = new Location();
        Order order = new Order(11L, 22L, 33L, new ArrayList<>(),
            10F, new Location(":(", null, null, null),
            Order.StatusEnum.ACCEPTED);
        order.setCourierID(99L);
        order.setCourierRating(10);
        order.setSpecialRequirements("newReq");

        Order orderRepo = new Order(1L, 2L, 3L, dishList,
            20F, location, Order.StatusEnum.UNPAID);
        orderRepo.setCourierID(44L);
        order.setCourierRating(8);

        when(userMicroservice.isCustomer(5L)).thenReturn(false);
        when(userMicroservice.isAdmin(5L)).thenReturn(true);
        when(orderDatabase.getById(11L)).thenReturn(orderRepo);

        assertDoesNotThrow(() -> {
            assertEquals(orderFacade.changeOrder(5L, order), order);
        });
        verify(orderDatabase, times(1)).save(order);

    }

    @Test
    void testGetOrderInvalidId() {
        assertThrows(MalformedException.class,
            () -> orderFacade.getOrder(null));
    }

    @Test
    void testGetOrderMissingDish() {
        when(orderDatabase.getById(1L)).thenReturn(null);
        assertThrows(MalformedException.class,
            () -> orderFacade.getOrder(1L));
    }

    @Test
    void testGetOrderOk() {
        Location location = new Location();
        Order order = new Order(1L, 2L, 3L, new ArrayList<>(),
            20F, location, Order.StatusEnum.UNPAID);
        when(orderDatabase.getById(1L)).thenReturn(order);
        List<Order> list = new ArrayList<>();
        list.add(order);
        assertDoesNotThrow(() -> {
            assertEquals(orderFacade.getOrder(1L), list);
        });
        verify(orderDatabase, times(1)).getById(1L);
    }

}
