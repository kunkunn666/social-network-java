package model;
//地理坐标
public class GeoPosition {
    public double longitude, latitude;

    public GeoPosition(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public double distanceTo(GeoPosition other) {
        double dx = this.longitude - other.longitude;
        double dy = this.latitude - other.latitude;
        return Math.sqrt(dx * dx + dy * dy) * 111.0;
    }
}