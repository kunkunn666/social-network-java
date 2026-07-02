package model;
//边
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