package nl.tudelft.sem.orders.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import io.swagger.annotations.Api;
import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import nl.tudelft.sem.orders.result.ForbiddenException;
import nl.tudelft.sem.orders.result.MalformedException;
import nl.tudelft.sem.orders.ring0.VendorLogic;
import nl.tudelft.sem.users.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;


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
    void vendorOrderCustomerOK () throws ApiException {
        Order order = new Order();
        List<Order> expected = new ArrayList<>();
        expected.add(order);
        long vendorID = 1;
        long customerID = 2;
        when(userMicroservice.isVendor(1)).thenReturn(true);
        when(userMicroservice.isCustomer(2)).thenReturn(true);
        when(vendorLogic.getPastOrdersForCustomer(1L,2L)).thenReturn(expected);

        ResponseEntity<List<Order>> expectedResponse = ResponseEntity.ok(expected);
        ResponseEntity<List<Order>> actual = vendorController.vendorCustomerIDPastGet(vendorID,customerID);

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
    void vendorOrderCustomerNotVendor () throws ApiException {
        long vendorID = 1;
        long customerID = 2;
        when(userMicroservice.isVendor(1)).thenReturn(false);
        when(userMicroservice.isCustomer(2)).thenReturn(true);

        ResponseEntity<List<Order>> expectedResponse = ResponseEntity.badRequest().build();
        ResponseEntity<List<Order>> actual = vendorController.vendorCustomerIDPastGet(vendorID,customerID);

        assertEquals(expectedResponse, actual);
    }

    @Test
    void vendorOrderCustomerNotCustomer() throws ApiException {
        long vendorID = 1;
        long customerID = 2;
        when(userMicroservice.isVendor(1)).thenReturn(true);
        when(userMicroservice.isCustomer(2)).thenReturn(false);

        ResponseEntity<List<Order>> expectedResponse = ResponseEntity.badRequest().build();
        ResponseEntity<List<Order>> actual = vendorController.vendorCustomerIDPastGet(vendorID,customerID);

        assertEquals(expectedResponse, actual);
    }

    @Test
    void vendorOrderCustomerException1() throws ApiException {
        long vendorID = 1;
        long customerID = 2;
        when(userMicroservice.isVendor(1)).thenThrow(new ApiException("L Request"));
        when(userMicroservice.isCustomer(2)).thenReturn(true);

        ResponseEntity<List<Order>> expectedResponse = ResponseEntity.badRequest().build();
        ResponseEntity<List<Order>> actual = vendorController.vendorCustomerIDPastGet(vendorID,customerID);

        assertEquals(expectedResponse, actual);
    }

    @Test
    void vendorOrderCustomerException2() throws ApiException {
        long vendorID = 1;
        long customerID = 2;
        when(userMicroservice.isVendor(1)).thenReturn(true);
        when(userMicroservice.isCustomer(2)).thenThrow(new ApiException("L Request"));

        ResponseEntity<List<Order>> expectedResponse = ResponseEntity.badRequest().build();
        ResponseEntity<List<Order>> actual = vendorController.vendorCustomerIDPastGet(vendorID,customerID);

        assertEquals(expectedResponse, actual);
    }
}
