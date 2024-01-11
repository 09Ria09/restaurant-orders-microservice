package nl.tudelft.sem.orders.ring0;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import nl.tudelft.sem.orders.model.Dish;
import nl.tudelft.sem.orders.ports.output.DeliveryMicroservice;
import nl.tudelft.sem.orders.ports.output.DishDatabase;
import nl.tudelft.sem.orders.ports.output.LocationService;
import nl.tudelft.sem.orders.ports.output.OrderDatabase;
import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import nl.tudelft.sem.orders.result.NotFoundException;
import nl.tudelft.sem.orders.ring0.distance.RadiusStrategy;
import nl.tudelft.sem.orders.ring0.distance.SearchStrategy;
import nl.tudelft.sem.users.ApiException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VendorLogicMockitoTest {

    private static Dish goodDish;
    private static Dish badDish;
    private VendorFacade vendorLogic;
    private DishDatabase dishDatabase;
    private UserMicroservice userMicroservice;

    @BeforeAll
    static void staticSetUp() {
        goodDish = new Dish(1L, 123L, "Dish", "Description", Arrays.asList("I1", "I2", "I3"), 39.99f);
        badDish = new Dish(2L, 123L, "Dish", "Description", Arrays.asList("I1", "I2", "I3", ""), 39.99f);
        goodDish.setAllergens(List.of("Not allergic to this allergen", "neither this one!"));
        badDish.setAllergens(List.of("Allergen", "Worse Allergen"));
    }

    @BeforeEach
    void setUp() {
        dishDatabase = mock(DishDatabase.class);
        userMicroservice = mock(UserMicroservice.class);

        vendorLogic = new VendorFacade(userMicroservice, mock(OrderDatabase.class), dishDatabase,
            mock(RadiusStrategy.class), mock(SearchStrategy.class));
    }

    @Test
    void testGetDishes() throws NotFoundException {
        Long vendorId = 1L;
        List<Dish> expectedDishes = List.of(goodDish, badDish);
        when(dishDatabase.findDishesByVendorID(vendorId)).thenReturn(expectedDishes);

        List<Dish> result = vendorLogic.getDishes(vendorId);

        assertEquals(expectedDishes, result);
    }

    @Test
    void testGetDishesRemoveUserAllergies() throws ApiException, NotFoundException {
        Long vendorId = 1L;
        long userId = 2L;
        List<Dish> dishes = List.of(goodDish, badDish);
        List<String> allergies = Arrays.asList("Allergen", "Another Allergen");
        when(userMicroservice.getCustomerAllergies(userId)).thenReturn(allergies);
        when(vendorLogic.getDishes(vendorId)).thenReturn(dishes);

        List<Dish> result = vendorLogic.getDishesRemoveUserAllergies(vendorId, userId);

        assertEquals(1, result.size());
        assertEquals(goodDish, dishes.get(0));
    }

    @Test
    void testGetDishesRemoveUserAllergiesWithNullUserId() throws NotFoundException {
        Long vendorId = 1L;
        List<Dish> expectedDishes = List.of(goodDish, badDish);

        when(vendorLogic.getDishes(vendorId)).thenReturn(expectedDishes);

        List<Dish> result = vendorLogic.getDishesRemoveUserAllergies(vendorId, null);

        assertEquals(expectedDishes, result);
    }

    @Test
    void testGetDishesRemoveUserAllergiesWithApiException() throws ApiException, NotFoundException {
        Long vendorId = 1L;
        long userId = 2L;
        List<Dish> expectedDishes = List.of(goodDish, badDish);
        when(vendorLogic.getDishes(vendorId)).thenReturn(expectedDishes);
        when(userMicroservice.getCustomerAllergies(userId)).thenThrow(new ApiException());

        List<Dish> result = vendorLogic.getDishesRemoveUserAllergies(vendorId, userId);

        assertEquals(expectedDishes, result);
    }
}