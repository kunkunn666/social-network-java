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
    SocialGraph graph;
    GraphPanel graphPanel;
    ControlPanel controlPanel;
    private JLabel statusLabel;
    private JSplitPane splitPane;

    /**
     * 构造函数：创建主窗口，初始化所有子组件和布局
     */
    public MainFrame() {
        graph = new SocialGraph("空图", "尚未加载数据");
        setTitle("社交网络可视化分析系统");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);
        createMainContent();
        createStatusBar();
        setVisible(true);
    }

    // ==================== 主体布局 ====================
    /**
     * 创建主内容区域：
     * 左侧放控制面板，右侧放图可视化面板，中间用可拖拽的分割线隔开
     */
    private void createMainContent() {
        graphPanel = new GraphPanel(graph);
        controlPanel = new ControlPanel(graph, graphPanel, this);

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

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, controlPanel, graphPanel);
        splitPane.setDividerLocation(300);
        splitPane.setOneTouchExpandable(true);
        add(splitPane, BorderLayout.CENTER);
    }

    // ==================== 底部状态栏 ====================

    /**
     * 创建底部状态栏：
     * 左侧显示当前操作状态信息
     * 右侧显示操作提示（快捷键、操作方式等）
     */
    private void createStatusBar() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)));

        //左侧：状态信息标签，显示当前操作结果
        statusLabel = new JLabel("就绪 — 请加载数据集");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        //右侧：操作提示标签，告诉用户如何使用
        JLabel hintLabel = new JLabel("拖拽节点移动 | 滚轮缩放 | 拖拽空白平移 | 悬停查看信息");
        hintLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        hintLabel.setForeground(Color.GRAY);
        hintLabel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        //将状态标签放在左侧，提示标签放在右侧
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(hintLabel, BorderLayout.EAST);

        //将状态栏面板添加到窗口底部
        add(statusPanel, BorderLayout.SOUTH);
    }

    // ==================== 图数据加载 ====================

    /**
     * 设置新的社交网络图数据
     * 当用户加载新数据集时调用此方法，更新所有相关面板
     */
    public void setGraph(SocialGraph newGraph) {
        this.graph = newGraph;
        graphPanel.setGraph(newGraph);
        controlPanel.setGraph(newGraph);

        // 更新底部状态栏，显示加载结果
        String statusText = "已加载: " + newGraph.dataName
                        + " (" + newGraph.nodeCount + "个节点, "
                        + newGraph.edgeCount + "条边)";
        updateStatus(statusText);
    }

    /**
     * 更新底部状态栏的文字信息
     */
    public void updateStatus(String text) {
        statusLabel.setText(text);
    }
}