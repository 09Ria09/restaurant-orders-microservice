package nl.tudelft.sem.orders.ring0.distance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import nl.tudelft.sem.users.model.Vendor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SimpleWordMatchStrategyTest {

    private SimpleWordMatchStrategy simpleWordMatchStrategy;
    private List<Vendor> vendors;

    @BeforeEach
    void setUp() {
        simpleWordMatchStrategy = new SimpleWordMatchStrategy();

        Vendor vendor1 = new Vendor();
        vendor1.setName("Vendor One");
        Vendor vendor2 = new Vendor();
        vendor2.setName("Vendor Two");
        Vendor vendor3 = new Vendor();
        vendor3.setName("Vendor Three");

        vendors = Arrays.asList(vendor1, vendor2, vendor3);
    }

    @Test
    void testFilterOnSearchStringNull() {
        List<Vendor> result = simpleWordMatchStrategy.filterOnSearchString(vendors, null);
        assertEquals(vendors, result);
    }

    @Test
    void testFilterOnSearchStringOneMatch() {
        List<Vendor> result = simpleWordMatchStrategy.filterOnSearchString(vendors, "One");
        assertEquals(1, result.size());
        assertEquals("Vendor One", result.get(0).getName());
    }

    @Test
    void testFilterOnSearchStringAllMatch() {
        List<Vendor> result = simpleWordMatchStrategy.filterOnSearchString(vendors, "Vendor");
        assertEquals(3, result.size());
    }

    @Test
    void testFilterOnSearchStringNoMatch() {
        List<Vendor> result = simpleWordMatchStrategy.filterOnSearchString(vendors, "Four");
        assertEquals(0, result.size());
    }
}