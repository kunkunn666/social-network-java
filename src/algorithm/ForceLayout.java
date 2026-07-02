package algorithm;

import model.SocialGraph;

/**
 * 节点布局算法
 * 
 * 思路：
 *   1. 先把节点排成一个大圆（每个节点均匀分布在圆上）
 *   2. 然后检查所有节点对，如果有两个距离太近（会重叠），就互相推开一点
 *   3. 重复第2步若干次，直到没有节点重叠为止
 * 
 * 用到的知识：两点间距离公式 sqrt((x1-x2)^2 + (y1-y2)^2)、for循环
 */
public class ForceLayout {

    // 节点圆的半径倍数，1.0表示占画布短边的35%
    private double radiusScale = 0.35;

    // 节点之间至少保持这么远（像素），保证不重叠
    private double minGap = 50;

    // 推开重叠节点的循环次数，多跑几次确保都没重叠
    private int pushRounds = 50;

    public void computeLayout(SocialGraph graph, int width, int height) {
        int n = graph.nodeCount;
        if (n == 0) return;

        // ========== 第一步：把节点均匀排成一个圆 ==========
        double centerX = width / 2.0;   // 画布中心x坐标
        double centerY = height / 2.0;  // 画布中心y坐标
        double radius = Math.min(width, height) * radiusScale;  // 圆的半径

        for (int i = 0; i < n; i++) {
            // 第i个节点在圆上的角度 = 360度 * i / 总人数
            double angle = 2.0 * 3.14159265 * i / n;

            // 用三角函数算出圆上的坐标
            // cos算x方向，sin算y方向
            graph.nodes[i].x = centerX + radius * Math.cos(angle);
            graph.nodes[i].y = centerY + radius * Math.sin(angle);
        }

        // ========== 第二步：反复检查，推开重叠的节点 ==========
        for (int round = 0; round < pushRounds; round++) {

            // 标记这一轮有没有推开过节点
            boolean pushed = false;

            // 两两检查：节点i和节点j
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    // 算两个节点的距离
                    double dx = graph.nodes[j].x - graph.nodes[i].x;
                    double dy = graph.nodes[j].y - graph.nodes[i].y;
                    double dist = Math.sqrt(dx * dx + dy * dy);

                    // 如果距离小于minGap，说明快重叠了
                    if (dist < minGap) {
                        // 需要推开多远：一人一半
                        double push = (minGap - dist) / 2.0 + 1;

                        // 沿着两个节点的连线方向推开
                        // dx/dist 和 dy/dist 就是方向（归一化）
                        if (dist > 0.01) {
                            graph.nodes[i].x -= push * dx / dist;
                            graph.nodes[i].y -= push * dy / dist;
                            graph.nodes[j].x += push * dx / dist;
                            graph.nodes[j].y += push * dy / dist;
                        } else {
                            // 两个节点完全重合了，随机推开
                            graph.nodes[i].x -= push;
                            graph.nodes[j].x += push;
                        }

                        pushed = true;
                    }
                }
            }

            // 如果这一轮没有推开任何节点，说明全部都不重叠了，提前结束
            if (!pushed) {
                break;
            }
        }

        // ========== 第三步：确保所有节点在画布范围内 ==========
        for (int i = 0; i < n; i++) {
            if (graph.nodes[i].x < 30)            graph.nodes[i].x = 30;
            if (graph.nodes[i].x > width - 30)     graph.nodes[i].x = width - 30;
            if (graph.nodes[i].y < 30)            graph.nodes[i].y = 30;
            if (graph.nodes[i].y > height - 30)    graph.nodes[i].y = height - 30;
        }
    }
}