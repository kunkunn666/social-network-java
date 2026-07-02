package algorithm;

import model.SocialGraph;

public class CommunityDetection {

    public int detectCommunities(SocialGraph graph) {
        int n = graph.nodeCount;
        if (n == 0) return 0;

        int[] community = new int[n];
        for (int i = 0; i < n; i++) {
            community[i] = i;
            graph.nodes[i].community = i;
        }

        boolean improved = true;
        int maxIterations = 20;
        int iter = 0;

        while (improved && iter < maxIterations) {
            improved = false;
            iter++;

            for (int i = 0; i < n; i++) {
                int bestCommunity = community[i];
                double bestGain = 0;

                int[] neighbors = getNeighbors(graph, i);
                double[] communityWeights = new double[n];

                for (int k = 0; k < neighbors.length; k++) {
                    int nb = neighbors[k];
                    int nbComm = community[nb];
                    double weight = graph.adjMatrix[i][nb];
                    if (weight <= 0) weight = 1.0;
                    communityWeights[nbComm] += weight;
                }

                double selfWeight = graph.adjMatrix[i][i];
                double totalWeight = getTotalNodeWeight(graph, i);

                for (int c = 0; c < n; c++) {
                    if (communityWeights[c] > 0) {
                        double gain = communityWeights[c] - (getCommunityTotalWeight(graph, community, c) + selfWeight) * totalWeight / (2.0 * graph.edgeCount * 2 + 1);
                        if (gain > bestGain + 0.0001) {
                            bestGain = gain;
                            bestCommunity = c;
                        }
                    }
                }

                if (bestCommunity != community[i]) {
                    community[i] = bestCommunity;
                    graph.nodes[i].community = bestCommunity;
                    improved = true;
                }
            }
        }

        return countAndRenameCommunities(graph, community);
    }

    private int[] getNeighbors(SocialGraph graph, int nodeId) {
        int size = graph.adjListSize[nodeId];
        int[] result = new int[size];
        for (int i = 0; i < size; i++) result[i] = graph.adjList[nodeId][i];
        return result;
    }

    private double getTotalNodeWeight(SocialGraph graph, int nodeId) {
        double sum = 0;
        for (int i = 0; i < graph.adjListSize[nodeId]; i++) {
            int nb = graph.adjList[nodeId][i];
            double w = graph.adjMatrix[nodeId][nb];
            sum += (w > 0) ? w : 1.0;
        }
        return sum;
    }

    private double getCommunityTotalWeight(SocialGraph graph, int[] community, int commId) {
        double sum = 0;
        for (int i = 0; i < graph.nodeCount; i++) {
            if (community[i] == commId) {
                sum += getTotalNodeWeight(graph, i);
            }
        }
        return sum;
    }

    private int countAndRenameCommunities(SocialGraph graph, int[] community) {
        int n = graph.nodeCount;
        int[] oldToNew = new int[n];
        for (int i = 0; i < n; i++) oldToNew[i] = -1;
        int count = 0;

        for (int i = 0; i < n; i++) {
            if (oldToNew[community[i]] == -1) {
                oldToNew[community[i]] = count;
                count++;
            }
            graph.nodes[i].community = oldToNew[community[i]];
        }
        return count;
    }

    public int detectCommunitiesByLabelPropagation(SocialGraph graph) {
        int n = graph.nodeCount;
        if (n == 0) return 0;

        int[] labels = new int[n];
        for (int i = 0; i < n; i++) {
            labels[i] = i;
            graph.nodes[i].community = i;
        }

        int maxIterations = 30;
        for (int iter = 0; iter < maxIterations; iter++) {
            boolean changed = false;

            int[] order = new int[n];
            for (int i = 0; i < n; i++) order[i] = i;
            for (int i = n - 1; i > 0; i--) {
                int j = (int) (Math.random() * (i + 1));
                int t = order[i]; order[i] = order[j]; order[j] = t;
            }

            for (int idx = 0; idx < n; idx++) {
                int i = order[idx];
                int[] neighbors = getNeighbors(graph, i);
                if (neighbors.length == 0) continue;

                int maxLabel = labels[i];
                int maxCount = 0;
                int[] labelCount = new int[n];

                for (int k = 0; k < neighbors.length; k++) {
                    int nb = neighbors[k];
                    int nbLabel = labels[nb];
                    labelCount[nbLabel]++;
                    if (labelCount[nbLabel] > maxCount) {
                        maxCount = labelCount[nbLabel];
                        maxLabel = nbLabel;
                    }
                }

                if (maxLabel != labels[i]) {
                    labels[i] = maxLabel;
                    graph.nodes[i].community = maxLabel;
                    changed = true;
                }
            }

            if (!changed) break;
        }

        return countAndRenameCommunities(graph, labels);
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

    public double calculateModularity(SocialGraph graph) {
        int n = graph.nodeCount;
        if (n == 0 || graph.edgeCount == 0) return 0;

        double m2 = graph.edgeCount * 2.0;
        double modularity = 0;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (graph.nodes[i].community == graph.nodes[j].community) {
                    double aij = graph.adjMatrix[i][j];
                    if (aij <= 0 && graph.adjListSize[i] > 0 && graph.adjListSize[j] > 0) {
                        aij = (areNeighbors(graph, i, j)) ? 1.0 : 0.0;
                    }
                    double ki = getTotalNodeWeight(graph, i);
                    double kj = getTotalNodeWeight(graph, j);
                    modularity += aij - (ki * kj) / m2;
                }
            }
        }
        return modularity / m2;
    }

    private boolean areNeighbors(SocialGraph graph, int a, int b) {
        for (int i = 0; i < graph.adjListSize[a]; i++) {
            if (graph.adjList[a][i] == b) return true;
        }
        return false;
    }
}
