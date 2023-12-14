package nl.tudelft.sem.orders.domain;

public class GeoLocation {
    double R = 6371e3;

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

    private double longitude, latitude;

    public GeoLocation(double longitude, double latitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double distanceTo(GeoLocation other) {
        if(other == null)
            return -1;

        // Based on the theoretical formula form this site: https://www.movable-type.co.uk/scripts/latlong.html

        double fi1 = this.latitude * Math.PI/180;
        double fi2 = other.latitude * Math.PI/180;

        double dFi = (this.latitude - other.latitude) * Math.PI/180;
        double dLambda = (this.longitude - other.longitude) * Math.PI/180;


        double a = Math.sin(dFi/2) * Math.sin(dFi/2)
                + Math.cos(fi1) * Math.cos(fi2) * Math.sin(dLambda/2) * Math.sin(dLambda/2);

        double c = 2*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));

        return R*c;
    }
}
