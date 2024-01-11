package nl.tudelft.sem.orders.test.mocks;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import nl.tudelft.sem.users.ApiException;
import nl.tudelft.sem.users.model.Admin;
import nl.tudelft.sem.users.model.Courier;
import nl.tudelft.sem.users.model.Customer;
import nl.tudelft.sem.users.model.Location;
import nl.tudelft.sem.users.model.UsersIdGet200Response;
import nl.tudelft.sem.users.model.Vendor;

public class MockUserMicroservice implements UserMicroservice {
    private UsersIdGet200Response[] users = {
        new UsersIdGet200Response(
            new Vendor().id(0L).name("asd").email("asdw").location(
                new Location().city("a"))),

        new UsersIdGet200Response(
            new Customer().id(1L).email("stary@stary.pl").name("Stary")
                .surname("Kowalski")
                .address(new Location().city("b")).addAllergensItem("Nuts")),

        new UsersIdGet200Response(
            new Customer().id(2L).email("idk@sharklasers.com").name("Drugi")
                .surname("Chłop").address(new Location().city("Beijng"))),

        new UsersIdGet200Response(
            new Vendor().id(3L).name("asd").email("asdw").location(
                new Location().city("c"))),
    };


    @Override
    public List<Vendor> getAllVendors() throws ApiException {
        return Arrays.asList(users).stream()
            .filter(u -> u.getActualInstance().getClass() == Vendor.class)
            .map(u -> (Vendor) u.getActualInstance()).collect(
                Collectors.toList());
    }

    @Override
    public nl.tudelft.sem.orders.model.Location getCustomerAddress(
        long customerId) throws ApiException {
        if (!isCustomer(customerId)) {
            throw new ApiException();
        }

        return locationConverter(
            ((Customer) users[(int) customerId].getActualInstance()).getAddress());
    }

    @Override
    public nl.tudelft.sem.orders.model.Location getVendorAddress(long vendorId)
        throws ApiException {
        if (!isVendor(vendorId)) {
            throw new ApiException();
        }

        return locationConverter(
            ((Vendor) users[(int) vendorId].getActualInstance()).getLocation());
    }

    @Override
    public List<String> getCustomerAllergies(long userId) throws ApiException {
        return null;
    }

    @Override
    public boolean isCustomer(long userId) throws ApiException {
        if (!doesUserExist(userId)) {
            throw new ApiException();
        }

        return users[(int) userId].getActualInstance().getClass()
            == Customer.class;
    }

    @Override
    public boolean isVendor(long userId) throws ApiException {
        if (!doesUserExist(userId)) {
            throw new ApiException();
        }

        return users[(int) userId].getActualInstance().getClass()
            == Vendor.class;
    }

    @Override
    public boolean isCourier(long userId) throws ApiException {
        if (!doesUserExist(userId)) {
            throw new ApiException();
        }

        return users[(int) userId].getActualInstance().getClass()
            == Courier.class;
    }

    @Override
    public boolean isAdmin(long userId) throws ApiException {
        if (!doesUserExist(userId)) {
            throw new ApiException();
        }

        return users[(int) userId].getActualInstance().getClass()
            == Admin.class;
    }

    @Override
    public boolean doesUserExist(long userId) {
        return userId < users.length && userId >= 0;
    }

    private nl.tudelft.sem.orders.model.Location locationConverter(
        nl.tudelft.sem.users.model.Location address) {
        nl.tudelft.sem.orders.model.Location
            location = new nl.tudelft.sem.orders.model.Location();
        location.setCountry(address.getCountry());
        location.setCity(address.getCity());
        location.setAddress(
            address.getStreet() + ' ' + address.getStreetNumber());
        // location.setPostalCode(address.getPostalCode());
        location.setAdditionalRemarks(address.getAdditionalRemarks());
        return location;
    }
}
