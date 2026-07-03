package view;

import model.Edge;
import model.Node;

import java.awt.*;
import java.util.Set;

/**
 * 图渲染器 — 负责所有绘制逻辑
 * 绘制边（普通/高亮/权重）、节点（圆+ID+标签）、高亮路径、图例
 */
class GraphRenderer {

    private static final Color[] COMMUNITY_COLORS = {
        new Color(255, 107, 107), new Color(78, 205, 196), new Color(255, 230, 109),
        new Color(162, 155, 254), new Color(93, 173, 226), new Color(253, 121, 168),
        new Color(133, 193, 233), new Color(171, 235, 198), new Color(245, 203, 167),
        new Color(230, 176, 170), new Color(200, 230, 201), new Color(174, 214, 241),
        new Color(249, 231, 159), new Color(215, 189, 226), new Color(171, 235, 198)
    };

    /** 根据节点度数计算半径 */
    static double getNodeRadius(GraphPanel panel, int nodeId) {
        if (panel.graph == null) return 12;
        Node n = panel.graph.nodes[nodeId];
        int base = 12;
        int extra = Math.min(n.degree, 16);
        return base + extra;
    }

    /** 根据节点状态返回颜色 */
    static Color getNodeColor(GraphPanel panel, int nodeId) {
        if (panel.graph == null) return Color.GRAY;
        Node n = panel.graph.nodes[nodeId];
        if (nodeId == panel.selectedNodeId) return new Color(156, 39, 176);
        if (nodeId == panel.hoverNodeId) return new Color(255, 152, 0);
        if (panel.highlightedNodes.contains(nodeId)) return new Color(156, 39, 176);

        if (panel.colorMode == 0) {
            if ("核心".equals(n.type)) return new Color(239, 83, 80);
            if ("活跃".equals(n.type)) return new Color(255, 193, 7);
            return new Color(129, 199, 132);
        } else if (panel.colorMode == 1) {
            int comm = n.community;
            if (comm < 0 || comm >= COMMUNITY_COLORS.length) return Color.GRAY;
            return COMMUNITY_COLORS[comm % COMMUNITY_COLORS.length];
        } else {
            return new Color(66, 133, 244);
        }
    }

