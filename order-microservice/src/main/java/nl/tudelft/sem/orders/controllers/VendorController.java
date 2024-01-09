package nl.tudelft.sem.orders.controllers;


import java.util.List;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import nl.tudelft.sem.orders.api.VendorApi;
import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import nl.tudelft.sem.orders.result.ForbiddenException;
import nl.tudelft.sem.orders.result.MalformedException;
import nl.tudelft.sem.orders.ring0.VendorLogic;
import nl.tudelft.sem.users.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

@RestController
public class VendorController implements VendorApi {
    private final transient VendorLogic vendorLogic;
    private final transient UserMicroservice userMicroservice;

    @Autowired
    VendorController(VendorLogic vendorLogic, UserMicroservice userMicroservice) {
        this.vendorLogic = vendorLogic;
        this.userMicroservice = userMicroservice;
    }

    @Override
    public ResponseEntity<List<Long>> vendorRadiusPost(Long userID,
                                                       String search,
                                                       Location location) {
        try {
            return ResponseEntity.ok(
                vendorLogic.vendorsInRadius(userID, search, location));
        } catch (MalformedException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<Void> vendorDishDishIDDelete(Long userID, Long dishID) {
        try {
            vendorLogic.deleteDishById(userID, dishID);
            return ResponseEntity.ok().build();
        } catch (ForbiddenException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (MalformedException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Override
    public ResponseEntity<List<Order>> vendorCustomerIDPastGet(
            Long userID,
            Long customerID
    ) {
        if (userID == null | customerID == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            if (!userMicroservice.isVendor(userID) | !userMicroservice.isCustomer(customerID)){
                return ResponseEntity.badRequest().build();
            }
        } catch (ApiException e) {
            return ResponseEntity.badRequest().build();
        }

        List<Order> customerOrders = vendorLogic.getPastOrdersForCustomer(userID, customerID);
        return ResponseEntity.ok(customerOrders);

    }
}
