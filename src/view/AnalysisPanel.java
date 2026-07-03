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
 * 分析面板 — 度分析、连通分量检测、删除节点、推荐好友
 * 度数分类（核心/活跃/边缘人物）、连通分量检测、查找节点的交往圈子并高亮、
 * 删除选中节点、推荐可能认识的人
 */
public class AnalysisPanel extends JPanel {

    private MainFrame mainFrame;
    private GraphPanel graphPanel;
    private CommunityDetection communityDetection;
    private ControlPanel controlPanel;

    private JTextArea degreeResultArea;
    private JLabel communityResultLabel;
    private JTextArea deleteResultArea;
    private JTextArea recommendResultArea;
    JComboBox<String> colorModeCombo;

    public AnalysisPanel(MainFrame mainFrame, GraphPanel graphPanel) {
        this.mainFrame = mainFrame;
        this.graphPanel = graphPanel;
        this.communityDetection = new CommunityDetection();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(createDegreePanel());
        add(Box.createVerticalStrut(8));
        add(createCommunityPanel());
        add(Box.createVerticalStrut(8));
        add(createDeletePanel());
        add(Box.createVerticalStrut(8));
        add(createRecommendPanel());
    }

    public void setControlPanel(ControlPanel cp) { this.controlPanel = cp; }

    public void setGraph(SocialGraph graph) {
        communityResultLabel.setText(" ");
        degreeResultArea.setText("");
        deleteResultArea.setText("");
        recommendResultArea.setText("");
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

    // ==================== 删除节点 ====================

    private JPanel createDeletePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(createTitledBorder("删除节点"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1;
        gbc.gridwidth = 2;

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 3, 0));
        JButton deleteBtn = new JButton("删除选中节点");
        deleteBtn.addActionListener(e -> deleteSelectedNode());
        JButton restoreBtn = new JButton("恢复原图");
        restoreBtn.addActionListener(e -> restoreGraph());
        btnPanel.add(deleteBtn);
        btnPanel.add(restoreBtn);
        panel.add(btnPanel, gbc);

        gbc.gridy = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1;
        deleteResultArea = new JTextArea(3, 10);
        deleteResultArea.setEditable(false);
        deleteResultArea.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        deleteResultArea.setBackground(new Color(248, 248, 248));
        JScrollPane scroll = new JScrollPane(deleteResultArea);
        panel.add(scroll, gbc);

        return panel;
    }

    private void deleteSelectedNode() {
        SocialGraph graph = graphPanel.graph;
        if (graph == null || graph.nodeCount == 0) return;
        int selectedId = graphPanel.getSelectedNodeId();
        if (selectedId < 0) {
            JOptionPane.showMessageDialog(this, "请先在图中点击选择一个节点", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int before = communityDetection.detectConnectedComponents(graph);
        String nodeName = graph.nodes[selectedId].name;

        graph.removeNode(selectedId);
        int after = communityDetection.detectConnectedComponents(graph);

        deleteResultArea.setText("已删除节点 " + nodeName + "\n" +
            "连通分量: " + before + " → " + after + "\n" +
            "剩余节点: " + graph.nodeCount + "  剩余边: " + graph.edgeCount);

        graphPanel.clearSelection();
        graphPanel.setGraph(graph);
        mainFrame.updateStatus("已删除节点 " + nodeName + "，连通分量由 " + before + " 变为 " + after);
    }

    private void restoreGraph() {
        if (controlPanel == null || controlPanel.currentFilePath == null) {
            JOptionPane.showMessageDialog(this, "未找到原始数据集路径", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        SocialGraph g = graphPanel.graph;
        if (g == null) return;
        g.loadFromCSV(controlPanel.currentFilePath);
        graphPanel.clearSelection();
        graphPanel.setGraph(g);
        deleteResultArea.setText("");
        mainFrame.updateStatus("已恢复原图");
    }

    // ==================== 推荐可能认识的人 ====================

    private JPanel createRecommendPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(createTitledBorder("推荐可能认识的人"));

        JButton recBtn = new JButton("推荐可能认识的人");
        recBtn.addActionListener(e -> recommendFriends());

        recommendResultArea = new JTextArea(5, 20);
        recommendResultArea.setEditable(false);
        recommendResultArea.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        JScrollPane scroll = new JScrollPane(recommendResultArea);

        panel.add(recBtn, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void recommendFriends() {
        SocialGraph graph = graphPanel.graph;
        if (graph == null || graph.nodeCount == 0) return;
        int selectedId = graphPanel.getSelectedNodeId();
        if (selectedId < 0) {
            JOptionPane.showMessageDialog(this, "请先在图中点击选择一个节点", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Node A = graph.nodes[selectedId];

        // 获取A的邻居集合
        boolean[] isNeighbor = new boolean[graph.nodeCount];
        int[] neighbors = graph.getNeighbors(selectedId);
        for (int nb : neighbors) isNeighbor[nb] = true;

        // 统计每个候选节点C（A的邻居的邻居）的共同邻居数
        int[] commonCount = new int[graph.nodeCount];
        for (int nb : neighbors) {
            int[] nbNeighbors = graph.getNeighbors(nb);
            for (int c : nbNeighbors) {
                if (c != selectedId && !isNeighbor[c]) {
                    commonCount[c]++;
                }
            }
        }

        // 收集候选节点并按共同邻居数排序
        int[] candidates = new int[graph.nodeCount];
        int candCount = 0;
        for (int i = 0; i < graph.nodeCount; i++) {
            if (commonCount[i] > 0) {
                candidates[candCount++] = i;
            }
        }

        // 冒泡排序
        for (int i = 0; i < candCount - 1; i++) {
            for (int j = 0; j < candCount - 1 - i; j++) {
                if (commonCount[candidates[j]] < commonCount[candidates[j + 1]]) {
                    int t = candidates[j]; candidates[j] = candidates[j + 1]; candidates[j + 1] = t;
                }
            }
        }

        int showCount = Math.min(candCount, 10);
        StringBuilder sb = new StringBuilder();
        sb.append("为 ").append(A.name).append(" 推荐可能认识的人:\n\n");
        if (candCount == 0) {
            sb.append("  暂无推荐");
        } else {
            for (int i = 0; i < showCount; i++) {
                int c = candidates[i];
                sb.append("  ").append(i + 1).append(". ").append(graph.nodes[c].name)
                  .append(" (共同好友: ").append(commonCount[c]).append(")\n");
            }
            if (candCount > 10) sb.append("  ... 共").append(candCount).append("人");
        }
        recommendResultArea.setText(sb.toString());
        mainFrame.updateStatus("为 " + A.name + " 推荐了 " + candCount + " 个可能认识的人");
    }

    private TitledBorder createTitledBorder(String title) {
        return BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            title, TitledBorder.LEFT, TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 12), new Color(60, 60, 60));
    }
}