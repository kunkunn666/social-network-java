package model;

/**
 * 边模型 — 社交网络中两个节点之间的连接关系
 */
public class Edge {
    public int from;
    public int to;
    public double weight;
    public boolean highlight;

    public Edge(int from, int to, double weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
        // 默认不高亮
        this.highlight = false;
    }
}