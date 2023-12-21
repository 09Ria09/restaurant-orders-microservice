package nl.tudelft.sem.orders.controllers;


import java.util.List;
import nl.tudelft.sem.orders.api.VendorApi;
import nl.tudelft.sem.orders.model.Dish;
import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import nl.tudelft.sem.orders.result.MalformedException;
import nl.tudelft.sem.orders.ring0.VendorLogic;
import nl.tudelft.sem.users.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<List<Dish>> vendorDishPost(Dish dish) {
        try {
            if (dish.getVendorID() == null || dish.getDishID() == null) {
                return ResponseEntity.badRequest().build();
            }
            if (!userMicroservice.isVendor(dish.getVendorID())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            return ResponseEntity.ok(
                    vendorLogic.addDish(dish));
        } catch (ApiException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<Void> vendorDishPut(Dish dish) {
        try {
            if (dish.getVendorID() == null || dish.getDishID() == null) {
                return ResponseEntity.badRequest().build();
            }
            //if it can't find the dish to be edited
            if (!vendorLogic.dishExists(dish.getDishID())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            vendorLogic.modifyDish(dish);
            return ResponseEntity.ok().build();
        } catch (ApiException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
