package view;

import model.SocialGraph;
import algorithm.GraphAlgorithms;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * 查询功能面板 — 最短路径、N跳圈子、附近用户
 * BFS无权最短路径（按跳数）、Dijkstra有权最短路径（按权重）、N跳交往圈子查询
 */
public class QueryPanel extends JPanel {

    /** 主窗口引用（用于更新状态栏） */
    private MainFrame mainFrame;

    /** 图可视化面板引用（用于高亮显示结果） */
    private GraphPanel graphPanel;

    /** 图算法工具类 */
    private GraphAlgorithms algorithms;

    // ========== 最短路径相关组件 ==========

    /** 起点节点名称输入框 */
    private JTextField fromNodeField;

    /** 终点节点名称输入框 */
    private JTextField toNodeField;

    /** 路径结果显示标签（显示距离） */
    private JLabel pathResultLabel;

    /** 路径详情文本区域（显示路径节点序列） */
    private JTextArea pathArea;

    // ========== N跳圈子相关组件 ==========

    /** N跳数值输入框 */
    private JTextField nHopField;

    /** 中心节点名称输入框 */
    private JTextField nHopNodeField;

    /** N跳圈子结果文本区域 */
    private JTextArea nHopResultArea;

    // ========== 附近用户相关组件 ==========

    /** 搜索半径输入框 */
    private JTextField nearbyRadiusField;

    /** 附近用户结果文本区域 */
    private JTextArea nearbyResultArea;

    /**
     * 构造函数：创建查询面板，包含最短路径、N跳圈子、附近用户三个子面板
     *
     * @param mainFrame 主窗口引用
     * @param graphPanel 图面板引用
     */
    public QueryPanel(MainFrame mainFrame, GraphPanel graphPanel) {
        this.mainFrame = mainFrame;
        this.graphPanel = graphPanel;
        this.algorithms = new GraphAlgorithms();

        // 使用垂直布局
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // 设置面板标题边框
        setBorder(createTitledBorder("查询功能模块"));

        // 依次添加三个子功能面板
        add(createPathPanel());      // 1. 最短路径查询面板
        add(createNHopPanel());      // 2. N跳圈子查询面板
        add(createNearbyPanel());    // 3. 附近用户查询面板
    }

    /**
     * 更新图数据引用，清空所有查询结果
     *
     * @param graph 新的图数据
     */
    public void setGraph(SocialGraph graph) {
        // 清空所有结果显示区域
        pathResultLabel.setText(" ");
        pathArea.setText("");
        nHopResultArea.setText("");
        nearbyResultArea.setText("");
    }

    // ==================== 最短路径 ====================

