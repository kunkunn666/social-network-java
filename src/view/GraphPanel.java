package view;

import model.SocialGraph;
import model.Node;
import model.Edge;
import algorithm.ForceLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

public class GraphPanel extends JPanel implements MouseListener, MouseMotionListener, ComponentListener, MouseWheelListener {

    private SocialGraph graph;
    private ForceLayout layout;
    private int selectedNodeId;
    private int draggedNodeId;
    private int hoverNodeId;
    private boolean showLabels;
    private boolean showWeights;
    private boolean showNodeNames;
    private boolean showNodeId;
    private int colorMode;
    private double scale;
    private double offsetX;
    private double offsetY;
    private int[] highlightedPath;
    private Set<Integer> highlightedNodes;
    private boolean dragMode;
    private int dragStartX, dragStartY;
    private boolean dragLocked;
    private ArrayList<int[]> tempPath;
    private int communityCount;

    private static final Color[] COMMUNITY_COLORS = {
        new Color(255, 107, 107), new Color(78, 205, 196), new Color(255, 230, 109),
        new Color(162, 155, 254), new Color(93, 173, 226), new Color(253, 121, 168),
        new Color(133, 193, 233), new Color(171, 235, 198), new Color(245, 203, 167),
        new Color(230, 176, 170), new Color(200, 230, 201), new Color(174, 214, 241),
        new Color(249, 231, 159), new Color(215, 189, 226), new Color(171, 235, 198)
    };

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
        this.tempPath = new ArrayList<>();

