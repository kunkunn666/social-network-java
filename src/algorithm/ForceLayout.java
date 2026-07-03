package algorithm;

/**
 * 力导向布局算法 — 计算节点在画布上的位置
 * 通过模拟节点间斥力与边引力，迭代计算使布局均匀、无重叠
 */

import model.SocialGraph;

public class ForceLayout {
    private double repulsion = 10000;
    private double attraction = 0.003;
    private double centerGravity = 0.008;
    private double maxSpeed = 10;
    private double minDistance = 70;

    public void computeLayout(SocialGraph graph, int width, int height) {
        circularInit(graph, width, height);
        double temperature = 120;

        for (int iter = 0; iter < 1000; iter++) {
            // 斥力
            for (int i = 0; i < graph.nodeCount; i++) {
                for (int j = i + 1; j < graph.nodeCount; j++) {
                    double dx = graph.nodes[i].x - graph.nodes[j].x;
                    double dy = graph.nodes[i].y - graph.nodes[j].y;
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    if (dist < 1) dist = 1;
                    double force = repulsion / (dist * dist);
                    // 距离越近斥力越大，指数级增长
                    if (dist < minDistance) force *= (minDistance / dist) * 2;
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
                graph.nodes[i].vx *= temperature / 120;
                graph.nodes[i].vy *= temperature / 120;
                graph.nodes[i].x += graph.nodes[i].vx;
                graph.nodes[i].y += graph.nodes[i].vy;
                graph.nodes[i].vx = 0; graph.nodes[i].vy = 0;
                graph.nodes[i].x = Math.max(40, Math.min(width - 40, graph.nodes[i].x));
                graph.nodes[i].y = Math.max(40, Math.min(height - 40, graph.nodes[i].y));
            }

            // 防重叠
            resolveOverlaps(graph);

            temperature *= 0.997;
            if (temperature < 0.5) break;
        }

        // 最终强力防重叠：纯分离迭代
        for (int iter = 0; iter < 80; iter++) {
            boolean moved = false;
            for (int i = 0; i < graph.nodeCount; i++) {
                for (int j = i + 1; j < graph.nodeCount; j++) {
                    double dx = graph.nodes[j].x - graph.nodes[i].x;
                    double dy = graph.nodes[j].y - graph.nodes[i].y;
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    if (dist < minDistance && dist > 0.01) {
                        double push = (minDistance - dist) * 0.6;
                        double px = push * (dx / dist), py = push * (dy / dist);
                        graph.nodes[i].x -= px; graph.nodes[i].y -= py;
                        graph.nodes[j].x += px; graph.nodes[j].y += py;
                        moved = true;
                    }
                }
            }
            if (!moved) break;
        }
    }

    private void resolveOverlaps(SocialGraph graph) {
        for (int i = 0; i < graph.nodeCount; i++) {
            for (int j = i + 1; j < graph.nodeCount; j++) {
                double dx = graph.nodes[j].x - graph.nodes[i].x;
                double dy = graph.nodes[j].y - graph.nodes[i].y;
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < minDistance && dist > 0.1) {
                    double push = (minDistance - dist) * 0.55;
                    double px = push * (dx / dist), py = push * (dy / dist);
                    graph.nodes[i].x -= px; graph.nodes[i].y -= py;
                    graph.nodes[j].x += px; graph.nodes[j].y += py;
                }
            }
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