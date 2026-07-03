package model;

/**
 * 边模型 — 社交网络中两个节点之间的连接关系
 * 包含起点、终点、权重、是否高亮
 */
public class Edge {
    public int from, to;
    public double weight;
    public boolean highlight;

    public Edge(int from, int to, double weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
        this.highlight = false;
    }

    public int other(int nodeId) {
        return (nodeId == from) ? to : from;
    }
}