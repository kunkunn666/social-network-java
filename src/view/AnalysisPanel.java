package view;

import model.SocialGraph;
import model.Node;
import algorithm.CommunityDetection;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.Set;
import java.util.HashSet;

/**
 * 分析面板 — 度分析、连通分量检测、删除节点、推荐好友
 * 度数分类（核心/活跃/边缘人物）、连通分量检测、查找节点的交往圈子并高亮、
 * 删除选中节点、推荐可能认识的人
 */
public class AnalysisPanel extends JPanel {

    /** 主窗口引用（用于更新状态栏） */
    private MainFrame mainFrame;

    /** 图可视化面板引用（用于高亮和更新显示） */
    private GraphPanel graphPanel;

    /** 社区检测工具类 */
    private CommunityDetection communityDetection;

    /** 控制面板引用（用于获取当前文件路径，恢复原图） */
    private ControlPanel controlPanel;

    // ========== 各功能的结果显示组件 ==========

    /** 度分析结果文本区域 */
    private JTextArea degreeResultArea;

    /** 连通分量检测结果标签 */
    private JLabel communityResultLabel;

    /** 删除节点结果文本区域 */
    private JTextArea deleteResultArea;

    /** 好友推荐结果文本区域 */
    private JTextArea recommendResultArea;

    /** 着色方式下拉框（由ControlPanel创建并管理） */
    JComboBox<String> colorModeCombo;

    /**
     * 构造函数：创建分析面板，包含度分析、连通分量、删除节点、好友推荐四个子面板
     *
     * @param mainFrame 主窗口引用
     * @param graphPanel 图面板引用
     */
    public AnalysisPanel(MainFrame mainFrame, GraphPanel graphPanel) {
        this.mainFrame = mainFrame;
        this.graphPanel = graphPanel;
        this.communityDetection = new CommunityDetection();

        // 使用垂直布局
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // 依次添加四个子功能面板
        add(createDegreePanel());      // 1. 度分析面板
        add(Box.createVerticalStrut(8)); // 垂直间距8像素
        add(createCommunityPanel());   // 2. 连通分量检测面板
        add(Box.createVerticalStrut(8));
        add(createDeletePanel());      // 3. 删除节点面板
        add(Box.createVerticalStrut(8));
        add(createRecommendPanel());   // 4. 好友推荐面板
    }

    /**
     * 设置控制面板引用（用于恢复原图功能）
     *
     * @param cp 控制面板对象
     */
    public void setControlPanel(ControlPanel cp) {
        this.controlPanel = cp;
    }

    /**
     * 更新图数据引用，清空所有分析结果
     *
     * @param graph 新的图数据
     */
    public void setGraph(SocialGraph graph) {
        // 清空所有结果显示
        communityResultLabel.setText(" ");
        degreeResultArea.setText("");
        deleteResultArea.setText("");
        recommendResultArea.setText("");
    }

    // ==================== 度分析 ====================

    /**
     * 创建度分析面板
     * 包含计算按钮和结果文本区域
     */
    private JPanel createDegreePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(createTitledBorder("度分析模块"));

        // 计算按钮
        JButton calculateButton = new JButton("计算度数分类");
        calculateButton.addActionListener(e -> calculateDegreeAnalysis());

        // 结果文本区域（只读）
        degreeResultArea = new JTextArea(6, 20);
        degreeResultArea.setEditable(false);
        degreeResultArea.setFont(new Font("微软雅黑", Font.PLAIN, 11));

        JScrollPane scroll = new JScrollPane(degreeResultArea);

