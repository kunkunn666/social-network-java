package algorithm;

/**
 * 图算法集 — 提供多种社交网络分析算法
 * 
 * 包含以下算法：
 * - BFS 无权最短路径（跳数最短）
 * - Dijkstra 有权最短路径（权重之和最小）
 * - 度中心性：衡量节点有多少直接邻居
 * - 接近中心性：衡量节点到其他所有节点的平均距离
 * - 图直径：图中最远两节点之间的最短距离
 * - 平均度：所有节点度数的平均值
 * - 图密度：实际边数占最大可能边数的比例
 */

import model.SocialGraph;
import model.Node;

public class GraphAlgorithms {

    /**
     * BFS（广度优先搜索）计算从指定起点到所有节点的最短路径距离
     * 
     * BFS 的工作原理：
     * 1. 从起点开始，标记距离为 0
     * 2. 将起点放入队列
     * 3. 不断从队列中取出节点，访问其所有邻居
     * 4. 如果邻居未被访问过，标记其距离 = 当前节点距离 + 1，并入队
     * 5. 重复直到队列为空
     * 
     * @param graph   社交网络图
     * @param startId 起点节点索引
     * @return 每个节点到起点的最短距离数组，不可达的节点距离为 -1
     */
    public int[] bfsShortestPath(SocialGraph graph, int startId) {
        int totalNodes = graph.nodeCount;
        // 距离数组，初始化为 -1（表示不可达）
        int[] distance = new int[totalNodes];
        for (int i = 0; i < totalNodes; i++) {
            distance[i] = -1;
        }

        // 检查起点是否有效
        if (startId < 0 || startId >= totalNodes) {
            return distance;
        }

        // BFS 队列（用数组模拟）
        int[] queue = new int[totalNodes];
        int queueHead = 0; // 队头指针
        int queueTail = 0; // 队尾指针

        // 将起点入队，距离为 0
        queue[queueTail] = startId;
        queueTail = queueTail + 1;
        distance[startId] = 0;

        // BFS 主循环
        while (queueHead < queueTail) {
            // 取出队头节点
            int currentNode = queue[queueHead];
            queueHead = queueHead + 1;

            // 遍历当前节点的所有邻居
            for (int i = 0; i < graph.adjListSize[currentNode]; i++) {
                int neighbor = graph.adjList[currentNode][i];
                // 如果邻居还没被访问过
                if (distance[neighbor] == -1) {
                    // 邻居的距离 = 当前节点距离 + 1
                    distance[neighbor] = distance[currentNode] + 1;
                    // 将邻居入队
                    queue[queueTail] = neighbor;
                    queueTail = queueTail + 1;
                }
            }
        }
        return distance;
    }

    /**
     * 计算两个节点之间的最短路径（返回路径上的所有节点）
     * 
     * 使用 BFS 同时记录每个节点的前驱节点，然后从终点回溯到起点得到完整路径
     * 
     * @param graph  社交网络图
     * @param fromId 起点节点索引
     * @param toId   终点节点索引
     * @return 路径上的节点索引数组，不可达则返回空数组
     */
    public int[] shortestPath(SocialGraph graph, int fromId, int toId) {
        // 检查起点和终点是否有效
        if (fromId < 0 || toId < 0 || fromId >= graph.nodeCount || toId >= graph.nodeCount) {
            return new int[0];
        }

        int totalNodes = graph.nodeCount;
        // prev[i] = 在最短路径中，节点 i 的前驱节点编号
        int[] previousNode = new int[totalNodes];
        // 距离数组，-1 表示未访问
        int[] distance = new int[totalNodes];
        for (int i = 0; i < totalNodes; i++) {
            previousNode[i] = -1;
            distance[i] = -1;
        }

        // BFS 队列
        int[] queue = new int[totalNodes];
        int queueHead = 0;
        int queueTail = 0;

        // 起点入队
        queue[queueTail] = fromId;
        queueTail = queueTail + 1;
        distance[fromId] = 0;

        boolean foundTarget = false;

        // BFS 主循环
        while (queueHead < queueTail && !foundTarget) {
            int currentNode = queue[queueHead];
            queueHead = queueHead + 1;

            // 遍历当前节点的所有邻居
            for (int i = 0; i < graph.adjListSize[currentNode]; i++) {
                int neighbor = graph.adjList[currentNode][i];
                if (distance[neighbor] == -1) {
                    // 标记距离和前驱
                    distance[neighbor] = distance[currentNode] + 1;
                    previousNode[neighbor] = currentNode;
                    // 入队
                    queue[queueTail] = neighbor;
                    queueTail = queueTail + 1;
                    // 如果找到了终点，可以提前结束
                    if (neighbor == toId) {
                        foundTarget = true;
                        break;
                    }
                }
            }
        }

        // 如果终点不可达，返回空数组
        if (distance[toId] == -1) {
            return new int[0];
        }

        // 从终点回溯到起点，构建路径
        int pathLength = distance[toId] + 1;
        int[] path = new int[pathLength];
        int traceNode = toId;
        for (int i = pathLength - 1; i >= 0; i--) {
            path[i] = traceNode;
            traceNode = previousNode[traceNode];
        }
        return path;
    }

