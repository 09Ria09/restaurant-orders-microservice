package nl.tudelft.sem.orders.ports.input;

import java.util.List;
import javax.persistence.EntityNotFoundException;
import nl.tudelft.sem.orders.model.Dish;
import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.result.ForbiddenException;
import nl.tudelft.sem.orders.result.MalformedException;
import nl.tudelft.sem.orders.result.NotFoundException;
import nl.tudelft.sem.users.ApiException;
import org.springframework.http.ResponseEntity;

public interface VendorLogicInterface {
    List<Long> vendorsInRadius(Long userId, String search, Location location)
        throws MalformedException;

    List<Dish> addDish(Dish dish) throws ApiException;

    void modifyDish(Dish dish) throws ApiException, EntityNotFoundException, IllegalStateException;

    void deleteDishById(Long userId, Long dishId) throws MalformedException, ForbiddenException;

    List<Order> getPastOrdersForCustomer(Long userID, Long customerID) throws ForbiddenException;

    List<Dish> getDishes(Long vendorId) throws NotFoundException;

    List<Dish> getDishesRemoveUserAllergies(Long vendorId, Long userId) throws NotFoundException;
}