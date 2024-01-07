package nl.tudelft.sem.orders.adapters.mocks;

import java.util.ArrayList;
import java.util.List;
import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import nl.tudelft.sem.users.ApiException;
import nl.tudelft.sem.users.model.UsersGetUserTypeIdGet200Response;
import nl.tudelft.sem.users.model.UsersIdGet200Response;
import nl.tudelft.sem.users.model.Vendor;

public class MockUserMicroservice implements UserMicroservice {
    @Override
    public List<Vendor> getAllVendors() throws ApiException {
        return new ArrayList<>();
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
    public boolean isCustomer(long userId) throws ApiException {
        return true;
    }

    @Override
    public boolean isVendor(long userId) throws ApiException {
        return true;
    }

    @Override
    public boolean isAdmin(long userId) throws ApiException {
        return false;
    }

    @Override
    public boolean isCourier(long userId) throws ApiException {
        return false;
    }

    public boolean doesUserExist(long userId) {
        return true;
    }
}
