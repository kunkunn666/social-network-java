package view;

import model.SocialGraph;
import model.Node;
import algorithm.GraphAlgorithms;
import algorithm.CommunityDetection;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class ControlPanel extends JScrollPane {

    SocialGraph graph;
    GraphPanel graphPanel;
    MainFrame mainFrame;
    GraphAlgorithms algorithms;
    CommunityDetection communityDetection;

    JComboBox<String> colorModeCombo;
    JCheckBox showLabelsCheck;
    JTextField fromNodeField;
    JTextField toNodeField;
    private JLabel pathResultLabel;
    private JTextArea pathArea;
    private JLabel communityResultLabel;
    private JLabel statsLabel;
    private JTextArea infoArea;
    private JTextArea degreeResultArea;
    private JTextField nHopField;
    private JTextField nHopNodeField;
    private JTextArea nHopResultArea;

    private static final String DATA_DIR = "D:/prg/VsCode/Java/social-network-java/data";

    public ControlPanel(SocialGraph graph, GraphPanel graphPanel, MainFrame mainFrame) {
        this.graph = graph;
        this.graphPanel = graphPanel;
        this.mainFrame = mainFrame;
        this.algorithms = new GraphAlgorithms();
        this.communityDetection = new CommunityDetection();

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        content.add(createDatasetPanel());
        content.add(Box.createVerticalStrut(8));
        content.add(createDegreePanel());
        content.add(Box.createVerticalStrut(8));
        content.add(createQueryPanel());
        content.add(Box.createVerticalStrut(8));
        content.add(createCommunityPanel());
        content.add(Box.createVerticalStrut(8));
        content.add(createDisplayPanel());
        content.add(Box.createVerticalStrut(8));
        content.add(createStatsPanel());
        content.add(Box.createVerticalStrut(8));
        content.add(createInfoPanel());
        content.add(Box.createVerticalGlue());

        setViewportView(content);
        setPreferredSize(new Dimension(300, 600));
        getVerticalScrollBar().setUnitIncrement(16);
    }

    public void setGraph(SocialGraph newGraph) {
        this.graph = newGraph;
        updateStats();
        pathResultLabel.setText(" ");
        communityResultLabel.setText(" ");
        infoArea.setText("");
        degreeResultArea.setText("");
        nHopResultArea.setText("");
    }

    // ==================== 数据集切换区 ====================
    private JPanel createDatasetPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(createTitledBorder("数据集"));

        JComboBox<String> datasetCombo = new JComboBox<>(new String[]{"空手道俱乐部", "StackOverflow标签"});
        JButton loadButton = new JButton("加载");
        loadButton.addActionListener(e -> loadDataset(datasetCombo.getSelectedIndex()));

        JPanel topPanel = new JPanel(new BorderLayout(5, 0));
        topPanel.add(datasetCombo, BorderLayout.CENTER);
        topPanel.add(loadButton, BorderLayout.EAST);

        panel.add(topPanel, BorderLayout.CENTER);
        return panel;
    }

    // ==================== 度分析模块 ====================
    private JPanel createDegreePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(createTitledBorder("度分析模块"));

        JButton calcBtn = new JButton("计算度数分类");
        calcBtn.addActionListener(e -> calculateDegreeAnalysis());

        degreeResultArea = new JTextArea(6, 20);
        degreeResultArea.setEditable(false);
        degreeResultArea.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        JScrollPane scroll = new JScrollPane(degreeResultArea);

        panel.add(calcBtn, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    public void calculateDegreeAnalysis() {
        if (graph == null || graph.nodeCount == 0) {
            JOptionPane.showMessageDialog(this, "请先加载数据", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        graph.classifyNodes();
        graphPanel.setColorMode(0);
        colorModeCombo.setSelectedIndex(0);

        StringBuilder sb = new StringBuilder();
        StringBuilder core = new StringBuilder();
        StringBuilder active = new StringBuilder();
        StringBuilder edge = new StringBuilder();
        int coreCount = 0, activeCount = 0, edgeCount = 0;

        for (int i = 0; i < graph.nodeCount; i++) {
            Node n = graph.nodes[i];
            if ("核心".equals(n.type)) { core.append("  ").append(n.name).append("(").append(n.degree).append(")\n"); coreCount++; }
            else if ("活跃".equals(n.type)) { active.append("  ").append(n.name).append("(").append(n.degree).append(")\n"); activeCount++; }
            else { edge.append("  ").append(n.name).append("(").append(n.degree).append(")\n"); edgeCount++; }
        }

        sb.append("【核心节点】(").append(coreCount).append("个)\n").append(core).append("\n");
        sb.append("【活跃节点】(").append(activeCount).append("个)\n").append(active).append("\n");
        sb.append("【边缘节点】(").append(edgeCount).append("个)\n").append(edge);
        degreeResultArea.setText(sb.toString());
        degreeResultArea.setCaretPosition(0);
        graphPanel.repaint();
        mainFrame.updateStatus("度等级分类完成: 核心" + coreCount + "个, 活跃" + activeCount + "个, 边缘" + edgeCount + "个");
    }

    // ==================== 查询功能模块 ====================
    private JPanel createQueryPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(createTitledBorder("查询功能模块"));

        // 最短路径
        JPanel pathPanel = new JPanel(new GridBagLayout());
        pathPanel.setBorder(BorderFactory.createTitledBorder("最短路径"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        pathPanel.add(new JLabel("起点:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        fromNodeField = new JTextField(6);
        pathPanel.add(fromNodeField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        pathPanel.add(new JLabel("终点:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        toNodeField = new JTextField(6);
        pathPanel.add(toNodeField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 3, 0));
        JButton bfsBtn = new JButton("BFS");
        bfsBtn.addActionListener(e -> calculateShortestPath());
        JButton dijkstraBtn = new JButton("Dijkstra");
        dijkstraBtn.addActionListener(e -> calculateDijkstraPath());
        btnPanel.add(bfsBtn);
        btnPanel.add(dijkstraBtn);
        pathPanel.add(btnPanel, gbc);

        gbc.gridy = 3;
        pathResultLabel = new JLabel(" ");
        pathResultLabel.setForeground(new Color(66, 133, 244));
        pathPanel.add(pathResultLabel, gbc);

        gbc.gridy = 4;
        pathArea = new JTextArea(3, 10);
        pathArea.setEditable(false);
        pathArea.setLineWrap(true);
        pathArea.setWrapStyleWord(true);
        pathArea.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        pathArea.setBackground(new Color(248, 248, 248));
        JScrollPane pathScroll = new JScrollPane(pathArea);
        pathScroll.setPreferredSize(new Dimension(200, 50));
        pathPanel.add(pathScroll, gbc);

        // N跳圈子
        JPanel nHopPanel = new JPanel(new GridBagLayout());
        nHopPanel.setBorder(BorderFactory.createTitledBorder("N跳交往圈子"));
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        nHopPanel.add(new JLabel("节点:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        nHopNodeField = new JTextField(6);
        nHopPanel.add(nHopNodeField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        nHopPanel.add(new JLabel("N跳:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        nHopField = new JTextField("2", 6);
        nHopPanel.add(nHopField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JButton nHopBtn = new JButton("查询N跳圈子");
        nHopBtn.addActionListener(e -> {
            try { calculateNHop(nHopNodeField.getText().trim(), Integer.parseInt(nHopField.getText().trim())); }
            catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "请输入有效跳数", "错误", JOptionPane.ERROR_MESSAGE); }
        });
        nHopPanel.add(nHopBtn, gbc);

        gbc.gridy = 3; gbc.weighty = 1; gbc.fill = GridBagConstraints.BOTH;
        nHopResultArea = new JTextArea(4, 10);
        nHopResultArea.setEditable(false);
        nHopResultArea.setLineWrap(true);
        nHopResultArea.setWrapStyleWord(true);
        nHopResultArea.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        nHopResultArea.setBackground(new Color(248, 248, 248));
        JScrollPane nHopScroll = new JScrollPane(nHopResultArea);
        nHopScroll.setPreferredSize(new Dimension(200, 80));
        nHopPanel.add(nHopScroll, gbc);

        // 附近地理用户
        JPanel nearbyPanel = new JPanel(new BorderLayout(3, 0));
        nearbyPanel.setBorder(BorderFactory.createTitledBorder("附近地理用户"));
        JButton nearbyBtn = new JButton("检索附近用户");
        nearbyBtn.addActionListener(e -> searchNearbyUsers());
        nearbyPanel.add(nearbyBtn, BorderLayout.CENTER);

        panel.add(pathPanel);
        panel.add(nHopPanel);
        panel.add(nearbyPanel);
        return panel;
    }

    public void calculateShortestPath() {
        if (graph == null || graph.nodeCount == 0) return;
        String fromName = fromNodeField.getText().trim();
        String toName = toNodeField.getText().trim();
        int fromId = graph.findNodeId(fromName);
        int toId = graph.findNodeId(toName);
        if (fromId == -1) { JOptionPane.showMessageDialog(this, "未找到起点: " + fromName, "错误", JOptionPane.ERROR_MESSAGE); return; }
        if (toId == -1) { JOptionPane.showMessageDialog(this, "未找到终点: " + toName, "错误", JOptionPane.ERROR_MESSAGE); return; }

        int[] path = algorithms.shortestPath(graph, fromId, toId);
        if (path.length == 0) {
            pathResultLabel.setText("BFS: 两点不可达");
            pathArea.setText("");
            graphPanel.setHighlightedPath(new int[0]);
        } else {
            pathResultLabel.setText("BFS距离: " + (path.length - 1) + " 跳");
            showPath(path);
            graphPanel.setHighlightedPath(path);
        }
        mainFrame.updateStatus("BFS最短路径: " + fromName + " → " + toName);
    }

    public void calculateDijkstraPath() {
        if (graph == null || graph.nodeCount == 0) return;
        String fromName = fromNodeField.getText().trim();
        String toName = toNodeField.getText().trim();
        int fromId = graph.findNodeId(fromName);
        int toId = graph.findNodeId(toName);
        if (fromId == -1) { JOptionPane.showMessageDialog(this, "未找到起点: " + fromName, "错误", JOptionPane.ERROR_MESSAGE); return; }
        if (toId == -1) { JOptionPane.showMessageDialog(this, "未找到终点: " + toName, "错误", JOptionPane.ERROR_MESSAGE); return; }

        int[] path = algorithms.dijkstraShortestPath(graph, fromId, toId);
        if (path.length == 0) {
            pathResultLabel.setText("Dijkstra: 两点不可达");
            pathArea.setText("");
            graphPanel.setHighlightedPath(new int[0]);
        } else {
            double dist = computePathWeight(path);
            pathResultLabel.setText("Dijkstra距离: " + String.format("%.1f", dist));
            showPath(path);
            graphPanel.setHighlightedPath(path);
        }
        mainFrame.updateStatus("Dijkstra最短路径: " + fromName + " → " + toName);
    }

    private void showPath(int[] path) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.length; i++) {
            if (i > 0) sb.append(" → ");
            sb.append(graph.nodes[path[i]].name);
        }
        pathArea.setText(sb.toString());
    }

    private double computePathWeight(int[] path) {
        double total = 0;
        for (int i = 0; i < path.length - 1; i++) {
            double w = graph.adjMatrix[path[i]][path[i + 1]];
            total += (w > 0) ? w : 1.0;
        }
        return total;
    }

    public void calculateNHop(String nodeName, int n) {
        if (graph == null || graph.nodeCount == 0) return;
        int nodeId = graph.findNodeId(nodeName);
        if (nodeId == -1) { JOptionPane.showMessageDialog(this, "未找到节点: " + nodeName, "错误", JOptionPane.ERROR_MESSAGE); return; }

        int[] dist = algorithms.bfsShortestPath(graph, nodeId);
        java.util.ArrayList<Integer> circle = new java.util.ArrayList<>();
        for (int i = 0; i < graph.nodeCount; i++) {
            if (dist[i] > 0 && dist[i] <= n) circle.add(i);
        }

        // 按跳数分组
        java.util.ArrayList<java.util.ArrayList<String>> hopGroups = new java.util.ArrayList<>();
        for (int d = 1; d <= n; d++) hopGroups.add(new java.util.ArrayList<>());
        for (int i = 0; i < graph.nodeCount; i++) {
            if (dist[i] > 0 && dist[i] <= n) {
                hopGroups.get(dist[i] - 1).add(graph.nodes[i].name);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append(nodeName).append(" 的").append(n).append("跳圈子 (共").append(circle.size()).append("节点)\n");
        for (int d = 1; d <= n; d++) {
            java.util.ArrayList<String> group = hopGroups.get(d - 1);
            if (group.isEmpty()) continue;
            sb.append("距离").append(d).append(": ");
            for (int j = 0; j < group.size(); j++) {
                if (j > 0) sb.append(", ");
                sb.append(group.get(j));
            }
            sb.append("\n");
        }
        nHopResultArea.setText(sb.toString());

        int[] pathArr = new int[circle.size() + 1];
        pathArr[0] = nodeId;
        for (int i = 0; i < circle.size(); i++) pathArr[i + 1] = circle.get(i);
        graphPanel.setHighlightedPath(pathArr);
        mainFrame.updateStatus(nodeName + " 的" + n + "跳圈子: " + circle.size() + "个节点");
    }

    public void searchNearbyUsers() {
        if (graph == null || graph.nodeCount == 0) {
            JOptionPane.showMessageDialog(this, "请先加载数据", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(this,
            "附近地理用户检索需要节点包含地理位置信息。\n当前数据集未包含地理坐标数据。\n\n建议: 使用包含经纬度信息的CSV数据集。",
            "附近地理用户检索", JOptionPane.INFORMATION_MESSAGE);
    }

    // ==================== 社区发现模块 ====================
    private JPanel createCommunityPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(createTitledBorder("连通分量检测"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1;
        gbc.gridwidth = 2;

        JButton detectBtn = new JButton("检测连通分量");
        detectBtn.addActionListener(e -> detectConnectedComponents());
        panel.add(detectBtn, gbc);

        gbc.gridy = 1;
        communityResultLabel = new JLabel(" ");
        communityResultLabel.setForeground(new Color(66, 133, 244));
        panel.add(communityResultLabel, gbc);

        return panel;
    }

    public void detectCommunities() { detectConnectedComponents(); }

    private void detectConnectedComponents() {
        if (graph == null || graph.nodeCount == 0) return;
        int count = communityDetection.detectConnectedComponents(graph);
        communityResultLabel.setText("共" + count + "个连通分量");
        graphPanel.setCommunityCount(count);
        if (count > 1) {
            colorModeCombo.setSelectedIndex(1);
            graphPanel.setColorMode(1);
        }
        mainFrame.updateStatus("连通分量检测完成: " + count + "个连通分量");
    }

    // ==================== 显示设置面板 ====================
    private JPanel createDisplayPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1, 3, 3));
        panel.setBorder(createTitledBorder("显示设置"));

        colorModeCombo = new JComboBox<>(new String[]{"按节点类型", "按社区", "统一颜色"});
        colorModeCombo.addActionListener(e -> graphPanel.setColorMode(colorModeCombo.getSelectedIndex()));

        showLabelsCheck = new JCheckBox("显示节点标签", true);
        showLabelsCheck.addActionListener(e -> graphPanel.setShowLabels(showLabelsCheck.isSelected()));

        JCheckBox showWeightsCheck = new JCheckBox("显示边权重", false);
        showWeightsCheck.addActionListener(e -> graphPanel.setShowWeights(showWeightsCheck.isSelected()));

        JButton layoutButton = new JButton("重新布局");
        layoutButton.addActionListener(e -> { graphPanel.relayout(); mainFrame.updateStatus("力导向布局已重置"); });

        JPanel zoomPanel = new JPanel(new GridLayout(1, 3, 3, 0));
        JButton zoomInBtn = new JButton("放大");
        JButton zoomOutBtn = new JButton("缩小");
        JButton resetBtn = new JButton("重置");
        zoomInBtn.addActionListener(e -> graphPanel.zoomIn());
        zoomOutBtn.addActionListener(e -> graphPanel.zoomOut());
        resetBtn.addActionListener(e -> graphPanel.resetView());
        zoomPanel.add(zoomInBtn);
        zoomPanel.add(zoomOutBtn);
        zoomPanel.add(resetBtn);

        panel.add(new JLabel("着色方式:"));
        panel.add(colorModeCombo);
        panel.add(showLabelsCheck);
        panel.add(showWeightsCheck);
        panel.add(layoutButton);
        panel.add(zoomPanel);
        return panel;
    }

    // ==================== 统计信息 ====================
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(createTitledBorder("统计信息"));
        statsLabel = new JLabel("<html>节点: 0<br>边: 0<br>平均度: 0<br>密度: 0<br>直径: 0</html>");
        statsLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        panel.add(statsLabel, BorderLayout.CENTER);
        return panel;
    }

    // ==================== 节点详情 ====================
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(createTitledBorder("节点详情"));

        infoArea = new JTextArea(8, 20);
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        JScrollPane scroll = new JScrollPane(infoArea);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // ==================== 辅助方法 ====================
    private TitledBorder createTitledBorder(String title) {
        return BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            title, TitledBorder.LEFT, TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 12), new Color(60, 60, 60));
    }

    private void loadDataset(int idx) {
        String filename, name, desc;
        boolean showNames, showId;
        if (idx == 0) {
            filename = DATA_DIR + "/karate_club.csv";
            name = "空手道俱乐部";
            desc = "Zachary空手道俱乐部社交网络";
            showNames = false; showId = true;
        } else {
            filename = DATA_DIR + "/stackoverflow_edges.csv";
            name = "StackOverflow标签";
            desc = "StackOverflow技术标签共现网络";
            showNames = true; showId = false;
        }
        SocialGraph g = new SocialGraph(name, desc);
        g.loadFromCSV(filename);
        graphPanel.setShowNodeNames(showNames);
        graphPanel.setShowNodeId(showId);
        mainFrame.setGraph(g);
    }

    private void updateStats() {
        if (graph == null) return;
        double avgDeg = algorithms.averageDegree(graph);
        double density = algorithms.graphDensity(graph);
        statsLabel.setText(String.format(
            "<html>节点数: %d<br>边数: %d<br>平均度: %.2f<br>图密度: %.4f<br>直径: %d</html>",
            graph.nodeCount, graph.edgeCount, avgDeg, density, algorithms.diameter(graph)));
    }

    public void updateSelectedNodeInfo() {
        int selectedId = graphPanel.getSelectedNodeId();
        if (selectedId < 0 || graph == null) return;
        Node n = graph.nodes[selectedId];
        double[] dc = algorithms.degreeCentrality(graph);
        double[] cc = algorithms.closenessCentrality(graph);

        StringBuilder sb = new StringBuilder();
        sb.append("名称: ").append(n.name).append("\n");
        sb.append("ID: ").append(n.id).append("\n");
        sb.append("度数: ").append(n.degree).append("\n");
        sb.append("类型: ").append(n.type).append("\n");
        sb.append(String.format("度中心性: %.4f\n", dc[selectedId]));
        sb.append(String.format("接近中心性: %.4f\n", cc[selectedId]));
        if (n.community >= 0) sb.append("社区: ").append(n.community + 1).append("\n");
        sb.append("\n邻居:\n");
        int[] neighbors = graph.getNeighbors(selectedId);
        int showCount = Math.min(neighbors.length, 10);
        for (int i = 0; i < showCount; i++)
            sb.append("  - ").append(graph.nodes[neighbors[i]].name).append("\n");
        if (neighbors.length > 10) sb.append("  ... 共").append(neighbors.length).append("个");
        infoArea.setText(sb.toString());
        mainFrame.updateStatus("选中节点: " + n.name + " (度数: " + n.degree + ", 类型: " + n.type + ")");
    }
}