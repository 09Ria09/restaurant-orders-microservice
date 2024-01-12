package nl.tudelft.sem.orders.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityNotFoundException;
import nl.tudelft.sem.orders.adapters.mocks.MockLocationAdapter;
import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.model.OrderOrderIDDishesPut200Response;
import nl.tudelft.sem.orders.model.OrderOrderIDDishesPutRequest;
import nl.tudelft.sem.orders.model.OrderOrderIDRatePostRequest;
import nl.tudelft.sem.orders.ports.output.LocationService;
import nl.tudelft.sem.orders.ports.output.UserMicroservice;
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

    private UserMicroservice userMicroservice;
    private LocationService locationService;
    private OrderFacade orderFacade;
    private OrderController orderController;

    @BeforeEach
    public void setUp() {
        userMicroservice = mock(UserMicroservice.class);
        locationService = mock(MockLocationAdapter.class);
        orderFacade = mock(OrderFacade.class);
        orderController = new OrderController(orderFacade, userMicroservice, locationService);
    }

    @Test
    void orderPostOk() throws ApiException {
        long userID = 1L;
        long vendorID = 2L;

        when(locationService.isCloseBy(any(), any())).thenReturn(true);
        when(userMicroservice.isCustomer(userID)).thenReturn(true);
        when(orderFacade.createOrder(userID, vendorID)).thenReturn(new Order());

        ResponseEntity<Order> responseEntity = orderController.orderPost(userID, vendorID);

        assertEquals(ResponseEntity.ok(new Order()), responseEntity);
        verify(userMicroservice).getCustomerAddress(userID);
        verify(userMicroservice).getVendorAddress(vendorID);
        verify(userMicroservice).isCustomer(userID);
        verify(orderFacade).createOrder(userID, vendorID);
    }

    @Test
    void orderPostForbidden() throws ApiException {
        long userID = 1L;
        long vendorID = 2L;

        when(locationService.isCloseBy(any(), any())).thenReturn(true);
        when(userMicroservice.isCustomer(userID)).thenReturn(false);
        when(orderFacade.createOrder(userID, vendorID)).thenReturn(new Order());

        ResponseEntity<Order> responseEntity = orderController.orderPost(userID, vendorID);

        assertEquals(ResponseEntity.status(HttpStatus.FORBIDDEN).build(), responseEntity);
        verify(userMicroservice).isCustomer(userID);
        verifyNoInteractions(orderFacade);
    }

    @Test
    void orderPostBadRequest() throws ApiException {
        long userID = 1L;
        long vendorID = 2L;
        when(locationService.isCloseBy(any(), any())).thenReturn(false);
        when(userMicroservice.isCustomer(userID)).thenReturn(true);
        when(orderFacade.createOrder(userID, vendorID)).thenReturn(new Order());

        ResponseEntity<Order> responseEntity = orderController.orderPost(userID, vendorID);

        assertEquals(ResponseEntity.status(HttpStatus.BAD_REQUEST).build(), responseEntity);
        verify(locationService).isCloseBy(any(), any());
        verifyNoInteractions(orderFacade);
    }

    @Test
    void orderPostApiExceptionBadRequest() throws ApiException {
        long userID = 1L;
        long vendorID = 2L;
        when(locationService.isCloseBy(any(), any())).thenReturn(false);
        when(userMicroservice.isCustomer(userID)).thenThrow(new ApiException());
        when(orderFacade.createOrder(userID, vendorID)).thenReturn(new Order());

        ResponseEntity<Order> responseEntity = orderController.orderPost(userID, vendorID);

        assertEquals(ResponseEntity.status(HttpStatus.BAD_REQUEST).build(), responseEntity);
        verifyNoInteractions(orderFacade);
    }

    @Test
    void orderOrderIDDishesPutOk() throws ApiException {
        long userID = 1L;
        long orderID = 2L;
        OrderOrderIDDishesPutRequest request = new OrderOrderIDDishesPutRequest();
        when(userMicroservice.isCustomer(userID)).thenReturn(true);
        when(orderFacade.updateDishes(orderID, userID, request.getDishes())).thenReturn(10.0F);

        ResponseEntity<OrderOrderIDDishesPut200Response> responseEntity =
            orderController.orderOrderIDDishesPut(userID, orderID, request);

        OrderOrderIDDishesPut200Response expectedResponse = new OrderOrderIDDishesPut200Response();
        expectedResponse.setPrice(10f);

        assertEquals(ResponseEntity.ok(expectedResponse), responseEntity);
        verify(userMicroservice).isCustomer(userID);
        verify(orderFacade).updateDishes(orderID, userID, request.getDishes());
    }

    @Test
    void orderOrderIDDishesPutForbidden() throws ApiException {
        long userID = 1L;
        long orderID = 2L;
        OrderOrderIDDishesPutRequest request = new OrderOrderIDDishesPutRequest();
        when(userMicroservice.isCustomer(userID)).thenReturn(false);
        when(orderFacade.updateDishes(orderID, userID, request.getDishes())).thenReturn(10.0F);

        ResponseEntity<OrderOrderIDDishesPut200Response> responseEntity =
            orderController.orderOrderIDDishesPut(userID, orderID, request);

        assertEquals(ResponseEntity.status(HttpStatus.FORBIDDEN).build(), responseEntity);
        verify(userMicroservice).isCustomer(userID);
        verifyNoInteractions(orderFacade);
    }

    @Test
    void orderOrderIDDishesPutBadRequest() throws ApiException {
        long userID = 1L;
        long orderID = 2L;
        OrderOrderIDDishesPutRequest request = new OrderOrderIDDishesPutRequest();
        when(userMicroservice.isCustomer(userID)).thenReturn(true);
        when(orderFacade.updateDishes(orderID, userID, request.getDishes())).thenThrow(new IllegalStateException());

        ResponseEntity<OrderOrderIDDishesPut200Response> responseEntity =
            orderController.orderOrderIDDishesPut(userID, orderID, request);

        assertEquals(ResponseEntity.status(HttpStatus.BAD_REQUEST).build(), responseEntity);
        verify(userMicroservice).isCustomer(userID);
        verify(orderFacade).updateDishes(orderID, userID, request.getDishes());
    }

    @Test
    void orderOrderIDDishesPutApiExceptionBadRequest() throws ApiException {
        long userID = 1L;
        long orderID = 2L;
        OrderOrderIDDishesPutRequest request = new OrderOrderIDDishesPutRequest();
        when(userMicroservice.isCustomer(userID)).thenThrow(new ApiException());
        when(orderFacade.updateDishes(orderID, userID, request.getDishes())).thenThrow(new IllegalStateException());

        ResponseEntity<OrderOrderIDDishesPut200Response> responseEntity =
            orderController.orderOrderIDDishesPut(userID, orderID, request);

        assertEquals(ResponseEntity.status(HttpStatus.FORBIDDEN).build(), responseEntity);
        verify(userMicroservice).isCustomer(userID);
        verifyNoInteractions(orderFacade);
    }

    @Test
    void orderOrderIDDishesPutNotFound() throws ApiException {
        long userID = 1L;
        long orderID = 2L;
        OrderOrderIDDishesPutRequest request = new OrderOrderIDDishesPutRequest();
        when(userMicroservice.isCustomer(userID)).thenReturn(true);
        when(orderFacade.updateDishes(orderID, userID, request.getDishes())).thenThrow(new EntityNotFoundException());

        ResponseEntity<OrderOrderIDDishesPut200Response> responseEntity =
            orderController.orderOrderIDDishesPut(userID, orderID, request);

        assertEquals(ResponseEntity.status(HttpStatus.NOT_FOUND).build(), responseEntity);
        verify(userMicroservice).isCustomer(userID);
        verify(orderFacade).updateDishes(orderID, userID, request.getDishes());
    }

    @Test
    void orderGetNullID() throws ApiException {
        Long userID = null;
        ResponseEntity<List<Order>> actual = orderController.orderGet(userID);
        assertEquals(ResponseEntity.badRequest().build(), actual);
    }

    @Test
    void orderGetAdmin() throws ApiException {
        Long userID = 1L;
        when(orderFacade.getOrders(1L))
                .thenReturn(new ArrayList<Order>());

        ResponseEntity<List<Order>> actual = orderController.orderGet(userID);

        verify(orderFacade).getOrders(1L);
    }

    @Test
    void orderGetIse() throws ApiException {
        Long userID = 1L;
        when(orderFacade.getOrders(1L))
            .thenThrow(new IllegalStateException());

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
        Order order = new Order(1L,
            1L,
            13L,
            new ArrayList<>(),
            1f,
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
    public void testOrderOrderIDRatePostBadRequest() throws ForbiddenException, MalformedException, ApiException {
        doThrow(MalformedException.class).when(orderFacade).rateOrder(1L, 2L, 11);
        OrderOrderIDRatePostRequest request = new OrderOrderIDRatePostRequest();
        request.setRating(11);

        assertEquals(HttpStatus.BAD_REQUEST,
                orderController.orderOrderIDRatePost(1L, 2L, request)
                        .getStatusCode());
    }

    @Test
    public void testOrderOrderIDRatePostForbidden() throws ForbiddenException, MalformedException, ApiException {
        doThrow(ForbiddenException.class).when(orderFacade).rateOrder(1L, 2L, 7);
        OrderOrderIDRatePostRequest request = new OrderOrderIDRatePostRequest();
        request.setRating(7);

        assertEquals(HttpStatus.FORBIDDEN,
                orderController.orderOrderIDRatePost(1L, 2L, request)
                        .getStatusCode());
    }

    @Test
    public void testOrderOrderIDRatePostOK() {
        OrderOrderIDRatePostRequest request = new OrderOrderIDRatePostRequest();
        request.setRating(7);

        assertEquals(HttpStatus.OK,
                orderController.orderOrderIDRatePost(1L, 2L, request)
                        .getStatusCode());
    }

    @Test
    public void testOrderPutMalformed() throws ForbiddenException, MalformedException, ApiException {
        Location location = new Location();
        Order order = new Order(1L, 2L, 3L, new ArrayList<>(),
                20F, location, Order.StatusEnum.UNPAID);

        when(orderFacade.changeOrder(1L, order)).thenThrow(MalformedException.class);
        assertEquals(HttpStatus.BAD_REQUEST,
                orderController.orderPut(1L, order)
                        .getStatusCode());
    }

    @Test
    public void testOrderPutForbidden() throws ForbiddenException, MalformedException, ApiException {
        Location location = new Location();
        Order order = new Order(1L, 2L, 3L, new ArrayList<>(),
                20F, location, Order.StatusEnum.UNPAID);

        when(orderFacade.changeOrder(1L, order)).thenThrow(ForbiddenException.class);
        assertEquals(HttpStatus.FORBIDDEN,
                orderController.orderPut(1L, order)
                        .getStatusCode());
    }

    @Test
    public void testOrderPutOk() throws ForbiddenException, MalformedException, ApiException {
        Location location = new Location();
        Order order = new Order(1L, 2L, 3L, new ArrayList<>(),
                20F, location, Order.StatusEnum.UNPAID);

        when(orderFacade.changeOrder(1L, order)).thenReturn(order);

        ResponseEntity<Order> response = orderController.orderPut(1L, order);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(order, response.getBody());
    }

    @Test
    public void testOrderOrderIDGetMalformed() throws MalformedException {
        when(orderFacade.getOrder(1L)).thenThrow(MalformedException.class);
        assertEquals(HttpStatus.BAD_REQUEST,
                orderController.orderOrderIDGet(1L)
                        .getStatusCode());
    }

    @Test
    public void testOrderOrderIDGetOk() throws MalformedException {
        Location location = new Location();
        Order order = new Order(1L, 2L, 3L, new ArrayList<>(),
                20F, location, Order.StatusEnum.UNPAID);
        List<Order> list = new ArrayList<>();
        list.add(order);

        when(orderFacade.getOrder(1L)).thenReturn(list);

        ResponseEntity<List<Order>> response = orderController.orderOrderIDGet(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(list, response.getBody());
    }


}
