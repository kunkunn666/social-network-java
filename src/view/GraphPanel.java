package view;

import model.SocialGraph;
import model.Node;
import algorithm.ForceLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Set;
import java.util.HashSet;

/**
 * 图可视化面板 — 负责鼠标交互、缩放平移、状态管理
 * 支持节点拖拽、画布平移、滚轮缩放、悬停提示、节点选中高亮
 * 渲染逻辑委托给 GraphRenderer
 */
public class GraphPanel extends JPanel implements MouseListener, MouseMotionListener, ComponentListener, MouseWheelListener {

    // ---- 包内可见：供 GraphRenderer 使用 ----

    /** 当前显示的社交网络图数据 */
    SocialGraph graph;

    /** 力导向布局算法，用于计算节点在画布上的位置 */
    ForceLayout layout;

    /** 当前被用户选中的节点ID（-1表示没有选中） */
    int selectedNodeId;

    /** 鼠标当前悬停的节点ID（-1表示没有悬停） */
    int hoverNodeId;

    /** 是否显示节点标签 */
    boolean showLabels;

    /** 是否在边上显示权重数值 */
    boolean showWeights;

    /** 是否显示节点名称 */
    boolean showNodeNames;

    /** 是否在节点上显示ID数字 */
    boolean showNodeId;

    /** 着色模式：0=按节点类型, 1=按社区, 2=统一颜色 */
    int colorMode;

    /** 当前缩放比例（1.0=原始大小, >1=放大, <1=缩小） */
    double scale;

    /** 画布水平偏移量（用于平移功能） */
    double offsetX;

    /** 画布垂直偏移量（用于平移功能） */
    double offsetY;

    /** 高亮路径：存储需要高亮显示的节点ID序列 */
    int[] highlightedPath;

    /** 高亮节点集合：存储需要高亮显示的节点ID */
    Set<Integer> highlightedNodes;

    /** 社区数量（用于设置图例显示） */
    int communityCount;
    // ----

    // ========== 拖拽相关状态 ==========

    /** 当前正在被拖拽的节点ID（-1表示没有拖拽节点） */
    private int draggedNodeId;

    /** 是否处于画布拖拽模式（拖拽空白区域平移画布） */
    private boolean dragMode;

    /** 拖拽开始时的鼠标X坐标 */
    private int dragStartX;

    /** 拖拽开始时的鼠标Y坐标 */
    private int dragStartY;

    /**
     * 构造函数：初始化面板状态和事件监听器
     *
     * @param graph 初始的社交网络图数据
     */
    public GraphPanel(SocialGraph graph) {
        // 保存图数据引用
        this.graph = graph;

        // 创建力导向布局算法实例
        this.layout = new ForceLayout();

        // 初始化所有状态变量的默认值
        this.selectedNodeId = -1;       // 没有选中节点
        this.draggedNodeId = -1;        // 没有拖拽节点
        this.hoverNodeId = -1;          // 没有悬停节点
        this.showLabels = true;         // 默认显示标签
        this.showWeights = false;       // 默认不显示权重
        this.showNodeNames = false;     // 默认不显示节点名称
        this.showNodeId = true;         // 默认显示节点ID
        this.colorMode = 0;             // 默认按节点类型着色
        this.scale = 1.0;               // 默认缩放比例100%
        this.offsetX = 0;               // 默认水平偏移0
        this.offsetY = 0;               // 默认垂直偏移0
        this.highlightedPath = new int[0];  // 默认无高亮路径
        this.highlightedNodes = new HashSet<>(); // 默认无高亮节点
        this.dragMode = false;          // 默认不处于拖拽模式
        this.communityCount = 0;        // 默认社区数为0

        // 设置面板背景色（浅灰蓝色）
        setBackground(new Color(245, 247, 250));

        // 注册各类事件监听器
        addMouseListener(this);         // 鼠标点击、按下、释放
        addMouseMotionListener(this);   // 鼠标移动、拖拽
        addComponentListener(this);     // 面板大小改变
        addMouseWheelListener(this);    // 鼠标滚轮缩放
    }

