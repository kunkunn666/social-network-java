package view;

import model.SocialGraph;
import model.Node;
import algorithm.GraphAlgorithms;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * 左侧控制面板 — 组装各功能子面板
 * 包含：数据集切换、度分析、查询功能（最短路径/N跳）、连通分量检测、
 * 显示设置（着色/标签/缩放/布局）、统计信息、节点详情
 */
public class ControlPanel extends JScrollPane {

    private SocialGraph graph;
    private GraphPanel graphPanel;
    private MainFrame mainFrame;
    private GraphAlgorithms algorithms;

    private QueryPanel queryPanel;
    private AnalysisPanel analysisPanel;
    private JCheckBox showLabelsCheck;
    private JLabel statsLabel;
    private JTextArea infoArea;
    String currentFilePath;

    private static final String DATA_DIR = "D:/prg/VsCode/Java/social-network-java/data";

    public ControlPanel(SocialGraph graph, GraphPanel graphPanel, MainFrame mainFrame) {
        this.graph = graph;
        this.graphPanel = graphPanel;
        this.mainFrame = mainFrame;
        this.algorithms = new GraphAlgorithms();

        queryPanel = new QueryPanel(mainFrame, graphPanel);
        analysisPanel = new AnalysisPanel(mainFrame, graphPanel);
        analysisPanel.setControlPanel(this);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        content.add(createDatasetPanel());
        content.add(Box.createVerticalStrut(8));
        content.add(analysisPanel);
        content.add(Box.createVerticalStrut(8));
        content.add(queryPanel);
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
        queryPanel.setGraph(newGraph);
        analysisPanel.setGraph(newGraph);
        infoArea.setText("");
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

    // ==================== 显示设置面板 ====================

    private JPanel createDisplayPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1, 3, 3));
        panel.setBorder(createTitledBorder("显示设置"));

        analysisPanel.colorModeCombo = new JComboBox<>(new String[]{"按节点类型", "按社区", "统一颜色"});
        analysisPanel.colorModeCombo.addActionListener(e -> graphPanel.setColorMode(analysisPanel.colorModeCombo.getSelectedIndex()));

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
        panel.add(analysisPanel.colorModeCombo);
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

    // ==================== 辅助方法 ====================

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
        currentFilePath = filename;
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

    private TitledBorder createTitledBorder(String title) {
        return BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            title, TitledBorder.LEFT, TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 12), new Color(60, 60, 60));
    }
}