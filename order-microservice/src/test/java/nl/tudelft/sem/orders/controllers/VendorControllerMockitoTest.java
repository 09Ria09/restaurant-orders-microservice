package nl.tudelft.sem.orders.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import nl.tudelft.sem.orders.result.ForbiddenException;
import nl.tudelft.sem.orders.result.MalformedException;
import nl.tudelft.sem.orders.ring0.VendorLogic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


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
}
