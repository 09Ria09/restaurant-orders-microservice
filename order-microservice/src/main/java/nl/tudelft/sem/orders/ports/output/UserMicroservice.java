package nl.tudelft.sem.orders.ports.output;

import nl.tudelft.sem.users.ApiException;
import nl.tudelft.sem.users.model.User;
import nl.tudelft.sem.users.model.UsersGetUserTypeIdGet200Response;
import nl.tudelft.sem.users.model.UsersIdGet200Response;

public interface UserMicroservice {
    UsersGetUserTypeIdGet200Response.UserTypeEnum getUserType(long userId) throws ApiException;
    UsersIdGet200Response getUserById(long userId) throws ApiException;

}
