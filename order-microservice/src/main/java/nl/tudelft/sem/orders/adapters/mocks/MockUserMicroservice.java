package nl.tudelft.sem.orders.adapters.mocks;

import java.util.ArrayList;
import java.util.List;
import nl.tudelft.sem.orders.model.Dish;
import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import nl.tudelft.sem.orders.ring0.distance.LocationMapper;
import nl.tudelft.sem.users.ApiException;
import nl.tudelft.sem.users.model.Vendor;

public class MockUserMicroservice implements UserMicroservice {
    @Override
    public List<Vendor> getAllVendors() throws ApiException {
        nl.tudelft.sem.users.model.Location location = new nl.tudelft.sem.users.model.Location();
        location.setCountry("the Netherlands");
        location.setCity("Delft");
        location.setStreet("Mekelweg");
        location.setStreetNumber("7");

        Vendor vendor = new Vendor();
        vendor.setId(2L);
        vendor.setName("TestVendor");
        vendor.setLocation(location);

        return new ArrayList<>(List.of(vendor));
    }

    @Override
    public Location getCustomerAddress(long customerId) throws ApiException {
        Location location = new Location();
        location.setCountry("the Netherlands");
        location.setCity("Delft");
        location.setAddress("Mekelweg 5");
        location.setPostalCode("2628DV");
        location.setAdditionalRemarks("Inside the building.");
        return location;
    }

    @Override
    public Location getVendorAddress(long vendorId) throws ApiException {
        Location location = new Location();
        location.setCountry("the Netherlands");
        location.setCity("Delft");
        location.setAddress("Mekelweg 7");
        location.setPostalCode("2628DV");
        return location;
    }

    @Override
    public List<String> getCustomerAllergies(long userId) throws ApiException {
        return new ArrayList<>();
    }

    @Override
    public boolean isCustomer(long userId) throws ApiException {
        return userId == 1;
    }

    @Override
    public boolean isVendor(long userId) throws ApiException {
        return userId == 2;
    }

    @Override
    public boolean isAdmin(long userId) throws ApiException {
        return userId == 3;
    }

    @Override
    public boolean isCourier(long userId) throws ApiException {
        return false;
    }

    public boolean doesUserExist(long userId) {
        return true;
    }

}
