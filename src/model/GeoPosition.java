package model;

/**
 * 地理位置模型 — 节点的经纬度坐标
 * 用于附近地理用户检索等基于位置的功能
 */
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