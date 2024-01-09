package nl.tudelft.sem.orders.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.swagger.annotations.Api;
import java.util.ArrayList;
import java.util.List;
import nl.tudelft.sem.orders.model.Dish;
import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.ports.output.DeliveryMicroservice;
import nl.tudelft.sem.orders.ports.output.DishDatabase;
import nl.tudelft.sem.orders.ports.output.LocationService;
import nl.tudelft.sem.orders.ports.output.OrderDatabase;
import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import nl.tudelft.sem.orders.result.ForbiddenException;
import nl.tudelft.sem.orders.result.MalformedException;
import nl.tudelft.sem.orders.ring0.VendorLogic;
import nl.tudelft.sem.users.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;



class VendorControllerMockitoTest {

    private UserMicroservice userMicroservice;
    private VendorLogic vendorLogic;
    private VendorLogic vendorLogicNOTmocked;
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
        vendorLogic = mock(VendorLogic.class);

        vendorLogicNOTmocked = new VendorLogic(orderDatabase, userMicroservice, deliveryMicroservice,
                locationService, dishDatabase);
        vendorController = new VendorController(vendorLogic, userMicroservice);
        vendorController2 = new VendorController(vendorLogicNOTmocked, userMicroservice);
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
        doThrow(ForbiddenException.class).when(vendorLogic).deleteDishById(1L, 1L);
        ResponseEntity<Void> response = vendorController.vendorDishDishIDDelete(1L, 1L);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void vendorDishDishIDDeleteForbiddenMalformed() throws ForbiddenException, MalformedException {
        doThrow(MalformedException.class).when(vendorLogic).deleteDishById(1L, 1L);
        ResponseEntity<Void> response = vendorController.vendorDishDishIDDelete(1L, 1L);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void vendorDishDishIDDeleteOK() {
        ResponseEntity<Void> response = vendorController.vendorDishDishIDDelete(1L, 1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void vendorOrderCustomerOK() throws ApiException, ForbiddenException {
        Order order = new Order();
        List<Order> expected = new ArrayList<>();
        expected.add(order);
        long vendorID = 1;
        long customerID = 2;
        when(vendorLogic.getPastOrdersForCustomer(1L, 2L)).thenReturn(expected);

        ResponseEntity<List<Order>> expectedResponse = ResponseEntity.ok(expected);
        ResponseEntity<List<Order>> actual = vendorController.vendorCustomerIDPastGet(vendorID, customerID);

        assertEquals(expectedResponse, actual);
    }

    @Test
    void vendorOrderCustomerNull1() throws ApiException {
        Order order = new Order();
        List<Order> expected = new ArrayList<>();
        expected.add(order);
        long vendorID = 1;
        long customerID = 2;

        ResponseEntity<List<Order>> expectedResponse = ResponseEntity.badRequest().build();
        assertEquals(expectedResponse, vendorController.vendorCustomerIDPastGet(null, customerID));
    }

    @Test
    void vendorOrderCustomerNull2() throws ApiException {
        Order order = new Order();
        List<Order> expected = new ArrayList<>();
        expected.add(order);
        long vendorID = 1;
        long customerID = 2;

        ResponseEntity<List<Order>> expectedResponse = ResponseEntity.badRequest().build();
        assertEquals(expectedResponse, vendorController.vendorCustomerIDPastGet(vendorID, null));
    }

    @Test
    void vendorOrderCustomerNotVendorOrCustomer() throws ApiException, ForbiddenException {
        long vendorID = 1;
        long customerID = 2;

        when(vendorLogic.getPastOrdersForCustomer(1L, 2L)).thenThrow(new ForbiddenException());

        ResponseEntity<List<Order>> expectedResponse = ResponseEntity.badRequest().build();
        ResponseEntity<List<Order>> actual = vendorController.vendorCustomerIDPastGet(vendorID, customerID);

        assertEquals(expectedResponse, actual);
    }
}
