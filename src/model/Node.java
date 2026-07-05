package model;

/**
 * 节点模型 — 社交网络中的个体
 * 
 * 每个节点代表社交网络中的一个人（或实体），记录了该节点的：
 * - 基本信息：编号、名称
 * - 画布坐标：用于可视化布局的 x, y 位置和速度 vx, vy
 * - 地理位置：经度、纬度（用于基于位置的功能，如附近的人）
 * - 网络属性：度数（有多少个朋友）、类型（核心/活跃/边缘）、所属社区编号
 */
public class Node {
    /** 节点唯一编号 */
    public int id;
    /** 节点显示名称 */
    public String name;
    /** 画布上的 X 坐标 */
    public double x, y;
    /** 速度向量 X 分量，用于力导向布局算法 */
    public double vx, vy;
    /** 地理经度 */
    public double longitude;
    /** 地理纬度 */
    public double latitude;
    /** 度数：与该节点相连的边数（即好友数量） */
    public int degree;
    /** 节点类型：核心 / 活跃 / 边缘，根据度数百分位自动分类 */
    public String type;
    /** 所属连通分量编号，-1 表示尚未检测 */
    public int community;

    /**
     * 构造一个新节点
     * @param id   节点编号
     * @param name 节点名称
     */
    public Node(int id, String name) {
        this.id = id;
        this.name = name;
        // 画布坐标初始化为原点
        this.x = 0;
        this.y = 0;
        // 速度初始化为零（静止状态）
        this.vx = 0;
        this.vy = 0;
        // 地理位置初始化为零
        this.longitude = 0;
        this.latitude = 0;
        // 度数初始为 0（还没有任何连接）
        this.degree = 0;
        // 默认类型为"边缘"
        this.type = "边缘";
        // 社区编号初始为 -1（未分配）
        this.community = -1;
    }

    /**
     * 计算当前节点到另一个节点的地理距离（单位：公里）
     * 使用简化的经纬度距离公式：√(Δlon² + Δlat²) × 111
     * 其中 111 是赤道上每度大约对应的公里数
     * 
     * @param other 另一个节点
     * @return 两点之间的地理距离（公里）
     */
    public double geoDistanceTo(Node other) {
        // 计算经度差
        double deltaLon = this.longitude - other.longitude;
        // 计算纬度差
        double deltaLat = this.latitude - other.latitude;
        // 简化的经纬度转公里公式：角度差 × 111 km/度
        return Math.sqrt(deltaLon * deltaLon + deltaLat * deltaLat) * 111.0;
    }

    /**
     * 返回节点的字符串表示，方便调试和显示
     */
    public String toString() {
        return name + "(id=" + id + ", degree=" + degree + ", type=" + type + ")";
    }
}