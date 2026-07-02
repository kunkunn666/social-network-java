package algorithm;

import model.SocialGraph;

public class ForceLayout {
    private double repulsion = 5000;
    private double attraction = 0.005;
    private double centerGravity = 0.01;
    private double maxSpeed = 8;
    private double minDistance = 45;

    public void computeLayout(SocialGraph graph, int width, int height) {
        circularInit(graph, width, height);
        double temperature = 100;

        for (int iter = 0; iter < 800; iter++) {
            // 斥力
            for (int i = 0; i < graph.nodeCount; i++) {
                for (int j = i + 1; j < graph.nodeCount; j++) {
                    double dx = graph.nodes[i].x - graph.nodes[j].x;
                    double dy = graph.nodes[i].y - graph.nodes[j].y;
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    if (dist < 1) dist = 1;
                    double force = repulsion / (dist * dist);
                    double fx = force * (dx / dist), fy = force * (dy / dist);
                    graph.nodes[i].vx += fx; graph.nodes[i].vy += fy;
                    graph.nodes[j].vx -= fx; graph.nodes[j].vy -= fy;
                }
            }

            // 引力
            for (int i = 0; i < graph.edgeCount; i++) {
                int from = graph.edges[i].from, to = graph.edges[i].to;
                double dx = graph.nodes[to].x - graph.nodes[from].x;
                double dy = graph.nodes[to].y - graph.nodes[from].y;
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < 1) dist = 1;
                double force = dist * attraction;
                double fx = force * (dx / dist), fy = force * (dy / dist);
                graph.nodes[from].vx += fx; graph.nodes[from].vy += fy;
                graph.nodes[to].vx -= fx; graph.nodes[to].vy -= fy;
            }

            // 更新位置
            double cx = width / 2.0, cy = height / 2.0;
            for (int i = 0; i < graph.nodeCount; i++) {
                graph.nodes[i].vx += (cx - graph.nodes[i].x) * centerGravity;
                graph.nodes[i].vy += (cy - graph.nodes[i].y) * centerGravity;
                double speed = Math.sqrt(graph.nodes[i].vx * graph.nodes[i].vx +
                                        graph.nodes[i].vy * graph.nodes[i].vy);
                if (speed > maxSpeed) {
                    graph.nodes[i].vx = (graph.nodes[i].vx / speed) * maxSpeed;
                    graph.nodes[i].vy = (graph.nodes[i].vy / speed) * maxSpeed;
                }
                graph.nodes[i].vx *= temperature / 100;
                graph.nodes[i].vy *= temperature / 100;
                graph.nodes[i].x += graph.nodes[i].vx;
                graph.nodes[i].y += graph.nodes[i].vy;
                graph.nodes[i].vx = 0; graph.nodes[i].vy = 0;
                graph.nodes[i].x = Math.max(30, Math.min(width - 30, graph.nodes[i].x));
                graph.nodes[i].y = Math.max(30, Math.min(height - 30, graph.nodes[i].y));
            }

            // 防重叠
            for (int i = 0; i < graph.nodeCount; i++) {
                for (int j = i + 1; j < graph.nodeCount; j++) {
                    double dx = graph.nodes[j].x - graph.nodes[i].x;
                    double dy = graph.nodes[j].y - graph.nodes[i].y;
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    if (dist < minDistance && dist > 0.1) {
                        double push = (minDistance - dist) / 2;
                        double px = push * (dx / dist), py = push * (dy / dist);
                        graph.nodes[i].x -= px; graph.nodes[i].y -= py;
                        graph.nodes[j].x += px; graph.nodes[j].y += py;
                    }
                }
            }

            temperature *= 0.995;
            if (temperature < 0.1) break;
        }
    }

    private void circularInit(SocialGraph graph, int width, int height) {
        double cx = width / 2.0, cy = height / 2.0;
        int n = graph.nodeCount;
        if (n == 0) return;
        double radius = Math.min(width, height) * 0.35;
        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n;
            graph.nodes[i].x = cx + radius * Math.cos(angle);
            graph.nodes[i].y = cy + radius * Math.sin(angle);
            graph.nodes[i].vx = 0; graph.nodes[i].vy = 0;
        }
    }
}