package nl.tudelft.sem.orders.controllers;

import nl.tudelft.sem.orders.api.VendorApi;
import nl.tudelft.sem.orders.ring0.OrderFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/vendor/")
public class VendorController implements VendorApi {
    @Autowired
    VendorController(OrderFacade ordersFacade) {
    }
}
