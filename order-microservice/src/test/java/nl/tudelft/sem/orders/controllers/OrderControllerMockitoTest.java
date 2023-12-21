package nl.tudelft.sem.orders.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import javax.persistence.EntityNotFoundException;
import nl.tudelft.sem.orders.adapters.mocks.MockLocationAdapter;
import nl.tudelft.sem.orders.adapters.remote.UserRemoteAdapter;
import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.model.OrderOrderIDDishesPut200Response;
import nl.tudelft.sem.orders.model.OrderOrderIDDishesPutRequest;
import nl.tudelft.sem.orders.ports.output.LocationService;
import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import nl.tudelft.sem.orders.ring0.OrderLogic;
import nl.tudelft.sem.users.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class OrderControllerMockitoTest {

    private UserMicroservice userMicroservice;
    private LocationService locationService;
    private OrderLogic orderLogic;
    private OrderController orderController;

    @BeforeEach
    public void setUp() {
        userMicroservice = mock(UserMicroservice.class);
        locationService = mock(MockLocationAdapter.class);
        orderLogic = mock(OrderLogic.class);
        orderController = new OrderController(orderLogic, userMicroservice, locationService);
    }

    @Test
    void orderPostOk() throws ApiException {
        long userID = 1L;
        long vendorID = 2L;

        when(locationService.isCloseBy(any(), any())).thenReturn(true);
        when(userMicroservice.isCustomer(userID)).thenReturn(true);
        when(orderLogic.createOrder(userID, vendorID)).thenReturn(new Order());

        ResponseEntity<Order> responseEntity = orderController.orderPost(userID, vendorID);

        assertEquals(ResponseEntity.ok(new Order()), responseEntity);
        verify(userMicroservice).getCustomerAddress(userID);
        verify(userMicroservice).getVendorAddress(vendorID);
        verify(userMicroservice).isCustomer(userID);
        verify(orderLogic).createOrder(userID, vendorID);
    }

    @Test
    void orderPostForbidden() throws ApiException {
        long userID = 1L;
        long vendorID = 2L;

        when(locationService.isCloseBy(any(), any())).thenReturn(true);
        when(userMicroservice.isCustomer(userID)).thenReturn(false);
        when(orderLogic.createOrder(userID, vendorID)).thenReturn(new Order());

        ResponseEntity<Order> responseEntity = orderController.orderPost(userID, vendorID);

        assertEquals(ResponseEntity.status(HttpStatus.FORBIDDEN).build(), responseEntity);
        verify(userMicroservice).isCustomer(userID);
        verifyNoInteractions(orderLogic);
    }

    @Test
    void orderPostBadRequest() throws ApiException {
        long userID = 1L;
        long vendorID = 2L;
        when(locationService.isCloseBy(any(), any())).thenReturn(false);
        when(userMicroservice.isCustomer(userID)).thenReturn(true);
        when(orderLogic.createOrder(userID, vendorID)).thenReturn(new Order());

        ResponseEntity<Order> responseEntity = orderController.orderPost(userID, vendorID);

        assertEquals(ResponseEntity.status(HttpStatus.BAD_REQUEST).build(), responseEntity);
        verify(locationService).isCloseBy(any(), any());
        verifyNoInteractions(orderLogic);
    }

    @Test
    void orderPostApiExceptionBadRequest() throws ApiException {
        long userID = 1L;
        long vendorID = 2L;
        when(locationService.isCloseBy(any(), any())).thenReturn(false);
        when(userMicroservice.isCustomer(userID)).thenThrow(new ApiException());
        when(orderLogic.createOrder(userID, vendorID)).thenReturn(new Order());

        ResponseEntity<Order> responseEntity = orderController.orderPost(userID, vendorID);

        assertEquals(ResponseEntity.status(HttpStatus.BAD_REQUEST).build(), responseEntity);
        verifyNoInteractions(orderLogic);
    }

    @Test
    void orderOrderIDDishesPutOk() throws ApiException {
        long userID = 1L;
        long orderID = 2L;
        OrderOrderIDDishesPutRequest request = new OrderOrderIDDishesPutRequest();
        when(userMicroservice.isCustomer(userID)).thenReturn(true);
        when(orderLogic.updateDishes(orderID, userID, request.getDishes())).thenReturn(10.0F);

        ResponseEntity<OrderOrderIDDishesPut200Response> responseEntity =
            orderController.orderOrderIDDishesPut(userID, orderID, request);

        OrderOrderIDDishesPut200Response expectedResponse = new OrderOrderIDDishesPut200Response();
        expectedResponse.setPrice(10f);

        assertEquals(ResponseEntity.ok(expectedResponse), responseEntity);
        verify(userMicroservice).isCustomer(userID);
        verify(orderLogic).updateDishes(orderID, userID, request.getDishes());
    }

    @Test
    void orderOrderIDDishesPutForbidden() throws ApiException {
        long userID = 1L;
        long orderID = 2L;
        OrderOrderIDDishesPutRequest request = new OrderOrderIDDishesPutRequest();
        when(userMicroservice.isCustomer(userID)).thenReturn(false);
        when(orderLogic.updateDishes(orderID, userID, request.getDishes())).thenReturn(10.0F);

        ResponseEntity<OrderOrderIDDishesPut200Response> responseEntity =
            orderController.orderOrderIDDishesPut(userID, orderID, request);

        assertEquals(ResponseEntity.status(HttpStatus.FORBIDDEN).build(), responseEntity);
        verify(userMicroservice).isCustomer(userID);
        verifyNoInteractions(orderLogic);
    }

    @Test
    void orderOrderIDDishesPutBadRequest() throws ApiException {
        long userID = 1L;
        long orderID = 2L;
        OrderOrderIDDishesPutRequest request = new OrderOrderIDDishesPutRequest();
        when(userMicroservice.isCustomer(userID)).thenReturn(true);
        when(orderLogic.updateDishes(orderID, userID, request.getDishes())).thenThrow(new IllegalStateException());

        ResponseEntity<OrderOrderIDDishesPut200Response> responseEntity =
            orderController.orderOrderIDDishesPut(userID, orderID, request);

        assertEquals(ResponseEntity.status(HttpStatus.BAD_REQUEST).build(), responseEntity);
        verify(userMicroservice).isCustomer(userID);
        verify(orderLogic).updateDishes(orderID, userID, request.getDishes());
    }

    @Test
    void orderOrderIDDishesPutApiExceptionBadRequest() throws ApiException {
        long userID = 1L;
        long orderID = 2L;
        OrderOrderIDDishesPutRequest request = new OrderOrderIDDishesPutRequest();
        when(userMicroservice.isCustomer(userID)).thenThrow(new ApiException());
        when(orderLogic.updateDishes(orderID, userID, request.getDishes())).thenThrow(new IllegalStateException());

        ResponseEntity<OrderOrderIDDishesPut200Response> responseEntity =
            orderController.orderOrderIDDishesPut(userID, orderID, request);

        assertEquals(ResponseEntity.status(HttpStatus.FORBIDDEN).build(), responseEntity);
        verify(userMicroservice).isCustomer(userID);
        verifyNoInteractions(orderLogic);
    }

    @Test
    void orderOrderIDDishesPutNotFound() throws ApiException {
        long userID = 1L;
        long orderID = 2L;
        OrderOrderIDDishesPutRequest request = new OrderOrderIDDishesPutRequest();
        when(userMicroservice.isCustomer(userID)).thenReturn(true);
        when(orderLogic.updateDishes(orderID, userID, request.getDishes())).thenThrow(new EntityNotFoundException());

        ResponseEntity<OrderOrderIDDishesPut200Response> responseEntity =
            orderController.orderOrderIDDishesPut(userID, orderID, request);

        assertEquals(ResponseEntity.status(HttpStatus.NOT_FOUND).build(), responseEntity);
        verify(userMicroservice).isCustomer(userID);
        verify(orderLogic).updateDishes(orderID, userID, request.getDishes());
    }
}
