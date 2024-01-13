package nl.tudelft.sem.orders.ring0.distance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import nl.tudelft.sem.users.model.Vendor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FuzzyFindStrategyTest {

    private FuzzyFindStrategy fuzzyFindStrategy;
    private List<Vendor> vendors;

    @BeforeEach
    void setUp() {
        fuzzyFindStrategy = new FuzzyFindStrategy();

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
        List<Vendor> result = fuzzyFindStrategy.filterOnSearchString(vendors, null);
        assertEquals(vendors, result);
    }

    @Test
    void testFilterOnSearchStringMatchNotExact() {
        List<Vendor> result = fuzzyFindStrategy.filterOnSearchString(vendors, "Vndar One");
        assertEquals(1, result.size());
        assertEquals("Vendor One", result.get(0).getName());
    }

    @Test
    void testFilterOnSearchStringMatchExact() {
        List<Vendor> result = fuzzyFindStrategy.filterOnSearchString(vendors, "Vendor Two");
        assertEquals(1, result.size());
        assertEquals("Vendor Two", result.get(0).getName());
    }

    @Test
    void testFilterOnSearchStringNoMatch() {
        List<Vendor> result = fuzzyFindStrategy.filterOnSearchString(vendors, "Four");
        assertEquals(0, result.size());
    }
}