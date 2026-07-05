package algorithm;

/**
 * 连通分量检测 — 找出图中的所有连通分量
 */

import model.SocialGraph;

public class CommunityDetection {

    /**
     * 使用 BFS 检测图中的所有连通分量
     * 
     * 算法步骤：
     * 1. 初始化 vset 数组，所有节点为 -1（未分配）
     * 2. 遍历每个节点，如果该节点还未分配，就从这个节点开始 BFS
     * 3. BFS 过程中，所有访问到的节点都属于同一个连通分量
     * 4. BFS 结束后，编号加 1，继续找下一个未分配的节点
     * 5. 将结果写回 graph.vset
     */
    public int detect(SocialGraph graph) {
        int totalNodes = graph.nodeCount;
        if (totalNodes == 0) {
            return 0; //空图，没有连通分量
        }

        //vset[i] = 节点 i 所属的连通分量编号（从 0 开始），-1 表示未分配
        for (int i = 0; i < totalNodes; i++) {
            graph.vset[i] = -1;
        }

        //BFS 队列
        int[] queue = new int[totalNodes];
        //当前连通分量编号
        int componentId = 0;

        //遍历所有节点
        for (int i = 0; i < totalNodes; i++) {
            //如果节点 i 已经分配了，跳过
            if (graph.vset[i] != -1) {
                continue;
            }

            // ------ 从节点 i 开始 BFS，探索一个新的连通分量 ------
            int queueHead = 0; //队头指针
            int queueTail = 0; //队尾指针

            //将起始节点入队
            queue[queueTail] = i;
            queueTail = queueTail + 1;
            //标记起始节点属于当前连通分量
            graph.vset[i] = componentId;

            //BFS 主循环
            while (queueHead < queueTail) {
                //取出队头节点
                int currentNode = queue[queueHead];
                queueHead = queueHead + 1;

                //遍历当前节点的所有邻居
                for (int k = 0; k < graph.adjListCount[currentNode]; k++) {
                    int neighbor = graph.adjList[currentNode][k];
                    //如果邻居还没被分配
                    if (graph.vset[neighbor] == -1) {
                        //标记邻居属于当前连通分量
                        graph.vset[neighbor] = componentId;
                        //将邻居入队，继续探索
                        queue[queueTail] = neighbor;
                        queueTail = queueTail + 1;
                    }
                }
            }

            //当前连通分量探索完毕，编号加 1，准备探索下一个
            componentId = componentId + 1;
        }

        //返回连通分量总数
        return componentId;
    }
}