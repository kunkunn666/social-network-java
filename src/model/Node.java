package model;

/**
 * 节点模型 — 社交网络中的个体
 * 包含节点ID、名称、度数、类型（核心/活跃/边缘）、所属社区编号、画布坐标
 */
public class Node {
    public int id;
    public String name;
    public double x, y, vx, vy;
    public double longitude, latitude;
    public int degree;
    public String type;
    public int community;

    public Node(int id, String name) {
        this.id = id;
        this.name = name;
        this.x = 0; this.y = 0;
        this.vx = 0; this.vy = 0;
        this.longitude = 0; this.latitude = 0;
        this.degree = 0;
        this.type = "边缘";
        this.community = -1;
    }

    public double distanceTo(Node other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public double geoDistanceTo(Node other) {
        double dx = this.longitude - other.longitude;
        double dy = this.latitude - other.latitude;
        return Math.sqrt(dx * dx + dy * dy) * 111.0;
    }

    public String toString() {
        return name + "(id=" + id + ", degree=" + degree + ", type=" + type + ")";
    }
}