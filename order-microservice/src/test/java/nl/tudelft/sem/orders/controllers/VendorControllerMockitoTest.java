package nl.tudelft.sem.orders.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import nl.tudelft.sem.orders.model.Dish;
import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import nl.tudelft.sem.orders.ring0.VendorLogic;
import nl.tudelft.sem.users.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;



class VendorControllerMockitoTest {

    private UserMicroservice userMicroservice;
    private VendorLogic vendorLogic;
    private VendorController vendorController;

    @BeforeEach
    public void setUp() {
        userMicroservice = mock(UserMicroservice.class);
        vendorLogic = mock(VendorLogic.class);
        vendorController = new VendorController(vendorLogic, userMicroservice);
    }

    @Test
    void vendorDishPostBadRequest() {
        Dish d = new Dish(null, 11L, "potato", "good",
                List.of("potatofirsthalf", "potatosecondhalf"), 5.5F);
        assertEquals(HttpStatus.BAD_REQUEST,
                vendorController.vendorDishPost(d)
                        .getStatusCode());
        Dish d2 = new Dish(1L, null, "potato", "good",
                List.of("potatofirsthalf", "potatosecondhalf"), 5.5F);
        assertEquals(HttpStatus.BAD_REQUEST,
                vendorController.vendorDishPost(d2)
                        .getStatusCode());
    }

    @Test
    void vendorDishPostUnauthorized() throws ApiException {
        Dish d = new Dish(1L, 11L, "potato", "good",
                List.of("potatofirsthalf", "potatosecondhalf"), 5.5F);
        when(userMicroservice.isVendor(11L)).thenReturn(false);
        assertEquals(HttpStatus.UNAUTHORIZED,
                vendorController.vendorDishPost(d)
                        .getStatusCode());
    }

    @Test
    void vendorDishPostOK() throws ApiException {
        Dish d = new Dish(1L, 11L, "potato", "good",
                List.of("potatofirsthalf", "potatosecondhalf"), 5.5F);
        when(userMicroservice.isVendor(11L)).thenReturn(true);
        assertEquals(HttpStatus.OK,
                vendorController.vendorDishPost(d)
                        .getStatusCode());
    }

    @Test
    void vendorDishPutBadRequest() {
        Dish d = new Dish(null, 11L, "potato", "good",
                List.of("potatofirsthalf", "potatosecondhalf"), 5.5F);
        assertEquals(HttpStatus.BAD_REQUEST,
                vendorController.vendorDishPut(d)
                        .getStatusCode());
        Dish d2 = new Dish(1L, null, "potato", "good",
                List.of("potatofirsthalf", "potatosecondhalf"), 5.5F);
        assertEquals(HttpStatus.BAD_REQUEST,
                vendorController.vendorDishPut(d2)
                        .getStatusCode());
    }

    @Test
    void vendorDishPutNotFound() {
        Dish d = new Dish(1L, 11L, "potato", "good",
                List.of("potatofirsthalf", "potatosecondhalf"), 5.5F);
        when(vendorLogic.dishExists(1L)).thenReturn(false);
        assertEquals(HttpStatus.NOT_FOUND,
                vendorController.vendorDishPut(d)
                        .getStatusCode());
    }

    @Test
    void vendorDishPutOK() {
        Dish d = new Dish(1L, 11L, "potato", "good",
                List.of("potatofirsthalf", "potatosecondhalf"), 5.5F);
        when(vendorLogic.dishExists(1L)).thenReturn(true);
        assertEquals(HttpStatus.OK,
                vendorController.vendorDishPut(d)
                        .getStatusCode());
    }
}