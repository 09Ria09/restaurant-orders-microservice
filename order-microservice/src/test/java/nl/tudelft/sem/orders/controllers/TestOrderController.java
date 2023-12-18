package nl.tudelft.sem.orders.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import javax.persistence.EntityNotFoundException;
import nl.tudelft.sem.orders.adapters.LocationMicroserviceAdapter;
import nl.tudelft.sem.orders.adapters.UserMicroserviceAdapter;
import nl.tudelft.sem.orders.controllers.OrderController;
import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.model.OrderOrderIDDishesPut200Response;
import nl.tudelft.sem.orders.model.OrderOrderIDDishesPutRequest;
import nl.tudelft.sem.orders.ring0.OrderFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class TestOrderController {

    private UserMicroserviceAdapter userMicroservice;
    private LocationMicroserviceAdapter locationMicroservice;
    private OrderFacade ordersFacade;
    private OrderController orderController;

    @BeforeEach
    public void setUp() {
        userMicroservice = mock(UserMicroserviceAdapter.class);
        locationMicroservice = mock(LocationMicroserviceAdapter.class);
        ordersFacade = mock(OrderFacade.class);
        orderController = new OrderController(ordersFacade, userMicroservice, locationMicroservice);
    }

    @Test
    void orderPostOk() {
        long userID = 1L;
        long vendorID = 2L;

        when(locationMicroservice.isCloseBy(any(), any())).thenReturn(true);
        when(userMicroservice.isCustomer(userID)).thenReturn(true);
        when(ordersFacade.createOrder(userID, vendorID)).thenReturn(new Order());

        ResponseEntity<Order> responseEntity = orderController.orderPost(userID, vendorID);

        assertEquals(ResponseEntity.ok(new Order()), responseEntity);
        verify(userMicroservice).getCustomerAddress(userID);
        verify(userMicroservice).getVendorAddress(vendorID);
        verify(userMicroservice).isCustomer(userID);
        verify(ordersFacade).createOrder(userID, vendorID);
    }

    @Test
    void orderPostForbidden() {
        long userID = 1L;
        long vendorID = 2L;

        when(locationMicroservice.isCloseBy(any(), any())).thenReturn(true);
        when(userMicroservice.isCustomer(userID)).thenReturn(false);
        when(ordersFacade.createOrder(userID, vendorID)).thenReturn(new Order());

        ResponseEntity<Order> responseEntity = orderController.orderPost(userID, vendorID);

        assertEquals(ResponseEntity.status(HttpStatus.FORBIDDEN).build(), responseEntity);
        verify(userMicroservice).isCustomer(userID);
        verifyNoInteractions(ordersFacade);
    }

    @Test
    void orderPostBadRequest() {
        long userID = 1L;
        long vendorID = 2L;
        when(locationMicroservice.isCloseBy(any(), any())).thenReturn(false);
        when(userMicroservice.isCustomer(userID)).thenReturn(true);
        when(ordersFacade.createOrder(userID, vendorID)).thenReturn(new Order());

        ResponseEntity<Order> responseEntity = orderController.orderPost(userID, vendorID);

        assertEquals(ResponseEntity.status(HttpStatus.BAD_REQUEST).build(), responseEntity);
        verify(locationMicroservice).isCloseBy(any(), any());
        verifyNoInteractions(ordersFacade);
    }

    @Test
    void orderOrderIDDishesPutOk() {
        long userID = 1L;
        long orderID = 2L;
        OrderOrderIDDishesPutRequest request = new OrderOrderIDDishesPutRequest();
        when(userMicroservice.isCustomer(userID)).thenReturn(true);
        when(ordersFacade.updateDishes(orderID, userID, request.getDishes())).thenReturn(10.0F);

        ResponseEntity<OrderOrderIDDishesPut200Response> responseEntity =
            orderController.orderOrderIDDishesPut(userID, orderID, request);

        OrderOrderIDDishesPut200Response expectedResponse = new OrderOrderIDDishesPut200Response();
        expectedResponse.setPrice(10f);

        assertEquals(ResponseEntity.ok(expectedResponse), responseEntity);
        verify(userMicroservice).isCustomer(userID);
        verify(ordersFacade).updateDishes(orderID, userID, request.getDishes());
    }

    @Test
    void orderOrderIDDishesPutForbidden() {
        long userID = 1L;
        long orderID = 2L;
        OrderOrderIDDishesPutRequest request = new OrderOrderIDDishesPutRequest();
        when(userMicroservice.isCustomer(userID)).thenReturn(false);
        when(ordersFacade.updateDishes(orderID, userID, request.getDishes())).thenReturn(10.0F);

        ResponseEntity<OrderOrderIDDishesPut200Response> responseEntity =
            orderController.orderOrderIDDishesPut(userID, orderID, request);

        assertEquals(ResponseEntity.status(HttpStatus.FORBIDDEN).build(), responseEntity);
        verify(userMicroservice).isCustomer(userID);
        verifyNoInteractions(ordersFacade);
    }

    @Test
    void orderOrderIDDishesPutBadRequest() {
        long userID = 1L;
        long orderID = 2L;
        OrderOrderIDDishesPutRequest request = new OrderOrderIDDishesPutRequest();
        when(userMicroservice.isCustomer(userID)).thenReturn(true);
        when(ordersFacade.updateDishes(orderID, userID, request.getDishes())).thenThrow(new IllegalStateException());

        ResponseEntity<OrderOrderIDDishesPut200Response> responseEntity =
            orderController.orderOrderIDDishesPut(userID, orderID, request);

        assertEquals(ResponseEntity.status(HttpStatus.BAD_REQUEST).build(), responseEntity);
        verify(userMicroservice).isCustomer(userID);
        verify(ordersFacade).updateDishes(orderID, userID, request.getDishes());
    }

    @Test
    void orderOrderIDDishesPutNotFound() {
        long userID = 1L;
        long orderID = 2L;
        OrderOrderIDDishesPutRequest request = new OrderOrderIDDishesPutRequest();
        when(userMicroservice.isCustomer(userID)).thenReturn(true);
        when(ordersFacade.updateDishes(orderID, userID, request.getDishes())).thenThrow(new EntityNotFoundException());

        ResponseEntity<OrderOrderIDDishesPut200Response> responseEntity =
            orderController.orderOrderIDDishesPut(userID, orderID, request);

        assertEquals(ResponseEntity.status(HttpStatus.NOT_FOUND).build(), responseEntity);
        verify(userMicroservice).isCustomer(userID);
        verify(ordersFacade).updateDishes(orderID, userID, request.getDishes());
    }
}