        setBackground(new Color(245, 247, 250));
        addMouseListener(this);
        addMouseMotionListener(this);
        addComponentListener(this);
        addMouseWheelListener(this);
    }

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

    public void setShowLabels(boolean show) {
        this.showLabels = show;
        repaint();
    }

    public void setShowWeights(boolean show) {
        this.showWeights = show;
        repaint();
    }

    public void setShowNodeNames(boolean show) {
        this.showNodeNames = show;
        repaint();
    }

    public void setShowNodeId(boolean show) {
        this.showNodeId = show;
        repaint();
    }

    public void setColorMode(int mode) {
        this.colorMode = mode;
        repaint();
    }

    public void setCommunityCount(int count) {
        this.communityCount = count;
    }

    public void setHighlightedPath(int[] path) {
        this.highlightedPath = (path != null) ? path : new int[0];
        repaint();
    }

    public void setHighlightedNodes(Set<Integer> nodes) {
        this.highlightedNodes = (nodes != null) ? nodes : new HashSet<>();
        repaint();
    }

    public int getSelectedNodeId() {
        return selectedNodeId;
    }

    public void clearSelection() {
        selectedNodeId = -1;
        highlightedPath = new int[0];
        highlightedNodes = new HashSet<>();
        graph.resetHighlights();
        repaint();
    }

    public void setDragLocked(boolean locked) {
        this.dragLocked = locked;
    }

    private int screenToGraphX(int sx) {
        return (int) ((sx - offsetX) / scale);
    }

    private int screenToGraphY(int sy) {
        return (int) ((sy - offsetY) / scale);
    }

    private int findNodeAt(int x, int y) {
        if (graph == null) return -1;
        int gx = screenToGraphX(x);
        int gy = screenToGraphY(y);
        for (int i = 0; i < graph.nodeCount; i++) {
            Node n = graph.nodes[i];
            double dx = gx - n.x;
            double dy = gy - n.y;
            double r = getNodeRadius(i);
            if (dx * dx + dy * dy <= r * r) return i;
        }
        return -1;
    }

    private double getNodeRadius(int nodeId) {
        if (graph == null) return 12;
        Node n = graph.nodes[nodeId];
        int base = 12;
        int extra = Math.min(n.degree, 16);
        return base + extra;
    }

    private Color getNodeColor(int nodeId) {
        if (graph == null) return Color.GRAY;
        Node n = graph.nodes[nodeId];
        if (nodeId == selectedNodeId) return new Color(156, 39, 176);
        if (nodeId == hoverNodeId) return new Color(255, 152, 0);
        if (highlightedNodes.contains(nodeId)) return new Color(156, 39, 176);

        if (colorMode == 0) {
            if ("核心".equals(n.type)) return new Color(239, 83, 80);
            if ("活跃".equals(n.type)) return new Color(255, 193, 7);
            return new Color(129, 199, 132);
        } else if (colorMode == 1) {
            int comm = n.community;
            if (comm < 0 || comm >= COMMUNITY_COLORS.length) return Color.GRAY;
            return COMMUNITY_COLORS[comm % COMMUNITY_COLORS.length];
        } else {
            return new Color(66, 133, 244);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (graph == null || graph.nodeCount == 0) {
            g.setColor(Color.GRAY);
            g.setFont(new Font("微软雅黑", Font.PLAIN, 16));
            g.drawString("请加载数据集", getWidth() / 2 - 60, getHeight() / 2);
            return;
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.translate(offsetX, offsetY);
        g2d.scale(scale, scale);

        drawEdges(g2d);
        drawHighlightedPath(g2d);
        drawNodes(g2d);

        g2d.scale(1 / scale, 1 / scale);
        g2d.translate(-offsetX, -offsetY);

        drawLegend(g2d);
    }

    private void drawEdges(Graphics2D g2d) {
        for (int i = 0; i < graph.edgeCount; i++) {
            Edge e = graph.edges[i];
            Node n1 = graph.nodes[e.from];
            Node n2 = graph.nodes[e.to];

            if (e.highlight || (highlightedNodes.contains(e.from) && highlightedNodes.contains(e.to))) {
                g2d.setColor(new Color(156, 39, 176));
                g2d.setStroke(new BasicStroke(3.0f));
            } else {
                float alpha = 0.3f;
                if (e.weight > 0) {
                    alpha = Math.min(0.7f, (float) (0.2 + e.weight / 100.0));
                }
                g2d.setColor(new Color(150, 150, 150, (int) (alpha * 255)));
                float width = 1.0f;
                if (e.weight > 0) width = Math.min(3.0f, (float) (0.5 + e.weight / 50.0));
                g2d.setStroke(new BasicStroke(width));
            }

            g2d.drawLine((int) n1.x, (int) n1.y, (int) n2.x, (int) n2.y);

            if (showWeights && e.weight > 0 && e.weight != 1.0) {
                int mx = (int) ((n1.x + n2.x) / 2);
                int my = (int) ((n1.y + n2.y) / 2);
                g2d.setColor(Color.DARK_GRAY);
                g2d.setFont(new Font("微软雅黑", Font.PLAIN, 10));
                String label = String.format("%.1f", e.weight);
                g2d.drawString(label, mx, my);
            }
        }
        g2d.setStroke(new BasicStroke(1.0f));
    }

    private void drawHighlightedPath(Graphics2D g2d) {
        if (highlightedPath.length < 2) return;
        g2d.setColor(new Color(156, 39, 176));
        g2d.setStroke(new BasicStroke(4.0f));
        for (int i = 0; i < highlightedPath.length - 1; i++) {
            Node n1 = graph.nodes[highlightedPath[i]];
            Node n2 = graph.nodes[highlightedPath[i + 1]];
            g2d.drawLine((int) n1.x, (int) n1.y, (int) n2.x, (int) n2.y);
        }
        g2d.setStroke(new BasicStroke(1.0f));
    }

    private void drawNodes(Graphics2D g2d) {
        Font labelFont = new Font("微软雅黑", Font.PLAIN, 10);
        for (int i = 0; i < graph.nodeCount; i++) {
            Node n = graph.nodes[i];
            double r = getNodeRadius(i);
            Color color = getNodeColor(i);

            // 绘制节点圆形
            g2d.setColor(color);
            g2d.fillOval((int) (n.x - r), (int) (n.y - r), (int) (r * 2), (int) (r * 2));

            g2d.setColor(color.darker());
            g2d.setStroke(new BasicStroke(2.0f));
            g2d.drawOval((int) (n.x - r), (int) (n.y - r), (int) (r * 2), (int) (r * 2));
            g2d.setStroke(new BasicStroke(1.0f));

            // 节点ID显示在圆心
            if (showNodeId) {
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("微软雅黑", Font.BOLD, 10));
                FontMetrics fmId = g2d.getFontMetrics();
                String idStr = String.valueOf(n.id);
                int idW = fmId.stringWidth(idStr);
                g2d.drawString(idStr, (int) (n.x - idW / 2), (int) (n.y + 4));
            }

            // 标签显示在节点下方，带半透明背景
            if (showLabels && showNodeNames) {
                g2d.setFont(labelFont);
                FontMetrics fm = g2d.getFontMetrics();
                String name = n.name;
                if (name.length() > 16) name = name.substring(0, 14) + "..";
                int tw = fm.stringWidth(name);
                int th = fm.getHeight();
                int lx = (int) (n.x - tw / 2) - 3;
                int ly = (int) (n.y + r + 2);

                // 半透明白色背景
                g2d.setColor(new Color(255, 255, 255, 200));
                g2d.fillRoundRect(lx, ly, tw + 6, th + 2, 4, 4);
                g2d.setColor(new Color(180, 180, 180));
                g2d.drawRoundRect(lx, ly, tw + 6, th + 2, 4, 4);

                // 文字
                g2d.setColor(new Color(40, 40, 40));
                g2d.drawString(name, lx + 3, ly + fm.getAscent() + 1);
            }
        }
    }

    private void drawLegend(Graphics2D g2d) {
        int x = 15;
        int y = 20;
        g2d.setColor(new Color(255, 255, 255, 220));
        g2d.fillRoundRect(x - 5, y - 18, 140, 100, 8, 8);
        g2d.setColor(new Color(200, 200, 200));
        g2d.drawRoundRect(x - 5, y - 18, 140, 100, 8, 8);

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 12));
        g2d.drawString("图例", x, y);
        y += 20;

        g2d.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        if (colorMode == 0) {
            drawLegendItem(g2d, x, y, new Color(239, 83, 80), "核心人物"); y += 18;
            drawLegendItem(g2d, x, y, new Color(255, 193, 7), "活跃人物"); y += 18;
            drawLegendItem(g2d, x, y, new Color(129, 199, 132), "边缘人物");
        } else if (colorMode == 1 && communityCount > 0) {
            for (int i = 0; i < Math.min(communityCount, 5); i++) {
                drawLegendItem(g2d, x, y, COMMUNITY_COLORS[i % COMMUNITY_COLORS.length], "社区 " + (i + 1));
                y += 18;
            }
            if (communityCount > 5) {
                g2d.setColor(Color.GRAY);
                g2d.drawString("等" + communityCount + "个社区", x + 18, y);
            }
        } else {
            drawLegendItem(g2d, x, y, new Color(66, 133, 244), "节点");
        }
    }

    private void drawLegendItem(Graphics2D g2d, int x, int y, Color color, String label) {
        g2d.setColor(color);
        g2d.fillOval(x, y - 8, 12, 12);
        g2d.setColor(Color.BLACK);
        g2d.drawString(label, x + 18, y + 2);
    }

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
    public void mouseClicked(MouseEvent e) {
    }

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

    public void zoomIn() {
        scale *= 1.2;
        repaint();
    }

    public void zoomOut() {
        scale /= 1.2;
        repaint();
    }

    public void resetView() {
        scale = 1.0;
        offsetX = 0;
        offsetY = 0;
        repaint();
    }
}
