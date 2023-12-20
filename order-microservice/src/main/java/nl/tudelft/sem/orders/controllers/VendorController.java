package nl.tudelft.sem.orders.controllers;


import java.util.List;
import nl.tudelft.sem.orders.api.VendorApi;
import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.orders.result.MalformedException;
import nl.tudelft.sem.orders.ring0.VendorFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VendorController implements VendorApi {
    private transient VendorFacade vendorFacade;

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
}
