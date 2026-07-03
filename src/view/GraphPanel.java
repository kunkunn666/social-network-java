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
    SocialGraph graph;
    ForceLayout layout;
    int selectedNodeId;
    int hoverNodeId;
    boolean showLabels;
    boolean showWeights;
    boolean showNodeNames;
    boolean showNodeId;
    int colorMode;
    double scale;
    double offsetX;
    double offsetY;
    int[] highlightedPath;
    Set<Integer> highlightedNodes;
    int communityCount;
    // ----

    private int draggedNodeId;
    private boolean dragMode;
    private int dragStartX, dragStartY;
    private boolean dragLocked;

    public GraphPanel(SocialGraph graph) {
        this.graph = graph;
        this.layout = new ForceLayout();
        this.selectedNodeId = -1;
        this.draggedNodeId = -1;
        this.hoverNodeId = -1;
        this.showLabels = true;
        this.showWeights = false;
        this.showNodeNames = false;
        this.showNodeId = true;
        this.colorMode = 0;
        this.scale = 1.0;
        this.offsetX = 0;
        this.offsetY = 0;
        this.highlightedPath = new int[0];
        this.highlightedNodes = new HashSet<>();
        this.dragMode = false;
        this.dragLocked = false;
        this.communityCount = 0;

        setBackground(new Color(245, 247, 250));
        addMouseListener(this);
        addMouseMotionListener(this);
        addComponentListener(this);
        addMouseWheelListener(this);
    }

    // ==================== 数据与状态 ====================

    public void setGraph(SocialGraph graph) {
        this.graph = graph;
        this.selectedNodeId = -1;
        this.hoverNodeId = -1;
        this.highlightedPath = new int[0];
        this.highlightedNodes = new HashSet<>();
        this.communityCount = 0;
        relayout();
    }

    public void relayout() {
        if (graph != null && graph.nodeCount > 0) {
            layout.computeLayout(graph, getWidth(), getHeight());
            scale = 1.0;
            offsetX = 0;
            offsetY = 0;
        }
        repaint();
    }

    public void setShowLabels(boolean show) { this.showLabels = show; repaint(); }
    public void setShowWeights(boolean show) { this.showWeights = show; repaint(); }
    public void setShowNodeNames(boolean show) { this.showNodeNames = show; repaint(); }
    public void setShowNodeId(boolean show) { this.showNodeId = show; repaint(); }
    public void setColorMode(int mode) { 
    this.colorMode = mode; 
    this.highlightedNodes = new HashSet<>();
    repaint(); 
}
    public void setCommunityCount(int count) { this.communityCount = count; }

    public void setHighlightedPath(int[] path) {
        this.highlightedPath = (path != null) ? path : new int[0];
        repaint();
    }

    public void setHighlightedNodes(Set<Integer> nodes) {
        this.highlightedNodes = (nodes != null) ? nodes : new HashSet<>();
        repaint();
    }

    public int getSelectedNodeId() { return selectedNodeId; }

    public void clearSelection() {
        selectedNodeId = -1;
        highlightedPath = new int[0];
        highlightedNodes = new HashSet<>();
        graph.resetHighlights();
        repaint();
    }

    public void setDragLocked(boolean locked) { this.dragLocked = locked; }

    // ==================== 坐标转换 ====================

    private int screenToGraphX(int sx) { return (int) ((sx - offsetX) / scale); }
    private int screenToGraphY(int sy) { return (int) ((sy - offsetY) / scale); }

    private int findNodeAt(int x, int y) {
        if (graph == null) return -1;
        int gx = screenToGraphX(x);
        int gy = screenToGraphY(y);
        for (int i = 0; i < graph.nodeCount; i++) {
            Node n = graph.nodes[i];
            double dx = gx - n.x;
            double dy = gy - n.y;
            double r = GraphRenderer.getNodeRadius(this, i);
            if (dx * dx + dy * dy <= r * r) return i;
        }
        return -1;
    }

    // ==================== 渲染（委托） ====================

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        GraphRenderer.paint(this, g);
    }

    // ==================== 鼠标交互 ====================

    @Override
    public void mousePressed(MouseEvent e) {
        int nodeId = findNodeAt(e.getX(), e.getY());
        if (nodeId >= 0) {
            selectedNodeId = nodeId;
            if (!dragLocked) draggedNodeId = nodeId;
        } else {
            dragMode = true;
            dragStartX = e.getX();
            dragStartY = e.getY();
        }
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        draggedNodeId = -1;
        dragMode = false;
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {
        hoverNodeId = -1;
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (draggedNodeId >= 0 && graph != null) {
            graph.nodes[draggedNodeId].x = screenToGraphX(e.getX());
            graph.nodes[draggedNodeId].y = screenToGraphY(e.getY());
            repaint();
        } else if (dragMode) {
            offsetX += e.getX() - dragStartX;
            offsetY += e.getY() - dragStartY;
            dragStartX = e.getX();
            dragStartY = e.getY();
            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int nodeId = findNodeAt(e.getX(), e.getY());
        if (nodeId != hoverNodeId) {
            hoverNodeId = nodeId;
            repaint();
            if (nodeId >= 0) {
                setToolTipText(graph.nodes[nodeId].name + " (度数: " + graph.nodes[nodeId].degree + ")");
            } else {
                setToolTipText(null);
            }
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        double oldScale = scale;
        if (e.getWheelRotation() < 0) scale *= 1.1;
        else scale /= 1.1;
        if (scale < 0.1) scale = 0.1;
        if (scale > 10) scale = 10;
        double factor = scale / oldScale;
        offsetX = e.getX() - factor * (e.getX() - offsetX);
        offsetY = e.getY() - factor * (e.getY() - offsetY);
        repaint();
    }

    @Override
    public void componentResized(ComponentEvent e) {
        if (graph != null && graph.nodeCount > 0) {
            layout.computeLayout(graph, getWidth(), getHeight());
        }
    }

    @Override public void componentMoved(ComponentEvent e) {}
    @Override public void componentShown(ComponentEvent e) {}
    @Override public void componentHidden(ComponentEvent e) {}

    // ==================== 视图控制 ====================

    public void zoomIn()  { scale *= 1.2; repaint(); }
    public void zoomOut() { scale /= 1.2; repaint(); }

    public void resetView() {
        scale = 1.0;
        offsetX = 0;
        offsetY = 0;
        repaint();
    }
}