    // ==================== 数据与状态 ====================

    /**
     * 设置新的图数据并重新布局
     * 当用户加载新数据集时调用
     *
     * @param graph 新的社交网络图数据
     */
    public void setGraph(SocialGraph graph) {
        // 更新图数据引用
        this.graph = graph;

        // 重置所有选中和高亮状态
        this.selectedNodeId = -1;
        this.hoverNodeId = -1;
        this.highlightedPath = new int[0];
        this.highlightedNodes = new HashSet<>();
        this.communityCount = 0;

        // 重新计算节点布局
        relayout();
    }

    /**
     * 重新计算力导向布局
     * 重置缩放和平移，让所有节点在面板中重新排列
     */
    private void relayout() {
        // 检查图数据是否存在
        if (graph != null && graph.nodeCount > 0) {
            // 获取面板的当前宽度和高度
            int panelWidth = getWidth();
            int panelHeight = getHeight();

            // 使用力导向布局算法计算节点位置
            layout.computeLayout(graph, panelWidth, panelHeight);

            // 重置缩放和平移，回到默认视角
            scale = 1.0;
            offsetX = 0;
            offsetY = 0;
        }

        // 触发重绘
        repaint();
    }

    /**
     * 设置是否显示节点名称
     * @param show true=显示名称, false=隐藏名称
     */
    public void setShowNodeNames(boolean show) {
        this.showNodeNames = show;
        repaint();
    }

    /**
     * 设置是否在节点上显示ID
     * @param show true=显示ID, false=隐藏ID
     */
    public void setShowNodeId(boolean show) {
        this.showNodeId = show;
        repaint();
    }

    /**
     * 设置着色模式
     * @param mode 0=按节点类型, 1=按社区, 2=统一颜色
     */
    public void setColorMode(int mode) {
        this.colorMode = mode;
        this.highlightedNodes = new HashSet<>();
        repaint();
    }

    /**
     * 设置社区数量（用于图例显示）
     * @param count 社区数量
     */
    public void setCommunityCount(int count) {
        this.communityCount = count;
    }

    /**
     * 设置高亮路径（用紫色粗线标记路径）
     * @param path 需要高亮的节点ID数组，按顺序连接
     */
    public void setHighlightedPath(int[] path) {
        // 如果传入null，则清空高亮路径
        if (path != null) {
            this.highlightedPath = path;
        } else {
            this.highlightedPath = new int[0];
        }
        repaint();
    }

    /**
     * 设置高亮节点集合（用紫色标记这些节点）
     * @param nodes 需要高亮的节点ID集合
     */
    public void setHighlightedNodes(Set<Integer> nodes) {
        // 如果传入null，则清空高亮节点集合
        if (nodes != null) {
            this.highlightedNodes = nodes;
        } else {
            this.highlightedNodes = new HashSet<>();
        }
        repaint();
    }

    /**
     * 获取当前选中节点的ID
     * @return 选中节点ID，-1表示没有选中
     */
    public int getSelectedNodeId() {
        return selectedNodeId;
    }

    /**
     * 清除所有选中、高亮路径和高亮节点
     */
    public void clearSelection() {
        selectedNodeId = -1;
        highlightedPath = new int[0];
        highlightedNodes = new HashSet<>();
        // 重置图中所有节点和边的highlight标记
        graph.resetHighlights();
        repaint();
    }

    // ==================== 坐标转换 ====================

    /**
     * 将屏幕坐标X转换为图坐标X
     * 屏幕坐标 = 图坐标 × 缩放 + 偏移
     * 所以 图坐标 = (屏幕坐标 - 偏移) / 缩放
     *
     * @param screenX 鼠标在屏幕上的X坐标
     * @return 对应图坐标系中的X坐标
     */
    private int screenToGraphX(int screenX) {
        // 先减去偏移量，再除以缩放比例
        double graphX = (screenX - offsetX) / scale;
        return (int) graphX;
    }

