package nl.tudelft.sem.orders.test.mocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import nl.tudelft.sem.orders.model.Order;
import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import nl.tudelft.sem.users.ApiException;
import nl.tudelft.sem.users.model.Customer;
import nl.tudelft.sem.users.model.Location;
import nl.tudelft.sem.users.model.User;
import nl.tudelft.sem.users.model.UsersGetUserTypeIdGet200Response;
import nl.tudelft.sem.users.model.UsersIdGet200Response;
import nl.tudelft.sem.users.model.Vendor;

public class MockUserMicroservice implements UserMicroservice {

    private Vendor[] vendors = {
        new Vendor().id(100L).name("asd").email("asdw").location(
            new Location().city("a")),
    };

    @Override
    public List<Vendor> getAllVendors() throws ApiException {
        return new ArrayList<>(Arrays.asList(vendors));
    }

    /**
     * @param customerId
     * @return
     * @throws ApiException
     */
    @Override
    public nl.tudelft.sem.orders.model.Location getCustomerAddress(long customerId) throws ApiException {
        return null;
    }

    /**
     * @param vendorId
     * @return
     * @throws ApiException
     */
    @Override
    public nl.tudelft.sem.orders.model.Location getVendorAddress(long vendorId) throws ApiException {
        return null;
    }

    /**
     * @param userId
     * @return
     * @throws ApiException
     */
    @Override
    public boolean isCustomer(long userId) throws ApiException {
        return false;
    }

    private Customer[] users = {
        new Customer().id(1L).email("stary@stary.pl").name("Stary")
            .surname("Kowalski")
            .address(new Location().city("b")).addAllergensItem("Nuts"),
        new Customer().id(2L).email("idk@sharklasers.com").name("Drugi")
            .surname("Chłop").address(new Location().city("Beijng")),
    };

    @Override
    public UsersGetUserTypeIdGet200Response.UserTypeEnum getUserType(
        long userId) throws ApiException {
        switch ((int) userId) {
            case 1:
            case 2:
                return UsersGetUserTypeIdGet200Response.UserTypeEnum.CUSTOMER;
            case 3:
            default:
                throw new ApiException();
        }
    }

    @Override
    public UsersIdGet200Response getUserById(long userId) throws ApiException {
        if (userId > users.length || userId < 0) {
            throw new ApiException();
        }

        var res = new UsersIdGet200Response();
        res.setActualInstance(users[(int) userId - 1]);
        return res;
    }
}
