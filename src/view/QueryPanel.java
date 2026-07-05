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

    private MainFrame mainFrame;
    private GraphPanel graphPanel;
    private GraphAlgorithms algorithms;

    private JTextField fromNodeField;
    private JTextField toNodeField;
    private JLabel pathResultLabel;
    private JTextArea pathArea;
    private JTextField nHopField;
    private JTextField nHopNodeField;
    private JTextArea nHopResultArea;
    private JTextField nearbyRadiusField;
    private JTextArea nearbyResultArea;

    public QueryPanel(MainFrame mainFrame, GraphPanel graphPanel) {
        this.mainFrame = mainFrame;
        this.graphPanel = graphPanel;
        this.algorithms = new GraphAlgorithms();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(createTitledBorder("查询功能模块"));

        add(createPathPanel());
        add(createNHopPanel());
        add(createNearbyPanel());
    }

    /** 更新图引用 */
    public void setGraph(SocialGraph graph) {
        pathResultLabel.setText(" ");
        pathArea.setText("");
        nHopResultArea.setText("");
        nearbyResultArea.setText("");
    }

    // ==================== 最短路径 ====================

    private JPanel createPathPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("最短路径"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        panel.add(new JLabel("起点:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        fromNodeField = new JTextField(6);
        panel.add(fromNodeField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panel.add(new JLabel("终点:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        toNodeField = new JTextField(6);
        panel.add(toNodeField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 3, 0));
        JButton bfsBtn = new JButton("BFS");
        bfsBtn.addActionListener(e -> calculateShortestPath());
        JButton dijkstraBtn = new JButton("Dijkstra");
        dijkstraBtn.addActionListener(e -> calculateDijkstraPath());
        btnPanel.add(bfsBtn);
        btnPanel.add(dijkstraBtn);
        panel.add(btnPanel, gbc);

        gbc.gridy = 3;
        pathResultLabel = new JLabel(" ");
        pathResultLabel.setForeground(new Color(66, 133, 244));
        panel.add(pathResultLabel, gbc);

        gbc.gridy = 4; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1;
        pathArea = new JTextArea(3, 10);
        pathArea.setEditable(false);
        pathArea.setLineWrap(true);
        pathArea.setWrapStyleWord(true);
        pathArea.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        pathArea.setBackground(new Color(248, 248, 248));
        JScrollPane pathScroll = new JScrollPane(pathArea);
        pathScroll.setPreferredSize(new Dimension(200, 50));
        panel.add(pathScroll, gbc);

        return panel;
    }

    public void calculateShortestPath() {
        SocialGraph graph = graphPanel.graph;
        if (graph == null || graph.nodeCount == 0) return;
        String fromName = fromNodeField.getText().trim();
        String toName = toNodeField.getText().trim();
        int fromId = graph.findNodeId(fromName);
        int toId = graph.findNodeId(toName);
        if (fromId == -1) { error("未找到起点: " + fromName); return; }
        if (toId == -1) { error("未找到终点: " + toName); return; }

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
        SocialGraph graph = graphPanel.graph;
        if (graph == null || graph.nodeCount == 0) return;
        String fromName = fromNodeField.getText().trim();
        String toName = toNodeField.getText().trim();
        int fromId = graph.findNodeId(fromName);
        int toId = graph.findNodeId(toName);
        if (fromId == -1) { error("未找到起点: " + fromName); return; }
        if (toId == -1) { error("未找到终点: " + toName); return; }

        int[] path = algorithms.dijkstraShortestPath(graph, fromId, toId);
        if (path.length == 0) {
            pathResultLabel.setText("Dijkstra: 两点不可达");
            pathArea.setText("");
            graphPanel.setHighlightedPath(new int[0]);
        } else {
            double dist = computePathWeight(graph, path);
            pathResultLabel.setText("Dijkstra距离: " + String.format("%.1f", dist));
            showPath(path);
            graphPanel.setHighlightedPath(path);
        }
        mainFrame.updateStatus("Dijkstra最短路径: " + fromName + " → " + toName);
    }

    private void showPath(int[] path) {
        SocialGraph graph = graphPanel.graph;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.length; i++) {
            if (i > 0) sb.append(" → ");
            sb.append(graph.nodes[path[i]].name);
        }
        pathArea.setText(sb.toString());
    }

    private double computePathWeight(SocialGraph graph, int[] path) {
        double total = 0;
        for (int i = 0; i < path.length - 1; i++) {
            double w = graph.adjMatrix[path[i]][path[i + 1]];
            total += (w > 0) ? w : 1.0;
        }
        return total;
    }

    // ==================== N跳圈子 ====================

    private JPanel createNHopPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("N跳交往圈子"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        panel.add(new JLabel("节点:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        nHopNodeField = new JTextField(6);
        panel.add(nHopNodeField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panel.add(new JLabel("N跳:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        nHopField = new JTextField("2", 6);
        panel.add(nHopField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JButton nHopBtn = new JButton("查询N跳圈子");
        nHopBtn.addActionListener(e -> {
            try {
                calculateNHop(nHopNodeField.getText().trim(), Integer.parseInt(nHopField.getText().trim()));
            } catch (NumberFormatException ex) {
                error("请输入有效跳数");
            }
        });
        panel.add(nHopBtn, gbc);

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

    public void calculateNHop(String nodeName, int n) {
        SocialGraph graph = graphPanel.graph;
        if (graph == null || graph.nodeCount == 0) return;
        int nodeId = graph.findNodeId(nodeName);
        if (nodeId == -1) { error("未找到节点: " + nodeName); return; }

        int[] dist = algorithms.bfsShortestPath(graph, nodeId);
        ArrayList<Integer> circle = new ArrayList<>();
        for (int i = 0; i < graph.nodeCount; i++) {
            if (dist[i] > 0 && dist[i] <= n) circle.add(i);
        }

        ArrayList<ArrayList<String>> hopGroups = new ArrayList<>();
        for (int d = 1; d <= n; d++) hopGroups.add(new ArrayList<>());
        for (int i = 0; i < graph.nodeCount; i++) {
            if (dist[i] > 0 && dist[i] <= n) {
                hopGroups.get(dist[i] - 1).add(graph.nodes[i].name);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append(nodeName).append(" 的").append(n).append("跳圈子 (共").append(circle.size()).append("节点)\n");
        for (int d = 1; d <= n; d++) {
            ArrayList<String> group = hopGroups.get(d - 1);
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

    // ==================== 附近用户 ====================

    private JPanel createNearbyPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(createTitledBorder("附近地理用户"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        panel.add(new JLabel("半径(km):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        nearbyRadiusField = new JTextField("5", 6);
        panel.add(nearbyRadiusField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        JButton nearbyBtn = new JButton("检索附近用户");
        nearbyBtn.addActionListener(e -> searchNearbyUsers());
        panel.add(nearbyBtn, gbc);

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

    public void searchNearbyUsers() {
        SocialGraph graph = graphPanel.graph;
        if (graph == null || graph.nodeCount == 0) return;

        int centerId = graphPanel.getSelectedNodeId();
        if (centerId < 0) {
            JOptionPane.showMessageDialog(this, "请先在图中点击选择一个节点", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        double radius;
        try { radius = Double.parseDouble(nearbyRadiusField.getText().trim()); }
        catch (NumberFormatException e) { error("请输入有效半径"); return; }

        model.Node[] nearby = graph.findNearby(centerId, radius);

        StringBuilder sb = new StringBuilder();
        sb.append(graph.nodes[centerId].name).append(" 周围 ").append(radius).append("km:\n");
        if (nearby.length == 0) {
            sb.append("  未找到附近用户");
        } else {
            for (model.Node n : nearby) {
                double dist = graph.nodes[centerId].geoDistanceTo(n);
                sb.append("  ").append(n.name).append(" (距离").append(String.format("%.1f", dist)).append("km)\n");
            }
        }
        nearbyResultArea.setText(sb.toString());

        // 高亮附近节点
        java.util.Set<Integer> nearbyIds = new java.util.HashSet<>();
        nearbyIds.add(centerId);
        for (model.Node n : nearby) nearbyIds.add(graph.findNodeId(n.name));
        graphPanel.setHighlightedNodes(nearbyIds);

        mainFrame.updateStatus(graph.nodes[centerId].name + " 周围" + radius + "km: " + nearby.length + "个节点");
    }

    // ==================== 辅助 ====================

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "错误", JOptionPane.ERROR_MESSAGE);
    }

    private TitledBorder createTitledBorder(String title) {
        return BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            title, TitledBorder.LEFT, TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 12), new Color(60, 60, 60));
    }
}