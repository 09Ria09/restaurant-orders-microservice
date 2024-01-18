package nl.tudelft.sem.orders;

import nl.tudelft.sem.orders.domain.config.MicroConfig;
import nl.tudelft.sem.users.api.UserApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Example microservice application.
 */
@SpringBootApplication
@Import(MicroConfig.class)
public class Application {


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
