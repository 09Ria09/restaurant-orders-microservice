package nl.tudelft.sem.orders.adapters;

import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import nl.tudelft.sem.users.ApiException;
import nl.tudelft.sem.users.api.UserApi;
import nl.tudelft.sem.users.model.User;
import nl.tudelft.sem.users.model.UsersGetUserTypeIdGet200Response;
import nl.tudelft.sem.users.model.UsersIdGet200Response;
import org.springframework.beans.factory.annotation.Autowired;

public class UserRemoteAdapter implements UserMicroservice {
    UserApi users;

    @Autowired
    UserRemoteAdapter(UserApi users) {this.users = users;}

    @Override
    public UsersGetUserTypeIdGet200Response.UserTypeEnum getUserType(long userId) throws ApiException {
        return users.usersGetUserTypeIdGet(userId).getUserType();
    }

    @Override
    public UsersIdGet200Response getUserById(long userId) throws ApiException {
        return users.usersIdGet(userId);
    }
}
