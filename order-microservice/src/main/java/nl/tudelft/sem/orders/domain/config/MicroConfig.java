package nl.tudelft.sem.orders.domain.config;

import nl.tudelft.sem.delivery.api.AdminApi;
import nl.tudelft.sem.delivery.api.DeliveryApi;
import nl.tudelft.sem.orders.adapters.database.DishDatabaseAdapter;
import nl.tudelft.sem.orders.adapters.database.OrderDatabaseAdapter;
import nl.tudelft.sem.orders.adapters.mocks.MockDeliveryMicroservice;
import nl.tudelft.sem.orders.adapters.mocks.MockLocationAdapter;
import nl.tudelft.sem.orders.adapters.mocks.MockPaymentAdapter;
import nl.tudelft.sem.orders.adapters.mocks.MockUserMicroservice;
import nl.tudelft.sem.orders.ports.output.DeliveryMicroservice;
import nl.tudelft.sem.orders.ports.output.DishDatabase;
import nl.tudelft.sem.orders.ports.output.LocationService;
import nl.tudelft.sem.orders.ports.output.OrderDatabase;
import nl.tudelft.sem.orders.ports.output.PaymentService;
import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import nl.tudelft.sem.orders.ring0.distance.FuzzyFindStrategy;
import nl.tudelft.sem.orders.ring0.distance.GeoDistanceStrategy;
import nl.tudelft.sem.orders.ring0.distance.LocationMapper;
import nl.tudelft.sem.orders.ring0.distance.RadiusStrategy;
import nl.tudelft.sem.orders.ring0.distance.SearchStrategy;
import nl.tudelft.sem.users.api.UserApi;
import nl.tudelft.sem.users.api.VendorApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MicroConfig {
    @Autowired
    private transient ApplicationContext context;

    @Bean
    public UserApi userApi() {
        return new UserApi();
    }

    @Bean
    public VendorApi vendorApi() {
        return new VendorApi();
    }

    @Bean
    public AdminApi adminApi() {
        return new AdminApi();
    }

    @Bean
    public DeliveryApi deliveryApi() {
        return new DeliveryApi();
    }

    // Pick the strategies here

    @Bean
    SearchStrategy searchStrategy() {
        return new FuzzyFindStrategy();
    }

    @Bean
    RadiusStrategy radiusStrategy() {
        return new GeoDistanceStrategy(
            context.getBean(DeliveryMicroservice.class),
            context.getBean(UserMicroservice.class),
            context.getBean(LocationService.class), context.getBean(
            LocationMapper.class));
    }

    @Bean
    public nl.tudelft.sem.delivery.api.VendorApi vendorApiDelivery() {
        return new nl.tudelft.sem.delivery.api.VendorApi();
    }

    @Bean
    public OrderDatabase orderDatabase() {
        return new OrderDatabaseAdapter();
    }

    @Bean
    public DishDatabase dishDatabase() {
        return new DishDatabaseAdapter();
    }

    /*@Bean
    public UserMicroservice userMicroservice() {
        return new UserRemoteAdapter(context.getBean(UserApi.class), context.getBean(
            VendorApi.class));
    }

    @Bean
    public DeliveryMicroservice deliveryMicroservice() {
        return new DeliveryRemoteAdapter(context.getBean(
            nl.tudelft.sem.delivery.api.VendorApi.class), context.getBean(
            AdminApi.class));
    }*/

    @Bean
    public UserMicroservice userMicroservice() {
        return new MockUserMicroservice();
    }

    @Bean
    public DeliveryMicroservice deliveryMicroservice() {
        return new MockDeliveryMicroservice();
    }

    @Bean
    public LocationService locationService() {
        return new MockLocationAdapter();
    }

    @Bean
    public PaymentService paymentService() {
        return new MockPaymentAdapter();
    }
}