    /** 主绘制入口 */
    static void paint(GraphPanel panel, Graphics g) {
        g.setColor(panel.getBackground());
        g.fillRect(0, 0, panel.getWidth(), panel.getHeight());

        if (panel.graph == null || panel.graph.nodeCount == 0) {
            g.setColor(Color.GRAY);
            g.setFont(new Font("微软雅黑", Font.PLAIN, 16));
            g.drawString("请加载数据集", panel.getWidth() / 2 - 60, panel.getHeight() / 2);
            return;
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.translate(panel.offsetX, panel.offsetY);
        g2d.scale(panel.scale, panel.scale);

        drawEdges(panel, g2d);
        drawHighlightedPath(panel, g2d);
        drawNodes(panel, g2d);

        g2d.scale(1 / panel.scale, 1 / panel.scale);
        g2d.translate(-panel.offsetX, -panel.offsetY);

        drawLegend(panel, g2d);
    }

    private static void drawEdges(GraphPanel panel, Graphics2D g2d) {
        Set<Integer> hl = panel.highlightedNodes;
        for (int i = 0; i < panel.graph.edgeCount; i++) {
            Edge e = panel.graph.edges[i];
            Node n1 = panel.graph.nodes[e.from];
            Node n2 = panel.graph.nodes[e.to];

            if (e.highlight || (hl.contains(e.from) && hl.contains(e.to))) {
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

            if (panel.showWeights && e.weight > 0 && e.weight != 1.0) {
                int mx = (int) ((n1.x + n2.x) / 2);
                int my = (int) ((n1.y + n2.y) / 2);
                g2d.setColor(Color.DARK_GRAY);
                g2d.setFont(new Font("微软雅黑", Font.PLAIN, 10));
                g2d.drawString(String.format("%.1f", e.weight), mx, my);
            }
        }
        g2d.setStroke(new BasicStroke(1.0f));
    }

    private static void drawHighlightedPath(GraphPanel panel, Graphics2D g2d) {
        if (panel.highlightedPath.length < 2) return;
        g2d.setColor(new Color(156, 39, 176));
        g2d.setStroke(new BasicStroke(4.0f));
        for (int i = 0; i < panel.highlightedPath.length - 1; i++) {
            Node n1 = panel.graph.nodes[panel.highlightedPath[i]];
            Node n2 = panel.graph.nodes[panel.highlightedPath[i + 1]];
            g2d.drawLine((int) n1.x, (int) n1.y, (int) n2.x, (int) n2.y);
        }
        g2d.setStroke(new BasicStroke(1.0f));
    }

    private static void drawNodes(GraphPanel panel, Graphics2D g2d) {
        Font labelFont = new Font("微软雅黑", Font.PLAIN, 10);
        for (int i = 0; i < panel.graph.nodeCount; i++) {
            Node n = panel.graph.nodes[i];
            double r = getNodeRadius(panel, i);
            Color color = getNodeColor(panel, i);

            g2d.setColor(color);
            g2d.fillOval((int) (n.x - r), (int) (n.y - r), (int) (r * 2), (int) (r * 2));

            g2d.setColor(color.darker());
            g2d.setStroke(new BasicStroke(2.0f));
            g2d.drawOval((int) (n.x - r), (int) (n.y - r), (int) (r * 2), (int) (r * 2));
            g2d.setStroke(new BasicStroke(1.0f));

            if (panel.showNodeId) {
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("微软雅黑", Font.BOLD, 10));
                FontMetrics fmId = g2d.getFontMetrics();
                String idStr = String.valueOf(n.id);
                int idW = fmId.stringWidth(idStr);
                g2d.drawString(idStr, (int) (n.x - idW / 2), (int) (n.y + 4));
            }

            if (panel.showLabels && panel.showNodeNames) {
                g2d.setFont(labelFont);
                FontMetrics fm = g2d.getFontMetrics();
                String name = n.name;
                if (name.length() > 16) name = name.substring(0, 14) + "..";
                int tw = fm.stringWidth(name);
                int th = fm.getHeight();
                int lx = (int) (n.x - tw / 2) - 3;
                int ly = (int) (n.y + r + 2);

                g2d.setColor(new Color(255, 255, 255, 200));
                g2d.fillRoundRect(lx, ly, tw + 6, th + 2, 4, 4);
                g2d.setColor(new Color(180, 180, 180));
                g2d.drawRoundRect(lx, ly, tw + 6, th + 2, 4, 4);
                g2d.setColor(new Color(40, 40, 40));
                g2d.drawString(name, lx + 3, ly + fm.getAscent() + 1);
            }
        }
    }

    private static void drawLegend(GraphPanel panel, Graphics2D g2d) {
        int x = 15, y = 20;
        g2d.setColor(new Color(255, 255, 255, 220));
        g2d.fillRoundRect(x - 5, y - 18, 140, 100, 8, 8);
        g2d.setColor(new Color(200, 200, 200));
        g2d.drawRoundRect(x - 5, y - 18, 140, 100, 8, 8);

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 12));
        g2d.drawString("图例", x, y);
        y += 20;
        g2d.setFont(new Font("微软雅黑", Font.PLAIN, 11));

        if (panel.colorMode == 0) {
            drawLegendItem(g2d, x, y, new Color(239, 83, 80), "核心人物"); y += 18;
            drawLegendItem(g2d, x, y, new Color(255, 193, 7), "活跃人物"); y += 18;
            drawLegendItem(g2d, x, y, new Color(129, 199, 132), "边缘人物");
        } else if (panel.colorMode == 1 && panel.communityCount > 0) {
            for (int i = 0; i < Math.min(panel.communityCount, 5); i++) {
                drawLegendItem(g2d, x, y, COMMUNITY_COLORS[i % COMMUNITY_COLORS.length], "社区 " + (i + 1));
                y += 18;
            }
            if (panel.communityCount > 5) {
                g2d.setColor(Color.GRAY);
                g2d.drawString("等" + panel.communityCount + "个社区", x + 18, y);
            }
        } else {
            drawLegendItem(g2d, x, y, new Color(66, 133, 244), "节点");
        }
    }

    private static void drawLegendItem(Graphics2D g2d, int x, int y, Color color, String label) {
        g2d.setColor(color);
        g2d.fillOval(x, y - 8, 12, 12);
        g2d.setColor(Color.BLACK);
        g2d.drawString(label, x + 18, y + 2);
    }
}