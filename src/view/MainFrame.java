package view;

import model.SocialGraph;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainFrame extends JFrame {

    private SocialGraph graph;
    private GraphPanel graphPanel;
    private ControlPanel controlPanel;
    private JLabel statusLabel;

    public MainFrame() {
        graph = new SocialGraph("空图", "尚未加载数据");

        setTitle("社交网络可视化分析系统");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);

        createMenuBar();
        createMainContent();
        createStatusBar();

        setVisible(true);
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("文件");
        JMenuItem openItem = new JMenuItem("打开...");
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
        openItem.addActionListener(e -> openFile());

        JMenuItem exitItem = new JMenuItem("退出");
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(openItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu viewMenu = new JMenu("视图");
        JMenuItem zoomInItem = new JMenuItem("放大");
        zoomInItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.CTRL_MASK));
        zoomInItem.addActionListener(e -> graphPanel.zoomIn());

        JMenuItem zoomOutItem = new JMenuItem("缩小");
        zoomOutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_MASK));
        zoomOutItem.addActionListener(e -> graphPanel.zoomOut());

        JMenuItem resetItem = new JMenuItem("重置视图");
        resetItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_MASK));
        resetItem.addActionListener(e -> graphPanel.resetView());

        JMenuItem relayoutItem = new JMenuItem("重新布局");
        relayoutItem.addActionListener(e -> graphPanel.relayout());

        viewMenu.add(zoomInItem);
        viewMenu.add(zoomOutItem);
        viewMenu.add(resetItem);
        viewMenu.addSeparator();
        viewMenu.add(relayoutItem);

        JMenu analysisMenu = new JMenu("分析");
        JMenuItem pathItem = new JMenuItem("最短路径...");
        pathItem.addActionListener(e -> showPathDialog());

        JMenuItem communityItem = new JMenuItem("社区检测");
        communityItem.addActionListener(e -> runCommunityDetection());

        JMenuItem statsItem = new JMenuItem("统计信息");
        statsItem.addActionListener(e -> showStatsDialog());

        analysisMenu.add(pathItem);
        analysisMenu.add(communityItem);
        analysisMenu.addSeparator();
        analysisMenu.add(statsItem);

        JMenu helpMenu = new JMenu("帮助");
        JMenuItem aboutItem = new JMenuItem("关于");
        aboutItem.addActionListener(e -> showAboutDialog());

        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(analysisMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private void createMainContent() {
        graphPanel = new GraphPanel(graph);
        controlPanel = new ControlPanel(graph, graphPanel);

        graphPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                SwingUtilities.invokeLater(() -> controlPanel.updateSelectedNodeInfo());
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                SwingUtilities.invokeLater(() -> controlPanel.updateSelectedNodeInfo());
            }
        });

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, controlPanel, graphPanel);
        splitPane.setDividerLocation(300);
        splitPane.setOneTouchExpandable(true);

        add(splitPane, BorderLayout.CENTER);
    }

    private void createStatusBar() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)));

        statusLabel = new JLabel("就绪");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        JLabel hintLabel = new JLabel("提示: 拖动节点可移动，拖动空白处可平移");
        hintLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        hintLabel.setForeground(Color.GRAY);
        hintLabel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(hintLabel, BorderLayout.EAST);

        add(statusPanel, BorderLayout.SOUTH);
    }

    private void openFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("D:\\prg\\VsCode\\Java\\social-network-java\\data"));
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            loadDataset(path, chooser.getSelectedFile().getName());
        }
    }

    private void loadDataset(String path, String name) {
        try {
            graph = new SocialGraph(name, path);
            graph.loadFromCSV(path);
            graphPanel.setGraph(graph);
            controlPanel = new ControlPanel(graph, graphPanel);
            updateStatus("已加载: " + name + " (" + graph.nodeCount + "个节点, " + graph.edgeCount + "条边)");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "加载失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showPathDialog() {
        if (graph.nodeCount == 0) {
            JOptionPane.showMessageDialog(this, "请先加载数据", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String input = JOptionPane.showInputDialog(this, "请输入起点和终点（用空格分隔）:", "最短路径查询", JOptionPane.QUESTION_MESSAGE);
        if (input != null && !input.trim().isEmpty()) {
            String[] parts = input.trim().split("\\s+");
            if (parts.length >= 2) {
                controlPanel.fromNodeField.setText(parts[0]);
                controlPanel.toNodeField.setText(parts[1]);
                controlPanel.calculateShortestPath();
            }
        }
    }

    private void runCommunityDetection() {
        if (graph.nodeCount == 0) {
            JOptionPane.showMessageDialog(this, "请先加载数据", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        controlPanel.detectCommunities();
    }

    private void showStatsDialog() {
        if (graph.nodeCount == 0) {
            JOptionPane.showMessageDialog(this, "请先加载数据", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        algorithm.GraphAlgorithms alg = new algorithm.GraphAlgorithms();
        StringBuilder sb = new StringBuilder();
        sb.append("=== 图统计信息 ===\n\n");
        sb.append("数据集: ").append(graph.datasetName).append("\n");
        sb.append("节点数: ").append(graph.nodeCount).append("\n");
        sb.append("边数: ").append(graph.edgeCount).append("\n");
        sb.append(String.format("平均度: %.2f\n", alg.averageDegree(graph)));
        sb.append(String.format("图密度: %.4f\n", alg.graphDensity(graph)));
        sb.append("直径: ").append(alg.diameter(graph)).append("\n");
        sb.append(String.format("平均路径长度: %.2f\n", alg.averagePathLength(graph)));

        model.Node[] topNodes = alg.getTopNodesByDegree(graph, 5);
        sb.append("\n=== 度数Top5节点 ===\n");
        for (int i = 0; i < topNodes.length; i++) {
            sb.append(String.format("  %d. %s (度: %d)\n", i + 1, topNodes[i].name, topNodes[i].degree));
        }

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setPreferredSize(new Dimension(400, 350));

        JOptionPane.showMessageDialog(this, scroll, "统计信息", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAboutDialog() {
        String msg = "社交网络可视化分析系统 v1.0\n\n" +
                     "功能特性:\n" +
                     "- 社交网络图可视化\n" +
                     "- 节点类型分类（核心/活跃/边缘）\n" +
                     "- 最短路径分析\n" +
                     "- 社区检测（模块度优化/标签传播）\n" +
                     "- 中心性指标计算\n" +
                     "- CSV数据导入\n\n" +
                     "技术: Java + Swing";
        JOptionPane.showMessageDialog(this, msg, "关于", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateStatus(String text) {
        statusLabel.setText(text);
    }
}