    /**
     * 将屏幕坐标Y转换为图坐标Y
     *
     * @param screenY 鼠标在屏幕上的Y坐标
     * @return 对应图坐标系中的Y坐标
     */
    private int screenToGraphY(int screenY) {
        // 先减去偏移量，再除以缩放比例
        double graphY = (screenY - offsetY) / scale;
        return (int) graphY;
    }

    /**
     * 查找鼠标位置下的节点
     * 遍历所有节点，判断鼠标是否在某个节点的圆形区域内
     *
     * @param x 鼠标屏幕X坐标
     * @param y 鼠标屏幕Y坐标
     * @return 找到的节点ID，-1表示没有找到
     */
    private int findNodeAt(int x, int y) {
        // 如果图数据为空，直接返回-1
        if (graph == null) return -1;

        // 将屏幕坐标转换为图坐标
        int graphX = screenToGraphX(x);
        int graphY = screenToGraphY(y);

        // 遍历所有节点，检查鼠标是否在节点圆内
        for (int nodeIndex = 0; nodeIndex < graph.nodeCount; nodeIndex++) {
            Node currentNode = graph.nodes[nodeIndex];

            // 计算鼠标到节点中心的距离（X方向和Y方向）
            double deltaX = graphX - currentNode.x;
            double deltaY = graphY - currentNode.y;

            // 获取该节点的显示半径
            double nodeRadius = GraphRenderer.getNodeRadius(this, nodeIndex);

            // 使用勾股定理判断：距离平方 <= 半径平方，表示在圆内
            double distanceSquared = deltaX * deltaX + deltaY * deltaY;
            double radiusSquared = nodeRadius * nodeRadius;

            if (distanceSquared <= radiusSquared) {
                return nodeIndex;
            }
        }
        return -1;
    }

    // ==================== 渲染（委托） ====================

    /**
     * 绘制面板内容
     * 将绘制工作委托给 GraphRenderer 类
     *
     * @param g 图形上下文对象
     */
    @Override
    protected void paintComponent(Graphics g) {
        // 先调用父类方法，绘制背景
        super.paintComponent(g);

        // 委托 GraphRenderer 绘制图的所有内容
        GraphRenderer.paint(this, g);
    }

    // ==================== 鼠标交互 ====================

    /**
     * 鼠标按下事件处理
     * 如果按在节点上：选中该节点（同时准备拖拽）
     * 如果按在空白区域：进入画布拖拽模式
     *
     * @param e 鼠标事件对象
     */
    @Override
    public void mousePressed(MouseEvent e) {
        // 获取鼠标点击位置
        int mouseX = e.getX();
        int mouseY = e.getY();

        // 查找鼠标下的节点
        int clickedNodeId = findNodeAt(mouseX, mouseY);

        if (clickedNodeId >= 0) {
            // 情况1：点击了节点 → 选中并准备拖拽
            selectedNodeId = clickedNodeId;
            draggedNodeId = clickedNodeId;
        } else {
            // 情况2：点击了空白区域 → 进入画布平移模式
            dragMode = true;
            dragStartX = mouseX;
            dragStartY = mouseY;
        }

        repaint();
    }

    /**
     * 鼠标释放事件处理
     * 结束拖拽操作
     *
     * @param e 鼠标事件对象
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        // 释放拖拽的节点
        draggedNodeId = -1;

        // 退出画布平移模式
        dragMode = false;
    }

    /**
     * 鼠标点击事件（目前不需要额外处理，但接口要求实现）
     */
    @Override
    public void mouseClicked(MouseEvent e) {}

    /**
     * 鼠标进入面板事件（目前不需要额外处理，但接口要求实现）
     */
    @Override
    public void mouseEntered(MouseEvent e) {}

    /**
     * 鼠标离开面板事件
     * 清除悬停状态，让节点恢复默认颜色
     *
     * @param e 鼠标事件对象
     */
    @Override
    public void mouseExited(MouseEvent e) {
        // 鼠标离开面板，清除悬停节点
        hoverNodeId = -1;
        repaint();
    }

