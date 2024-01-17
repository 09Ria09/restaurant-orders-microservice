# The Order Microservice

This is the implementation of the order microservice of team 13a.

Our API specification is contained in the `documentation` repo.

This requires at least Java version 15 to run.

## Building the application

Building the application can be done by invoking `./gradlew build`.

## Testing the application

To run all tests one can invoke `./gradlew test`.

Furhtermore to run mutation tests one can invoke `./gradlew pitest`

The reports will be generated to the `build/reports` folder.

## Running the application

By default running `./gradlew bootRun` will start up the application with mocks instead of external services on port `8082`. The port can be changed in `order-microservice/src/main/resources/application.properties` while the other microservices can be connected by chaning the configuration in `./order-microservice/src/main/java/nl/tudelft/sem/orders/domain/config/MicroConfig.java` 

From:

```java
    @Bean
    public UserMicroservice userMicroservice() {
        return new MockUserMicroservice();
    }

    @Bean
    public DeliveryMicroservice deliveryMicroservice() {
        return new MockDeliveryMicroservice();
    }
```

To:

```java
    @Bean
    public UserMicroservice userMicroservice() {
        return new UserRemoteProxy(context.getBean(UserApi.class), context.getBean(
            VendorApi.class), context.getBean(LocationMapper.class));
    }

    @Bean
    public DeliveryMicroservice deliveryMicroservice() {
        return new DeliveryRemoteProxy(context.getBean(
            nl.tudelft.sem.delivery.api.VendorApi.class), context.getBean(AdminApi.class), context.getBean(
            DeliveryApi.class));
    }
```