    /**
     * 创建最短路径查询面板
     * 包含起点、终点输入框，BFS和Dijkstra两个查询按钮，以及结果显示区域
     */
    private JPanel createPathPanel() {
        // 使用网格包布局（GridBagLayout），可以灵活控制组件位置和大小
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("最短路径"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);  // 组件间距2像素
        gbc.fill = GridBagConstraints.HORIZONTAL; // 组件水平填充

        // 第0行：起点标签和输入框
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        panel.add(new JLabel("起点:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        fromNodeField = new JTextField(6);  // 6字符宽度的输入框
        panel.add(fromNodeField, gbc);

        // 第1行：终点标签和输入框
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panel.add(new JLabel("终点:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        toNodeField = new JTextField(6);
        panel.add(toNodeField, gbc);

        // 第2行：查询按钮（BFS和Dijkstra并排）
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 3, 0));

        // BFS按钮：使用广度优先搜索计算最短路径（按跳数）
        JButton bfsButton = new JButton("BFS");
        bfsButton.addActionListener(e -> calculateShortestPath());

        // Dijkstra按钮：使用Dijkstra算法计算带权最短路径（按权重）
        JButton dijkstraButton = new JButton("Dijkstra");
        dijkstraButton.addActionListener(e -> calculateDijkstraPath());

        buttonPanel.add(bfsButton);
        buttonPanel.add(dijkstraButton);
        panel.add(buttonPanel, gbc);

        // 第3行：路径结果显示标签（显示距离）
        gbc.gridy = 3;
        pathResultLabel = new JLabel(" ");
        pathResultLabel.setForeground(new Color(66, 133, 244)); // 蓝色文字
        panel.add(pathResultLabel, gbc);

        // 第4行：路径详情文本区域（显示节点序列）
        gbc.gridy = 4; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1;
        pathArea = new JTextArea(3, 10);
        pathArea.setEditable(false);
        pathArea.setLineWrap(true);
        pathArea.setWrapStyleWord(true);
        pathArea.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        pathArea.setBackground(new Color(248, 248, 248)); // 浅灰背景

        JScrollPane pathScroll = new JScrollPane(pathArea);
        pathScroll.setPreferredSize(new Dimension(200, 50));
        panel.add(pathScroll, gbc);

        return panel;
    }

    /**
     * 使用BFS（广度优先搜索）计算无权最短路径
     * 按跳数（边的数量）计算最短路径，不考虑边的权重
     */
    public void calculateShortestPath() {
        // 获取图数据
        SocialGraph graph = graphPanel.graph;

        // 检查图数据是否有效
        if (graph == null || graph.nodeCount == 0) return;

        // 获取用户输入的起点和终点名称
        String fromNodeName = fromNodeField.getText().trim();
        String toNodeName = toNodeField.getText().trim();

        // 根据名称查找节点ID
        int fromNodeId = graph.findNodeId(fromNodeName);
        int toNodeId = graph.findNodeId(toNodeName);

        // 检查节点是否存在
        if (fromNodeId == -1) {
            error("未找到起点: " + fromNodeName);
            return;
        }
        if (toNodeId == -1) {
            error("未找到终点: " + toNodeName);
            return;
        }

        // 调用BFS算法计算最短路径
        int[] shortestPath = algorithms.shortestPath(graph, fromNodeId, toNodeId);

        if (shortestPath.length == 0) {
            // 没有路径：两点不可达
            pathResultLabel.setText("BFS: 两点不可达");
            pathArea.setText("");
            graphPanel.setHighlightedPath(new int[0]);
        } else {
            // 有路径：显示跳数和路径详情
            int hopCount = shortestPath.length - 1; // 跳数 = 路径节点数 - 1
            pathResultLabel.setText("BFS距离: " + hopCount + " 跳");
            showPath(shortestPath);
            graphPanel.setHighlightedPath(shortestPath);
        }

        // 更新状态栏
        mainFrame.updateStatus("BFS最短路径: " + fromNodeName + " → " + toNodeName);
    }

    /**
     * 使用Dijkstra算法计算带权最短路径
     * 按边的权重之和计算最短路径
     */
    public void calculateDijkstraPath() {
        // 获取图数据
        SocialGraph graph = graphPanel.graph;

        // 检查图数据是否有效
        if (graph == null || graph.nodeCount == 0) return;

        // 获取用户输入的起点和终点名称
        String fromNodeName = fromNodeField.getText().trim();
        String toNodeName = toNodeField.getText().trim();

        // 根据名称查找节点ID
        int fromNodeId = graph.findNodeId(fromNodeName);
        int toNodeId = graph.findNodeId(toNodeName);

        // 检查节点是否存在
        if (fromNodeId == -1) {
            error("未找到起点: " + fromNodeName);
            return;
        }
        if (toNodeId == -1) {
            error("未找到终点: " + toNodeName);
            return;
        }

        // 调用Dijkstra算法计算带权最短路径
        int[] shortestPath = algorithms.dijkstraShortestPath(graph, fromNodeId, toNodeId);

        if (shortestPath.length == 0) {
            // 没有路径：两点不可达
            pathResultLabel.setText("Dijkstra: 两点不可达");
            pathArea.setText("");
            graphPanel.setHighlightedPath(new int[0]);
        } else {
            // 计算路径的总权重
            double totalWeight = computePathWeight(graph, shortestPath);

            pathResultLabel.setText("Dijkstra距离: " + String.format("%.1f", totalWeight));
            showPath(shortestPath);
            graphPanel.setHighlightedPath(shortestPath);
        }

        // 更新状态栏
        mainFrame.updateStatus("Dijkstra最短路径: " + fromNodeName + " → " + toNodeName);
    }

    /**
     * 将路径节点序列显示为可读文本
     * 格式：节点A → 节点B → 节点C
     *
     * @param path 路径节点ID数组
     */
    private void showPath(int[] path) {
        SocialGraph graph = graphPanel.graph;
        StringBuilder pathText = new StringBuilder();

        for (int stepIndex = 0; stepIndex < path.length; stepIndex++) {
            // 在第2个及之后的节点前添加箭头
            if (stepIndex > 0) {
                pathText.append(" → ");
            }
            // 添加节点名称
            pathText.append(graph.nodes[path[stepIndex]].name);
        }

        pathArea.setText(pathText.toString());
    }

    /**
     * 计算路径上所有边的权重之和
     * 遍历路径上的每对相邻节点，累加它们之间的边权重
     *
     * @param graph 图数据
     * @param path 路径节点ID数组
     * @return 路径总权重
     */
    private double computePathWeight(SocialGraph graph, int[] path) {
        double totalWeight = 0;

        // 遍历路径上的每一对相邻节点
        for (int stepIndex = 0; stepIndex < path.length - 1; stepIndex++) {
            int currentNodeId = path[stepIndex];
            int nextNodeId = path[stepIndex + 1];

            // 从邻接矩阵中获取边权重
            double edgeWeight = graph.adjMatrix[currentNodeId][nextNodeId];

            // 如果权重为0（不存在边），使用默认权重1.0
            if (edgeWeight > 0) {
                totalWeight += edgeWeight;
            } else {
                totalWeight += 1.0;
            }
        }

        return totalWeight;
    }

    // ==================== N跳圈子 ====================

    /**
     * 创建N跳交往圈子查询面板
     * 输入节点名称和跳数N，查询该节点N跳范围内的所有节点
     */
    private JPanel createNHopPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("N跳交往圈子"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 第0行：节点名称输入
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        panel.add(new JLabel("节点:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        nHopNodeField = new JTextField(6);
        panel.add(nHopNodeField, gbc);

        // 第1行：N跳数值输入
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panel.add(new JLabel("N跳:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        nHopField = new JTextField("2", 6);  // 默认值为2跳
        panel.add(nHopField, gbc);

        // 第2行：查询按钮
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JButton nHopButton = new JButton("查询N跳圈子");
        nHopButton.addActionListener(e -> {
            try {
                String nodeName = nHopNodeField.getText().trim();
                int hopCount = Integer.parseInt(nHopField.getText().trim());
                calculateNHop(nodeName, hopCount);
            } catch (NumberFormatException ex) {
                error("请输入有效跳数");
            }
        });
        panel.add(nHopButton, gbc);

        // 第3行：结果文本区域
        gbc.gridy = 3; gbc.weighty = 1; gbc.fill = GridBagConstraints.BOTH;
        nHopResultArea = new JTextArea(4, 10);
        nHopResultArea.setEditable(false);
        nHopResultArea.setLineWrap(true);
        nHopResultArea.setWrapStyleWord(true);
        nHopResultArea.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        nHopResultArea.setBackground(new Color(248, 248, 248));

        JScrollPane nHopScroll = new JScrollPane(nHopResultArea);
        nHopScroll.setPreferredSize(new Dimension(200, 80));
        panel.add(nHopScroll, gbc);

        return panel;
    }

    /**
     * 计算N跳交往圈子
     * 从指定节点出发，使用BFS计算到所有节点的距离，
     * 然后按距离分组显示N跳内的所有节点
     *
     * @param nodeName 中心节点名称
     * @param maxHopCount 最大跳数N
     */
    public void calculateNHop(String nodeName, int maxHopCount) {
        // 获取图数据
        SocialGraph graph = graphPanel.graph;

        // 检查图数据是否有效
        if (graph == null || graph.nodeCount == 0) return;

        // 查找中心节点ID
        int centerNodeId = graph.findNodeId(nodeName);
        if (centerNodeId == -1) {
            error("未找到节点: " + nodeName);
            return;
        }

        // 步骤1：使用BFS计算从中心节点到所有节点的距离
        int[] distanceFromCenter = algorithms.bfsShortestPath(graph, centerNodeId);

        // 步骤2：收集所有在N跳范围内的节点（距离>0且<=N）
        ArrayList<Integer> circleNodeIds = new ArrayList<>();
        for (int nodeIndex = 0; nodeIndex < graph.nodeCount; nodeIndex++) {
            // 距离大于0（排除自身）且不超过N跳
            if (distanceFromCenter[nodeIndex] > 0 && distanceFromCenter[nodeIndex] <= maxHopCount) {
                circleNodeIds.add(nodeIndex);
            }
        }

        // 步骤3：按距离分组，每个距离级别创建一个列表
        ArrayList<ArrayList<String>> hopGroups = new ArrayList<>();
        for (int distanceLevel = 1; distanceLevel <= maxHopCount; distanceLevel++) {
            hopGroups.add(new ArrayList<>());
        }

        // 步骤4：将节点按距离分配到对应的分组中
        for (int nodeIndex = 0; nodeIndex < graph.nodeCount; nodeIndex++) {
            int distance = distanceFromCenter[nodeIndex];
            if (distance > 0 && distance <= maxHopCount) {
                // 距离为d的节点放入第(d-1)组（因为列表索引从0开始）
                hopGroups.get(distance - 1).add(graph.nodes[nodeIndex].name);
            }
        }

        // 步骤5：构建结果文本
        StringBuilder resultText = new StringBuilder();
        resultText.append(nodeName).append(" 的").append(maxHopCount).append("跳圈子 (共")
                   .append(circleNodeIds.size()).append("节点)\n");

        for (int distanceLevel = 1; distanceLevel <= maxHopCount; distanceLevel++) {
            ArrayList<String> group = hopGroups.get(distanceLevel - 1);

            // 跳过空的分组
            if (group.isEmpty()) continue;

            resultText.append("距离").append(distanceLevel).append(": ");

            // 拼接该距离下的所有节点名称
            for (int nodeIndex = 0; nodeIndex < group.size(); nodeIndex++) {
                if (nodeIndex > 0) {
                    resultText.append(", ");
                }
                resultText.append(group.get(nodeIndex));
            }
            resultText.append("\n");
        }

        nHopResultArea.setText(resultText.toString());

        // 步骤6：将N跳圈子节点（含中心节点）设置为高亮路径
        int[] highlightedPathArray = new int[circleNodeIds.size() + 1];
        highlightedPathArray[0] = centerNodeId; // 中心节点放在第一个
        for (int index = 0; index < circleNodeIds.size(); index++) {
            highlightedPathArray[index + 1] = circleNodeIds.get(index);
        }
        graphPanel.setHighlightedPath(highlightedPathArray);

        // 更新状态栏
        mainFrame.updateStatus(nodeName + " 的" + maxHopCount + "跳圈子: " + circleNodeIds.size() + "个节点");
    }

    // ==================== 附近用户 ====================

    /**
     * 创建附近地理用户查询面板
     * 以选中节点为中心，按地理距离搜索附近用户
     */
    private JPanel createNearbyPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(createTitledBorder("附近地理用户"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 第0行：半径输入
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        panel.add(new JLabel("半径(km):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        nearbyRadiusField = new JTextField("5", 6);  // 默认5公里
        panel.add(nearbyRadiusField, gbc);

        // 第1行：检索按钮
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        JButton nearbyButton = new JButton("检索附近用户");
        nearbyButton.addActionListener(e -> searchNearbyUsers());
        panel.add(nearbyButton, gbc);

        // 第2行：结果文本区域
        gbc.gridy = 2; gbc.weighty = 1; gbc.fill = GridBagConstraints.BOTH;
        nearbyResultArea = new JTextArea(4, 10);
        nearbyResultArea.setEditable(false);
        nearbyResultArea.setLineWrap(true);
        nearbyResultArea.setWrapStyleWord(true);
        nearbyResultArea.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        nearbyResultArea.setBackground(new Color(248, 248, 248));

        JScrollPane scroll = new JScrollPane(nearbyResultArea);
        panel.add(scroll, gbc);

        return panel;
    }

    /**
     * 搜索附近用户
     * 以图面板中当前选中的节点为中心，搜索指定半径范围内的其他节点
     */
    public void searchNearbyUsers() {
        // 获取图数据
        SocialGraph graph = graphPanel.graph;

        // 检查图数据是否有效
        if (graph == null || graph.nodeCount == 0) return;

        // 获取当前选中的节点作为中心
        int centerNodeId = graphPanel.getSelectedNodeId();
        if (centerNodeId < 0) {
            JOptionPane.showMessageDialog(this, "请先在图中点击选择一个节点", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 解析用户输入的搜索半径
        double searchRadius;
        try {
            searchRadius = Double.parseDouble(nearbyRadiusField.getText().trim());
        } catch (NumberFormatException e) {
            error("请输入有效半径");
            return;
        }

        // 调用图对象的findNearby方法，搜索附近节点
        model.Node[] nearbyNodeList = graph.findNearby(centerNodeId, searchRadius);

        // 构建结果文本
        StringBuilder resultText = new StringBuilder();
        resultText.append(graph.nodes[centerNodeId].name).append(" 周围 ").append(searchRadius).append("km:\n");

        if (nearbyNodeList.length == 0) {
            resultText.append("  未找到附近用户");
        } else {
            // 遍历每个附近节点，显示名称和距离
            for (model.Node nearNode : nearbyNodeList) {
                double distance = graph.nodes[centerNodeId].geoDistanceTo(nearNode);
                resultText.append("  ").append(nearNode.name)
                          .append(" (距离").append(String.format("%.1f", distance)).append("km)\n");
            }
        }

        nearbyResultArea.setText(resultText.toString());

        // 高亮显示附近节点和中心节点
        java.util.Set<Integer> nearbyNodeIds = new java.util.HashSet<>();
        nearbyNodeIds.add(centerNodeId); // 包含中心节点
        for (model.Node nearNode : nearbyNodeList) {
            nearbyNodeIds.add(graph.findNodeId(nearNode.name));
        }
        graphPanel.setHighlightedNodes(nearbyNodeIds);

        // 更新状态栏
        mainFrame.updateStatus(graph.nodes[centerNodeId].name + " 周围" + searchRadius
                             + "km: " + nearbyNodeList.length + "个节点");
    }

    // ==================== 辅助方法 ====================

    /**
     * 显示错误对话框
     *
     * @param message 错误信息
     */
    private void error(String message) {
        JOptionPane.showMessageDialog(this, message, "错误", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * 创建带标题边框的面板标题
     *
     * @param title 标题文字
     * @return 带标题的边框对象
     */
    private TitledBorder createTitledBorder(String title) {
        return BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            title, TitledBorder.LEFT, TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 12), new Color(60, 60, 60));
    }
}