package nl.tudelft.sem.orders.ring0;

import static java.util.Collections.disjoint;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityNotFoundException;
import nl.tudelft.sem.orders.model.Dish;
import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.orders.ports.input.VendorLogicInterface;
import nl.tudelft.sem.orders.ports.output.DishDatabase;
import nl.tudelft.sem.orders.ports.output.OrderDatabase;
import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import nl.tudelft.sem.orders.result.ForbiddenException;
import nl.tudelft.sem.orders.result.MalformedException;
import nl.tudelft.sem.orders.result.NotFoundException;
import nl.tudelft.sem.orders.ring0.distance.RadiusStrategy;
import nl.tudelft.sem.orders.ring0.distance.SearchStrategy;
import nl.tudelft.sem.users.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VendorFacade implements VendorLogicInterface {
    private final transient OrderDatabase orderDatabase;
    private transient UserMicroservice userMicroservice;
    private transient DishDatabase dishDatabase;
    private transient RadiusStrategy radiusStrategy;
    private transient SearchStrategy searchStrategy;


    /**
     * Creates a new Vedor facade.
     *
     * @param userMicroservice The user microservice
     * @param orderDatabase The database output port.
     * @param dishDatabase The dish database.
     * @param radiusStrategy The chosen radius strategy.
     * @param searchStrategy The chosen search strategy.
     */
    @Autowired
    public VendorFacade(UserMicroservice userMicroservice,
                        OrderDatabase orderDatabase,
                        DishDatabase dishDatabase,
                        RadiusStrategy radiusStrategy,
                        SearchStrategy searchStrategy) {
        this.userMicroservice = userMicroservice;
        this.dishDatabase = dishDatabase;
        this.searchStrategy = searchStrategy;
        this.radiusStrategy = radiusStrategy;
        this.orderDatabase = orderDatabase;
    }

    @Override
    public List<Long> vendorsInRadius(Long userId, String search,
                                      Location location)
        throws MalformedException {
        try {
            if (!userMicroservice.isCustomer(userId)) {
                throw new MalformedException();
            }

            if (location == null) {
                location = userMicroservice.getCustomerAddress(userId);
            }

            var vendors = radiusStrategy.performRadiusCheck(userId, location);

            return searchStrategy.filterOnSearchString(vendors, search);
        } catch (Exception e) {
            throw new MalformedException();
        }
    }

    /**
     * Adds the given dish to the database.
     *
     * @param dish the dish to be added
     * @return the added dish
     */
    public List<Dish> addDish(Dish dish) throws ApiException {
        Dish d = new Dish();
        d.setVendorID(dish.getVendorID());
        d.setName(dish.getName());
        d.setDescription(dish.getDescription());
        d.setIngredients(dish.getIngredients());
        d.setPrice(dish.getPrice());

        if (dish.getVendorID() == null || dish.getDishID() == null) {
            throw new IllegalStateException();
        }

        if (!userMicroservice.isVendor(dish.getVendorID())) {
            throw new SecurityException();
        }

        dishDatabase.save(d);
        List<Dish> res = new ArrayList<>();
        res.add(d);
        return res;
    }

    /**
     * Modifies dish.
     *
     * @param dish Changed dish.
     * @throws ApiException .
     * @throws EntityNotFoundException thrown if dish to be changed does not exist
     * @throws IllegalStateException thrown if invalid dish
     */
    public void modifyDish(Dish dish) throws ApiException, EntityNotFoundException, IllegalStateException {

        if (dish.getVendorID() == null || dish.getDishID() == null) {
            throw new IllegalStateException();
        }

        if (dishDatabase.getById(dish.getDishID()) == null) {
            throw new EntityNotFoundException();
        }

        dishDatabase.save(dish);
    }

    /**
     * Deletes a dish by its ID.
     *
     * @param userId The ID of the user who is attempting to delete the dish.
     * @param dishId The ID of the dish to be deleted.
     * @throws MalformedException If the dish or user does not exist.
     * @throws ForbiddenException If the user is not a vendor or does not own the dish.
     */
    @Override
    public void deleteDishById(Long userId, Long dishId)
        throws MalformedException, ForbiddenException {
        Dish dish = dishDatabase.getById(dishId);

        if (dish == null) {
            throw new MalformedException();
        }

        try {
            if (userMicroservice.isVendor(userId) && dish.getVendorID().equals(userId)) {
                dishDatabase.delete(dish);
            } else {
                throw new ForbiddenException();
            }
        } catch (ApiException e) {
            throw new MalformedException();
        }
    }

    public List<Dish> getDishes(Long vendorId) throws NotFoundException {
        return dishDatabase.findDishesByVendorID(vendorId);
    }

    /**
     * Gets all the dishes of a restaurant and filters them according to the user's allergies.
     * If there is no userId or it's not found, return all dishes.
     */
    public List<Dish> getDishesRemoveUserAllergies(Long vendorId, Long userId) throws NotFoundException {
        if (userId == null) {
            return getDishes(vendorId);
        }

        List<Dish> dishes = getDishes(vendorId);

        try {
            List<String> allergies = userMicroservice.getCustomerAllergies(userId);

            return dishes.stream()
                .filter(dish -> dish.getAllergens() == null || !disjoint(dish.getAllergens(), allergies))
                .collect(Collectors.toList());

        } catch (ApiException ignored) {
            return dishes;
        }
    }

}