        panel.add(calculateButton, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    /**
     * 计算度数分析
     * 将节点按度数分为三类：核心人物、活跃人物、边缘人物
     * 分类后自动切换着色模式为"按节点类型"
     */
    public void calculateDegreeAnalysis() {
        // 获取图数据
        SocialGraph graph = graphPanel.graph;

        // 检查图数据是否有效
        if (graph == null || graph.nodeCount == 0) {
            JOptionPane.showMessageDialog(this, "请先加载数据", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 步骤1：对节点进行分类（核心/活跃/边缘）
        graph.classifyNodes();

        // 步骤2：切换着色模式为"按节点类型"
        graphPanel.setColorMode(0);
        colorModeCombo.setSelectedIndex(0);

        // 步骤3：统计各类型节点的数量和名称
        StringBuilder coreNodeList = new StringBuilder();   // 核心人物列表
        StringBuilder activeNodeList = new StringBuilder(); // 活跃人物列表
        StringBuilder edgeNodeList = new StringBuilder();   // 边缘人物列表
        int coreCount = 0, activeCount = 0, edgeCount = 0;

        for (int nodeIndex = 0; nodeIndex < graph.nodeCount; nodeIndex++) {
            Node currentNode = graph.nodes[nodeIndex];

            if ("核心".equals(currentNode.type)) {
                // 核心人物：度数最高的节点
                coreNodeList.append("  ").append(currentNode.name)
                           .append("(").append(currentNode.degree).append(")\n");
                coreCount++;
            } else if ("活跃".equals(currentNode.type)) {
                // 活跃人物：度数中等的节点
                activeNodeList.append("  ").append(currentNode.name)
                             .append("(").append(currentNode.degree).append(")\n");
                activeCount++;
            } else {
                // 边缘人物：度数较低的节点
                edgeNodeList.append("  ").append(currentNode.name)
                           .append("(").append(currentNode.degree).append(")\n");
                edgeCount++;
            }
        }

        // 步骤4：构建结果文本
        StringBuilder resultText = new StringBuilder();
        resultText.append("【核心人物】(").append(coreCount).append("个)\n").append(coreNodeList).append("\n");
        resultText.append("【活跃人物】(").append(activeCount).append("个)\n").append(activeNodeList).append("\n");
        resultText.append("【边缘人物】(").append(edgeCount).append("个)\n").append(edgeNodeList);

        degreeResultArea.setText(resultText.toString());
        degreeResultArea.setCaretPosition(0); // 滚动到顶部

        // 刷新图面板显示
        graphPanel.repaint();

        // 更新状态栏
        mainFrame.updateStatus("度等级分类完成: 核心" + coreCount + "个, 活跃" + activeCount + "个, 边缘" + edgeCount + "个");
    }

    // ==================== 连通分量 ====================

    /**
     * 创建连通分量检测面板
     * 包含检测按钮、结果标签和"查找交往圈子"按钮
     */
    private JPanel createCommunityPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(createTitledBorder("连通分量检测"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 第0行：检测连通分量按钮
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1;
        gbc.gridwidth = 2;
        JButton detectButton = new JButton("检测连通分量");
        detectButton.addActionListener(e -> detectConnectedComponents());
        panel.add(detectButton, gbc);

        // 第1行：结果标签（显示连通分量数量）
        gbc.gridy = 1;
        communityResultLabel = new JLabel(" ");
        communityResultLabel.setForeground(new Color(66, 133, 244)); // 蓝色文字
        panel.add(communityResultLabel, gbc);

        // 第2行：查找交往圈子按钮（查找选中节点所属的连通分量）
        gbc.gridy = 2;
        JButton findCircleButton = new JButton("查找该节点的交往圈子");
        findCircleButton.addActionListener(e -> findNodeCircle());
        panel.add(findCircleButton, gbc);

        return panel;
    }

    /**
     * 检测图的连通分量
     * 使用并查集算法找出图中的所有连通分量，并给每个节点标记所属社区
     */
    private void detectConnectedComponents() {
        SocialGraph graph = graphPanel.graph;

        // 检查图数据是否有效
        if (graph == null || graph.nodeCount == 0) return;

        // 检测连通分量
        int componentCount = communityDetection.detectConnectedComponents(graph);

        // 显示结果
        communityResultLabel.setText("共" + componentCount + "个连通分量");
        graphPanel.setCommunityCount(componentCount);

        // 如果有多个连通分量，自动切换到按社区着色模式
        if (componentCount > 1) {
            colorModeCombo.setSelectedIndex(1);
            graphPanel.setColorMode(1);
        }

        mainFrame.updateStatus("连通分量检测完成: " + componentCount + "个连通分量");
    }

    /**
     * 查找选中节点所属的交往圈子（连通分量）
     * 将所有与选中节点连通的节点高亮显示
     */
    private void findNodeCircle() {
        SocialGraph graph = graphPanel.graph;

        // 获取选中的节点ID
        int selectedNodeId = graphPanel.getSelectedNodeId();
        if (selectedNodeId < 0) {
            JOptionPane.showMessageDialog(this, "请先在图中点击选择一个节点", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 如果该节点还没有社区标记，先检测连通分量
        if (graph.nodes[selectedNodeId].community < 0) {
            communityDetection.detectConnectedComponents(graph);
        }

        // 获取选中节点所属的社区编号
        int targetCommunityId = graph.nodes[selectedNodeId].community;

        // 收集所有属于同一社区的节点
        Set<Integer> circleNodeIds = new HashSet<>();
        for (int nodeIndex = 0; nodeIndex < graph.nodeCount; nodeIndex++) {
            if (graph.nodes[nodeIndex].community == targetCommunityId) {
                circleNodeIds.add(nodeIndex);
            }
        }

        // 高亮这些节点
        graphPanel.setHighlightedNodes(circleNodeIds);

        mainFrame.updateStatus("节点 " + graph.nodes[selectedNodeId].name
                             + " 的交往圈子: " + circleNodeIds.size() + "个节点");
    }

    // ==================== 删除节点 ====================

    /**
     * 创建删除节点面板
     * 包含删除按钮、恢复按钮和结果文本区域
     */
    private JPanel createDeletePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(createTitledBorder("删除节点"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 第0行：删除和恢复按钮（并排）
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1;
        gbc.gridwidth = 2;

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 3, 0));

        // 删除选中节点按钮
        JButton deleteButton = new JButton("删除选中节点");
        deleteButton.addActionListener(e -> deleteSelectedNode());

        // 恢复原图按钮（从原始文件重新加载）
        JButton restoreButton = new JButton("恢复原图");
        restoreButton.addActionListener(e -> restoreGraph());

        buttonPanel.add(deleteButton);
        buttonPanel.add(restoreButton);
        panel.add(buttonPanel, gbc);

        // 第1行：结果文本区域
        gbc.gridy = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1;
        deleteResultArea = new JTextArea(3, 10);
        deleteResultArea.setEditable(false);
        deleteResultArea.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        deleteResultArea.setBackground(new Color(248, 248, 248));

        JScrollPane scroll = new JScrollPane(deleteResultArea);
        panel.add(scroll, gbc);

        return panel;
    }

    /**
     * 删除选中的节点
     * 从图中移除该节点及其所有关联的边，并更新连通分量信息
     */
    private void deleteSelectedNode() {
        SocialGraph graph = graphPanel.graph;

        // 检查图数据是否有效
        if (graph == null || graph.nodeCount == 0) return;

        // 获取选中的节点ID
        int selectedNodeId = graphPanel.getSelectedNodeId();
        if (selectedNodeId < 0) {
            JOptionPane.showMessageDialog(this, "请先在图中点击选择一个节点", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 记录删除前的连通分量数量和节点名称
        int componentCountBefore = communityDetection.detectConnectedComponents(graph);
        String deletedNodeName = graph.nodes[selectedNodeId].name;

        // 执行删除操作
        graph.removeNode(selectedNodeId);

        // 重新计算删除后的连通分量数量
        int componentCountAfter = communityDetection.detectConnectedComponents(graph);

        // 显示删除结果
        deleteResultArea.setText("已删除节点 " + deletedNodeName + "\n" +
            "连通分量: " + componentCountBefore + " → " + componentCountAfter + "\n" +
            "剩余节点: " + graph.nodeCount + "  剩余边: " + graph.edgeCount);

        // 清除图面板的选中状态并刷新
        graphPanel.clearSelection();
        graphPanel.setGraph(graph);

        mainFrame.updateStatus("已删除节点 " + deletedNodeName
                             + "，连通分量由 " + componentCountBefore + " 变为 " + componentCountAfter);
    }

    /**
     * 恢复原图
     * 从原始数据文件重新加载图数据，撤销所有删除操作
     */
    private void restoreGraph() {
        // 检查是否有原始文件路径
        if (controlPanel == null || controlPanel.currentFilePath == null) {
            JOptionPane.showMessageDialog(this, "未找到原始数据集路径", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SocialGraph graph = graphPanel.graph;
        if (graph == null) return;

        // 从原始文件重新加载数据
        graph.loadFromCSV(controlPanel.currentFilePath);

        // 清除选中状态并刷新显示
        graphPanel.clearSelection();
        graphPanel.setGraph(graph);

        // 清空删除结果区域
        deleteResultArea.setText("");

        mainFrame.updateStatus("已恢复原图");
    }

    // ==================== 推荐可能认识的人 ====================

    /**
     * 创建好友推荐面板
     * 包含推荐按钮和结果文本区域
     */
    private JPanel createRecommendPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(createTitledBorder("推荐可能认识的人"));

        // 推荐按钮
        JButton recommendButton = new JButton("推荐可能认识的人");
        recommendButton.addActionListener(e -> recommendFriends());

        // 结果文本区域（只读）
        recommendResultArea = new JTextArea(5, 20);
        recommendResultArea.setEditable(false);
        recommendResultArea.setFont(new Font("微软雅黑", Font.PLAIN, 11));

        JScrollPane scroll = new JScrollPane(recommendResultArea);

        panel.add(recommendButton, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    /**
     * 推荐可能认识的人
     * 基于"共同邻居"原则：如果A不认识C，但A和C有很多共同好友，则推荐C给A
     *
     * 算法步骤：
     * 1. 获取选中节点A的邻居集合
     * 2. 遍历A的每个邻居B，再遍历B的每个邻居C
     * 3. 如果C不是A本人，也不是A的直接邻居，则C是候选推荐对象
     * 4. 统计每个候选C与A的共同邻居数（即C被多少个A的邻居直接连接）
     * 5. 按共同邻居数降序排序，取前10个作为推荐结果
     */
    private void recommendFriends() {
        SocialGraph graph = graphPanel.graph;

        // 检查图数据是否有效
        if (graph == null || graph.nodeCount == 0) return;

        // 获取选中的节点（用户A）
        int selectedNodeId = graphPanel.getSelectedNodeId();
        if (selectedNodeId < 0) {
            JOptionPane.showMessageDialog(this, "请先在图中点击选择一个节点", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Node userA = graph.nodes[selectedNodeId];

        // ===== 步骤1：获取A的邻居集合 =====
        // neighborFlags[i] = true 表示节点i是A的邻居或A本身
        boolean[] neighborFlags = new boolean[graph.nodeCount];
        int[] aNeighborIds = graph.getNeighbors(selectedNodeId);
        for (int neighborId : aNeighborIds) {
            neighborFlags[neighborId] = true; // 标记直接邻居
        }

        // ===== 步骤2：遍历A的邻居的邻居，统计共同邻居数 =====
        // commonNeighborCount[i] = 节点i与A有多少个共同邻居
        int[] commonNeighborCount = new int[graph.nodeCount];

        for (int neighborId : aNeighborIds) {
            // 获取邻居B的所有邻居
            int[] bNeighborIds = graph.getNeighbors(neighborId);

            for (int candidateId : bNeighborIds) {
                // 条件：候选C不是A本人，也不是A的直接邻居
                boolean isNotA = (candidateId != selectedNodeId);
                boolean isNotDirectNeighbor = !neighborFlags[candidateId];

                if (isNotA && isNotDirectNeighbor) {
                    // C与A有一个共同邻居B，计数+1
                    commonNeighborCount[candidateId]++;
                }
            }
        }

        // ===== 步骤3：收集所有有共同邻居的候选节点 =====
        int[] candidateIds = new int[graph.nodeCount];
        int candidateCount = 0;

        for (int nodeIndex = 0; nodeIndex < graph.nodeCount; nodeIndex++) {
            if (commonNeighborCount[nodeIndex] > 0) {
                // 该节点与A有共同邻居，加入候选列表
                candidateIds[candidateCount] = nodeIndex;
                candidateCount++;
            }
        }

        // ===== 步骤4：按共同邻居数降序排序（冒泡排序） =====
        for (int outerIndex = 0; outerIndex < candidateCount - 1; outerIndex++) {
            for (int innerIndex = 0; innerIndex < candidateCount - 1 - outerIndex; innerIndex++) {
                int currentId = candidateIds[innerIndex];
                int nextId = candidateIds[innerIndex + 1];

                // 如果当前节点的共同邻居数小于下一个节点，交换位置
                if (commonNeighborCount[currentId] < commonNeighborCount[nextId]) {
                    // 交换两个候选节点
                    int tempId = candidateIds[innerIndex];
                    candidateIds[innerIndex] = candidateIds[innerIndex + 1];
                    candidateIds[innerIndex + 1] = tempId;
                }
            }
        }

        // ===== 步骤5：构建推荐结果文本（最多显示10个） =====
        int showLimit = Math.min(candidateCount, 10);
        StringBuilder resultText = new StringBuilder();
        resultText.append("为 ").append(userA.name).append(" 推荐可能认识的人:\n\n");

        if (candidateCount == 0) {
            resultText.append("  暂无推荐");
        } else {
            for (int rankIndex = 0; rankIndex < showLimit; rankIndex++) {
                int candidateId = candidateIds[rankIndex];
                resultText.append("  ").append(rankIndex + 1).append(". ")
                          .append(graph.nodes[candidateId].name)
                          .append(" (共同好友: ").append(commonNeighborCount[candidateId]).append(")\n");
            }

            // 如果候选超过10个，显示总数提示
            if (candidateCount > 10) {
                resultText.append("  ... 共").append(candidateCount).append("人");
            }
        }

        recommendResultArea.setText(resultText.toString());

        mainFrame.updateStatus("为 " + userA.name + " 推荐了 " + candidateCount + " 个可能认识的人");
    }

    // ==================== 辅助方法 ====================

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