package nl.tudelft.sem.orders.controllers;

import java.util.List;
import nl.tudelft.sem.orders.api.VendorApi;
import nl.tudelft.sem.orders.model.Analytic;
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
        } catch (ApiException e) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
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
        } catch (ApiException e) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
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

    /**
     * Gets the analytics for a vendor.
     *
     * @param userID The id of the vendor (required)
     * @return The calculated analytics
     */
    public ResponseEntity<List<Analytic>> vendorAnalyticsGet(
            Long userID
    ) {
        if (userID == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            return ResponseEntity.ok(vendorFacade.getVendorAnalysis(userID));
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
