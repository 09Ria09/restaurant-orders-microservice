package nl.tudelft.sem.orders.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityNotFoundException;
import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.model.OrderOrderIDDishesPut200Response;
import nl.tudelft.sem.orders.model.OrderOrderIDDishesPutRequest;
import nl.tudelft.sem.orders.model.OrderOrderIDRatePostRequest;
import nl.tudelft.sem.orders.result.ForbiddenException;
import nl.tudelft.sem.orders.result.MalformedException;
import nl.tudelft.sem.orders.result.NotFoundException;
import nl.tudelft.sem.orders.ring0.OrderFacade;
import nl.tudelft.sem.users.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


class OrderControllerMockitoTest {
    private OrderFacade orderFacade;
    private OrderController orderController;

    @BeforeEach
    public void setUp() {
        orderFacade = mock(OrderFacade.class);
        orderController = new OrderController(orderFacade);
    }

    @Test
    void orderGetNullID() {
        Long userID = null;
        ResponseEntity<List<Order>> actual = orderController.orderGet(userID);
        assertEquals(ResponseEntity.badRequest().build(), actual);
    }

    @Test
    void orderGetAdmin() throws ApiException {
        Long userID = 1L;
        when(orderFacade.getOrders(1L)).thenReturn(new ArrayList<Order>());

        ResponseEntity<List<Order>> actual = orderController.orderGet(userID);

        verify(orderFacade).getOrders(1L);
    }

    @Test
    void orderGetIse() throws ApiException {
        Long userID = 1L;
        when(orderFacade.getOrders(1L)).thenThrow(new IllegalStateException());

        assertEquals(HttpStatus.BAD_REQUEST, orderController.orderGet(userID).getStatusCode());
    }

    @Test
    void orderGetException() throws ApiException {
        Long userID = 1L;

        when(orderFacade.getOrders(1L)).thenThrow(new ApiException());

        ResponseEntity<List<Order>> expected = ResponseEntity.badRequest().build();
        ResponseEntity<List<Order>> actual = orderController.orderGet(userID);
        assertEquals(expected, actual);
    }

    @Test
    public void testOrderOrderIDReorderPost() throws Exception {
        Order order = new Order(1L, 1L, 13L, new ArrayList<>(), 1f,
            new Location().city("Kraków").country("PL").postalCode("123ZT"),
            nl.tudelft.sem.orders.model.Order.StatusEnum.PENDING).courierID(3L);

        when(orderFacade.reorder(anyLong(), anyLong())).thenReturn(order);
        ResponseEntity<Order> response = orderController.orderOrderIDReorderPost(1L, 1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(order, response.getBody());
    }

    @Test
    public void testOrderOrderIDReorderPostNotFound() throws Exception {
        when(orderFacade.reorder(anyLong(), anyLong())).thenThrow(NotFoundException.class);
        ResponseEntity<Order> response = orderController.orderOrderIDReorderPost(1L, 1L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testOrderOrderIDReorderPostMalformed() throws Exception {
        when(orderFacade.reorder(anyLong(), anyLong())).thenThrow(MalformedException.class);
        ResponseEntity<Order> response = orderController.orderOrderIDReorderPost(1L, 1L);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }


    @Test
    public void testOrderPostSuccess() throws ApiException, MalformedException, ForbiddenException {
        long userID = 1L;
        long vendorID = 2L;

        when(orderFacade.createOrder(userID, vendorID)).thenReturn(new Order());
        ResponseEntity<Order> response = orderController.orderPost(userID, vendorID);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(new Order(), response.getBody());
    }

    @Test
    public void testOrderPostBadRequest() throws ApiException, MalformedException, ForbiddenException {
        long userID = 1L;
        long vendorID = 2L;

        when(orderFacade.createOrder(userID, vendorID)).thenThrow(new ApiException());
        ResponseEntity<Order> response = orderController.orderPost(userID, vendorID);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testOrderPostForbidden() throws ApiException, MalformedException, ForbiddenException {
        long userID = 1L;
        long vendorID = 2L;

        when(orderFacade.createOrder(userID, vendorID)).thenThrow(new ForbiddenException());
        ResponseEntity<Order> response = orderController.orderPost(userID, vendorID);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testOrderOrderIDDishesPutSuccess() throws IllegalStateException, EntityNotFoundException, ApiException {
        long userID = 1L;
        long orderID = 2L;

        OrderOrderIDDishesPutRequest request = new OrderOrderIDDishesPutRequest();
        when(orderFacade.updateDishes(orderID, userID, request.getDishes())).thenReturn(10.0F);
        ResponseEntity<OrderOrderIDDishesPut200Response> response =
            orderController.orderOrderIDDishesPut(userID, orderID, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testOrderOrderIDDishesPutBadRequest()
        throws IllegalStateException, EntityNotFoundException, ApiException {
        long userID = 1L;
        long orderID = 2L;

        OrderOrderIDDishesPutRequest request = new OrderOrderIDDishesPutRequest();
        when(orderFacade.updateDishes(orderID, userID, request.getDishes())).thenThrow(new IllegalStateException());
        ResponseEntity<OrderOrderIDDishesPut200Response> response =
            orderController.orderOrderIDDishesPut(userID, orderID, request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testOrderOrderIDDishesPutNotFound()
        throws IllegalStateException, EntityNotFoundException, ApiException {
        long userID = 1L;
        long orderID = 2L;
        OrderOrderIDDishesPutRequest request = new OrderOrderIDDishesPutRequest();

        when(orderFacade.updateDishes(orderID, userID, request.getDishes())).thenThrow(new EntityNotFoundException());
        ResponseEntity<OrderOrderIDDishesPut200Response> response =
            orderController.orderOrderIDDishesPut(userID, orderID, request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testOrderOrderIDDishesPutForbidden()
        throws IllegalStateException, EntityNotFoundException, ApiException {
        long userID = 1L;
        long orderID = 2L;
        OrderOrderIDDishesPutRequest request = new OrderOrderIDDishesPutRequest();

        when(orderFacade.updateDishes(orderID, userID, request.getDishes())).thenThrow(new ApiException());
        ResponseEntity<OrderOrderIDDishesPut200Response> response =
            orderController.orderOrderIDDishesPut(userID, orderID, request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testOrderOrderIDRatePostBadRequest() throws ForbiddenException, MalformedException, ApiException {
        doThrow(MalformedException.class).when(orderFacade).rateOrder(1L, 2L, 11);
        OrderOrderIDRatePostRequest request = new OrderOrderIDRatePostRequest();
        request.setRating(11);

        assertEquals(HttpStatus.BAD_REQUEST, orderController.orderOrderIDRatePost(1L, 2L, request).getStatusCode());
    }

    @Test
    public void testOrderOrderIDRatePostForbidden() throws ForbiddenException, MalformedException, ApiException {
        doThrow(ForbiddenException.class).when(orderFacade).rateOrder(1L, 2L, 7);
        OrderOrderIDRatePostRequest request = new OrderOrderIDRatePostRequest();
        request.setRating(7);

        assertEquals(HttpStatus.FORBIDDEN, orderController.orderOrderIDRatePost(1L, 2L, request).getStatusCode());
    }

    @Test
    public void testOrderOrderIDRatePostOK() {
        OrderOrderIDRatePostRequest request = new OrderOrderIDRatePostRequest();
        request.setRating(7);

        assertEquals(HttpStatus.OK, orderController.orderOrderIDRatePost(1L, 2L, request).getStatusCode());
    }

}
