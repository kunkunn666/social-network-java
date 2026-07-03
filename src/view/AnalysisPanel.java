package view;

import model.SocialGraph;
import model.Node;
import algorithm.CommunityDetection;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.Set;
import java.util.HashSet;

/**
 * 分析面板 — 度分析、连通分量检测
 * 度数分类（核心/活跃/边缘人物）、连通分量检测、查找节点的交往圈子并高亮
 */
public class AnalysisPanel extends JPanel {

    private MainFrame mainFrame;
    private GraphPanel graphPanel;
    private CommunityDetection communityDetection;

    private JTextArea degreeResultArea;
    private JLabel communityResultLabel;
    JComboBox<String> colorModeCombo;

    public AnalysisPanel(MainFrame mainFrame, GraphPanel graphPanel) {
        this.mainFrame = mainFrame;
        this.graphPanel = graphPanel;
        this.communityDetection = new CommunityDetection();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(createDegreePanel());
        add(Box.createVerticalStrut(8));
        add(createCommunityPanel());
    }

    public void setGraph(SocialGraph graph) {
        communityResultLabel.setText(" ");
        degreeResultArea.setText("");
    }

    // ==================== 度分析 ====================

    private JPanel createDegreePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(createTitledBorder("度分析模块"));

        JButton calcBtn = new JButton("计算度数分类");
        calcBtn.addActionListener(e -> calculateDegreeAnalysis());

        degreeResultArea = new JTextArea(6, 20);
        degreeResultArea.setEditable(false);
        degreeResultArea.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        JScrollPane scroll = new JScrollPane(degreeResultArea);

        panel.add(calcBtn, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    public void calculateDegreeAnalysis() {
        SocialGraph graph = graphPanel.graph;
        if (graph == null || graph.nodeCount == 0) {
            JOptionPane.showMessageDialog(this, "请先加载数据", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        graph.classifyNodes();
        graphPanel.setColorMode(0);
        colorModeCombo.setSelectedIndex(0);

        StringBuilder sb = new StringBuilder();
        StringBuilder core = new StringBuilder();
        StringBuilder active = new StringBuilder();
        StringBuilder edge = new StringBuilder();
        int coreCount = 0, activeCount = 0, edgeCount = 0;

        for (int i = 0; i < graph.nodeCount; i++) {
            Node n = graph.nodes[i];
            if ("核心".equals(n.type)) { core.append("  ").append(n.name).append("(").append(n.degree).append(")\n"); coreCount++; }
            else if ("活跃".equals(n.type)) { active.append("  ").append(n.name).append("(").append(n.degree).append(")\n"); activeCount++; }
            else { edge.append("  ").append(n.name).append("(").append(n.degree).append(")\n"); edgeCount++; }
        }

        sb.append("【核心人物】(").append(coreCount).append("个)\n").append(core).append("\n");
        sb.append("【活跃人物】(").append(activeCount).append("个)\n").append(active).append("\n");
        sb.append("【边缘人物】(").append(edgeCount).append("个)\n").append(edge);
        degreeResultArea.setText(sb.toString());
        degreeResultArea.setCaretPosition(0);
        graphPanel.repaint();
        mainFrame.updateStatus("度等级分类完成: 核心" + coreCount + "个, 活跃" + activeCount + "个, 边缘" + edgeCount + "个");
    }

    // ==================== 连通分量 ====================

    private JPanel createCommunityPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(createTitledBorder("连通分量检测"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1;
        gbc.gridwidth = 2;

        JButton detectBtn = new JButton("检测连通分量");
        detectBtn.addActionListener(e -> detectConnectedComponents());
        panel.add(detectBtn, gbc);

        gbc.gridy = 1;
        communityResultLabel = new JLabel(" ");
        communityResultLabel.setForeground(new Color(66, 133, 244));
        panel.add(communityResultLabel, gbc);

        gbc.gridy = 2;
        JButton findCircleBtn = new JButton("查找该节点的交往圈子");
        findCircleBtn.addActionListener(e -> findNodeCircle());
        panel.add(findCircleBtn, gbc);

        return panel;
    }

    private void detectConnectedComponents() {
        SocialGraph graph = graphPanel.graph;
        if (graph == null || graph.nodeCount == 0) return;
        int count = communityDetection.detectConnectedComponents(graph);
        communityResultLabel.setText("共" + count + "个连通分量");
        graphPanel.setCommunityCount(count);
        if (count > 1) {
            colorModeCombo.setSelectedIndex(1);
            graphPanel.setColorMode(1);
        }
        mainFrame.updateStatus("连通分量检测完成: " + count + "个连通分量");
    }

    private void findNodeCircle() {
        SocialGraph graph = graphPanel.graph;
        int selectedId = graphPanel.getSelectedNodeId();
        if (selectedId < 0) {
            JOptionPane.showMessageDialog(this, "请先在图中点击选择一个节点", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (graph.nodes[selectedId].community < 0) {
            communityDetection.detectConnectedComponents(graph);
        }
        int targetCommunity = graph.nodes[selectedId].community;
        Set<Integer> circleNodes = new HashSet<>();
        for (int i = 0; i < graph.nodeCount; i++) {
            if (graph.nodes[i].community == targetCommunity) {
                circleNodes.add(i);
            }
        }
        graphPanel.setHighlightedNodes(circleNodes);
        mainFrame.updateStatus("节点 " + graph.nodes[selectedId].name + " 的交往圈子: " + circleNodes.size() + "个节点");
    }

    private TitledBorder createTitledBorder(String title) {
        return BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            title, TitledBorder.LEFT, TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 12), new Color(60, 60, 60));
    }
}