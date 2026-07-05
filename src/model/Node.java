package model;

/**
 * 节点模型 — 社交网络中的个体
 */
public class Node {
    public int id;
    public String name;
    public double x, y;
    public double vx, vy;
    public double longitude;//地理经度
    public double latitude;//地理纬度
    public int degree;//度数
    public String type;//结点类型：核心 / 活跃 / 边缘

    /**
     * 构造一个新节点
     */
    public Node(int id, String name) {
        this.id = id;
        this.name = name;
        this.x = 0;
        this.y = 0;
        this.vx = 0;
        this.vy = 0;
        this.longitude = 0;
        this.latitude = 0;
        this.degree = 0;
        this.type = "边缘";
    }

    /**
     * 计算当前节点到另一个节点的平面距离
     */
    public double geoDistanceTo(Node other) {
        double deltaLon = this.longitude - other.longitude;
        double deltaLat = this.latitude - other.latitude;
        return Math.sqrt(deltaLon * deltaLon + deltaLat * deltaLat);//两点间距离公式
    }

    public String toString() {
        return name + "(id=" + id + ", degree=" + degree + ", type=" + type + ")";
    }
}