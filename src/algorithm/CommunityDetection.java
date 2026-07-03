package algorithm;

import model.SocialGraph;

public class CommunityDetection {

    /**
     * BFS连通分量检测：用数组 component[N] 存储每个节点所属连通分量编号
     * @return 连通分量个数
     */
    public int detectConnectedComponents(SocialGraph graph) {
        int n = graph.nodeCount;
        if (n == 0) return 0;

        // component[i] = 节点i所属的连通分量编号（0-based）
        int[] component = new int[n];
        for (int i = 0; i < n; i++) component[i] = -1;

        int[] queue = new int[n];
        int compId = 0;

        for (int i = 0; i < n; i++) {
            if (component[i] != -1) continue;

            // BFS遍历当前连通分量
            int head = 0, tail = 0;
            queue[tail++] = i;
            component[i] = compId;

            while (head < tail) {
                int cur = queue[head++];
                for (int k = 0; k < graph.adjListSize[cur]; k++) {
                    int nb = graph.adjList[cur][k];
                    if (component[nb] == -1) {
                        component[nb] = compId;
                        queue[tail++] = nb;
                    }
                }
            }
            compId++;
        }

        // 回写到 Node.community
        for (int i = 0; i < n; i++) {
            graph.nodes[i].community = component[i];
        }

        return compId;
    }

    public int getCommunityCount(SocialGraph graph) {
        int n = graph.nodeCount;
        boolean[] seen = new boolean[n];
        int count = 0;
        for (int i = 0; i < n; i++) {
            int c = graph.nodes[i].community;
            if (c >= 0 && !seen[c]) {
                seen[c] = true;
                count++;
            }
        }
        return count;
    }

    public int[] getCommunityNodes(SocialGraph graph, int communityId) {
        int n = graph.nodeCount;
        int count = 0;
        for (int i = 0; i < n; i++) {
            if (graph.nodes[i].community == communityId) count++;
        }
        int[] result = new int[count];
        int idx = 0;
        for (int i = 0; i < n; i++) {
            if (graph.nodes[i].community == communityId) {
                result[idx++] = i;
            }
        }
        return result;
    }
}