package view;

import model.SocialGraph;
import algorithm.GraphAlgorithms;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

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

        createMenuBar();
        createMainContent();
        createStatusBar();

        setVisible(true);
    }

    // ==================== 菜单栏 ====================
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // 【文件】菜单
        JMenu fileMenu = new JMenu("文件");
        fileMenu.add(createMenuItem("加载CSV节点/边数据集", KeyEvent.VK_O, e -> openFile()));
        fileMenu.add(createMenuItem("切换数据集", 0, e -> switchDataset()));
        fileMenu.addSeparator();
        fileMenu.add(createMenuItem("保存当前网络布局", KeyEvent.VK_S, e -> saveLayout()));
        fileMenu.add(createMenuItem("导出分析结果", 0, e -> exportResults()));
        fileMenu.addSeparator();
        fileMenu.add(createMenuItem("重置画布", 0, e -> resetCanvas()));
        fileMenu.add(createMenuItem("退出程序", KeyEvent.VK_Q, e -> System.exit(0)));

        // 【视图】菜单
        JMenu viewMenu = new JMenu("视图");
        viewMenu.add(createMenuItem("放大", KeyEvent.VK_EQUALS, e -> graphPanel.zoomIn()));
        viewMenu.add(createMenuItem("缩小", KeyEvent.VK_MINUS, e -> graphPanel.zoomOut()));
        viewMenu.add(createMenuItem("重置视图", KeyEvent.VK_0, e -> graphPanel.resetView()));
        viewMenu.addSeparator();
        JCheckBoxMenuItem dragLockItem = new JCheckBoxMenuItem("节点拖拽锁定");
        dragLockItem.addActionListener(e -> graphPanel.setDragLocked(dragLockItem.isSelected()));
        viewMenu.add(dragLockItem);
        JCheckBoxMenuItem showLabelItem = new JCheckBoxMenuItem("显示节点名称标签", true);
        showLabelItem.addActionListener(e -> { graphPanel.setShowLabels(showLabelItem.isSelected()); controlPanel.showLabelsCheck.setSelected(showLabelItem.isSelected()); });
        viewMenu.add(showLabelItem);
        JCheckBoxMenuItem communityColorItem = new JCheckBoxMenuItem("按社区着色");
        communityColorItem.addActionListener(e -> {
            if (communityColorItem.isSelected()) { graphPanel.setColorMode(1); controlPanel.colorModeCombo.setSelectedIndex(1); }
            else { graphPanel.setColorMode(0); controlPanel.colorModeCombo.setSelectedIndex(0); }
        });
        viewMenu.add(communityColorItem);
        viewMenu.addSeparator();
        viewMenu.add(createMenuItem("重置节点防重叠布局", 0, e -> graphPanel.relayout()));
        viewMenu.add(createMenuItem("清空路径高亮", 0, e -> graphPanel.clearSelection()));

        // 【分析】菜单
        JMenu analysisMenu = new JMenu("分析");
        analysisMenu.add(createMenuItem("度等级分类计算", 0, e -> controlPanel.calculateDegreeAnalysis()));
        analysisMenu.add(createMenuItem("BFS无权最短路径", 0, e -> showPathDialog()));
        analysisMenu.add(createMenuItem("Dijkstra有权最短路径", 0, e -> showDijkstraDialog()));
        analysisMenu.addSeparator();
        analysisMenu.add(createMenuItem("附近地理用户检索", 0, e -> controlPanel.searchNearbyUsers()));
        analysisMenu.add(createMenuItem("N跳交往圈子查询", 0, e -> showNHopDialog()));
        analysisMenu.addSeparator();
        analysisMenu.add(createMenuItem("Girvan-Newman社区发现", 0, e -> controlPanel.detectCommunities()));
        analysisMenu.add(createMenuItem("统计信息", 0, e -> showStatsDialog()));

        // 【帮助】菜单
        JMenu helpMenu = new JMenu("帮助");
        helpMenu.add(createMenuItem("系统操作说明", 0, e -> showHelpDialog("操作说明",
            "1. 通过【文件】菜单或左侧面板下拉框加载数据集\n" +
            "2. 鼠标拖拽节点可移动位置，拖拽空白处可平移画布\n" +
            "3. 鼠标滚轮可缩放画布\n" +
            "4. 悬停节点可查看基本信息\n" +
            "5. 点击节点可查看详细信息\n" +
            "6. 使用【分析】菜单或左侧面板执行各类图算法")));
        helpMenu.add(createMenuItem("各算法原理简介", 0, e -> showHelpDialog("算法原理简介",
            "【度等级分类】按节点度数百分位排序，前20%为核心、20%-50%为活跃、后50%为边缘\n" +
            "【BFS最短路径】广度优先搜索，适用于无权图的两点最短路径\n" +
            "【Dijkstra算法】适用于有权图的最短路径，支持边权重\n" +
            "【社区发现】基于模块度优化的Louvain算法与标签传播算法\n" +
            "【力导向布局】模拟物理斥力/引力迭代计算节点位置，保障无重叠\n" +
            "【中心性指标】度中心性、接近中心性、介数中心性")));
        helpMenu.add(createMenuItem("数据集介绍", 0, e -> showHelpDialog("数据集介绍",
            "【空手道俱乐部】Zachary经典社交网络数据集，34个节点78条边\n" +
            "  描述一个大学空手道俱乐部的成员关系网络\n" +
            "【StackOverflow标签】技术标签共现网络\n" +
            "  节点为StackOverflow技术标签，边表示标签在同一问题中共同出现")));
        helpMenu.add(createMenuItem("程序版本信息", 0, e -> showAboutDialog()));

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(analysisMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }

    private JMenuItem createMenuItem(String text, int key, ActionListener listener) {
        JMenuItem item = new JMenuItem(text);
        if (key > 0) item.setAccelerator(KeyStroke.getKeyStroke(key, InputEvent.CTRL_MASK));
        item.addActionListener(listener);
        return item;
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

    // ==================== 文件菜单操作 ====================
    private void openFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File("D:/prg/VsCode/Java/social-network-java/data"));
        chooser.setFileFilter(new FileNameExtensionFilter("CSV文件 (*.csv)", "csv"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            loadDatasetFromPath(chooser.getSelectedFile().getAbsolutePath(), chooser.getSelectedFile().getName());
        }
    }

    private void switchDataset() {
        String[] options = {"空手道俱乐部", "StackOverflow标签"};
        String choice = (String) JOptionPane.showInputDialog(this, "选择数据集:", "切换数据集",
            JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (choice != null) {
            String path = "D:/prg/VsCode/Java/social-network-java/data/";
            if ("空手道俱乐部".equals(choice)) path += "karate_club.csv";
            else path += "stackoverflow_edges.csv";
            loadDatasetFromPath(path, choice);
        }
    }

    void loadDatasetFromPath(String path, String name) {
        try {
            SocialGraph g = new SocialGraph(name, path);
            g.loadFromCSV(path);
            setGraph(g);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "加载失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveLayout() {
        if (graph == null || graph.nodeCount == 0) {
            JOptionPane.showMessageDialog(this, "请先加载数据", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("CSV文件 (*.csv)", "csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter pw = new PrintWriter(chooser.getSelectedFile(), "UTF-8")) {
                pw.println("id,name,x,y,degree,type,community");
                for (int i = 0; i < graph.nodeCount; i++) {
                    pw.printf("%d,%s,%.0f,%.0f,%d,%s,%d%n",
                        graph.nodes[i].id, graph.nodes[i].name,
                        graph.nodes[i].x, graph.nodes[i].y,
                        graph.nodes[i].degree, graph.nodes[i].type,
                        graph.nodes[i].community);
                }
                updateStatus("布局已保存");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "保存失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportResults() {
        if (graph == null || graph.nodeCount == 0) {
            JOptionPane.showMessageDialog(this, "请先加载数据", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("文本文件 (*.txt)", "txt"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter pw = new PrintWriter(chooser.getSelectedFile(), "UTF-8")) {
                pw.println("=== 社交网络分析结果 ===");
                pw.println("数据集: " + graph.datasetName);
                pw.println("节点数: " + graph.nodeCount);
                pw.println("边数: " + graph.edgeCount);
                pw.printf("平均度: %.2f%n", algorithms.averageDegree(graph));
                pw.printf("图密度: %.4f%n", algorithms.graphDensity(graph));
                pw.println("直径: " + algorithms.diameter(graph));
                pw.printf("平均路径长度: %.2f%n", algorithms.averagePathLength(graph));
                pw.println("\n=== 度数Top5节点 ===");
                model.Node[] top = algorithms.getTopNodesByDegree(graph, 5);
                for (int i = 0; i < top.length; i++)
                    pw.printf("  %d. %s (度: %d, 类型: %s)%n", i + 1, top[i].name, top[i].degree, top[i].type);
                updateStatus("分析结果已导出");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "导出失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void resetCanvas() {
        if (graph != null) {
            graphPanel.clearSelection();
            graphPanel.relayout();
        }
        updateStatus("画布已重置");
    }

    // ==================== 分析菜单操作 ====================
    private void showPathDialog() {
        if (graph == null || graph.nodeCount == 0) {
            JOptionPane.showMessageDialog(this, "请先加载数据", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String input = JOptionPane.showInputDialog(this, "请输入起点和终点（用空格分隔）:", "BFS最短路径查询", JOptionPane.QUESTION_MESSAGE);
        if (input != null && !input.trim().isEmpty()) {
            String[] parts = input.trim().split("\\s+");
            if (parts.length >= 2) {
                controlPanel.fromNodeField.setText(parts[0]);
                controlPanel.toNodeField.setText(parts[1]);
                controlPanel.calculateShortestPath();
            }
        }
    }

    private void showDijkstraDialog() {
        if (graph == null || graph.nodeCount == 0) {
            JOptionPane.showMessageDialog(this, "请先加载数据", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String input = JOptionPane.showInputDialog(this, "请输入起点和终点（用空格分隔）:", "Dijkstra有权最短路径查询", JOptionPane.QUESTION_MESSAGE);
        if (input != null && !input.trim().isEmpty()) {
            String[] parts = input.trim().split("\\s+");
            if (parts.length >= 2) {
                controlPanel.fromNodeField.setText(parts[0]);
                controlPanel.toNodeField.setText(parts[1]);
                controlPanel.calculateDijkstraPath();
            }
        }
    }

    private void showNHopDialog() {
        if (graph == null || graph.nodeCount == 0) {
            JOptionPane.showMessageDialog(this, "请先加载数据", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String input = JOptionPane.showInputDialog(this, "请输入节点名和跳数N（用空格分隔）:", "N跳交往圈子查询", JOptionPane.QUESTION_MESSAGE);
        if (input != null && !input.trim().isEmpty()) {
            String[] parts = input.trim().split("\\s+");
            if (parts.length >= 2) {
                try {
                    int n = Integer.parseInt(parts[1]);
                    controlPanel.calculateNHop(parts[0], n);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "请输入有效的跳数", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void showStatsDialog() {
        if (graph == null || graph.nodeCount == 0) {
            JOptionPane.showMessageDialog(this, "请先加载数据", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("=== 图统计信息 ===\n\n");
        sb.append("数据集: ").append(graph.datasetName).append("\n");
        sb.append("节点数: ").append(graph.nodeCount).append("\n");
        sb.append("边数: ").append(graph.edgeCount).append("\n");
        sb.append(String.format("平均度: %.2f\n", algorithms.averageDegree(graph)));
        sb.append(String.format("图密度: %.4f\n", algorithms.graphDensity(graph)));
        sb.append("直径: ").append(algorithms.diameter(graph)).append("\n");
        sb.append(String.format("平均路径长度: %.2f\n", algorithms.averagePathLength(graph)));

        model.Node[] topNodes = algorithms.getTopNodesByDegree(graph, 5);
        sb.append("\n=== 度数Top5节点 ===\n");
        for (int i = 0; i < topNodes.length; i++)
            sb.append(String.format("  %d. %s (度: %d, 类型: %s)\n", i + 1, topNodes[i].name, topNodes[i].degree, topNodes[i].type));

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setPreferredSize(new Dimension(420, 380));
        JOptionPane.showMessageDialog(this, scroll, "统计信息", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showHelpDialog(String title, String content) {
        JTextArea textArea = new JTextArea(content);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setPreferredSize(new Dimension(500, 300));
        JOptionPane.showMessageDialog(this, scroll, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAboutDialog() {
        String msg = "社交网络可视化分析系统 v1.0\n\n" +
            "功能特性:\n" +
            "- 社交网络图可视化\n" +
            "- 节点类型分类（核心/活跃/边缘）\n" +
            "- BFS/Dijkstra最短路径分析\n" +
            "- 社区检测（模块度优化/标签传播）\n" +
            "- 中心性指标计算\n" +
            "- 力导向防重叠布局\n" +
            "- CSV数据导入导出\n\n" +
            "技术栈: Java + Swing\n" +
            "架构: MVC分层设计";
        JOptionPane.showMessageDialog(this, msg, "关于", JOptionPane.INFORMATION_MESSAGE);
    }
}