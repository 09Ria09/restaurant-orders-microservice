package nl.tudelft.sem.orders.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import nl.tudelft.sem.orders.model.Location;
import nl.tudelft.sem.orders.test.TestConfig;
import nl.tudelft.sem.orders.test.mocks.MockDeliveryMicroservice;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

@SpringBootTest
@Import(TestConfig.class)
public class VendorControllerTest {
    @Autowired
    private VendorController vendorController;

    @Autowired
    private MockDeliveryMicroservice deliveryMicroservice;

    @Test
    void vendorRadiusPostFail() {
        deliveryMicroservice.setFailRadius(true);
        assertEquals(HttpStatus.BAD_REQUEST,
            vendorController.vendorRadiusPost(1L, "none", null)
                .getStatusCode());
        deliveryMicroservice.setFailRadius(false);
    }

    @Test
    void vendorRadiusPostOk() {
        List<Long> retried =
            vendorController.vendorRadiusPost(1L, null,
                new Location().city("a")).getBody();

        assertEquals(0L, retried.get(0));
        assertEquals(1, retried.size());
    }
}