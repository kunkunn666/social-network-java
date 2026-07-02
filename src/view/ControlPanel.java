package view;

import model.SocialGraph;
import model.Node;
import algorithm.GraphAlgorithms;
import algorithm.CommunityDetection;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class ControlPanel extends JPanel {

    private SocialGraph graph;
    private GraphPanel graphPanel;
    private GraphAlgorithms algorithms;
    private CommunityDetection communityDetection;

    private JLabel statsLabel;
    private JComboBox<String> datasetCombo;
    private JComboBox<String> colorModeCombo;
    private JCheckBox showLabelsCheck;
    private JCheckBox showWeightsCheck;
    private JTextField searchField;
    private JTextArea infoArea;
    JTextField fromNodeField;
    JTextField toNodeField;
    private JLabel pathResultLabel;
    private JComboBox<String> algorithmCombo;
    private JLabel communityResultLabel;

    public ControlPanel(SocialGraph graph, GraphPanel graphPanel) {
        this.graph = graph;
        this.graphPanel = graphPanel;
        this.algorithms = new GraphAlgorithms();
        this.communityDetection = new CommunityDetection();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setPreferredSize(new Dimension(280, 600));

        add(createDatasetPanel());
        add(Box.createVerticalStrut(8));
        add(createDisplayPanel());
        add(Box.createVerticalStrut(8));
        add(createSearchPanel());
        add(Box.createVerticalStrut(8));
        add(createPathPanel());
        add(Box.createVerticalStrut(8));
        add(createCommunityPanel());
        add(Box.createVerticalStrut(8));
        add(createStatsPanel());
        add(Box.createVerticalStrut(8));
        add(createInfoPanel());
        add(Box.createVerticalGlue());
    }

    private JPanel createDatasetPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(5, 5));
        panel.setBorder(createTitledBorder("数据集"));

        datasetCombo = new JComboBox<>(new String[]{"空手道俱乐部", "StackOverflow标签"});
        datasetCombo.addActionListener(e -> loadDataset());

        JButton loadButton = new JButton("加载");
        loadButton.addActionListener(e -> loadDataset());

        JButton fileButton = new JButton("选择文件...");
        fileButton.addActionListener(e -> chooseFile());

        JPanel topPanel = new JPanel(new BorderLayout(5, 0));
        topPanel.add(datasetCombo, BorderLayout.CENTER);
        topPanel.add(loadButton, BorderLayout.EAST);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(fileButton, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createDisplayPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1, 5, 5));
        panel.setBorder(createTitledBorder("显示设置"));

        colorModeCombo = new JComboBox<>(new String[]{"按节点类型", "按社区", "统一颜色"});
        colorModeCombo.addActionListener(e -> {
            graphPanel.setColorMode(colorModeCombo.getSelectedIndex());
        });

        showLabelsCheck = new JCheckBox("显示节点标签", true);
        showLabelsCheck.addActionListener(e -> {
            graphPanel.setShowLabels(showLabelsCheck.isSelected());
        });

        showWeightsCheck = new JCheckBox("显示边权重", false);
        showWeightsCheck.addActionListener(e -> {
            graphPanel.setShowWeights(showWeightsCheck.isSelected());
        });

        JButton layoutButton = new JButton("重新布局");
        layoutButton.addActionListener(e -> graphPanel.relayout());

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

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(5, 5));
        panel.setBorder(createTitledBorder("节点搜索"));

        searchField = new JTextField();
        searchField.addActionListener(e -> searchNode());

        JButton searchBtn = new JButton("搜索");
        searchBtn.addActionListener(e -> searchNode());

        panel.add(searchField, BorderLayout.CENTER);
        panel.add(searchBtn, BorderLayout.EAST);

        return panel;
    }

    private JPanel createPathPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(createTitledBorder("最短路径"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        panel.add(new JLabel("起点:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1;
        fromNodeField = new JTextField();
        panel.add(fromNodeField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panel.add(new JLabel("终点:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1;
        toNodeField = new JTextField();
        panel.add(toNodeField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.weightx = 1;
        JButton pathBtn = new JButton("计算最短路径");
        pathBtn.addActionListener(e -> calculateShortestPath());
        panel.add(pathBtn, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        pathResultLabel = new JLabel(" ");
        pathResultLabel.setForeground(new Color(66, 133, 244));
        panel.add(pathResultLabel, gbc);

        return panel;
    }

    private JPanel createCommunityPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(createTitledBorder("社区检测"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1;
        gbc.gridwidth = 2;

        algorithmCombo = new JComboBox<>(new String[]{"模块度优化算法", "标签传播算法"});
        panel.add(algorithmCombo, gbc);

        gbc.gridy = 1;
        JButton detectBtn = new JButton("检测社区");
        detectBtn.addActionListener(e -> detectCommunities());
        panel.add(detectBtn, gbc);

        gbc.gridy = 2;
        communityResultLabel = new JLabel(" ");
        communityResultLabel.setForeground(new Color(66, 133, 244));
        panel.add(communityResultLabel, gbc);

        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(createTitledBorder("统计信息"));

        statsLabel = new JLabel("<html>节点: 0<br>边: 0<br>平均度: 0<br>密度: 0</html>");
        statsLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        panel.add(statsLabel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(createTitledBorder("节点详情"));

        infoArea = new JTextArea(5, 20);
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        JScrollPane scroll = new JScrollPane(infoArea);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private TitledBorder createTitledBorder(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            title,
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 12),
            new Color(60, 60, 60)
        );
        return border;
    }

    private static final String DATA_DIR = "D:/prg/VsCode/Java/social-network-java/data";

    private void loadDataset() {
        int idx = datasetCombo.getSelectedIndex();
        String filename = "";
        String name = "";
        String desc = "";

        if (idx == 0) {
            filename = DATA_DIR + "/karate_club.csv";
            name = "空手道俱乐部";
            desc = "Zachary空手道俱乐部社交网络";
        } else if (idx == 1) {
            filename = DATA_DIR + "/stackoverflow_edges.csv";
            name = "StackOverflow标签";
            desc = "StackOverflow技术标签共现网络";
        }

        graph = new SocialGraph(name, desc);
        graph.loadFromCSV(filename);
        graphPanel.setGraph(graph);
        updateStats();
        pathResultLabel.setText(" ");
        communityResultLabel.setText(" ");
        infoArea.setText("");
    }

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File(DATA_DIR));
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            graph = new SocialGraph(file.getName(), "自定义数据集");
            graph.loadFromCSV(file.getAbsolutePath());
            graphPanel.setGraph(graph);
            updateStats();
            pathResultLabel.setText(" ");
            communityResultLabel.setText(" ");
            infoArea.setText("");
        }
    }

    private void searchNode() {
        String query = searchField.getText().trim();
        if (query.isEmpty() || graph == null) return;

        Node node = graph.getNodeByName(query);
        if (node == null) {
            for (int i = 0; i < graph.nodeCount; i++) {
                if (graph.nodes[i].name.toLowerCase().contains(query.toLowerCase())) {
                    node = graph.nodes[i];
                    break;
                }
            }
        }

        if (node != null) {
            showNodeInfo(node.id);
        } else {
            JOptionPane.showMessageDialog(this, "未找到节点: " + query, "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void calculateShortestPath() {
        if (graph == null || graph.nodeCount == 0) return;

        String fromName = fromNodeField.getText().trim();
        String toName = toNodeField.getText().trim();

        int fromId = graph.findNodeId(fromName);
        int toId = graph.findNodeId(toName);

        if (fromId == -1) {
            JOptionPane.showMessageDialog(this, "未找到起点: " + fromName, "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (toId == -1) {
            JOptionPane.showMessageDialog(this, "未找到终点: " + toName, "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int[] path = algorithms.shortestPath(graph, fromId, toId);
        if (path.length == 0) {
            pathResultLabel.setText("两点不可达");
            graphPanel.setHighlightedPath(new int[0]);
        } else {
            pathResultLabel.setText("距离: " + (path.length - 1) + " 跳");
            graphPanel.setHighlightedPath(path);
        }
    }

    public void detectCommunities() {
        if (graph == null || graph.nodeCount == 0) return;

        int count;
        if (algorithmCombo.getSelectedIndex() == 0) {
            count = communityDetection.detectCommunities(graph);
        } else {
            count = communityDetection.detectCommunitiesByLabelPropagation(graph);
        }

        double modularity = communityDetection.calculateModularity(graph);
        communityResultLabel.setText("共" + count + "个社区, Q=" + String.format("%.3f", modularity));
        graphPanel.setCommunityCount(count);
        colorModeCombo.setSelectedIndex(1);
        graphPanel.setColorMode(1);
    }

    private void showNodeInfo(int nodeId) {
        if (nodeId < 0 || nodeId >= graph.nodeCount) return;
        Node n = graph.nodes[nodeId];

        double[] dc = algorithms.degreeCentrality(graph);
        double[] cc = algorithms.closenessCentrality(graph);

        StringBuilder sb = new StringBuilder();
        sb.append("名称: ").append(n.name).append("\n");
        sb.append("ID: ").append(n.id).append("\n");
        sb.append("度数: ").append(n.degree).append("\n");
        sb.append("类型: ").append(n.type).append("\n");
        sb.append(String.format("度中心性: %.4f\n", dc[nodeId]));
        sb.append(String.format("接近中心性: %.4f\n", cc[nodeId]));
        if (n.community >= 0) {
            sb.append("社区: ").append(n.community + 1).append("\n");
        }
        sb.append("\n邻居:\n");
        int[] neighbors = graph.getNeighbors(nodeId);
        int showCount = Math.min(neighbors.length, 10);
        for (int i = 0; i < showCount; i++) {
            sb.append("  - ").append(graph.nodes[neighbors[i]].name).append("\n");
        }
        if (neighbors.length > 10) {
            sb.append("  ... 共").append(neighbors.length).append("个");
        }

        infoArea.setText(sb.toString());
    }

    private void updateStats() {
        if (graph == null) return;
        double avgDeg = algorithms.averageDegree(graph);
        double density = algorithms.graphDensity(graph);

        String html = String.format(
            "<html>节点数: %d<br>边数: %d<br>平均度: %.2f<br>图密度: %.4f<br>直径: %d</html>",
            graph.nodeCount, graph.edgeCount, avgDeg, density, algorithms.diameter(graph)
        );
        statsLabel.setText(html);
    }

    public void updateSelectedNodeInfo() {
        int selectedId = graphPanel.getSelectedNodeId();
        if (selectedId >= 0 && graph != null) {
            showNodeInfo(selectedId);
        }
    }
}