    /**
     * Dijkstra 算法计算有权最短路径（考虑边权重）
     * 
     * 原理：每次选择当前距离起点最近的未访问节点，更新其所有邻居的距离。
     * 与 BFS 不同的是，Dijkstra 考虑了边的权重，权重越大表示经过该边越"费力"。
     * 
     * @param graph  社交网络图
     * @param fromId 起点节点索引
     * @param toId   终点节点索引
     * @return 路径上的节点索引数组，不可达则返回空数组
     */
    public int[] dijkstraShortestPath(SocialGraph graph, int fromId, int toId) {
        int totalNodes = graph.nodeCount;
        if (fromId < 0 || toId < 0 || fromId >= totalNodes || toId >= totalNodes) {
            return new int[0];
        }

        // 距离数组：dist[i] = 起点到节点 i 的最短距离
        double[] distance = new double[totalNodes];
        // 前驱节点数组
        int[] previousNode = new int[totalNodes];
        // 访问标记数组
        boolean[] visited = new boolean[totalNodes];

        // 初始化：所有距离为无穷大，前驱为 -1
        for (int i = 0; i < totalNodes; i++) {
            distance[i] = Double.MAX_VALUE;
            previousNode[i] = -1;
        }
        distance[fromId] = 0;

        // 主循环：每次找一个最近的未访问节点
        for (int k = 0; k < totalNodes; k++) {
            // 找到当前距离最小的未访问节点
            int closestNode = -1;
            double minDistance = Double.MAX_VALUE;
            for (int i = 0; i < totalNodes; i++) {
                if (!visited[i] && distance[i] < minDistance) {
                    minDistance = distance[i];
                    closestNode = i;
                }
            }

            // 如果找不到或已经到达终点，结束
            if (closestNode == -1 || closestNode == toId) {
                break;
            }

            // 标记为已访问
            visited[closestNode] = true;

            // 更新 closestNode 的所有邻居的距离
            for (int i = 0; i < graph.adjListSize[closestNode]; i++) {
                int neighbor = graph.adjList[closestNode][i];
                // 获取边的权重
                double edgeWeight = graph.adjMatrix[closestNode][neighbor];
                if (edgeWeight <= 0) {
                    edgeWeight = 1.0; // 权重为 0 或负数时，当作 1 处理
                }
                // 如果通过 closestNode 到 neighbor 的距离更短，就更新
                if (!visited[neighbor] && distance[closestNode] + edgeWeight < distance[neighbor]) {
                    distance[neighbor] = distance[closestNode] + edgeWeight;
                    previousNode[neighbor] = closestNode;
                }
            }
        }

        // 如果终点不可达
        if (distance[toId] == Double.MAX_VALUE) {
            return new int[0];
        }

        // 回溯构建路径
        int pathLength = 0;
        for (int cur = toId; cur != -1; cur = previousNode[cur]) {
            pathLength = pathLength + 1;
        }

        int[] path = new int[pathLength];
        int traceNode = toId;
        for (int i = pathLength - 1; i >= 0; i--) {
            path[i] = traceNode;
            traceNode = previousNode[traceNode];
        }
        return path;
    }

