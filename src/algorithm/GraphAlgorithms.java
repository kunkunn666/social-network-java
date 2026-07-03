package algorithm;

/**
 * 图算法集 — 提供多种社交网络分析算法
 * 包含：BFS无权最短路径、Dijkstra有权最短路径、度中心性、接近中心性、介数中心性、
 * 图直径、平均路径长度、平均度、图密度、TopN度数节点
 */

import model.SocialGraph;
import model.Node;

public class GraphAlgorithms {

    public int[] bfsShortestPath(SocialGraph graph, int startId) {
        int n = graph.nodeCount;
        int[] dist = new int[n];
        for (int i = 0; i < n; i++) dist[i] = -1;
        if (startId < 0 || startId >= n) return dist;

        int[] queue = new int[n];
        int head = 0, tail = 0;
        queue[tail++] = startId;
        dist[startId] = 0;

        while (head < tail) {
            int cur = queue[head++];
            for (int i = 0; i < graph.adjListSize[cur]; i++) {
                int nb = graph.adjList[cur][i];
                if (dist[nb] == -1) {
                    dist[nb] = dist[cur] + 1;
                    queue[tail++] = nb;
                }
            }
        }
        return dist;
    }

    public int shortestPathLength(SocialGraph graph, int fromId, int toId) {
        if (fromId < 0 || toId < 0 || fromId >= graph.nodeCount || toId >= graph.nodeCount) return -1;
        int[] dist = bfsShortestPath(graph, fromId);
        return dist[toId];
    }

    public int[] shortestPath(SocialGraph graph, int fromId, int toId) {
        if (fromId < 0 || toId < 0 || fromId >= graph.nodeCount || toId >= graph.nodeCount) return new int[0];
        int n = graph.nodeCount;
        int[] prev = new int[n];
        int[] dist = new int[n];
        for (int i = 0; i < n; i++) { prev[i] = -1; dist[i] = -1; }

        int[] queue = new int[n];
        int head = 0, tail = 0;
        queue[tail++] = fromId;
        dist[fromId] = 0;

        boolean found = false;
        while (head < tail && !found) {
            int cur = queue[head++];
            for (int i = 0; i < graph.adjListSize[cur]; i++) {
                int nb = graph.adjList[cur][i];
                if (dist[nb] == -1) {
                    dist[nb] = dist[cur] + 1;
                    prev[nb] = cur;
                    queue[tail++] = nb;
                    if (nb == toId) { found = true; break; }
                }
            }
        }

        if (dist[toId] == -1) return new int[0];
        int pathLen = dist[toId] + 1;
        int[] path = new int[pathLen];
        int cur = toId;
        for (int i = pathLen - 1; i >= 0; i--) {
            path[i] = cur;
            cur = prev[cur];
        }
        return path;
    }

    public int[] dijkstraShortestPath(SocialGraph graph, int fromId, int toId) {
        int n = graph.nodeCount;
        if (fromId < 0 || toId < 0 || fromId >= n || toId >= n) return new int[0];

        double[] dist = new double[n];
        int[] prev = new int[n];
        boolean[] visited = new boolean[n];
        for (int i = 0; i < n; i++) { dist[i] = Double.MAX_VALUE; prev[i] = -1; }
        dist[fromId] = 0;

        for (int k = 0; k < n; k++) {
            int u = -1;
            double minDist = Double.MAX_VALUE;
            for (int i = 0; i < n; i++) {
                if (!visited[i] && dist[i] < minDist) { minDist = dist[i]; u = i; }
            }
            if (u == -1 || u == toId) break;
            visited[u] = true;

            for (int i = 0; i < graph.adjListSize[u]; i++) {
                int v = graph.adjList[u][i];
                double w = graph.adjMatrix[u][v];
                if (w <= 0) w = 1.0;
                if (!visited[v] && dist[u] + w < dist[v]) {
                    dist[v] = dist[u] + w;
                    prev[v] = u;
                }
            }
        }

        if (dist[toId] == Double.MAX_VALUE) return new int[0];
        int pathLen = 0;
        for (int cur = toId; cur != -1; cur = prev[cur]) pathLen++;
        int[] path = new int[pathLen];
        int cur = toId;
        for (int i = pathLen - 1; i >= 0; i--) { path[i] = cur; cur = prev[cur]; }
        return path;
    }

    public double[] degreeCentrality(SocialGraph graph) {
        int n = graph.nodeCount;
        double[] centrality = new double[n];
        if (n <= 1) return centrality;
        for (int i = 0; i < n; i++) {
            centrality[i] = (double) graph.nodes[i].degree / (n - 1);
        }
        return centrality;
    }

