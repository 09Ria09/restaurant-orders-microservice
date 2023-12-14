package nl.tudelft.sem.orders.domain.config;

import nl.tudelft.sem.users.api.UserApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MicroConfig {
    @Bean
    static public UserApi userApi() {
        return new UserApi();
    }
}
