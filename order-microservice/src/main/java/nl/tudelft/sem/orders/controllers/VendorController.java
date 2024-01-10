package nl.tudelft.sem.orders.controllers;


import java.util.List;
import nl.tudelft.sem.orders.api.VendorApi;
import nl.tudelft.sem.orders.model.Analytic;
import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import nl.tudelft.sem.orders.result.ForbiddenException;
import nl.tudelft.sem.orders.result.MalformedException;
import nl.tudelft.sem.orders.ring0.VendorLogic;
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

        return ResponseEntity.ok(vendorLogic.getVendorAnalysis(userID));
    }

}
