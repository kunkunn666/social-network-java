package algorithm;

/**
 * 力导向布局算法 — 计算节点在画布上的位置
 * 
 * 本算法通过模拟物理力来安排节点的位置，使图的布局美观、均匀、无重叠。
 * 
 * 核心思想：
 * 1. 斥力阶段：所有节点之间互相排斥，距离越近斥力越大（像磁铁同极相斥）
 * 2. 引力阶段：有边相连的节点之间互相吸引，距离越远引力越大（像弹簧）
 * 3. 更新位置：根据合力更新节点的速度和位置，并限制在画布范围内
 * 4. 防重叠阶段：检测并推开距离过近的节点
 * 
 * 算法通过多次迭代逐渐降温（temperature 逐渐减小），最终达到稳定布局。
 */

import model.SocialGraph;

public class ForceLayout {
    /** 斥力系数：控制节点间排斥力的大小，值越大节点越分散 */
    private double repulsion = 10000;
    /** 引力系数：控制边两端节点间吸引力的大小，值越大边越短 */
    private double attraction = 0.003;
    /** 中心引力系数：所有节点向画布中心靠拢的力度 */
    private double centerGravity = 0.008;
    /** 最大速度：限制节点每次迭代的移动距离，防止飞得太远 */
    private double maxSpeed = 10;
    /** 最小间距：节点之间的最小允许距离，小于此距离会被推开 */
    private double minDistance = 70;

