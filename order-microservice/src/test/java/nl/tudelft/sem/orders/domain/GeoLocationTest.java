package nl.tudelft.sem.orders.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}