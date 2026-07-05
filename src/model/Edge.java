package model;

/**
 * 边模型 — 社交网络中两个节点之间的连接关系
 * 
 * 每条边代表两个人之间的好友关系，包含：
 * - 两端节点的编号
 * - 边的权重（表示关系强度，默认为 1.0）
 * - 是否高亮（用于可视化时突出显示某些路径）
 */
public class Edge {
    public int from;
    public int to;
    public double weight;
    public boolean highlight;

    /**
     * 构造一条新边
     * 
     * @param from   起始节点编号
     * @param to     目标节点编号
     * @param weight 边的权重
     */
    public Edge(int from, int to, double weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
        // 默认不高亮
        this.highlight = false;
    }
}