    /**
     * 执行力导向布局算法，计算所有节点的画布坐标
     * 
     * 整个算法分为以下几个阶段：
     * 
     * 【初始化阶段】将节点均匀地排列在圆形上
     * 
     * 【迭代阶段】（最多 1000 次迭代，每次迭代包含以下四个子阶段）：
     *   1. 斥力计算：每对节点之间根据距离计算排斥力，距离越近力越大
     *   2. 引力计算：每条边两端的节点之间根据距离计算吸引力，距离越远力越大
     *   3. 位置更新：添加中心引力，限制速度，更新位置，限制在画布内
     *   4. 防重叠：检测并推开距离过近的节点
     * 
     * 【最终分离阶段】二次强力防重叠，确保所有节点不重叠
     * 
     * @param graph  社交网络图
     * @param width  画布宽度（像素）
     * @param height 画布高度（像素）
     */
    public void computeLayout(SocialGraph graph, int width, int height) {
        // ==================== 初始化阶段 ====================
        // 将所有节点初始排列在圆形上
        circularInit(graph, width, height);

        // 温度参数：控制迭代的"热度"，温度越高节点移动越快
        // 随着迭代进行，温度逐渐降低，节点慢慢稳定下来
        double temperature = 120;

        // ==================== 主迭代循环 ====================
        // 最多迭代 1000 次
        for (int iter = 0; iter < 1000; iter++) {

            // -------------------------------------------------------
            // 阶段 1：斥力计算（所有节点对之间互相排斥）
            // -------------------------------------------------------
            // 原理：每对节点之间都有一个排斥力，距离越近斥力越大。
            // 这确保了没有边相连的节点也会保持距离，不会挤在一起。
            // 公式：力 = 斥力系数 / 距离²（距离越近，力指数级增长）
            for (int i = 0; i < graph.nodeCount; i++) {
                for (int j = i + 1; j < graph.nodeCount; j++) {
                    // 计算节点 i 和 j 之间的水平距离和垂直距离
                    double deltaX = graph.nodes[i].x - graph.nodes[j].x;
                    double deltaY = graph.nodes[i].y - graph.nodes[j].y;
                    // 计算欧几里得距离
                    double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                    // 防止距离为 0 导致除零错误
                    if (distance < 1) {
                        distance = 1;
                    }
                    // 斥力大小：斥力系数 / 距离²
                    double force = repulsion / (distance * distance);
                    // 如果两个节点距离太近，额外增加斥力以推开它们
                    if (distance < minDistance) {
                        force = force * (minDistance / distance) * 2;
                    }
                    // 将力分解为 x 和 y 方向的分量
                    double forceX = force * (deltaX / distance);
                    double forceY = force * (deltaY / distance);
                    // 节点 i 受到远离 j 的力（正方向）
                    graph.nodes[i].vx = graph.nodes[i].vx + forceX;
                    graph.nodes[i].vy = graph.nodes[i].vy + forceY;
                    // 节点 j 受到远离 i 的力（反方向，牛顿第三定律）
                    graph.nodes[j].vx = graph.nodes[j].vx - forceX;
                    graph.nodes[j].vy = graph.nodes[j].vy - forceY;
                }
            }

            // -------------------------------------------------------
            // 阶段 2：引力计算（有边相连的节点之间互相吸引）
            // -------------------------------------------------------
            // 原理：有边相连的节点之间有一个吸引力，距离越远引力越大。
            // 这就像一根弹簧，拉得越远弹力越大，把相连的节点拉回一起。
            // 公式：力 = 距离 × 引力系数
            for (int i = 0; i < graph.edgeCount; i++) {
                // 获取边的两个端点
                int fromNode = graph.edges[i].from;
                int toNode = graph.edges[i].to;
                // 计算两个端点之间的水平和垂直距离
                double deltaX = graph.nodes[toNode].x - graph.nodes[fromNode].x;
                double deltaY = graph.nodes[toNode].y - graph.nodes[fromNode].y;
                // 计算欧几里得距离
                double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                // 防止距离为 0
                if (distance < 1) {
                    distance = 1;
                }
                // 引力大小：距离 × 引力系数（距离越远，引力越大）
                double force = distance * attraction;
                // 将力分解为 x 和 y 方向的分量
                double forceX = force * (deltaX / distance);
                double forceY = force * (deltaY / distance);
                // from 节点受到向 to 节点的拉力
                graph.nodes[fromNode].vx = graph.nodes[fromNode].vx + forceX;
                graph.nodes[fromNode].vy = graph.nodes[fromNode].vy + forceY;
                // to 节点受到向 from 节点的拉力（反方向）
                graph.nodes[toNode].vx = graph.nodes[toNode].vx - forceX;
                graph.nodes[toNode].vy = graph.nodes[toNode].vy - forceY;
            }

            // -------------------------------------------------------
            // 阶段 3：更新位置（应用速度、限制范围、重置速度）
            // -------------------------------------------------------
            // 画布中心坐标
            double centerX = width / 2.0;
            double centerY = height / 2.0;

            for (int i = 0; i < graph.nodeCount; i++) {
                // 3a. 添加中心引力：所有节点都向画布中心微微靠拢
                // 防止布局漂移到画布之外
                graph.nodes[i].vx = graph.nodes[i].vx + (centerX - graph.nodes[i].x) * centerGravity;
                graph.nodes[i].vy = graph.nodes[i].vy + (centerY - graph.nodes[i].y) * centerGravity;

                // 3b. 限制最大速度：防止节点移动太快，造成布局不稳定
                double currentSpeed = Math.sqrt(
                        graph.nodes[i].vx * graph.nodes[i].vx +
                        graph.nodes[i].vy * graph.nodes[i].vy);
                if (currentSpeed > maxSpeed) {
                    // 将速度按比例缩放到最大速度
                    double speedRatio = maxSpeed / currentSpeed;
                    graph.nodes[i].vx = graph.nodes[i].vx * speedRatio;
                    graph.nodes[i].vy = graph.nodes[i].vy * speedRatio;
                }

                // 3c. 温度衰减：随着迭代进行，速度逐渐减小
                // temperature 从 120 逐渐降到接近 0，节点慢慢稳定
                double temperatureFactor = temperature / 120;
                graph.nodes[i].vx = graph.nodes[i].vx * temperatureFactor;
                graph.nodes[i].vy = graph.nodes[i].vy * temperatureFactor;

                // 3d. 更新位置：位置 = 原位置 + 速度
                graph.nodes[i].x = graph.nodes[i].x + graph.nodes[i].vx;
                graph.nodes[i].y = graph.nodes[i].y + graph.nodes[i].vy;

                // 3e. 重置速度为零（下一轮迭代会重新计算）
                graph.nodes[i].vx = 0;
                graph.nodes[i].vy = 0;

                // 3f. 限制位置在画布范围内（保留 40 像素的边距）
                // Math.max(40, ...) 确保不小于 40
                // Math.min(width - 40, ...) 确保不大于 width - 40
                graph.nodes[i].x = Math.max(40, Math.min(width - 40, graph.nodes[i].x));
                graph.nodes[i].y = Math.max(40, Math.min(height - 40, graph.nodes[i].y));
            }

            // -------------------------------------------------------
            // 阶段 4：防重叠（迭代中轻量级推开重叠节点）
            // -------------------------------------------------------
            resolveOverlaps(graph);

            // 温度衰减：每次迭代温度降低 0.3%
            temperature = temperature * 0.997;
            // 如果温度已经很低，提前结束迭代
            if (temperature < 0.5) {
                break;
            }
        }

        // ==================== 最终分离阶段 ====================
        // 在所有迭代结束后，再进行一轮强力防重叠处理
        // 最多 80 次纯分离迭代，确保所有节点都不重叠
        for (int iter = 0; iter < 80; iter++) {
            boolean anyNodeMoved = false;
            // 检查每一对节点
            for (int i = 0; i < graph.nodeCount; i++) {
                for (int j = i + 1; j < graph.nodeCount; j++) {
                    // 计算两个节点之间的距离
                    double deltaX = graph.nodes[j].x - graph.nodes[i].x;
                    double deltaY = graph.nodes[j].y - graph.nodes[i].y;
                    double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

                    // 如果距离小于最小间距，就推开它们
                    if (distance < minDistance && distance > 0.01) {
                        // 需要推开的距离 = (最小间距 - 当前距离) × 0.6
                        double pushDistance = (minDistance - distance) * 0.6;
                        // 分解为 x 和 y 方向的分量
                        double pushX = pushDistance * (deltaX / distance);
                        double pushY = pushDistance * (deltaY / distance);
                        // 节点 i 向左下方向推开
                        graph.nodes[i].x = graph.nodes[i].x - pushX;
                        graph.nodes[i].y = graph.nodes[i].y - pushY;
                        // 节点 j 向右上方向推开
                        graph.nodes[j].x = graph.nodes[j].x + pushX;
                        graph.nodes[j].y = graph.nodes[j].y + pushY;
                        anyNodeMoved = true;
                    }
                }
            }
            // 如果本轮没有任何节点移动，说明已经全部到位，提前结束
            if (!anyNodeMoved) {
                break;
            }
        }
    }