    public double[] betweennessCentrality(SocialGraph graph) {
        int n = graph.nodeCount;
        double[] bc = new double[n];
        if (n <= 2) return bc;

        for (int s = 0; s < n; s++) {
            int[] stack = new int[n];
            int stackTop = -1;
            int[][] pred = new int[n][n];
            int[] predLen = new int[n];
            double[] sigma = new double[n];
            int[] dist = new int[n];
            for (int i = 0; i < n; i++) dist[i] = -1;
            sigma[s] = 1;
            dist[s] = 0;

            int[] queue = new int[n];
            int qHead = 0, qTail = 0;
            queue[qTail++] = s;

            while (qHead < qTail) {
                int v = queue[qHead++];
                stack[++stackTop] = v;
                for (int i = 0; i < graph.adjListSize[v]; i++) {
                    int w = graph.adjList[v][i];
                    if (dist[w] == -1) {
                        dist[w] = dist[v] + 1;
                        queue[qTail++] = w;
                    }
                    if (dist[w] == dist[v] + 1) {
                        sigma[w] += sigma[v];
                        pred[w][predLen[w]++] = v;
                    }
                }
            }

            double[] delta = new double[n];
            while (stackTop >= 0) {
                int w = stack[stackTop--];
                for (int i = 0; i < predLen[w]; i++) {
                    int v = pred[w][i];
                    delta[v] += (sigma[v] / sigma[w]) * (1 + delta[w]);
                }
                if (w != s) bc[w] += delta[w];
            }
        }

        for (int i = 0; i < n; i++) {
            bc[i] = bc[i] / ((n - 1) * (n - 2));
        }
        return bc;
    }

    public double[] closenessCentrality(SocialGraph graph) {
        int n = graph.nodeCount;
        double[] cc = new double[n];
        if (n <= 1) return cc;
        for (int i = 0; i < n; i++) {
            int[] dist = bfsShortestPath(graph, i);
            int sum = 0;
            int reachable = 0;
            for (int j = 0; j < n; j++) {
                if (dist[j] > 0) {
                    sum += dist[j];
                    reachable++;
                }
            }
            if (sum > 0 && reachable > 0) {
                cc[i] = (double) reachable / (n - 1) * (double) reachable / sum;
            }
        }
        return cc;
    }

    public int findNodeWithHighestDegree(SocialGraph graph) {
        if (graph.nodeCount == 0) return -1;
        int maxId = 0;
        for (int i = 1; i < graph.nodeCount; i++) {
            if (graph.nodes[i].degree > graph.nodes[maxId].degree) maxId = i;
        }
        return maxId;
    }

    public double averageDegree(SocialGraph graph) {
        if (graph.nodeCount == 0) return 0;
        int sum = 0;
        for (int i = 0; i < graph.nodeCount; i++) sum += graph.nodes[i].degree;
        return (double) sum / graph.nodeCount;
    }

    public double averagePathLength(SocialGraph graph) {
        int n = graph.nodeCount;
        if (n <= 1) return 0;
        long total = 0;
        int count = 0;
        for (int i = 0; i < n; i++) {
            int[] dist = bfsShortestPath(graph, i);
            for (int j = i + 1; j < n; j++) {
                if (dist[j] > 0) {
                    total += dist[j];
                    count++;
                }
            }
        }
        return count > 0 ? (double) total / count : 0;
    }

    public int diameter(SocialGraph graph) {
        int n = graph.nodeCount;
        if (n <= 1) return 0;
        int maxDist = 0;
        for (int i = 0; i < n; i++) {
            int[] dist = bfsShortestPath(graph, i);
            for (int j = 0; j < n; j++) {
                if (dist[j] > maxDist) maxDist = dist[j];
            }
        }
        return maxDist;
    }

    public double graphDensity(SocialGraph graph) {
        int n = graph.nodeCount;
        if (n <= 1) return 0;
        double maxEdges = (double) n * (n - 1) / 2;
        return (double) graph.edgeCount / maxEdges;
    }

    public Node[] getTopNodesByDegree(SocialGraph graph, int k) {
        int n = graph.nodeCount;
        if (k > n) k = n;
        if (k <= 0) return new Node[0];
        int[] indices = new int[n];
        for (int i = 0; i < n; i++) indices[i] = i;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - 1 - i; j++) {
                if (graph.nodes[indices[j]].degree < graph.nodes[indices[j + 1]].degree) {
                    int t = indices[j]; indices[j] = indices[j + 1]; indices[j + 1] = t;
                }
            }
        }
        Node[] result = new Node[k];
        for (int i = 0; i < k; i++) result[i] = graph.nodes[indices[i]];
        return result;
    }
}
