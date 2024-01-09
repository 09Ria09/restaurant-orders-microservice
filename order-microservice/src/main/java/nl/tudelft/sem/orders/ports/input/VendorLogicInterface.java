package nl.tudelft.sem.orders.ports.input;

import java.util.List;
import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.result.ForbiddenException;
import nl.tudelft.sem.orders.result.MalformedException;
import org.springframework.http.ResponseEntity;

public interface VendorLogicInterface {
    List<Long> vendorsInRadius(Long userId, String search, Location location)
        throws MalformedException;

    void deleteDishById(Long userId, Long dishId) throws MalformedException, ForbiddenException;

    List<Order> getPastOrdersForCustomer(Long userID, Long customerID) throws ForbiddenException;
}