    /**
     * 迭代中的轻量级防重叠处理
     * 检测每对节点是否距离过近，如果过近则将它们推开
     * 使用 0.55 的系数，力度比最终分离阶段（0.6）稍弱
     * 
     * @param graph 社交网络图
     */
    private void resolveOverlaps(SocialGraph graph) {
        for (int i = 0; i < graph.nodeCount; i++) {
            for (int j = i + 1; j < graph.nodeCount; j++) {
                // 计算两个节点之间的水平和垂直距离
                double deltaX = graph.nodes[j].x - graph.nodes[i].x;
                double deltaY = graph.nodes[j].y - graph.nodes[i].y;
                // 计算欧几里得距离
                double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

                // 如果距离小于最小间距且大于 0.1（避免除零）
                if (distance < minDistance && distance > 0.1) {
                    // 需要推开的距离 = (最小间距 - 当前距离) × 0.55
                    double pushDistance = (minDistance - distance) * 0.55;
                    // 分解为 x 和 y 方向的分量
                    double pushX = pushDistance * (deltaX / distance);
                    double pushY = pushDistance * (deltaY / distance);
                    // 将两个节点互相推开
                    graph.nodes[i].x = graph.nodes[i].x - pushX;
                    graph.nodes[i].y = graph.nodes[i].y - pushY;
                    graph.nodes[j].x = graph.nodes[j].x + pushX;
                    graph.nodes[j].y = graph.nodes[j].y + pushY;
                }
            }
        }
    }

    /**
     * 圆形初始化：将所有节点均匀地排列在画布中心的圆形上
     * 这样布局开始时节点就有一个合理的初始位置，有利于算法快速收敛
     * 
     * @param graph  社交网络图
     * @param width  画布宽度
     * @param height 画布高度
     */
    private void circularInit(SocialGraph graph, int width, int height) {
        // 计算画布中心坐标
        double centerX = width / 2.0;
        double centerY = height / 2.0;
        int nodeCount = graph.nodeCount;
        if (nodeCount == 0) {
            return; // 没有节点，无需初始化
        }
        // 圆的半径取画布短边的 35%
        double radius = Math.min(width, height) * 0.35;

        // 将每个节点均匀分布在圆上
        for (int i = 0; i < nodeCount; i++) {
            // 计算节点在圆上的角度（均匀分布，从 0 到 2π）
            double angle = 2 * Math.PI * i / nodeCount;
            // 根据角度计算 x, y 坐标
            // x = 中心x + 半径 × cos(角度)
            // y = 中心y + 半径 × sin(角度)
            graph.nodes[i].x = centerX + radius * Math.cos(angle);
            graph.nodes[i].y = centerY + radius * Math.sin(angle);
            // 初始速度为零
            graph.nodes[i].vx = 0;
            graph.nodes[i].vy = 0;
        }
    }
}