package nl.tudelft.sem.orders.test;

import nl.tudelft.sem.orders.ports.output.DeliveryMicroservice;
import nl.tudelft.sem.orders.ports.output.LocationService;
import nl.tudelft.sem.orders.ports.output.OrderDatabase;
import nl.tudelft.sem.orders.ports.output.PaymentService;
import nl.tudelft.sem.orders.ports.output.UserMicroservice;
import nl.tudelft.sem.orders.test.mocks.MockDeliveryMicroservice;
import nl.tudelft.sem.orders.test.mocks.MockLocationService;
import nl.tudelft.sem.orders.test.mocks.MockOrderDatabase;
import nl.tudelft.sem.orders.test.mocks.MockPaymentService;
import nl.tudelft.sem.orders.test.mocks.MockUserMicroservice;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;

@TestConfiguration
public class TestConfig {
    @Autowired
    private ApplicationContext context;

    @Bean
    @Primary
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public OrderDatabase orderDatabaseTest() {
        return new MockOrderDatabase();
    }

    @Bean
    @Primary
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public LocationService locationServiceTest() {
        return new MockLocationService();
    }

    @Bean
    @Primary
    public UserMicroservice userMicroTest() {
        return new MockUserMicroservice();
    }

    @Bean
    @Primary
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public DeliveryMicroservice deliveryMicroTest() {
        return new MockDeliveryMicroservice();
    }

    @Bean
    @Primary
    public PaymentService paymentServiceTest() {
        return new MockPaymentService();
    }

    @Bean
    public MockOrderDatabase orderDatabaseMock() {
        return (MockOrderDatabase) context.getBean(OrderDatabase.class);
    }

    @Bean
    public MockDeliveryMicroservice mockDeliveryMicroservice() {
        return (MockDeliveryMicroservice) context.getBean(DeliveryMicroservice.class);
    }


}