    /**
     * 计算每个节点的度中心性
     * 
     * 度中心性 = 节点的度数 / (总节点数 - 1)
     * 值越大，说明该节点直接连接的朋友越多，在网络中越"活跃"
     * 
     * @param graph 社交网络图
     * @return 每个节点的度中心性数组
     */
    public double[] degreeCentrality(SocialGraph graph) {
        int totalNodes = graph.nodeCount;
        double[] centrality = new double[totalNodes];
        if (totalNodes <= 1) {
            return centrality; // 少于 2 个节点时，中心性都是 0
        }
        // 度中心性 = 度数 / 最大可能度数
        for (int i = 0; i < totalNodes; i++) {
            centrality[i] = (double) graph.nodes[i].degree / (totalNodes - 1);
        }
        return centrality;
    }

    /**
     * 计算每个节点的接近中心性
     * 
     * 接近中心性衡量一个节点到其他所有节点的平均距离有多近：
     * 接近中心性 = (可达节点数 / (总节点数-1)) × (可达节点数 / 所有距离之和)
     * 值越大，说明该节点到其他节点越"近"，在网络中越中心
     * 
     * @param graph 社交网络图
     * @return 每个节点的接近中心性数组
     */
    public double[] closenessCentrality(SocialGraph graph) {
        int totalNodes = graph.nodeCount;
        double[] centrality = new double[totalNodes];
        if (totalNodes <= 1) {
            return centrality;
        }

        for (int i = 0; i < totalNodes; i++) {
            // 用 BFS 计算节点 i 到所有其他节点的距离
            int[] distance = bfsShortestPath(graph, i);

            int totalDistance = 0;  // 所有距离之和
            int reachableCount = 0; // 可达节点数

            for (int j = 0; j < totalNodes; j++) {
                if (distance[j] > 0) {
                    totalDistance = totalDistance + distance[j];
                    reachableCount = reachableCount + 1;
                }
            }

            // 如果有可达节点，计算接近中心性
            if (totalDistance > 0 && reachableCount > 0) {
                double reachableRatio = (double) reachableCount / (totalNodes - 1);
                double averageDistance = (double) reachableCount / totalDistance;
                centrality[i] = reachableRatio * averageDistance;
            }
        }
        return centrality;
    }

    /**
     * 计算图的平均度数
     * 平均度数 = 所有节点的度数之和 / 节点总数
     * 
     * @param graph 社交网络图
     * @return 平均度数
     */
    public double averageDegree(SocialGraph graph) {
        if (graph.nodeCount == 0) {
            return 0;
        }
        // 累加所有节点的度数
        int totalDegreeSum = 0;
        for (int i = 0; i < graph.nodeCount; i++) {
            totalDegreeSum = totalDegreeSum + graph.nodes[i].degree;
        }
        // 除以节点总数
        return (double) totalDegreeSum / graph.nodeCount;
    }

    /**
     * 计算图的直径
     * 直径 = 图中所有可达节点对之间最短路径的最大值
     * 
     * @param graph 社交网络图
     * @return 图的直径
     */
    public int diameter(SocialGraph graph) {
        int totalNodes = graph.nodeCount;
        if (totalNodes <= 1) {
            return 0;
        }

        int maxDistance = 0;

        // 对每个节点运行 BFS
        for (int i = 0; i < totalNodes; i++) {
            int[] distance = bfsShortestPath(graph, i);
            // 找到从节点 i 出发的最远距离
            for (int j = 0; j < totalNodes; j++) {
                if (distance[j] > maxDistance) {
                    maxDistance = distance[j];
                }
            }
        }
        return maxDistance;
    }

    /**
     * 计算图的密度
     * 密度 = 实际边数 / 最大可能边数
     * 最大可能边数 = n × (n-1) / 2（无向完全图）
     * 密度范围是 0 到 1，值越大说明图越"密集"
     * 
     * @param graph 社交网络图
     * @return 图密度（0 ~ 1）
     */
    public double graphDensity(SocialGraph graph) {
        int totalNodes = graph.nodeCount;
        if (totalNodes <= 1) {
            return 0;
        }
        // 最大可能的边数（无向完全图）
        double maxPossibleEdges = (double) totalNodes * (totalNodes - 1) / 2;
        // 实际边数 / 最大可能边数
        return (double) graph.edgeCount / maxPossibleEdges;
    }
}