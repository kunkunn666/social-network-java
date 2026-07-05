package model;

/**
 * 社交网络图模型 — 核心数据层
 * 
 * 本类是社交网络分析的核心数据结构，负责：
 * 1. 从 CSV 文件加载数据集，构建节点和边
 * 2. 维护邻接矩阵（快速查询两节点间权重）和邻接表（快速遍历邻居）
 * 3. 按度数的百分位对节点分类（核心 / 活跃 / 边缘）
 * 4. 提供节点查找、邻居查询、高亮重置、附近搜索等基础操作
 * 5. 支持删除节点并重建图结构
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;

public class SocialGraph {
    /** 图中所有节点 */
    public Node[] nodes;
    /** 当前节点数量 */
    public int nodeCount;
    /** 图中所有边 */
    public Edge[] edges;
    /** 当前边的数量 */
    public int edgeCount;
    /** 邻接矩阵：adjMatrix[i][j] 表示节点 i 到节点 j 的边的权重，0 表示无边 */
    public double[][] adjMatrix;
    /** 邻接表：adjList[i] 存储节点 i 的所有邻居编号 */
    public int[][] adjList;
    /** 每个节点的邻居数量：adjListSize[i] 表示节点 i 有多少个邻居 */
    public int[] adjListSize;
    /** 数据集名称 */
    public String datasetName;
    /** 数据集描述 */
    public String datasetDescription;

    /** 最大节点数限制 */
    private static final int MAX_NODES = 500;
    /** 最大边数限制 */
    private static final int MAX_EDGES = 5000;
    /** 每个节点允许的最大邻居数（用于邻接表） */
    private static final int MAX_DEGREE = 200;

    /**
     * 构造一个空的社交网络图
     * 
     * @param name        数据集名称
     * @param description 数据集描述
     */
    public SocialGraph(String name, String description) {
        this.datasetName = name;
        this.datasetDescription = description;
        // 初始化节点数组，预分配最大容量
        this.nodes = new Node[MAX_NODES];
        this.nodeCount = 0;
        // 初始化边数组，预分配最大容量
        this.edges = new Edge[MAX_EDGES];
        this.edgeCount = 0;
        // 初始化邻接矩阵，所有值初始为 0（表示无边）
        this.adjMatrix = new double[MAX_NODES][MAX_NODES];
        // 初始化邻接表，每个节点最多 MAX_DEGREE 个邻居
        this.adjList = new int[MAX_NODES][MAX_DEGREE];
        // 初始化每个节点的邻居计数器
        this.adjListSize = new int[MAX_NODES];
    }

    /**
     * 向图中添加一个新节点
     * 尝试将 name 解析为整数作为节点编号，如果失败则使用自动编号
     * 
     * @param name 节点名称
     * @return 新节点的索引编号，如果已满则返回 -1
     */
    public int addNode(String name) {
        // 如果节点数已达上限，无法添加
        if (nodeCount >= MAX_NODES) {
            return -1;
        }
        // 尝试将名称解析为整数作为节点 ID
        try {
            int parsedId = Integer.parseInt(name);
            nodes[nodeCount] = new Node(parsedId, name);
        } catch (NumberFormatException e) {
            // 如果名称不是数字，使用自动递增编号
            nodes[nodeCount] = new Node(nodeCount + 1, name);
        }
        // 节点计数加 1，返回新节点的索引
        nodeCount = nodeCount + 1;
        return nodeCount - 1;
    }

    /**
     * 根据名称查找节点的索引编号
     * 
     * @param name 要查找的节点名称
     * @return 节点索引，如果找不到则返回 -1
     */
    public int findNodeId(String name) {
        // 遍历所有节点，逐个比较名称
        for (int i = 0; i < nodeCount; i++) {
            if (nodes[i].name.equals(name)) {
                return i;
            }
        }
        // 没有找到匹配的节点
        return -1;
    }

    /**
     * 添加一条无向边（同时更新邻接矩阵、邻接表和节点度数）
     * 
     * @param from   起始节点索引
     * @param to     目标节点索引
     * @param weight 边的权重
     */
    public void addEdge(int from, int to, double weight) {
        // 检查端点是否有效
        if (from < 0 || from >= nodeCount || to < 0 || to >= nodeCount) {
            return;
        }
        // 检查边数是否已达上限
        if (edgeCount >= MAX_EDGES) {
            return;
        }

        // 步骤 1：将边存入边数组
        edges[edgeCount] = new Edge(from, to, weight);
        edgeCount = edgeCount + 1;

        // 步骤 2：更新邻接矩阵（无向图，两边都设置）
        adjMatrix[from][to] = weight;
        adjMatrix[to][from] = weight;

        // 步骤 3：更新邻接表（无向图，两边都添加）
        if (adjListSize[from] < MAX_DEGREE) {
            adjList[from][adjListSize[from]] = to;
            adjListSize[from] = adjListSize[from] + 1;
        }
        if (adjListSize[to] < MAX_DEGREE) {
            adjList[to][adjListSize[to]] = from;
            adjListSize[to] = adjListSize[to] + 1;
        }

        // 步骤 4：更新两个节点的度数
        nodes[from].degree = nodes[from].degree + 1;
        nodes[to].degree = nodes[to].degree + 1;
    }

    /**
     * 从 CSV 文件加载数据集，构建整个社交网络
     * 
     * CSV 文件格式要求：
     * - 第一行为标题行（会被跳过）
     * - 每行格式：节点1名称,节点2名称[,权重]
     * - 同时查找对应的 _nodes.csv 文件加载节点坐标
     * 
     * @param filepath CSV 文件路径
     */
    public void loadFromCSV(String filepath) {
        // 步骤 1：清空旧数据
        clearData();

        // 步骤 2：检查文件是否存在
        File csvFile = new File(filepath);
        if (!csvFile.exists()) {
            System.out.println("错误: 文件不存在 - " + filepath);
            return;
        }

        // 步骤 3：查找对应的节点坐标文件
        String nodeFile = findNodeFile(filepath);

        // 步骤 4：第一遍扫描 — 只读取节点名称，创建所有节点
        try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                // 跳过标题行
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                // 按逗号分割每行
                String[] parts = line.split(",");
                if (parts.length < 2) {
                    continue; // 格式不对，跳过
                }
                String name1 = parts[0].trim();
                String name2 = parts[1].trim();
                // 如果节点还不存在，就创建它
                if (findNodeId(name1) == -1) {
                    addNode(name1);
                }
                if (findNodeId(name2) == -1) {
                    addNode(name2);
                }
            }
        } catch (Exception e) {
            System.out.println("读取CSV出错: " + e.getMessage());
        }

        // 步骤 5：加载节点坐标文件（如果存在）
        if (nodeFile != null) {
            loadNodeCoordinates(nodeFile);
        } else {
            System.out.println("错误: 未找到节点坐标文件 - " + filepath.replace(".csv", "_nodes.csv"));
        }

        // 步骤 6：第二遍扫描 — 读取边数据，创建所有边
        try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                // 跳过标题行
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                // 按逗号分割每行
                String[] parts = line.split(",");
                if (parts.length < 2) {
                    continue; // 格式不对，跳过
                }
                String name1 = parts[0].trim();
                String name2 = parts[1].trim();

                // 解析权重，默认为 1.0
                double edgeWeight = 1.0;
                if (parts.length >= 3) {
                    try {
                        edgeWeight = Double.parseDouble(parts[2].trim());
                    } catch (NumberFormatException ignored) {
                        // 权重解析失败，使用默认值 1.0
                    }
                }

                // 找到两端节点的索引
                int id1 = findNodeId(name1);
                int id2 = findNodeId(name2);

                // 确保两个节点都存在且不是同一个节点
                if (id1 != -1 && id2 != -1 && id1 != id2) {
                    addEdge(id1, id2, edgeWeight);
                }
            }
        } catch (Exception e) {
            System.out.println("读取边数据出错: " + e.getMessage());
        }

        // 步骤 7：对节点按度数分类
        classifyNodes();
    }

    /**
     * 根据边文件路径，查找对应的节点坐标文件
     * 例如：edges.csv → edges_nodes.csv
     * 
     * @param edgeFile 边文件路径
     * @return 节点坐标文件路径，如果不存在则返回 null
     */
    private String findNodeFile(String edgeFile) {
        String nodeFile = edgeFile.replace(".csv", "_nodes.csv");
        if (new File(nodeFile).exists()) {
            return nodeFile;
        }
        return null;
    }

    /**
     * 从节点坐标文件中加载每个节点的经纬度
     * CSV 格式：节点标识,经度,纬度
     * 节点标识可以是名称或编号
     * 
     * @param filepath 节点坐标文件路径
     */
    private void loadNodeCoordinates(String filepath) {
        File coordinateFile = new File(filepath);
        if (!coordinateFile.exists()) {
            System.out.println("错误: 节点坐标文件不存在 - " + filepath);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                // 跳过标题行
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                // 按逗号分割：节点标识,经度,纬度
                String[] parts = line.split(",");
                if (parts.length < 3) {
                    continue; // 格式不对，跳过
                }

                String idStr = parts[0].trim();
                // 在节点数组中查找匹配的节点（通过名称或编号）
                int matchedNodeId = -1;
                for (int i = 0; i < nodeCount; i++) {
                    if (nodes[i].name.equals(idStr) || String.valueOf(nodes[i].id).equals(idStr)) {
                        matchedNodeId = i;
                        break;
                    }
                }

                // 如果找到了匹配的节点，设置其经纬度
                if (matchedNodeId >= 0) {
                    try {
                        nodes[matchedNodeId].longitude = Double.parseDouble(parts[1].trim());
                        nodes[matchedNodeId].latitude = Double.parseDouble(parts[2].trim());
                    } catch (NumberFormatException e) {
                        System.out.println("错误: 坐标格式错误 - " + line);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("读取节点坐标出错: " + e.getMessage());
        }
    }

    /**
     * 清空图中所有数据，将所有数组和计数器重置
     */
    private void clearData() {
        // 清空节点数组
        for (int i = 0; i < nodeCount; i++) {
            nodes[i] = null;
        }
        nodeCount = 0;

        // 清空边数组
        for (int i = 0; i < edgeCount; i++) {
            edges[i] = null;
        }
        edgeCount = 0;

        // 清空邻接矩阵和邻接表
        for (int i = 0; i < MAX_NODES; i++) {
            for (int j = 0; j < MAX_NODES; j++) {
                adjMatrix[i][j] = 0;
            }
            adjListSize[i] = 0;
        }
    }

    /**
     * 按度数百分位对节点进行分类
     * 
     * 分类规则：
     * - 前 20%：核心节点（度数最高的节点）
     * - 20% ~ 50%：活跃节点
     * - 后 50%：边缘节点
     */
    public void classifyNodes() {
        if (nodeCount == 0) {
            return; // 没有节点，无需分类
        }

        // 步骤 1：将所有节点的度数复制到一个数组中
        int[] sortedDegrees = new int[nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            sortedDegrees[i] = nodes[i].degree;
        }

        // 步骤 2：对度数数组进行降序排序（冒泡排序）
        for (int i = 0; i < nodeCount - 1; i++) {
            for (int j = 0; j < nodeCount - 1 - i; j++) {
                if (sortedDegrees[j] < sortedDegrees[j + 1]) {
                    // 交换，使较大的值排在前面
                    int temp = sortedDegrees[j];
                    sortedDegrees[j] = sortedDegrees[j + 1];
                    sortedDegrees[j + 1] = temp;
                }
            }
        }

        // 步骤 3：计算前 20% 和 50% 位置的度数值作为分类阈值
        // 前 20% 位置：nodeCount * 0.2 - 1，至少为 0
        int coreIndex = Math.max(0, (int) (nodeCount * 0.2) - 1);
        // 前 50% 位置：nodeCount * 0.5 - 1，至少为 0
        int activeIndex = Math.max(0, (int) (nodeCount * 0.5) - 1);

        int coreThreshold = sortedDegrees[coreIndex];     // 核心节点的度数下限
        int activeThreshold = sortedDegrees[activeIndex]; // 活跃节点的度数下限

        // 步骤 4：根据阈值给每个节点分类
        for (int i = 0; i < nodeCount; i++) {
            int nodeDegree = nodes[i].degree;
            if (nodeDegree >= coreThreshold) {
                nodes[i].type = "核心";
            } else if (nodeDegree >= activeThreshold) {
                nodes[i].type = "活跃";
            } else {
                nodes[i].type = "边缘";
            }
        }
    }

    /**
     * 获取指定节点的所有邻居编号
     * 
     * @param nodeId 节点索引
     * @return 邻居编号数组，如果节点无效则返回空数组
     */
    public int[] getNeighbors(int nodeId) {
        // 检查节点是否有效
        if (nodeId < 0 || nodeId >= nodeCount) {
            return new int[0];
        }
        // 从邻接表中复制邻居列表
        int neighborCount = adjListSize[nodeId];
        int[] result = new int[neighborCount];
        for (int i = 0; i < neighborCount; i++) {
            result[i] = adjList[nodeId][i];
        }
        return result;
    }

    /**
     * 查找指定中心节点附近的所有节点（基于地理距离）
     * 
     * @param centerId 中心节点索引
     * @param maxKm    最大距离（公里）
     * @return 在指定距离内的节点数组
     */
    public Node[] findNearby(int centerId, double maxKm) {
        // 检查中心节点是否有效
        if (centerId < 0 || centerId >= nodeCount) {
            return new Node[0];
        }

        // 第一遍：统计符合距离条件的节点数量
        int nearbyCount = 0;
        for (int i = 0; i < nodeCount; i++) {
            if (i != centerId) {
                // 计算节点 i 到中心节点的地理距离
                double geoDistance = nodes[centerId].geoDistanceTo(nodes[i]);
                if (geoDistance <= maxKm) {
                    nearbyCount = nearbyCount + 1;
                }
            }
        }

        // 第二遍：收集符合条件的节点
        Node[] result = new Node[nearbyCount];
        int resultIndex = 0;
        for (int i = 0; i < nodeCount; i++) {
            if (i != centerId) {
                double geoDistance = nodes[centerId].geoDistanceTo(nodes[i]);
                if (geoDistance <= maxKm) {
                    result[resultIndex] = nodes[i];
                    resultIndex = resultIndex + 1;
                }
            }
        }
        return result;
    }

    /**
     * 重置所有边的高亮状态为 false
     */
    public void resetHighlights() {
        for (int i = 0; i < edgeCount; i++) {
            edges[i].highlight = false;
        }
    }

    /**
     * 删除指定节点及其所有相连的边，并重建整个邻接结构
     * 
     * 删除节点的步骤：
     * 1. 收集所有不被删除的节点（跳过目标节点）
     * 2. 收集所有不涉及目标节点的边，并重新映射节点编号
     *    （因为删除一个节点后，后面的节点编号会前移，需要调整）
     * 3. 清空整个图结构
     * 4. 重新插入保留的节点和边，重建邻接矩阵和邻接表
     * 
     * @param nodeId 要删除的节点索引
     */
    public void removeNode(int nodeId) {
        // 检查节点编号是否有效
        if (nodeId < 0 || nodeId >= nodeCount) {
            return;
        }

        // ========== 步骤 1：收集保留的节点 ==========
        // 创建一个临时数组存放不被删除的节点
        Node[] keptNodes = new Node[MAX_NODES];
        int keptNodeCount = 0;
        for (int i = 0; i < nodeCount; i++) {
            if (i != nodeId) {
                // 保留这个节点
                keptNodes[keptNodeCount] = nodes[i];
                keptNodeCount = keptNodeCount + 1;
            }
        }

        // ========== 步骤 2：收集保留的边，并重新映射编号 ==========
        // 创建一个临时数组存放不被删除的边
        Edge[] keptEdges = new Edge[MAX_EDGES];
        int keptEdgeCount = 0;
        for (int i = 0; i < edgeCount; i++) {
            Edge currentEdge = edges[i];
            // 如果这条边的两端都不是被删除的节点，则保留
            if (currentEdge.from != nodeId && currentEdge.to != nodeId) {
                // 重新映射节点编号：删除 nodeId 后，所有编号大于 nodeId 的节点
                // 在新图中的编号都会减少 1
                int newFrom;
                if (currentEdge.from > nodeId) {
                    newFrom = currentEdge.from - 1; // 编号前移
                } else {
                    newFrom = currentEdge.from;      // 编号不变
                }

                int newTo;
                if (currentEdge.to > nodeId) {
                    newTo = currentEdge.to - 1;       // 编号前移
                } else {
                    newTo = currentEdge.to;           // 编号不变
                }

                // 用新的编号创建边
                keptEdges[keptEdgeCount] = new Edge(newFrom, newTo, currentEdge.weight);
                keptEdgeCount = keptEdgeCount + 1;
            }
        }

        // ========== 步骤 3：清空整个图结构 ==========
        clearData();

        // ========== 步骤 4：重新插入保留的节点 ==========
        for (int i = 0; i < keptNodeCount; i++) {
            nodes[i] = keptNodes[i];
            // 重置度数为 0，因为后面 addEdge 会重新计算
            nodes[i].degree = 0;
        }
        nodeCount = keptNodeCount;

        // ========== 步骤 5：重新插入保留的边，重建邻接结构 ==========
        for (int i = 0; i < keptEdgeCount; i++) {
            Edge currentEdge = keptEdges[i];

            // 5a. 将边存入边数组
            edges[i] = currentEdge;

            // 5b. 重建邻接矩阵（无向图，两边都设）
            adjMatrix[currentEdge.from][currentEdge.to] = currentEdge.weight;
            adjMatrix[currentEdge.to][currentEdge.from] = currentEdge.weight;

            // 5c. 重建邻接表（无向图，两边都添加）
            adjList[currentEdge.from][adjListSize[currentEdge.from]] = currentEdge.to;
            adjListSize[currentEdge.from] = adjListSize[currentEdge.from] + 1;

            adjList[currentEdge.to][adjListSize[currentEdge.to]] = currentEdge.from;
            adjListSize[currentEdge.to] = adjListSize[currentEdge.to] + 1;

            // 5d. 更新两个节点的度数
            nodes[currentEdge.from].degree = nodes[currentEdge.from].degree + 1;
            nodes[currentEdge.to].degree = nodes[currentEdge.to].degree + 1;
        }
        edgeCount = keptEdgeCount;
    }
}