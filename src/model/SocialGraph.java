package model;

/**
 * 社交网络图模型 — 核心数据层
 * 加载CSV数据集，构建邻接矩阵和邻接表，按度百分位分类节点（核心/活跃/边缘），
 * 提供节点查找、邻居查询、高亮重置等基础操作
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;

public class SocialGraph {
    public Node[] nodes;
    public int nodeCount;
    public Edge[] edges;
    public int edgeCount;
    public double[][] adjMatrix;
    public int[][] adjList;
    public int[] adjListSize;
    public String datasetName;
    public String datasetDescription;

    private static final int MAX_NODES = 500;
    private static final int MAX_EDGES = 5000;
    private static final int MAX_DEGREE = 200;

    public SocialGraph(String name, String description) {
        this.datasetName = name;
        this.datasetDescription = description;
        this.nodes = new Node[MAX_NODES];
        this.nodeCount = 0;
        this.edges = new Edge[MAX_EDGES];
        this.edgeCount = 0;
        this.adjMatrix = new double[MAX_NODES][MAX_NODES];
        this.adjList = new int[MAX_NODES][MAX_DEGREE];
        this.adjListSize = new int[MAX_NODES];
    }

    public int addNode(String name) {
        if (nodeCount >= MAX_NODES) return -1;
        try {
            int id = Integer.parseInt(name);
            nodes[nodeCount] = new Node(id, name);
        } catch (NumberFormatException e) {
            nodes[nodeCount] = new Node(nodeCount + 1, name);
        }
        nodeCount++;
        return nodeCount - 1;
    }

    public int findNodeId(String name) {
        for (int i = 0; i < nodeCount; i++) {
            if (nodes[i].name.equals(name)) return i;
        }
        return -1;
    }

    public Node getNode(int id) {
        return (id >= 0 && id < nodeCount) ? nodes[id] : null;
    }

    public Node getNodeByName(String name) {
        int id = findNodeId(name);
        return (id >= 0) ? nodes[id] : null;
    }

    public void addEdge(int from, int to, double weight) {
        if (from < 0 || from >= nodeCount || to < 0 || to >= nodeCount) return;
        if (edgeCount >= MAX_EDGES) return;
        edges[edgeCount] = new Edge(from, to, weight);
        edgeCount++;
        adjMatrix[from][to] = weight;
        adjMatrix[to][from] = weight;
        if (adjListSize[from] < MAX_DEGREE) {
            adjList[from][adjListSize[from]] = to;
            adjListSize[from]++;
        }
        if (adjListSize[to] < MAX_DEGREE) {
            adjList[to][adjListSize[to]] = from;
            adjListSize[to]++;
        }
        nodes[from].degree++;
        nodes[to].degree++;
    }

    public double getWeight(int from, int to) {
        return adjMatrix[from][to];
    }

    public void loadFromCSV(String filepath) {
        clearData();

        File f = new File(filepath);
        if (!f.exists()) {
            System.out.println("错误: 文件不存在 - " + filepath);
            return;
        }

        String nodeFile = findNodeFile(filepath);

        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {
                if (isFirstLine) { isFirstLine = false; continue; }
                String[] parts = line.split(",");
                if (parts.length < 2) continue;
                String name1 = parts[0].trim(), name2 = parts[1].trim();
                if (findNodeId(name1) == -1) addNode(name1);
                if (findNodeId(name2) == -1) addNode(name2);
            }
        } catch (Exception e) {
            System.out.println("读取CSV出错: " + e.getMessage());
        }

        if (nodeFile != null) {
            loadNodeCoordinates(nodeFile);
        } else {
            for (int i = 0; i < nodeCount; i++) {
                nodes[i].longitude = 116.2 + (double) i / nodeCount * 0.6;
                nodes[i].latitude = 39.8 + (double) (i % 10) / 10 * 0.4;
            }
        }

        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {
                if (isFirstLine) { isFirstLine = false; continue; }
                String[] parts = line.split(",");
                if (parts.length < 2) continue;
                String name1 = parts[0].trim(), name2 = parts[1].trim();
                double weight = 1.0;
                if (parts.length >= 3) {
                    try { weight = Double.parseDouble(parts[2].trim()); }
                    catch (NumberFormatException ignored) {}
                }
                int id1 = findNodeId(name1), id2 = findNodeId(name2);
                if (id1 != -1 && id2 != -1 && id1 != id2) addEdge(id1, id2, weight);
            }
        } catch (Exception e) {
            System.out.println("读取边数据出错: " + e.getMessage());
        }

        classifyNodes();
    }

    private String findNodeFile(String edgeFile) {
        String nodeFile = edgeFile.replace(".csv", "_nodes.csv");
        if (new File(nodeFile).exists()) return nodeFile;
        return null;
    }

    private void loadNodeCoordinates(String filepath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {
                if (isFirstLine) { isFirstLine = false; continue; }
                String[] parts = line.split(",");
                if (parts.length < 3) continue;

                String idStr = parts[0].trim();
                int nodeId = -1;
                for (int i = 0; i < nodeCount; i++) {
                    if (nodes[i].name.equals(idStr) || String.valueOf(nodes[i].id).equals(idStr)) {
                        nodeId = i; break;
                    }
                }

                if (nodeId >= 0) {
                    try {
                        boolean is3Column;
                        try {
                            Double.parseDouble(parts[1].trim());
                            is3Column = true;
                        } catch (NumberFormatException e) {
                            is3Column = false;
                        }

                        if (is3Column) {
                            nodes[nodeId].longitude = Double.parseDouble(parts[1].trim());
                            nodes[nodeId].latitude = Double.parseDouble(parts[2].trim());
                        } else if (parts.length >= 4) {
                            nodes[nodeId].longitude = Double.parseDouble(parts[2].trim());
                            nodes[nodeId].latitude = Double.parseDouble(parts[3].trim());
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (Exception e) {
            System.out.println("读取节点坐标出错: " + e.getMessage());
        }
    }

    private void clearData() {
        for (int i = 0; i < nodeCount; i++) nodes[i] = null;
        nodeCount = 0;
        for (int i = 0; i < edgeCount; i++) edges[i] = null;
        edgeCount = 0;
        for (int i = 0; i < MAX_NODES; i++) {
            for (int j = 0; j < MAX_NODES; j++) adjMatrix[i][j] = 0;
            adjListSize[i] = 0;
        }
    }

    public void classifyNodes() {
        if (nodeCount == 0) return;

        // 按度数排序，取阈值
        int[] sortedDegrees = new int[nodeCount];
        for (int i = 0; i < nodeCount; i++) sortedDegrees[i] = nodes[i].degree;
        for (int i = 0; i < nodeCount - 1; i++) {
            for (int j = 0; j < nodeCount - 1 - i; j++) {
                if (sortedDegrees[j] < sortedDegrees[j + 1]) {
                    int t = sortedDegrees[j]; sortedDegrees[j] = sortedDegrees[j + 1]; sortedDegrees[j + 1] = t;
                }
            }
        }
        // 前20%为"核心"，20%~50%为"活跃"，后50%为"边缘"
        int coreIdx = Math.max(0, (int) (nodeCount * 0.2) - 1);
        int activeIdx = Math.max(0, (int) (nodeCount * 0.5) - 1);
        int coreT = sortedDegrees[coreIdx];
        int activeT = sortedDegrees[activeIdx];

        for (int i = 0; i < nodeCount; i++) {
            int d = nodes[i].degree;
            if (d >= coreT) nodes[i].type = "核心";
            else if (d >= activeT) nodes[i].type = "活跃";
            else nodes[i].type = "边缘";
        }
    }

    public int[] getNeighbors(int nodeId) {
        if (nodeId < 0 || nodeId >= nodeCount) return new int[0];
        int size = adjListSize[nodeId];
        int[] result = new int[size];
        for (int i = 0; i < size; i++) result[i] = adjList[nodeId][i];
        return result;
    }

    public int getNeighborCount(int nodeId) {
        return (nodeId < 0 || nodeId >= nodeCount) ? 0 : adjListSize[nodeId];
    }

    public String[] getAllNames() {
        String[] names = new String[nodeCount];
        for (int i = 0; i < nodeCount; i++) names[i] = nodes[i].name;
        for (int i = 0; i < nodeCount - 1; i++) {
            for (int j = 0; j < nodeCount - 1 - i; j++) {
                if (names[j].compareTo(names[j + 1]) > 0) {
                    String t = names[j]; names[j] = names[j + 1]; names[j + 1] = t;
                }
            }
        }
        return names;
    }

    public Node[] findNearby(int centerId, double maxKm) {
        if (centerId < 0 || centerId >= nodeCount) return new Node[0];
        int count = 0;
        for (int i = 0; i < nodeCount; i++) {
            if (i != centerId && nodes[centerId].geoDistanceTo(nodes[i]) <= maxKm) count++;
        }
        Node[] result = new Node[count];
        int idx = 0;
        for (int i = 0; i < nodeCount; i++) {
            if (i != centerId && nodes[centerId].geoDistanceTo(nodes[i]) <= maxKm) {
                result[idx++] = nodes[i];
            }
        }
        return result;
    }

    public Node[] findCircle(int centerId, int maxHops) {
        if (centerId < 0 || centerId >= nodeCount) return new Node[0];
        boolean[] visited = new boolean[MAX_NODES];
        int[] distance = new int[MAX_NODES];
        for (int i = 0; i < nodeCount; i++) distance[i] = -1;
        int[] queue = new int[MAX_NODES];
        int head = 0, tail = 0;
        queue[tail++] = centerId;
        visited[centerId] = true;
        distance[centerId] = 0;
        while (head < tail) {
            int cur = queue[head++];
            if (distance[cur] >= maxHops) continue;
            for (int i = 0; i < adjListSize[cur]; i++) {
                int nb = adjList[cur][i];
                if (!visited[nb]) {
                    visited[nb] = true;
                    distance[nb] = distance[cur] + 1;
                    queue[tail++] = nb;
                }
            }
        }
        int count = 0;
        for (int i = 0; i < nodeCount; i++) {
            if (visited[i] && i != centerId) count++;
        }
        Node[] result = new Node[count];
        int idx = 0;
        for (int i = 0; i < nodeCount; i++) {
            if (visited[i] && i != centerId) result[idx++] = nodes[i];
        }
        return result;
    }

    public void resetHighlights() {
        for (int i = 0; i < edgeCount; i++) edges[i].highlight = false;
    }

    public String getStats() {
        int core = 0, active = 0, edge = 0;
        for (int i = 0; i < nodeCount; i++) {
            if ("核心".equals(nodes[i].type)) core++;
            else if ("活跃".equals(nodes[i].type)) active++;
            else edge++;
        }
        return "节点: " + nodeCount + "  边: " + edgeCount + "  核心: " + core + "  活跃: " + active + "  边缘: " + edge;
    }
}