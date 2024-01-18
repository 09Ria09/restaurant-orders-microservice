package nl.tudelft.sem.orders.ring0;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import nl.tudelft.sem.orders.model.Analytic;
import nl.tudelft.sem.orders.model.Dish;
import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.ports.output.DishDatabase;
import nl.tudelft.sem.orders.ports.output.OrderDatabase;
import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import nl.tudelft.sem.orders.result.ForbiddenException;
import nl.tudelft.sem.orders.result.MalformedException;
import nl.tudelft.sem.orders.result.NotFoundException;
import nl.tudelft.sem.orders.ring0.distance.RadiusStrategy;
import nl.tudelft.sem.orders.ring0.distance.SearchStrategy;
import nl.tudelft.sem.orders.ring0.distance.SimpleWordMatchStrategy;
import nl.tudelft.sem.users.ApiException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VendorLogicMockitoTest {

    private static Dish goodDish;
    private static Dish badDish;
    private static Dish nullDish;
    private VendorFacade vendorFacade;
    private DishDatabase dishDatabase;
    private UserMicroservice userMicroservice;
    private VendorAnalytics vendorAnalytics;

    OrderDatabase orderDatabase;

    @BeforeAll
    static void staticSetUp() {
        goodDish = new Dish(1L, 123L, "Dish", "Description", Arrays.asList("I1", "I2", "I3"), 39.99f);
        badDish = new Dish(2L, 123L, "Dish", "Description", Arrays.asList("I1", "I2", "I3", ""), 39.99f);
        nullDish = new Dish(2L, 123L, "Dish", "Description", Arrays.asList("I1", "I2", "I3", ""), 39.99f);
        goodDish.setAllergens(List.of("Not allergic to this allergen", "neither this one!"));
        badDish.setAllergens(List.of("Allergen", "Worse Allergen"));
        nullDish.setAllergens(null);
    }

    @BeforeEach
    void setUp() {
        dishDatabase = mock(DishDatabase.class);
        userMicroservice = mock(UserMicroservice.class);
        orderDatabase = mock(OrderDatabase.class);
        vendorAnalytics = mock(VendorAnalytics.class);

        vendorFacade = new VendorFacade(userMicroservice, orderDatabase, dishDatabase,
                mock(RadiusStrategy.class), new SimpleWordMatchStrategy(), vendorAnalytics);
    }

    @Test
    void testAddDish() throws ApiException {
        var ings = new ArrayList<String>();
        ings.add("abc");
        Dish d = new Dish().dishID(3L).name("asd").vendorID(2L).description("test").ingredients(ings).price(1f);

        when(userMicroservice.isVendor(2L)).thenReturn(true);

        var ret = assertDoesNotThrow(() -> vendorFacade.addDish(d));

        var d2 = new Dish().name("asd").vendorID(2L).description("test").ingredients(ings).price(1f);
        verify(dishDatabase, times(1)).save(d2);

        assertEquals(ret.get(0), d2);
    }

    @Test
    void vendorsInRadius() throws ApiException {
        when(userMicroservice.isCustomer(2L)).thenReturn(true);

        List<Long> retried = assertDoesNotThrow(() ->
            vendorFacade.vendorsInRadius(2L, "none", null));

        assertEquals(retried, new ArrayList<>());
    }

    @Test
    void testGetDishes() throws NotFoundException {
        Long vendorId = 1L;
        List<Dish> expectedDishes = List.of(goodDish, badDish);
        when(dishDatabase.findDishesByVendorID(vendorId)).thenReturn(expectedDishes);

        List<Dish> result = vendorFacade.getDishes(vendorId);

        assertEquals(expectedDishes, result);
    }

    @Test
    void testGetDishesRemoveUserAllergies() throws ApiException, NotFoundException {
        Long vendorId = 1L;
        long userId = 2L;
        List<Dish> dishes = List.of(goodDish, badDish, nullDish);
        List<String> allergies = Arrays.asList("Allergen", "Another Allergen");
        when(userMicroservice.getCustomerAllergies(userId)).thenReturn(allergies);
        when(vendorFacade.getDishes(vendorId)).thenReturn(dishes);

        List<Dish> result = vendorFacade.getDishesRemoveUserAllergies(vendorId, userId);

        assertEquals(2, result.size());
        assertEquals(goodDish, result.get(0));
        assertEquals(nullDish, result.get(1));
    }

    @Test
    void testGetDishesRemoveUserAllergiesWithNullUserId() throws NotFoundException {
        Long vendorId = 1L;
        List<Dish> expectedDishes = List.of(goodDish, badDish);

        when(vendorFacade.getDishes(vendorId)).thenReturn(expectedDishes);

        List<Dish> result = vendorFacade.getDishesRemoveUserAllergies(vendorId, null);

        assertEquals(expectedDishes, result);
    }

    @Test
    void testGetDishesRemoveUserAllergiesWithApiException() throws ApiException, NotFoundException {
        Long vendorId = 1L;
        long userId = 2L;
        List<Dish> expectedDishes = List.of(goodDish, badDish);
        when(vendorFacade.getDishes(vendorId)).thenReturn(expectedDishes);
        when(userMicroservice.getCustomerAllergies(userId)).thenThrow(new ApiException());

        List<Dish> result = vendorFacade.getDishesRemoveUserAllergies(vendorId, userId);

        assertEquals(expectedDishes, result);
    }

    @Test
    void vendorOrderCustomerNotCustomer() throws ApiException, ForbiddenException {
        long vendorID = 1;
        long customerID = 2;
        when(userMicroservice.isVendor(1)).thenReturn(true);
        when(userMicroservice.isCustomer(2)).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> vendorFacade.getPastOrdersForCustomer(1L, 2L));
    }

    @Test
    void vendorOrderCustomerNotVendor() throws ApiException, ForbiddenException {
        long vendorID = 1;
        long customerID = 2;
        when(userMicroservice.isVendor(1)).thenReturn(false);
        when(userMicroservice.isCustomer(2)).thenReturn(true);

        assertThrows(ForbiddenException.class, () -> vendorFacade.getPastOrdersForCustomer(1L, 2L));
    }

    @Test
    void vendorOrderCustomerException() throws ApiException, ForbiddenException {
        long vendorID = 1;
        long customerID = 2;
        when(userMicroservice.isVendor(1)).thenReturn(true);
        when(userMicroservice.isCustomer(2)).thenThrow(new ApiException("L Request"));


        assertThrows(ForbiddenException.class, () -> vendorFacade.getPastOrdersForCustomer(1L, 2L));
    }

    @Test
    void vendorOrderCustomerOk() throws ApiException, ForbiddenException {
        long vendorID = 1;
        long customerID = 2;
        Order order = new Order();
        List<Order> expected = new ArrayList<>();
        expected.add(order);

        when(userMicroservice.isVendor(1)).thenReturn(true);
        when(userMicroservice.isCustomer(2)).thenReturn(true);

        when(orderDatabase.findByVendorIDAndCustomerID(1L, 2L)).thenReturn(expected);
        assertEquals(expected, vendorFacade.getPastOrdersForCustomer(1L, 2L));
    }

    @Test
    void vendorAnalytics() throws MalformedException {
        var ret = new ArrayList<Analytic>();

        ret.add(new Analytic());

        when(vendorAnalytics.analyseOrders(2115L)).thenReturn(ret);

        assertEquals(ret, vendorFacade.getVendorAnalysis(2115L));

        verify(vendorAnalytics, times(1)).analyseOrders(2115L);
    }
}