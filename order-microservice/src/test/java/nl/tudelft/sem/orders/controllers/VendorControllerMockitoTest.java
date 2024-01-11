package nl.tudelft.sem.orders.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import nl.tudelft.sem.orders.model.Dish;
import nl.tudelft.sem.orders.ports.output.DeliveryMicroservice;
import nl.tudelft.sem.orders.ports.output.DishDatabase;
import nl.tudelft.sem.orders.ports.output.LocationService;
import nl.tudelft.sem.orders.ports.output.OrderDatabase;
import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import nl.tudelft.sem.orders.result.ForbiddenException;
import nl.tudelft.sem.orders.result.MalformedException;
import nl.tudelft.sem.orders.result.NotFoundException;
import nl.tudelft.sem.orders.ring0.VendorFacade;
import nl.tudelft.sem.orders.ring0.distance.RadiusStrategy;
import nl.tudelft.sem.orders.ring0.distance.SearchStrategy;
import nl.tudelft.sem.users.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


class VendorControllerMockitoTest {

    private UserMicroservice userMicroservice;
    private VendorFacade vendorFacade;
    private VendorFacade vendorFacadeNOTmocked;
    private VendorController vendorController;
    private VendorController vendorController2;
    private OrderDatabase orderDatabase;
    private DeliveryMicroservice deliveryMicroservice;
    private LocationService locationService;
    private DishDatabase dishDatabase;

    @BeforeEach
    public void setUp() {
        userMicroservice = mock(UserMicroservice.class);
        orderDatabase = mock(OrderDatabase.class);
        deliveryMicroservice = mock(DeliveryMicroservice.class);
        locationService = mock(LocationService.class);
        dishDatabase = mock(DishDatabase.class);
        vendorFacade = mock(VendorFacade.class);

        vendorFacadeNOTmocked = new VendorFacade(userMicroservice, orderDatabase, dishDatabase,
            mock(RadiusStrategy.class), mock(SearchStrategy.class)
        );

        vendorController = new VendorController(vendorFacade);
        vendorController2 = new VendorController(vendorFacadeNOTmocked);
    }

    @Test
    void vendorDishPostBadRequest() throws ApiException {
        Dish d = new Dish(null, 11L, "potato", "good",
                List.of("potatofirsthalf", "potatosecondhalf"), 5.5F);
        assertEquals(HttpStatus.BAD_REQUEST,
                vendorController2.vendorDishPost(d)
                        .getStatusCode());

        Dish d2 = new Dish(1L, null, "potato", "good",
                List.of("potatofirsthalf", "potatosecondhalf"), 5.5F);
        assertEquals(HttpStatus.BAD_REQUEST,
                vendorController2.vendorDishPost(d2).getStatusCode());
    }

    @Test
    void vendorDishPostUnauthorized() throws ApiException {
        Dish d = new Dish(1L, 11L, "potato", "good",
                List.of("potatofirsthalf", "potatosecondhalf"), 5.5F);
        when(userMicroservice.isVendor(11L)).thenReturn(false);
        assertEquals(HttpStatus.UNAUTHORIZED,
                vendorController2.vendorDishPost(d)
                        .getStatusCode());
    }

    @Test
    void vendorDishPostOK() throws ApiException {
        Dish d = new Dish(1L, 11L, "potato", "good",
                List.of("potatofirsthalf", "potatosecondhalf"), 5.5F);
        when(userMicroservice.isVendor(11L)).thenReturn(true);
        assertEquals(HttpStatus.OK,
                vendorController2.vendorDishPost(d)
                        .getStatusCode());
    }

    @Test
    void vendorDishPutBadRequest() {
        Dish d = new Dish(null, 11L, "potato", "good",
                List.of("potatofirsthalf", "potatosecondhalf"), 5.5F);
        assertEquals(HttpStatus.BAD_REQUEST,
                vendorController2.vendorDishPut(d)
                        .getStatusCode());
        Dish d2 = new Dish(1L, null, "potato", "good",
                List.of("potatofirsthalf", "potatosecondhalf"), 5.5F);
        assertEquals(HttpStatus.BAD_REQUEST,
                vendorController2.vendorDishPut(d2)
                        .getStatusCode());
    }

    @Test
    void vendorDishPutNotFound() {
        Dish d = new Dish(1L, 11L, "potato", "good",
                List.of("potatofirsthalf", "potatosecondhalf"), 5.5F);
        when(dishDatabase.getById(1L)).thenReturn(null);
        assertEquals(HttpStatus.NOT_FOUND,
                vendorController2.vendorDishPut(d)
                        .getStatusCode());
    }

    @Test
    void vendorDishPutOK() {
        Dish d = new Dish(1L, 11L, "potato", "good",
                List.of("potatofirsthalf", "potatosecondhalf"), 5.5F);
        when(dishDatabase.getById(1L)).thenReturn(d);
        assertEquals(HttpStatus.OK,
                vendorController2.vendorDishPut(d)
                        .getStatusCode());
    }

    @Test
    void vendorDishDishIDDeleteForbidden() throws ForbiddenException, MalformedException {
        doThrow(ForbiddenException.class).when(vendorFacade).deleteDishById(1L, 1L);
        ResponseEntity<Void> response = vendorController.vendorDishDishIDDelete(1L, 1L);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void vendorDishDishIDDeleteForbiddenMalformed() throws ForbiddenException, MalformedException {
        doThrow(MalformedException.class).when(vendorFacade).deleteDishById(1L, 1L);
        ResponseEntity<Void> response = vendorController.vendorDishDishIDDelete(1L, 1L);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void vendorDishDishIDDeleteOK() {
        ResponseEntity<Void> response = vendorController.vendorDishDishIDDelete(1L, 1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testVendorDishVendorIDGet() throws NotFoundException {
        Long vendorId = 1L;
        Long userId = 2L;
        List<Dish> expectedDishes = List.of(new Dish(), new Dish(), new Dish());
        when(vendorFacade.getDishesRemoveUserAllergies(vendorId, userId)).thenReturn(expectedDishes);

        ResponseEntity<List<Dish>> result = vendorController.vendorDishVendorIDGet(vendorId, userId);

        assertEquals(ResponseEntity.ok(expectedDishes), result);
    }

    @Test
    void testVendorDishVendorIDGetWithNotFoundException() throws NotFoundException {
        Long vendorId = 1L;
        Long userId = 2L;
        when(vendorFacade.getDishesRemoveUserAllergies(vendorId, userId)).thenThrow(new NotFoundException());

        ResponseEntity<List<Dish>> result = vendorController.vendorDishVendorIDGet(vendorId, userId);

        assertEquals(ResponseEntity.badRequest().build(), result);
    }
}
