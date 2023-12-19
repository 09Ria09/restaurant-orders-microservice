package nl.tudelft.sem.orders.adapters.remote;

import java.util.List;
import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import nl.tudelft.sem.users.ApiException;
import nl.tudelft.sem.users.api.UserApi;
import nl.tudelft.sem.users.api.VendorApi;
import nl.tudelft.sem.users.model.UsersGetUserTypeIdGet200Response;
import nl.tudelft.sem.users.model.UsersIdGet200Response;
import nl.tudelft.sem.users.model.Vendor;
import org.springframework.beans.factory.annotation.Autowired;

public class UserRemoteAdapter implements UserMicroservice {
    private transient UserApi users;
    private transient VendorApi vendors;

    public UserRemoteAdapter(UserApi users, VendorApi vendors) {
        this.users = users;
        this.vendors = vendors;
    }

    @Override
    public UsersGetUserTypeIdGet200Response.UserTypeEnum getUserType(
        long userId) throws ApiException {
        return users.usersGetUserTypeIdGet(userId).getUserType();
    }

    @Override
    public UsersIdGet200Response getUserById(long userId) throws ApiException {
        return users.usersIdGet(userId);
    }

    @Override
    public List<Vendor> getAllVendors() throws ApiException {
        return vendors.vendorsGet();
    }
}
