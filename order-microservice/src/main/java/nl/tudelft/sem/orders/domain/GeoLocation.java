package nl.tudelft.sem.orders.domain;

import java.util.Objects;
import lombok.Generated;

public class GeoLocation {
    private static double radius = 6371e3;

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    private double longitude;
    private double latitude;

    public GeoLocation(double longitude, double latitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Calculate the real distance in meters between two locations.
     *
     * @param other The other location.
     * @return The earth surface distance in metric meters.
     */
    public double distanceTo(GeoLocation other) {
        if (other == null) {
            return -1;
        }

        // Based on the theoretical formula form this site: https://www.movable-type.co.uk/scripts/latlong.html

        double fi1 = this.latitude * Math.PI / 180;
        double fi2 = other.latitude * Math.PI / 180;

        double deltaFi = (this.latitude - other.latitude) * Math.PI / 180;
        double deltaLambda = (this.longitude - other.longitude) * Math.PI / 180;


        double a = Math.sin(deltaFi / 2) * Math.sin(deltaFi / 2)
            + Math.cos(fi1) * Math.cos(fi2) * Math.sin(deltaLambda / 2)
            * Math.sin(deltaLambda / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return radius * c;
    }

    @Override
    @Generated
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GeoLocation that = (GeoLocation) o;
        return Double.compare(longitude, that.longitude) == 0
            && Double.compare(latitude, that.latitude) == 0;
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(longitude, latitude);
    }
}
