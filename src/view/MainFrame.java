package view;

/**
 * 主窗口 — 应用顶层容器
 * 组装左侧控制面板、右侧图可视化面板、底部状态栏，提供图数据加载和状态更新接口
 */

import model.SocialGraph;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainFrame extends JFrame {

    // ========== 核心组件 ==========

    /** 当前正在显示的社交网络图数据 */
    SocialGraph graph;

    /** 右侧：图可视化面板，用于绘制和交互 */
    GraphPanel graphPanel;

    /** 左侧：控制面板，包含查询、分析、设置等功能 */
    ControlPanel controlPanel;

    /** 底部状态栏标签，显示当前操作提示信息 */
    private JLabel statusLabel;

    /** 水平分割面板，将左侧控制面板和右侧图面板分开 */
    private JSplitPane splitPane;

    /**
     * 构造函数：创建主窗口，初始化所有子组件和布局
     */
    public MainFrame() {
        // 创建一个空的社交网络图，等待用户加载数据
        graph = new SocialGraph("空图", "尚未加载数据");

        // 设置窗口标题
        setTitle("社交网络可视化分析系统");

        // 点击关闭按钮时退出程序
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 设置窗口初始大小：宽度1200像素，高度750像素
        setSize(1200, 750);

        // 让窗口显示在屏幕中央
        setLocationRelativeTo(null);

        // 创建主内容区域（左侧面板 + 右侧图面板）
        createMainContent();

        // 创建底部状态栏
        createStatusBar();

        // 显示窗口
        setVisible(true);
    }

    // ==================== 主体布局 ====================

    /**
     * 创建主内容区域：
     * 左侧放控制面板，右侧放图可视化面板，中间用可拖拽的分割线隔开
     */
    private void createMainContent() {
        // 创建图可视化面板，用于绘制社交网络图
        graphPanel = new GraphPanel(graph);

        // 创建左侧控制面板，包含查询、分析、设置等所有功能
        controlPanel = new ControlPanel(graph, graphPanel, this);

        // 给图面板添加鼠标监听器：
        // 当用户点击图面板上的节点时，自动更新左侧的节点详情信息
        graphPanel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                // 鼠标按下时，延迟更新选中节点信息（确保选中状态已更新）
                SwingUtilities.invokeLater(() -> controlPanel.updateSelectedNodeInfo());
            }
            public void mouseClicked(MouseEvent e) {
                // 鼠标点击时，更新选中节点信息
                SwingUtilities.invokeLater(() -> controlPanel.updateSelectedNodeInfo());
            }
        });

        // 创建水平分割面板，左侧放控制面板，右侧放图面板
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, controlPanel, graphPanel);

        // 设置分割线初始位置：左侧面板宽度为300像素
        splitPane.setDividerLocation(300);

        // 启用一键展开/折叠功能（分割线上有小箭头按钮）
        splitPane.setOneTouchExpandable(true);

        // 将分割面板添加到窗口的中央区域
        add(splitPane, BorderLayout.CENTER);
    }

    // ==================== 底部状态栏 ====================

    /**
     * 创建底部状态栏：
     * 左侧显示当前操作状态信息
     * 右侧显示操作提示（快捷键、操作方式等）
     */
    private void createStatusBar() {
        // 创建状态栏面板，使用边界布局（左状态 + 右提示）
        JPanel statusPanel = new JPanel(new BorderLayout());

        // 设置状态栏顶部边框线（浅灰色分隔线）
        statusPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)));

        // 左侧：状态信息标签，显示当前操作结果
        statusLabel = new JLabel("就绪 — 请加载数据集");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        // 右侧：操作提示标签，告诉用户如何使用
        JLabel hintLabel = new JLabel("拖拽节点移动 | 滚轮缩放 | 拖拽空白平移 | 悬停查看信息");
        hintLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        hintLabel.setForeground(Color.GRAY);
        hintLabel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        // 将状态标签放在左侧，提示标签放在右侧
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(hintLabel, BorderLayout.EAST);

        // 将状态栏面板添加到窗口底部
        add(statusPanel, BorderLayout.SOUTH);
    }

    // ==================== 图数据加载 ====================

    /**
     * 设置新的社交网络图数据
     * 当用户加载新数据集时调用此方法，更新所有相关面板
     *
     * @param newGraph 新加载的社交网络图对象
     */
    public void setGraph(SocialGraph newGraph) {
        // 保存新图数据的引用
        this.graph = newGraph;

        // 更新图面板：重新布局和绘制
        graphPanel.setGraph(newGraph);

        // 更新控制面板：刷新统计信息、清空查询结果
        controlPanel.setGraph(newGraph);

        // 更新底部状态栏，显示加载结果
        String statusText = "已加载: " + newGraph.datasetName
                          + " (" + newGraph.nodeCount + "个节点, "
                          + newGraph.edgeCount + "条边)";
        updateStatus(statusText);
    }

    /**
     * 更新底部状态栏的文字信息
     *
     * @param text 要显示的状态文字
     */
    public void updateStatus(String text) {
        statusLabel.setText(text);
    }
}