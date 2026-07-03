package view;

import model.SocialGraph;
import algorithm.GraphAlgorithms;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainFrame extends JFrame {

    SocialGraph graph;
    GraphPanel graphPanel;
    ControlPanel controlPanel;
    private JLabel statusLabel;
    private JSplitPane splitPane;
    private GraphAlgorithms algorithms;

    public MainFrame() {
        graph = new SocialGraph("空图", "尚未加载数据");
        algorithms = new GraphAlgorithms();

        setTitle("社交网络可视化分析系统");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);

        createMainContent();
        createStatusBar();

        setVisible(true);
    }

    // ==================== 主体布局 ====================
    private void createMainContent() {
        graphPanel = new GraphPanel(graph);
        controlPanel = new ControlPanel(graph, graphPanel, this);

        graphPanel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                SwingUtilities.invokeLater(() -> controlPanel.updateSelectedNodeInfo());
            }
            public void mouseClicked(MouseEvent e) {
                SwingUtilities.invokeLater(() -> controlPanel.updateSelectedNodeInfo());
            }
        });

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, controlPanel, graphPanel);
        splitPane.setDividerLocation(300);
        splitPane.setOneTouchExpandable(true);
        add(splitPane, BorderLayout.CENTER);
    }

    // ==================== 底部状态栏 ====================
    private void createStatusBar() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)));

        statusLabel = new JLabel("就绪 — 请加载数据集");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        JLabel hintLabel = new JLabel("拖拽节点移动 | 滚轮缩放 | 拖拽空白平移 | 悬停查看信息");
        hintLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        hintLabel.setForeground(Color.GRAY);
        hintLabel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(hintLabel, BorderLayout.EAST);
        add(statusPanel, BorderLayout.SOUTH);
    }

    // ==================== 图数据加载 ====================
    public void setGraph(SocialGraph newGraph) {
        this.graph = newGraph;
        graphPanel.setGraph(newGraph);
        controlPanel.setGraph(newGraph);
        updateStatus("已加载: " + newGraph.datasetName + " (" + newGraph.nodeCount + "个节点, " + newGraph.edgeCount + "条边)");
    }

    public void updateStatus(String text) {
        statusLabel.setText(text);
    }
}