package nl.tudelft.sem.orders.controllers;

import java.util.List;
import javax.persistence.EntityNotFoundException;
import nl.tudelft.sem.orders.api.VendorApi;
import nl.tudelft.sem.orders.model.Dish;
import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.result.ForbiddenException;
import nl.tudelft.sem.orders.result.MalformedException;
import nl.tudelft.sem.orders.result.NotFoundException;
import nl.tudelft.sem.orders.ring0.VendorFacade;
import nl.tudelft.sem.users.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class VendorController implements VendorApi {
    private final transient VendorFacade vendorFacade;

    @Autowired
    VendorController(VendorFacade vendorFacade) {
        this.vendorFacade = vendorFacade;
    }

    @Override
    public ResponseEntity<List<Long>> vendorRadiusPost(Long userID,
                                                       String search,
                                                       Location location) {
        try {
            return ResponseEntity.ok(
                vendorFacade.vendorsInRadius(userID, search, location));
        } catch (MalformedException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<List<Dish>> vendorDishPost(Dish dish) {
        try {
            return ResponseEntity.ok(
                    vendorFacade.addDish(dish));
        } catch (ApiException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @Override
    public ResponseEntity<Void> vendorDishPut(Dish dish) {
        try {
            vendorFacade.modifyDish(dish);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException | ApiException e) {
            return ResponseEntity.badRequest().build();
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public ResponseEntity<Void> vendorDishDishIDDelete(Long userID, Long dishID) {
        try {
            vendorFacade.deleteDishById(userID, dishID);
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

        List<Order> customerOrders;
        try {
            customerOrders = vendorFacade.getPastOrdersForCustomer(userID, customerID);
        } catch (ForbiddenException e) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(customerOrders);
    }

    @Override
    public ResponseEntity<List<Dish>> vendorDishVendorIDGet(Long vendorID, Long userID) {
        try {
            return ResponseEntity.ok(vendorFacade.getDishesRemoveUserAllergies(vendorID, userID));
        } catch (NotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
