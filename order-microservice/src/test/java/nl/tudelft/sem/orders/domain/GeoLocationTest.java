package nl.tudelft.sem.orders.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GeoLocationTest {

    @Test
    void getLongitude() {
        GeoLocation a = new GeoLocation(1, 2);

        assertEquals(a.getLongitude(), 1);
    }

    @Test
    void setLongitude() {
        GeoLocation a = new GeoLocation(1, 2);
        a.setLongitude(3);

        assertEquals(a.getLongitude(), 3);
    }

    @Test
    void getLatitude() {
        GeoLocation a = new GeoLocation(1, 2);

        assertEquals(a.getLatitude(), 2);
    }

    @Test
    void setLatitude() {
        GeoLocation a = new GeoLocation(1, 2);
        a.setLatitude(3);

        assertEquals(a.getLatitude(), 3);
    }

    @Test
    void distanceTo() {
        GeoLocation a = new GeoLocation(1, 2);

        assertEquals(-1, a.distanceTo(null));
    }

    @Test
    void floatTest() {
        GeoLocation a = new GeoLocation(1.123, -42.122);

        GeoLocation b = new GeoLocation(-12.22, 12.5212);

        assertEquals(6227471.223550948, a.distanceTo(b));
    }

    @Test
    void eq() {
        GeoLocation a = new GeoLocation(1.123, -42.122);
        GeoLocation b = new GeoLocation(-12.22, 12.5212);

        assertTrue(a.equals(a));
        assertTrue(!a.equals(b));
        assertTrue(!a.equals(null));

        assertEquals(1.752084807E9, a.hashCode(), 0.0001);
    }
}