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

    // ========== 连通分量颜色调色板 ==========
    //为不同连通分量预设不同的颜色
    private static final Color[] COMPONENT_COLORS = {
        new Color(255, 107, 107),  // 红色调
        new Color(78, 205, 196),   // 青色调
        new Color(255, 230, 109),  // 黄色调
        new Color(162, 155, 254),  // 紫色调
        new Color(93, 173, 226),   // 蓝色调
        new Color(253, 121, 168),  // 粉色调
        new Color(133, 193, 233),  // 浅蓝调
        new Color(171, 235, 198),  // 浅绿调
        new Color(245, 203, 167),  // 橙色调
        new Color(230, 176, 170),  // 浅红调
        new Color(200, 230, 201),  // 淡绿调
        new Color(174, 214, 241),  // 天蓝调
        new Color(249, 231, 159),  // 淡黄调
        new Color(215, 189, 226),  // 淡紫调
        new Color(171, 235, 198)   // 薄荷绿调
    };

    /**
     * 根据节点度数计算节点的显示半径
     * 度数越高的节点，显示的圆越大
     * 基础半径为12像素，每增加1度最多增加1像素，上限为额外16像素
     */
    static double getNodeRadius(GraphPanel panel, int nodeId) {
        //如果图数据为空，返回默认半径
        if (panel.graph == null) return 12;

        Node currentNode = panel.graph.nodes[nodeId];

        //基础半径为12像素
        int baseRadius = 12;
        //额外半径 = 度数（但不超过16），让高度数节点更大更显眼
        int extraRadius = Math.min(currentNode.degree, 16);
        //最终半径 = 基础半径 + 额外半径
        return baseRadius + extraRadius;
    }

    /**
     * 根据节点状态确定节点的显示颜色
     * 优先级：选中 > 悬停 > 高亮 > 着色模式（类型/社区/统一）
     */
    static Color getNodeColor(GraphPanel panel, int nodeId) {
        //如果图数据为空，返回灰色
        if (panel.graph == null) return Color.GRAY;

        Node currentNode = panel.graph.nodes[nodeId];

        //优先级1：选中的节点 → 紫色
        if (nodeId == panel.selectedNodeId) {
            return new Color(156, 39, 176);
        }

        //优先级2：鼠标悬停的节点 → 橙色
        if (nodeId == panel.hoverNodeId) {
            return new Color(255, 152, 0);
        }

        //优先级3：在高亮集合中的节点 → 紫色
        if (panel.highlightedNodes.contains(nodeId)) {
            return new Color(156, 39, 176);
        }

        //优先级4：根据着色模式决定颜色
        if (panel.colorMode == 0) {
            //模式0：按节点类型着色
            if ("核心".equals(currentNode.type)) {
                return new Color(239, 83, 80);   // 核心人物 → 红色
            }
            if ("活跃".equals(currentNode.type)) {
                return new Color(255, 193, 7);   // 活跃人物 → 黄色
            }
            return new Color(129, 199, 132);     // 边缘人物 → 绿色
        } else if (panel.colorMode == 1) {
            //模式1：按连通分量着色
            int componentId = panel.graph.vset[nodeId];

            //如果编号无效，返回灰色
            if (componentId < 0 || componentId >= COMPONENT_COLORS.length) {
                return Color.GRAY;
            }

            //使用连通分量编号对应的颜色
            return COMPONENT_COLORS[componentId % COMPONENT_COLORS.length];
        } else {
            //模式2：统一颜色 → 蓝色
            return new Color(66, 133, 244);
        }
    }

    // ==================== 主绘制入口 ====================
    /**
     * 主绘制方法：绘制整个图面板的所有内容
     * 绘制顺序：背景 → 边 → 高亮路径 → 节点 → 图例
     */
    static void paint(GraphPanel panel, Graphics g) {
        //绘制背景（填充面板背景色）
        g.setColor(panel.getBackground());
        g.fillRect(0, 0, panel.getWidth(), panel.getHeight());

        //如果图数据为空，在面板中央显示提示文字
        if (panel.graph == null || panel.graph.nodeCount == 0) {
            g.setColor(Color.GRAY);
            g.setFont(new Font("微软雅黑", Font.PLAIN, 16));
            //计算文字居中位置
            int centerX = panel.getWidth() / 2 - 60;
            int centerY = panel.getHeight() / 2;
            g.drawString("请加载数据集", centerX, centerY);
            return;
        }

        //转换为 Graphics2D 以支持高级绘图功能（抗锯齿、缩放等）
        Graphics2D g2d = (Graphics2D) g;

        //开启抗锯齿：让线条和圆形边缘更平滑
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //应用平移和缩放变换
        //先平移（offsetX, offsetY），再缩放（scale）
        g2d.translate(panel.offsetX, panel.offsetY);
        g2d.scale(panel.scale, panel.scale);

        //按顺序绘制图的各个组成部分
        drawEdges(panel, g2d);           // 先画边（在底层）
        drawHighlightedPath(panel, g2d); // 再画高亮路径（在边上面）
        drawNodes(panel, g2d);           // 最后画节点（在最上层）

        //恢复坐标变换（为绘制图例做准备）
        //图例需要固定在屏幕坐标上，不受缩放平移影响
        g2d.scale(1 / panel.scale, 1 / panel.scale);
        g2d.translate(-panel.offsetX, -panel.offsetY);

        //绘制图例（固定在屏幕左上角）
        drawLegend(panel, g2d);
    }

    // ==================== 绘制边 ====================
    /**
     * 绘制图中所有的边
     * 普通边：半透明灰色，粗细根据权重调整
     * 高亮边：紫色粗线
     */
    private static void drawEdges(GraphPanel panel, Graphics2D g2d) {
        //获取高亮节点集合
        Set<Integer> highlightedNodeSet = panel.highlightedNodes;

        //遍历所有的边
        for (int edgeIndex = 0; edgeIndex < panel.graph.edgeCount; edgeIndex++) {
            Edge currentEdge = panel.graph.edges[edgeIndex];

            //获取边两端的节点
            Node fromNode = panel.graph.nodes[currentEdge.from];
            Node toNode = panel.graph.nodes[currentEdge.to];

            //判断这条边是否需要高亮显示
            //条件：边本身被标记为highlight，或者两端节点都在高亮集合中
            boolean isEdgeHighlighted = currentEdge.highlight;

            boolean bothEndsHighlighted = highlightedNodeSet.contains(currentEdge.from)
                                        && highlightedNodeSet.contains(currentEdge.to);

            if (isEdgeHighlighted || bothEndsHighlighted) {
                //高亮边：紫色，粗线（3像素宽）
                g2d.setColor(new Color(156, 39, 176));
                g2d.setStroke(new BasicStroke(3.0f));
            } else {
                //普通边：根据权重计算透明度和线宽
                float alpha = 0.3f; //默认透明度30%

                if (currentEdge.weight > 0) {
                    //权重越高，透明度越高（越不透明）
                    //透明度范围：0.2 + 权重/100，最大0.7
                    alpha = Math.min(0.7f, (float) (0.2 + currentEdge.weight / 100.0));
                }

                //设置边的颜色（带透明度）
                int alphaInt = (int) (alpha * 255);
                g2d.setColor(new Color(150, 150, 150, alphaInt));

                //设置边的线宽：权重越高线越粗
                float lineWidth = 1.0f; //默认线宽1像素
                if (currentEdge.weight > 0) {
                    //线宽范围：0.5 + 权重/50，最大3像素
                    lineWidth = Math.min(3.0f, (float) (0.5 + currentEdge.weight / 50.0));
                }
                g2d.setStroke(new BasicStroke(lineWidth));
            }

            //绘制边：从 fromNode 到 toNode 画一条直线
            g2d.drawLine((int) fromNode.x, (int) fromNode.y, (int) toNode.x, (int) toNode.y);

            //如果开启了权重显示且权重不为默认值1.0，在边的中点显示权重数字
            if (panel.showWeights && currentEdge.weight > 0 && currentEdge.weight != 1.0) {
                //计算边的中点位置
                int midX = (int) ((fromNode.x + toNode.x) / 2);
                int midY = (int) ((fromNode.y + toNode.y) / 2);

                g2d.setColor(Color.DARK_GRAY);
                g2d.setFont(new Font("微软雅黑", Font.PLAIN, 10));

                //格式化权重为一位小数
                String weightText = String.format("%.1f", currentEdge.weight);
                g2d.drawString(weightText, midX, midY);
            }
        }

        //恢复默认线宽
        g2d.setStroke(new BasicStroke(1.0f));
    }

    // ==================== 绘制高亮路径 ====================
    /**
     * 绘制高亮路径（最短路径或N跳圈子）
     * 用紫色粗线连接路径上的相邻节点
     */
    private static void drawHighlightedPath(GraphPanel panel, Graphics2D g2d) {
        //如果路径长度小于2（只有0或1个节点），不需要画线
        if (panel.highlightedPath.length < 2) return;

        //设置路径绘制样式：紫色，粗线（4像素宽）
        g2d.setColor(new Color(156, 39, 176));
        g2d.setStroke(new BasicStroke(4.0f));

        //依次连接路径上的相邻节点
        for (int pathIndex = 0; pathIndex < panel.highlightedPath.length - 1; pathIndex++) {
            //获取当前节点和下一个节点
            int currentNodeId = panel.highlightedPath[pathIndex];
            int nextNodeId = panel.highlightedPath[pathIndex + 1];

            Node currentNode = panel.graph.nodes[currentNodeId];
            Node nextNode = panel.graph.nodes[nextNodeId];

            //画一条从当前节点到下一个节点的线
            g2d.drawLine((int) currentNode.x, (int) currentNode.y,
                        (int) nextNode.x, (int) nextNode.y);
        }

        //恢复默认线宽
        g2d.setStroke(new BasicStroke(1.0f));
    }

    // ==================== 绘制节点 ====================
    /**
     * 绘制图中所有的节点
     * 每个节点由圆形 + 可选ID + 可选名称标签组成
     */
    private static void drawNodes(GraphPanel panel, Graphics2D g2d) {
        //设置标签字体
        Font labelFont = new Font("微软雅黑", Font.PLAIN, 10);

        //遍历所有节点
        for (int nodeIndex = 0; nodeIndex < panel.graph.nodeCount; nodeIndex++) {
            Node currentNode = panel.graph.nodes[nodeIndex];

            //计算节点的半径和颜色
            double nodeRadius = getNodeRadius(panel, nodeIndex);
            Color nodeColor = getNodeColor(panel, nodeIndex);

            // ---- 绘制节点圆形填充 ----
            g2d.setColor(nodeColor);
            //计算圆形的左上角坐标
            int circleLeft = (int) (currentNode.x - nodeRadius);
            int circleTop = (int) (currentNode.y - nodeRadius);
            int circleDiameter = (int) (nodeRadius * 2);
            g2d.fillOval(circleLeft, circleTop, circleDiameter, circleDiameter);

            // ---- 绘制节点圆形边框 ----
            g2d.setColor(nodeColor.darker());  //边框颜色比填充稍深
            g2d.setStroke(new BasicStroke(2.0f));
            g2d.drawOval(circleLeft, circleTop, circleDiameter, circleDiameter);
            g2d.setStroke(new BasicStroke(1.0f)); //恢复默认线宽

            // ---- 绘制节点ID（在圆形中央） ----
            if (panel.showNodeId) {
                g2d.setColor(Color.WHITE);  //白色文字
                g2d.setFont(new Font("微软雅黑", Font.BOLD, 10));

                //获取字体度量信息，用于居中文字
                FontMetrics idMetrics = g2d.getFontMetrics();
                String idText = String.valueOf(currentNode.id);

                //计算文字宽度，使ID在节点中心居中
                int idTextWidth = idMetrics.stringWidth(idText);
                int idX = (int) (currentNode.x - idTextWidth / 2);
                int idY = (int) (currentNode.y + 4); //稍微偏下，视觉上更居中

                g2d.drawString(idText, idX, idY);
            }

            // ---- 绘制节点名称标签（在圆形下方） ----
            if (panel.showLabels && panel.showNodeNames) {
                g2d.setFont(labelFont);

                //获取字体度量信息
                FontMetrics nameMetrics = g2d.getFontMetrics();

                //如果名称太长，截断并添加省略号
                String displayName = currentNode.name;
                if (displayName.length() > 16) {
                    displayName = displayName.substring(0, 14) + "..";
                }

                //计算标签背景的尺寸
                int textWidth = nameMetrics.stringWidth(displayName);
                int textHeight = nameMetrics.getHeight();

                //计算标签背景的位置（节点下方）
                int labelX = (int) (currentNode.x - textWidth / 2) - 3;
                int labelY = (int) (currentNode.y + nodeRadius + 2);

                //绘制标签背景（半透明白色圆角矩形）
                g2d.setColor(new Color(255, 255, 255, 200));
                g2d.fillRoundRect(labelX, labelY, textWidth + 6, textHeight + 2, 4, 4);

                //绘制标签边框
                g2d.setColor(new Color(180, 180, 180));
                g2d.drawRoundRect(labelX, labelY, textWidth + 6, textHeight + 2, 4, 4);

                //绘制标签文字
                g2d.setColor(new Color(40, 40, 40));
                int textX = labelX + 3;
                int textY = labelY + nameMetrics.getAscent() + 1;
                g2d.drawString(displayName, textX, textY);
            }
        }
    }

    // ==================== 绘制图例 ====================
    /**
     * 绘制图例：固定在屏幕左上角，解释颜色含义
     * 根据当前着色模式显示不同的图例内容
     */
    private static void drawLegend(GraphPanel panel, Graphics2D g2d) {
        //图例位置（屏幕坐标，左上角）
        int legendX = 15;
        int legendY = 20;

        //图例背景尺寸
        int legendWidth = 140;
        int legendHeight = 100;

        //绘制图例背景（半透明白色圆角矩形）
        g2d.setColor(new Color(255, 255, 255, 220));
        g2d.fillRoundRect(legendX - 5, legendY - 18, legendWidth, legendHeight, 8, 8);

        //绘制图例边框
        g2d.setColor(new Color(200, 200, 200));
        g2d.drawRoundRect(legendX - 5, legendY - 18, legendWidth, legendHeight, 8, 8);

        //绘制图例标题
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 12));
        g2d.drawString("图例", legendX, legendY);
        legendY += 20;

        g2d.setFont(new Font("微软雅黑", Font.PLAIN, 11));

        //根据着色模式绘制不同的图例项
        if (panel.colorMode == 0) {
            //模式0：按节点类型着色
            drawLegendItem(g2d, legendX, legendY, new Color(239, 83, 80), "核心人物");
            legendY += 18;
            drawLegendItem(g2d, legendX, legendY, new Color(255, 193, 7), "活跃人物");
            legendY += 18;
            drawLegendItem(g2d, legendX, legendY, new Color(129, 199, 132), "边缘人物");
        } else if (panel.colorMode == 1 && panel.componentCount > 0) {
            //模式1：按连通分量着色，最多显示5个
            int maxDisplay = Math.min(panel.componentCount, 5);
            for (int componentIndex = 0; componentIndex < maxDisplay; componentIndex++) {
                Color componentColor = COMPONENT_COLORS[componentIndex % COMPONENT_COLORS.length];
                String componentLabel = "分量 " + (componentIndex + 1);
                drawLegendItem(g2d, legendX, legendY, componentColor, componentLabel);
                legendY += 18;
            }
            if (panel.componentCount > 5) {
                g2d.setColor(Color.GRAY);
                g2d.drawString("等" + panel.componentCount + "个连通分量", legendX + 18, legendY);
            }
        } else {
            //模式2：统一颜色
            drawLegendItem(g2d, legendX, legendY, new Color(66, 133, 244), "节点");
        }
    }

    /**
     * 绘制图例中的一项：一个小色块 + 说明文字
     */
    private static void drawLegendItem(Graphics2D g2d, int x, int y, Color color, String label) {
        //绘制色块（12×12像素的实心圆）
        g2d.setColor(color);
        g2d.fillOval(x, y - 8, 12, 12);

        //绘制说明文字
        g2d.setColor(Color.BLACK);
        g2d.drawString(label, x + 18, y + 2);
    }
}