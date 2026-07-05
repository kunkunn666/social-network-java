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

    /** 当前社交网络图数据 */
    private SocialGraph graph;

    /** 右侧图可视化面板引用 */
    private GraphPanel graphPanel;

    /** 主窗口引用（用于更新状态栏） */
    private MainFrame mainFrame;

    /** 图算法工具类，提供各种分析算法 */
    private GraphAlgorithms algorithms;

    /** 查询功能子面板 */
    private QueryPanel queryPanel;

    /** 分析功能子面板 */
    private AnalysisPanel analysisPanel;

    /** 统计信息标签，显示节点数、边数等 */
    private JLabel statsLabel;

    /** 节点详情文本区域，显示选中节点的详细信息 */
    private JTextArea infoArea;

    /** 当前加载的数据文件路径（用于恢复原图） */
    String currentFilePath;

    /**
     * 构造函数：创建控制面板，组装所有子功能面板
     *
     * @param graph 初始图数据
     * @param graphPanel 图可视化面板引用
     * @param mainFrame 主窗口引用
     */
    public ControlPanel(SocialGraph graph, GraphPanel graphPanel, MainFrame mainFrame) {
        // 保存引用
        this.graph = graph;
        this.graphPanel = graphPanel;
        this.mainFrame = mainFrame;

        // 创建图算法工具类
        this.algorithms = new GraphAlgorithms();

        // 创建两个子功能面板
        queryPanel = new QueryPanel(mainFrame, graphPanel);
        analysisPanel = new AnalysisPanel(mainFrame, graphPanel);
        analysisPanel.setControlPanel(this); // 让分析面板持有控制面板的引用（用于恢复原图）

        // 创建主内容面板，使用垂直布局
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // 设置内容面板的内边距（上下左右各10像素）
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 按顺序添加各个功能面板
        content.add(createDatasetPanel());    // 1. 数据集选择面板
        content.add(Box.createVerticalStrut(8)); // 垂直间距8像素
        content.add(createStatsPanel());      // 2. 统计信息面板
        content.add(Box.createVerticalStrut(8));
        content.add(createInfoPanel());       // 3. 节点详情面板
        content.add(Box.createVerticalStrut(8));
        content.add(analysisPanel);           // 4. 分析功能面板（度分析、连通分量等）
        content.add(Box.createVerticalStrut(8));
        content.add(queryPanel);              // 5. 查询功能面板（最短路径、N跳等）
        content.add(Box.createVerticalStrut(8));
        content.add(createDisplayPanel());    // 6. 显示设置面板（着色方式）
        content.add(Box.createVerticalGlue()); // 底部弹性空间，让面板靠上对齐

        // 将内容面板设置为滚动视图的内容
        setViewportView(content);

        // 设置控制面板的默认大小
        setPreferredSize(new Dimension(300, 600));

        // 设置滚动速度（每次滚轮滚动16像素）
        getVerticalScrollBar().setUnitIncrement(16);
    }

    /**
     * 设置新的图数据，更新所有子面板
     *
     * @param newGraph 新的社交网络图数据
     */
    public void setGraph(SocialGraph newGraph) {
        this.graph = newGraph;

        // 更新统计信息
        updateStats();

        // 通知子面板更新图数据
        queryPanel.setGraph(newGraph);
        analysisPanel.setGraph(newGraph);

        // 清空节点详情区域
        infoArea.setText("");
    }

    // ==================== 数据集切换区 ====================

    /**
     * 创建数据集选择面板
     * 包含一个下拉框（选择数据集）和一个加载按钮
     */
    private JPanel createDatasetPanel() {
        // 创建面板，使用边界布局（上下左右有5像素间距）
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        // 设置面板标题边框
        panel.setBorder(createTitledBorder("数据集"));

        // 创建数据集下拉框，包含两个预设数据集
        JComboBox<String> datasetCombo = new JComboBox<>(new String[]{"空手道俱乐部", "StackOverflow标签"});

        // 创建加载按钮
        JButton loadButton = new JButton("加载");

        // 点击加载按钮时，根据下拉框选择加载对应的数据集
        loadButton.addActionListener(e -> loadDataset(datasetCombo.getSelectedIndex()));

        // 创建顶部面板，将下拉框和按钮放在一行
        JPanel topPanel = new JPanel(new BorderLayout(5, 0));
        topPanel.add(datasetCombo, BorderLayout.CENTER);  // 下拉框占中间
        topPanel.add(loadButton, BorderLayout.EAST);      // 按钮在右边

        panel.add(topPanel, BorderLayout.CENTER);
        return panel;
    }

    // ==================== 显示设置面板 ====================

    /**
     * 创建显示设置面板
     * 包含着色方式选择下拉框
     */
    private JPanel createDisplayPanel() {
        // 创建面板，使用边界布局
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(createTitledBorder("显示设置"));

        // 创建着色方式下拉框
        analysisPanel.colorModeCombo = new JComboBox<>(new String[]{"按节点类型", "按社区", "统一颜色"});

        // 选择改变时，更新图面板的着色模式
        analysisPanel.colorModeCombo.addActionListener(e -> {
            int selectedMode = analysisPanel.colorModeCombo.getSelectedIndex();
            graphPanel.setColorMode(selectedMode);
        });

        // 标签放在左侧，下拉框放在中间
        panel.add(new JLabel("着色方式:"), BorderLayout.WEST);
        panel.add(analysisPanel.colorModeCombo, BorderLayout.CENTER);

        return panel;
    }

    // ==================== 统计信息 ====================

    /**
     * 创建统计信息面板
     * 显示节点数、边数、平均度、图密度、直径等基本信息
     */
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(createTitledBorder("统计信息"));

        // 统计信息标签，使用HTML格式显示多行文本
        statsLabel = new JLabel("<html>节点: 0<br>边: 0<br>平均度: 0<br>密度: 0<br>直径: 0</html>");
        statsLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        panel.add(statsLabel, BorderLayout.CENTER);
        return panel;
    }

    // ==================== 节点详情 ====================

    /**
     * 创建节点详情面板
     * 显示选中节点的名称、ID、度数、类型、中心性、邻居列表等信息
     */
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(createTitledBorder("节点详情"));

        // 创建只读文本区域，用于显示节点详情
        infoArea = new JTextArea(8, 20);
        infoArea.setEditable(false);   // 不允许编辑
        infoArea.setLineWrap(true);    // 自动换行
        infoArea.setWrapStyleWord(true); // 按单词边界换行
        infoArea.setFont(new Font("微软雅黑", Font.PLAIN, 11));

        // 将文本区域放入滚动面板
        JScrollPane scroll = new JScrollPane(infoArea);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    /**
     * 更新选中节点的详细信息
     * 当用户点击图面板中的节点时调用
     */
    public void updateSelectedNodeInfo() {
        // 获取当前选中的节点ID
        int selectedId = graphPanel.getSelectedNodeId();

        // 如果没有选中节点或图数据为空，直接返回
        if (selectedId < 0 || graph == null) return;

        // 获取选中节点对象
        Node selectedNode = graph.nodes[selectedId];

        // 计算度中心性和接近中心性
        double[] degreeCentralityValues = algorithms.degreeCentrality(graph);
        double[] closenessCentralityValues = algorithms.closenessCentrality(graph);

        // 构建节点详情文本
        StringBuilder infoBuilder = new StringBuilder();
        infoBuilder.append("名称: ").append(selectedNode.name).append("\n");
        infoBuilder.append("ID: ").append(selectedNode.id).append("\n");
        infoBuilder.append("度数: ").append(selectedNode.degree).append("\n");
        infoBuilder.append("类型: ").append(selectedNode.type).append("\n");
        infoBuilder.append(String.format("度中心性: %.4f\n", degreeCentralityValues[selectedId]));
        infoBuilder.append(String.format("接近中心性: %.4f\n", closenessCentralityValues[selectedId]));

        // 如果节点有连通分量归属，显示分量编号
        if (graph.vset[selectedId] >= 0) {
            infoBuilder.append("分量: ").append(graph.vset[selectedId] + 1).append("\n");
        }

        // 显示邻居列表（最多显示10个）
        infoBuilder.append("\n邻居:\n");
        int[] neighborIds = graph.getNeighbors(selectedId);
        int showLimit = Math.min(neighborIds.length, 10);

        for (int neighborIndex = 0; neighborIndex < showLimit; neighborIndex++) {
            infoBuilder.append("  - ").append(graph.nodes[neighborIds[neighborIndex]].name).append("\n");
        }

        // 如果邻居超过10个，显示总数提示
        if (neighborIds.length > 10) {
            infoBuilder.append("  ... 共").append(neighborIds.length).append("个");
        }

        // 更新文本区域
        infoArea.setText(infoBuilder.toString());

        // 更新底部状态栏
        String statusText = "选中节点: " + selectedNode.name
                          + " (度数: " + selectedNode.degree
                          + ", 类型: " + selectedNode.type + ")";
        mainFrame.updateStatus(statusText);
    }

    // ==================== 辅助方法 ====================

    /**
     * 加载数据集
     * 根据索引选择不同的数据集文件并加载
     *
     * @param datasetIndex 数据集索引：0=空手道俱乐部, 1=StackOverflow标签
     */
    private void loadDataset(int datasetIndex) {
        // 根据索引设置不同的数据集参数
        String filename;   // 数据文件路径
        String name;       // 数据集名称
        String desc;       // 数据集描述
        boolean showNames; // 是否显示节点名称
        boolean showId;    // 是否显示节点ID

        if (datasetIndex == 0) {
            // 空手道俱乐部数据集
            filename = "D:/prg/VsCode/Java/social-network-java/data/karate_club.csv";
            name = "空手道俱乐部";
            desc = "Zachary空手道俱乐部社交网络";
            showNames = false;  // 节点名称较长，不显示
            showId = true;      // 显示ID数字
        } else {
            // StackOverflow标签数据集
            filename = "D:/prg/VsCode/Java/social-network-java/data/stackoverflow_edges.csv";
            name = "StackOverflow标签";
            desc = "StackOverflow技术标签共现网络";
            showNames = true;   // 显示标签名称
            showId = false;     // 不显示ID数字
        }

        // 创建新的图对象并加载数据
        SocialGraph newGraph = new SocialGraph(name, desc);
        newGraph.loadFromCSV(filename);

        // 保存文件路径（用于恢复原图功能）
        currentFilePath = filename;

        // 设置图面板的显示选项
        graphPanel.setShowNodeNames(showNames);
        graphPanel.setShowNodeId(showId);

        // 更新主窗口的图数据
        mainFrame.setGraph(newGraph);
    }

    /**
     * 更新统计信息面板
     * 计算并显示节点数、边数、平均度、图密度、直径
     */
    private void updateStats() {
        // 如果图数据为空，直接返回
        if (graph == null) return;

        // 计算各项统计指标
        double averageDegree = algorithms.averageDegree(graph);
        double graphDensity = algorithms.graphDensity(graph);
        int graphDiameter = algorithms.diameter(graph);

        // 使用HTML格式更新标签文本
        String statsText = String.format(
            "<html>节点数: %d<br>边数: %d<br>平均度: %.2f<br>图密度: %.4f<br>直径: %d</html>",
            graph.nodeCount, graph.edgeCount, averageDegree, graphDensity, graphDiameter);

        statsLabel.setText(statsText);
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