package nl.tudelft.sem.orders.ports.output;

import java.util.List;
import nl.tudelft.sem.users.ApiException;
import nl.tudelft.sem.users.model.UsersGetUserTypeIdGet200Response;
import nl.tudelft.sem.users.model.UsersIdGet200Response;
import nl.tudelft.sem.users.model.Vendor;

public interface UserMicroservice {
    UsersGetUserTypeIdGet200Response.UserTypeEnum getUserType(long userId)
            throws ApiException;

    UsersIdGet200Response getUserById(long userId) throws ApiException;

    List<Vendor> getAllVendors() throws ApiException;
}