    /**
     * 鼠标拖拽事件处理
     * 两种情况：
     * 1. 拖拽节点：移动节点到新位置
     * 2. 拖拽空白区域：平移整个画布
     *
     * @param e 鼠标事件对象
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        if (draggedNodeId >= 0 && graph != null) {
            // 情况1：正在拖拽节点，更新节点位置
            // 将屏幕坐标转换为图坐标，作为节点的新位置
            graph.nodes[draggedNodeId].x = screenToGraphX(e.getX());
            graph.nodes[draggedNodeId].y = screenToGraphY(e.getY());
            repaint();
        } else if (dragMode) {
            // 情况2：正在拖拽画布，更新偏移量
            // 计算鼠标移动的增量
            int mouseDeltaX = e.getX() - dragStartX;
            int mouseDeltaY = e.getY() - dragStartY;

            // 将增量加到偏移量上
            offsetX += mouseDeltaX;
            offsetY += mouseDeltaY;

            // 更新拖拽起始点为当前鼠标位置（为下一次移动做准备）
            dragStartX = e.getX();
            dragStartY = e.getY();

            repaint();
        }
    }

    /**
     * 鼠标移动事件处理
     * 检测鼠标是否悬停在某个节点上，更新悬停状态和提示文字
     *
     * @param e 鼠标事件对象
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        // 查找鼠标当前位置下的节点
        int nodeUnderMouse = findNodeAt(e.getX(), e.getY());

        // 只有当悬停节点发生变化时才更新（避免不必要的重绘）
        if (nodeUnderMouse != hoverNodeId) {
            hoverNodeId = nodeUnderMouse;
            repaint();

            if (nodeUnderMouse >= 0) {
                // 鼠标悬停在节点上，显示节点信息提示
                Node hoveredNode = graph.nodes[nodeUnderMouse];
                String tooltip = hoveredNode.name + " (度数: " + hoveredNode.degree + ")";
                setToolTipText(tooltip);
            } else {
                // 鼠标没有悬停在节点上，清除提示
                setToolTipText(null);
            }
        }
    }

    /**
     * 鼠标滚轮事件处理
     * 向上滚动放大，向下滚动缩小
     * 缩放时以鼠标位置为中心，而不是以画布中心
     *
     * @param e 鼠标滚轮事件对象
     */
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        // 保存旧的缩放比例
        double oldScale = scale;

        // 根据滚轮方向调整缩放比例
        // 向上滚动（负值）：放大10%
        // 向下滚动（正值）：缩小10%
        if (e.getWheelRotation() < 0) {
            scale = scale * 1.1;
        } else {
            scale = scale / 1.1;
        }

        // 限制缩放范围：最小0.1倍，最大10倍
        if (scale < 0.1) {
            scale = 0.1;
        }
        if (scale > 10) {
            scale = 10;
        }

        // 以鼠标位置为中心进行缩放
        // 调整偏移量，使鼠标指向的图位置保持不变
        double scaleFactor = scale / oldScale;

        // 新的偏移量 = 鼠标位置 - 缩放因子 × (鼠标位置 - 旧偏移量)
        offsetX = e.getX() - scaleFactor * (e.getX() - offsetX);
        offsetY = e.getY() - scaleFactor * (e.getY() - offsetY);

        repaint();
    }

    /**
     * 面板大小改变事件处理
     * 当窗口大小改变时，重新计算节点布局
     *
     * @param e 组件事件对象
     */
    @Override
    public void componentResized(ComponentEvent e) {
        // 只有图数据有效时才重新布局
        if (graph != null && graph.nodeCount > 0) {
            layout.computeLayout(graph, getWidth(), getHeight());
        }
    }

    // 以下方法为 ComponentListener 接口要求实现，暂无特殊处理
    @Override public void componentMoved(ComponentEvent e) {}
    @Override public void componentShown(ComponentEvent e) {}
    @Override public void componentHidden(ComponentEvent e) {}
}