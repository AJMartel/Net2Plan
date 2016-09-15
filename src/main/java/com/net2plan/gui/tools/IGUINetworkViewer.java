/*******************************************************************************
 * Copyright (c) 2015 Pablo Pavon Mariño.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Contributors:
 * Pablo Pavon Mariño - initial API and implementation
 ******************************************************************************/


package com.net2plan.gui.tools;

import java.util.Comparator;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import com.net2plan.internal.plugins.IGUIModule;

/**
 * Template for any tool requiring network visualization.
 *
 * @author Pablo Pavon-Marino, Jose-Luis Izquierdo-Zaragoza
 * @since 0.3.0
 */
public abstract class IGUINetworkViewer //extends IGUIModule implements INetworkCallback //, ThreadExecutionController.IThreadExecutionHandler 
{
//	public static Color COLOR_INITIALNODE = new Color(0, 153, 51);
//    public static Color COLOR_ENDNODE = new Color(0, 162, 215);

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Reference to the topology panel.
     *
     * @since 0.3.0
     */
//    protected TopologyPanel topologyPanel;

    /**
     * Reference to the popup menu in the topology panel.
     *
     * @since 0.3.0
     */
//    protected ITopologyCanvasPlugin popupPlugin;

//    private JPanel leftPane;
//    private ViewReportPane reportPane;
//    private JTabbedPane rightPane;
//    private ViewEditTopologyTablesPane viewEditTopTables;
////    private JTabbedPane netPlanView;
////    private JTabbedPane reportContainer;
////    private RunnableSelector reportSelector;
////    private ThreadExecutionController reportController;
//    private int viewNetPlanTabIndex;
////    private JButton closeAllReports;
//
////    private Map<NetworkElementType, AdvancedJTableNetworkElement> netPlanViewTable;
////    private Map<NetworkElementType, FixedColumnDecorator> netPlanViewTableDecorator;
////    private Map<NetworkElementType, JComponent> netPlanViewTableComponent;
////    private JCheckBox showInitialPlan;
//
//    public boolean allowDocumentUpdate;
//    private NetPlan currentNetPlan, initialNetPlan;

//    /**
//     * Constructor that allows set a title for the tool in the top section of the panel.
//     *
//     * @param title Title of the tool (null or empty means no title)
//     * @since 0.3.0
//     */
//    public IGUINetworkViewer(String title) {
//        super(title);
//    }

//    @Override
//    public long addLink(long originNode, long destinationNode) {
//        long layer = getDesign().getNetworkLayerDefault().getId();
//        return addLink(layer, originNode, destinationNode);
//    }

//    @Override
//    public long addLink(long layer, long originNode, long destinationNode) {
//        if (!isEditable()) throw new UnsupportedOperationException("Not supported");
//
//        NetPlan netPlan = getDesign();
//        Link link = netPlan.addLink(netPlan.getNodeFromId(originNode), netPlan.getNodeFromId(destinationNode), 0, 0, 200000, null, netPlan.getNetworkLayerFromId(layer));
//
//        if (layer == netPlan.getNetworkLayerDefault().getId()) {
//            getTopologyPanel().getCanvas().addLink(link);
//            getTopologyPanel().getCanvas().refresh();
//        }
//
//        updateNetPlanView();
//        return link.getId();
//    }
//
//    @Override
//    public Pair<Long, Long> addLinkBidirectional(long originNode, long destinationNode) {
//        return addLinkBidirectional(getDesign().getNetworkLayerDefault().getId(), originNode, destinationNode);
//    }
//
//    @Override
//    public Pair<Long, Long> addLinkBidirectional(long layer, long originNode, long destinationNode) {
//        if (!isEditable()) throw new UnsupportedOperationException("Not supported");
//
//        NetPlan netPlan = getDesign();
//        Pair<Link, Link> links = netPlan.addLinkBidirectional(netPlan.getNodeFromId(originNode), netPlan.getNodeFromId(destinationNode), 0, 0, 200000, null, netPlan.getNetworkLayerFromId(layer));
//        if (layer == netPlan.getNetworkLayerDefault().getId()) {
//            getTopologyPanel().getCanvas().addLink(links.getFirst());
//            getTopologyPanel().getCanvas().addLink(links.getSecond());
//            getTopologyPanel().getCanvas().refresh();
//        }
//
//        updateNetPlanView();
//        return Pair.of(links.getFirst().getId(), links.getSecond().getId());
//    }
//
//    @Override
//    public void addNode(Point2D pos) {
//        if (!isEditable()) throw new UnsupportedOperationException("Not supported");
//
//        NetPlan netPlan = getDesign();
//        long nodeId = netPlan.getNetworkElementNextId();
//        Node node = netPlan.addNode(pos.getX(), pos.getY(), "Node " + nodeId, null);
//        getTopologyPanel().getCanvas().addNode(node);
//        getTopologyPanel().getCanvas().refresh();
//        updateNetPlanView();
//    }

//    @Override
//    public void configure(JPanel contentPane) {
////        topologyPanel = new TopologyPanel(this, JUNGCanvas.class);
////
////        leftPane = new JPanel(new BorderLayout());
////        JPanel logSection = configureLeftBottomPanel();
////        if (logSection == null) {
////            leftPane.add(topologyPanel, BorderLayout.CENTER);
////        } else {
////            JSplitPane splitPaneTopology = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
////            splitPaneTopology.setTopComponent(topologyPanel);
////            splitPaneTopology.setBottomComponent(logSection);
////            splitPaneTopology.setResizeWeight(0.8);
////            splitPaneTopology.addPropertyChangeListener(new ProportionalResizeJSplitPaneListener());
////            splitPaneTopology.setBorder(new LineBorder(contentPane.getBackground()));
////            leftPane.add(splitPaneTopology, BorderLayout.CENTER);
////        }
////
////        rightPane = new JTabbedPane();
////
////        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
////        splitPane.setLeftComponent(leftPane);
////        splitPane.setRightComponent(rightPane);
////        splitPane.setResizeWeight(0.5);
////        splitPane.addPropertyChangeListener(new ProportionalResizeJSplitPaneListener());
////
////        splitPane.setBorder(BorderFactory.createEmptyBorder());
////        contentPane.add(splitPane, "grow");
////
////
////        viewEditTopTables = new ViewEditTopologyTablesPane((GUINetworkDesign) this , new BorderLayout());
////        addTab(isEditable() ? "View/edit network state" : "View network state", viewEditTopTables);
////        viewNetPlanTabIndex = 0;
////        
////        reportPane = new ViewReportPane((GUINetworkDesign) this , JSplitPane.VERTICAL_SPLIT);
////        addTab("View reports", reportPane);
////        
////        loadDesign(new NetPlan());
////
////        addKeyCombinationAction("Resets the tool", new AbstractAction() {
////            @Override
////            public void actionPerformed(ActionEvent e) {
////                reset();
////            }
////        }, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
////
////        addKeyCombinationAction("Outputs current design to console", new AbstractAction() {
////            @Override
////            public void actionPerformed(ActionEvent e) {
////                System.out.println(getDesign().toString());
////            }
////        }, KeyStroke.getKeyStroke(KeyEvent.VK_F11, InputEvent.CTRL_DOWN_MASK));
////
////        for (int tabId = 0; tabId <= 8; tabId++) {
////            final int key;
////            switch (tabId) {
////                case 0:
////                    key = KeyEvent.VK_1;
////                    break;
////
////                case 1:
////                    key = KeyEvent.VK_2;
////                    break;
////
////                case 2:
////                    key = KeyEvent.VK_3;
////                    break;
////
////                case 3:
////                    key = KeyEvent.VK_4;
////                    break;
////
////                case 4:
////                    key = KeyEvent.VK_5;
////                    break;
////
////                case 5:
////                    key = KeyEvent.VK_6;
////                    break;
////
////                case 6:
////                    key = KeyEvent.VK_7;
////                    break;
////
////                case 7:
////                    key = KeyEvent.VK_8;
////                    break;
////
////                case 8:
////                    key = KeyEvent.VK_9;
////                    break;
////
////                default:
////                    throw new RuntimeException("Bad");
////            }
////
////            addKeyCombinationAction("Open right tab " + tabId, new SwitchTabAction(tabId), KeyStroke.getKeyStroke(key, InputEvent.CTRL_DOWN_MASK));
////        }
//    }

//    @Override
//    public Object execute(ThreadExecutionController controller) {
////        if (controller == reportController) {
////            Triple<File, String, Class> report = reportSelector.getRunnable();
////            Map<String, String> reportParameters = reportSelector.getRunnableParameters();
////            Map<String, String> net2planParameters = Configuration.getNet2PlanOptions();
////            IReport instance = ClassLoaderUtils.getInstance(report.getFirst(), report.getSecond(), IReport.class);
////            String title = null;
////            try {
////                title = instance.getTitle();
////            } catch (UnsupportedOperationException ex) {
////            }
////            if (title == null) title = "Untitled";
////
////            Pair<String, ? extends JPanel> aux = Pair.of(title, new ReportBrowser(instance.executeReport(getDesign().copy(), reportParameters, net2planParameters)));
////            try {
////                ((Closeable) instance.getClass().getClassLoader()).close();
////            } catch (Throwable e) {
////            }
////
////            return aux;
////        } else {
//            throw new RuntimeException("Bad");
////        }
//    }

//    @Override
//    public void executionFailed(ThreadExecutionController controller) {
////        if (controller == reportController) {
////            ErrorHandling.showErrorDialog("Error executing report");
////        } else {
//            ErrorHandling.showErrorDialog("Bad");
////        }
//    }

//    @Override
//    public void executionFinished(ThreadExecutionController controller, Object out) {
////        if (controller == reportController) {
////            Pair<String, ? extends JPanel> aux = (Pair<String, ? extends JPanel>) out;
////            reportContainer.addTab(aux.getFirst(), new TabIcon(TabIcon.IconType.TIMES_SIGN), aux.getSecond());
////            reportContainer.setSelectedIndex(reportContainer.getTabCount() - 1);
////        } else {
//            ErrorHandling.showErrorDialog("Bad");
////        }
//    }

//    @Override
//    public NetPlan getDesign() {
//        return currentNetPlan;
//    }
//
//    @Override
//    public NetPlan getInitialDesign() {
//        return initialNetPlan;
//    }

//    @Override
//    public List<JComponent> getCanvasActions(Point2D pos) {
//        List<JComponent> actions = new LinkedList<JComponent>();
//
//        if (isEditable())
//            actions.add(new JMenuItem(new AddNodeAction("Add node here", pos)));
//
//        return actions;
//    }
//
//    @Override
//    public List<JComponent> getLinkActions(long link, Point2D pos) {
//        List<JComponent> actions = new LinkedList<JComponent>();
//
//        if (isEditable())
//            actions.add(new JMenuItem(new RemoveLinkAction("Remove link", link)));
//
//        return actions;
//    }
//
//    @Override
//    public List<JComponent> getNodeActions(long nodeId, Point2D pos) {
//        List<JComponent> actions = new LinkedList<JComponent>();
//
//        if (isEditable()) {
//            actions.add(new JMenuItem(new RemoveNodeAction("Remove node", nodeId)));
//
//            NetPlan netPlan = getDesign();
//            Node node = netPlan.getNodeFromId(nodeId);
//            if (netPlan.getNumberOfNodes() > 1) {
//                actions.add(new JPopupMenu.Separator());
//                JMenu unidirectionalMenu = new JMenu("Create unidirectional link");
//                JMenu bidirectionalMenu = new JMenu("Create bidirectional link");
//
//                String nodeName = node.getName() == null ? "" : node.getName();
//                String nodeString = Long.toString(nodeId) + (nodeName.isEmpty() ? "" : " (" + nodeName + ")");
//
//                long layer = netPlan.getNetworkLayerDefault().getId();
//                for (Node auxNode : netPlan.getNodes()) {
//                    if (auxNode.equals(nodeId)) continue;
//
//                    String auxNodeName = auxNode.getName() == null ? "" : auxNode.getName();
//                    String auxNodeString = Long.toString(auxNode.getId()) + (auxNodeName.isEmpty() ? "" : " (" + auxNodeName + ")");
//
//                    AbstractAction unidirectionalAction = new AddLinkAction(nodeString + " => " + auxNodeString, layer, nodeId, auxNode.getId());
//                    unidirectionalMenu.add(unidirectionalAction);
//
//                    AbstractAction bidirectionalAction = new AddLinkBidirectionalAction(nodeString + " <=> " + auxNodeString, layer, nodeId, auxNode.getId());
//                    bidirectionalMenu.add(bidirectionalAction);
//                }
//
//                actions.add(unidirectionalMenu);
//                actions.add(bidirectionalMenu);
//            }
//        }
//
//        return actions;
//    }
//
//    @Override
//    public boolean isEditable() {
//        return false;
//    }
//
//    @Override
//    public void layerChanged(long layer) {
//    }
//
//    @Override
//    public void loadDesign(NetPlan netPlan) {
//        netPlan.checkCachesConsistency();
//        setNetPlan(netPlan);
//        netPlan.checkCachesConsistency();
//        topologyPanel.updateLayerChooser();
//        topologyPanel.getCanvas().zoomAll();
//        resetView();
//    }
//
//    @Override
//    public void loadTrafficDemands(NetPlan demands) {
//        if (!demands.hasDemands() && !demands.hasMulticastDemands())
//            throw new Net2PlanException("Selected file doesn't contain a demand set");
//
//        NetPlan netPlan = getDesign();
//        if (netPlan.hasDemands() || netPlan.hasMulticastDemands()) {
//            int result = JOptionPane.showConfirmDialog(null, "Current network structure contains a demand set. Overwrite?", "Loading demand set", JOptionPane.YES_NO_OPTION);
//            if (result != JOptionPane.YES_OPTION) return;
//        }
//
//        NetPlan aux_netPlan = netPlan.copy();
//        try {
//            netPlan.removeAllDemands();
//            for (Demand demand : demands.getDemands())
//                netPlan.addDemand(netPlan.getNode(demand.getIngressNode().getIndex()), netPlan.getNode(demand.getEgressNode().getIndex()), demand.getOfferedTraffic(), demand.getAttributes());
//
//            netPlan.removeAllMulticastDemands();
//            for (MulticastDemand demand : demands.getMulticastDemands()) {
//                Set<Node> egressNodesThisNetPlan = new HashSet<Node>();
//                for (Node n : demand.getEgressNodes()) egressNodesThisNetPlan.add(netPlan.getNode(n.getIndex()));
//                netPlan.addMulticastDemand(netPlan.getNode(demand.getIngressNode().getIndex()), egressNodesThisNetPlan, demand.getOfferedTraffic(), demand.getAttributes());
//            }
//
//            updateNetPlanView();
//        } catch (Throwable ex) {
//            getDesign().assignFrom(aux_netPlan);
//            throw new RuntimeException(ex);
//        }
//    }
//
//    @Override
//    public void moveNode(long node, Point2D pos) {
//        if (!isEditable()) throw new UnsupportedOperationException("Not supported");
//
//        TableModel nodeTableModel = viewEditTopTables.getNetPlanViewTable().get(NetworkElementType.NODE).getModel();
//        int numRows = nodeTableModel.getRowCount();
//        for (int row = 0; row < numRows; row++) {
//            if ((long) nodeTableModel.getValueAt(row, 0) == node) {
//                nodeTableModel.setValueAt(pos.getX(), row, AdvancedJTable_node.COLUMN_XCOORD);
//                nodeTableModel.setValueAt(pos.getY(), row, AdvancedJTable_node.COLUMN_YCOORD);
//            }
//        }
//    }
//
//    @Override
//    public void removeLink(long link) {
//        if (!isEditable()) throw new UnsupportedOperationException("Not supported");
//
//        NetPlan netPlan = getDesign();
//        if (netPlan.getLinkFromId(link).getLayer().equals(getDesign().getNetworkLayerDefault())) {
//            getTopologyPanel().getCanvas().removeLink(netPlan.getLinkFromId(link));
//            getTopologyPanel().getCanvas().refresh();
//        }
//        netPlan.getLinkFromId(link).remove();
//
//        updateNetPlanView();
//    }
//
//    @Override
//    public void removeNode(long node) {
//        if (!isEditable()) throw new UnsupportedOperationException("Not supported");
//
//        NetPlan netPlan = getDesign();
//        getTopologyPanel().getCanvas().removeNode(netPlan.getNodeFromId(node));
//        getTopologyPanel().getCanvas().refresh();
//        netPlan.getNodeFromId(node).remove();
//        updateNetPlanView();
//    }
//
//    @Override
//    public void reset() {
//        try {
//            boolean reset = askForReset();
//            if (!reset) return;
//
//            reset_internal();
////            reportSelector.reset();
////            reportContainer.removeAll();
//        } catch (Throwable ex) {
//            ErrorHandling.addErrorOrException(ex, IGUINetworkViewer.class);
//            ErrorHandling.showErrorDialog("Unable to reset");
//        }
//    }
//
//    @Override
//    public void resetView() {
//        topologyPanel.getCanvas().resetPickedAndUserDefinedColorState();
//        for (Entry<NetworkElementType, AdvancedJTableNetworkElement> entry : viewEditTopTables.getNetPlanViewTable().entrySet()) {
//            switch (entry.getKey()) {
//                case DEMAND:
//                    clearDemandSelection();
//                    break;
//
//                case MULTICAST_DEMAND:
//                    clearMulticastDemandSelection();
//                    break;
//
//                case FORWARDING_RULE:
//                    clearForwardingRuleSelection();
//                    break;
//
//                case LINK:
//                    clearLinkSelection();
//                    break;
//
//                case NODE:
//                    clearNodeSelection();
//                    break;
//
//                case PROTECTION_SEGMENT:
//                    clearProtectionSegmentSelection();
//                    break;
//
//                case ROUTE:
//                    clearRouteSelection();
//                    break;
//
//                case MULTICAST_TREE:
//                    clearMulticastTreeSelection();
//                    break;
//
//                case SRG:
//                    clearSRGSelection();
//                    break;
//
//                default:
//                    break;
//            }
//        }
//    }
//
//    public void showDemand(long demandId) {
//        NetPlan netPlan = getDesign();
//        NetworkLayer layer = netPlan.getDemandFromId(demandId).getLayer();
//        selectNetPlanViewItem(layer.getId(), NetworkElementType.DEMAND, demandId);
//        Demand demand = netPlan.getDemandFromId(demandId);
//
//        Map<Node, Color> nodes = new HashMap<Node, Color>();
//        nodes.put(demand.getIngressNode(), COLOR_INITIALNODE);
//        nodes.put(demand.getEgressNode(), COLOR_ENDNODE);
//        Map<Link, Pair<Color, Boolean>> links = new HashMap<Link, Pair<Color, Boolean>>();
//
//        DoubleMatrix1D x_e = netPlan.getMatrixDemand2LinkTrafficCarried(layer).viewRow(demand.getIndex()).copy();
//        for (int e = 0; e < x_e.size(); e++)
//            if (x_e.get(e) > 0) {
//                links.put(netPlan.getLink(e, layer), Pair.of(Color.BLUE, false));
//            }
//        topologyPanel.getCanvas().showAndPickNodesAndLinks(nodes, links);
//        topologyPanel.getCanvas().refresh();
//    }
//
//    @Override
//    public void showMulticastDemand(long demandId) {
//        NetPlan netPlan = getDesign();
//        MulticastDemand demand = netPlan.getMulticastDemandFromId(demandId);
//        NetworkLayer layer = demand.getLayer();
//        selectNetPlanViewItem(layer.getId(), NetworkElementType.MULTICAST_DEMAND, demandId);
//
//        Map<Node, Color> nodes = new HashMap<Node, Color>();
//        nodes.put(demand.getIngressNode(), COLOR_INITIALNODE);
//        for (Node n : demand.getEgressNodes()) nodes.put(n, COLOR_ENDNODE);
//        Map<Link, Pair<Color, Boolean>> links = new HashMap<Link, Pair<Color, Boolean>>();
//
//        DoubleMatrix1D x_e = netPlan.getMatrixMulticastDemand2LinkTrafficCarried(layer).viewRow(demand.getIndex()).copy();
//        for (int e = 0; e < x_e.size(); e++)
//            if (x_e.get(e) > 0) links.put(netPlan.getLinkFromId(e), Pair.of(Color.BLUE, false));
//        topologyPanel.getCanvas().showAndPickNodesAndLinks(nodes, links);
//        topologyPanel.getCanvas().refresh();
//    }
//
//    @Override
//    public void showForwardingRule(Pair<Integer, Integer> demandLink) {
//        NetPlan netPlan = getDesign();
//        Demand demand = netPlan.getDemand(demandLink.getFirst());
//        Link link = netPlan.getLink(demandLink.getSecond());
//        NetworkLayer layer = demand.getLayer();
//        selectNetPlanViewItem(layer.getId(), NetworkElementType.FORWARDING_RULE, Pair.of(demand.getIndex(), link.getIndex()));
//
//        Map<Node, Color> nodes = new HashMap<Node, Color>();
//        nodes.put(demand.getIngressNode(), COLOR_INITIALNODE);
//        nodes.put(demand.getEgressNode(), COLOR_ENDNODE);
//        Map<Link, Pair<Color, Boolean>> links = new HashMap<Link, Pair<Color, Boolean>>();
//        links.put(link, Pair.of(Color.BLUE, false));
//        topologyPanel.getCanvas().showAndPickNodesAndLinks(nodes, links);
//        topologyPanel.getCanvas().refresh();
//    }
//
//    @Override
//    public void showLink(long linkId) {
//        NetPlan netPlan = getDesign();
//        Link link = netPlan.getLinkFromId(linkId);
//        selectNetPlanViewItem(link.getLayer().getId(), NetworkElementType.LINK, linkId);
//
//        topologyPanel.getCanvas().showNode(link.getOriginNode(), COLOR_INITIALNODE);
//        topologyPanel.getCanvas().showNode(link.getDestinationNode(), COLOR_ENDNODE);
//
//        topologyPanel.getCanvas().showLink(link, link.isUp() ? Color.BLUE : Color.RED, false);
//        topologyPanel.getCanvas().refresh();
//    }
//
//    @Override
//    public void showNode(long nodeId) {
//        selectNetPlanViewItem(getDesign().getNetworkLayerDefault().getId(), NetworkElementType.NODE, nodeId);
//
//        topologyPanel.getCanvas().showNode(getDesign().getNodeFromId(nodeId), Color.BLUE);
//        topologyPanel.getCanvas().refresh();
//    }
//
//    @Override
//    public void showProtectionSegment(long segmentId) {
//        NetPlan netPlan = getDesign();
//        ProtectionSegment segment = netPlan.getProtectionSegmentFromId(segmentId);
//        selectNetPlanViewItem(segment.getLayer().getId(), NetworkElementType.PROTECTION_SEGMENT, segmentId);
//        Map<Link, Pair<Color, Boolean>> res = new HashMap<Link, Pair<Color, Boolean>>();
//        for (Link e : segment.getSeqLinks()) res.put(e, Pair.of(Color.YELLOW, false));
//        topologyPanel.getCanvas().showAndPickNodesAndLinks(null, res);
//        topologyPanel.getCanvas().refresh();
//    }
//
//    @Override
//    public void showRoute(long routeId) // yellow segment link not used, orange segment link used, blue not segment link used. The same for initial state, in dashed
//    {
//        NetPlan netPlan = getDesign();
//        Route route = netPlan.getRouteFromId(routeId);
//        NetworkLayer layer = route.getLayer();
//        selectNetPlanViewItem(layer.getId(), NetworkElementType.ROUTE, routeId);
//
//        NetPlan initialState = getInitialDesign();
//        Map<Link, Pair<Color, Boolean>> coloredLinks = new HashMap<Link, Pair<Color, Boolean>>();
//        if (inOnlineSimulationMode() && viewEditTopTables.isInitialNetPlanShown ()) {
//            Route initialRoute = initialState.getRouteFromId(route.getId());
//            if (initialRoute != null) {
//                for (ProtectionSegment s : initialRoute.getPotentialBackupProtectionSegments())
//                    for (Link e : s.getSeqLinks())
//                        if (netPlan.getLinkFromId(e.getId()) != null)
//                            coloredLinks.put(netPlan.getLinkFromId(e.getId()), Pair.of(Color.YELLOW, true));
//                for (Link linkOrSegment : initialRoute.getSeqLinksAndProtectionSegments())
//                    if (linkOrSegment instanceof ProtectionSegment) {
//                        for (Link e : ((ProtectionSegment) linkOrSegment).getSeqLinks())
//                            if (netPlan.getLinkFromId(e.getId()) != null)
//                                coloredLinks.put(netPlan.getLinkFromId(e.getId()), Pair.of(Color.ORANGE, true));
//                    } else if (netPlan.getLinkFromId(linkOrSegment.getId()) != null)
//                        coloredLinks.put(netPlan.getLinkFromId(linkOrSegment.getId()), Pair.of(Color.BLUE, true));
//            }
//        }
//        for (ProtectionSegment s : route.getPotentialBackupProtectionSegments())
//            for (Link e : s.getSeqLinks())
//                coloredLinks.put(e, Pair.of(Color.YELLOW, false));
//        for (Link linkOrSegment : route.getSeqLinksAndProtectionSegments())
//            if (linkOrSegment instanceof ProtectionSegment) {
//                for (Link e : ((ProtectionSegment) linkOrSegment).getSeqLinks())
//                    coloredLinks.put(netPlan.getLinkFromId(e.getId()), Pair.of(Color.ORANGE, false));
//            } else coloredLinks.put(linkOrSegment, Pair.of(Color.BLUE, false));
//        topologyPanel.getCanvas().showAndPickNodesAndLinks(null, coloredLinks);
//        topologyPanel.getCanvas().refresh();
//    }
//
//    @Override
//    public void showMulticastTree(long treeId) {
//        NetPlan netPlan = getDesign();
//        MulticastTree tree = netPlan.getMulticastTreeFromId(treeId);
//        NetworkLayer layer = tree.getLayer();
//        selectNetPlanViewItem(layer.getId(), NetworkElementType.MULTICAST_TREE, treeId);
//
//        NetPlan currentState = getDesign();
//        NetPlan initialState = getInitialDesign();
//        Map<Node, Color> coloredNodes = new HashMap<Node, Color>();
//        Map<Link, Pair<Color, Boolean>> coloredLinks = new HashMap<Link, Pair<Color, Boolean>>();
//        if (inOnlineSimulationMode() && viewEditTopTables.isInitialNetPlanShown ()) {
//            MulticastTree initialTree = initialState.getMulticastTreeFromId(treeId);
//            if (initialTree != null)
//                for (Link e : initialTree.getLinkSet())
//                    if (currentState.getLinkFromId(e.getId()) != null)
//                        coloredLinks.put(currentState.getLinkFromId(e.getId()), Pair.of(Color.BLUE, true));
//        }
//        for (Link e : tree.getLinkSet()) coloredLinks.put(e, Pair.of(Color.BLUE, false));
//        coloredNodes.put(tree.getIngressNode(), COLOR_INITIALNODE);
//        for (Node n : tree.getEgressNodes()) coloredNodes.put(n, COLOR_ENDNODE);
//        topologyPanel.getCanvas().showAndPickNodesAndLinks(coloredNodes, coloredLinks);
//        topologyPanel.getCanvas().refresh();
//    }
//
//    @Override
//    public void showSRG(long srg) {
//        showSRG(getDesign().getNetworkLayerDefault().getId(), srg);
//    }
//
//    @Override
//    public void showSRG(long layer, long srg) {
//        NetPlan netPlan = getDesign();
//        selectNetPlanViewItem(layer, NetworkElementType.SRG, srg);
//
//        Set<Node> nodeIds_thisSRG = netPlan.getSRGFromId(srg).getNodes();
//        Set<Link> linkIds_thisSRG_thisLayer = netPlan.getSRGFromId(srg).getLinks(netPlan.getNetworkLayerFromId(layer));
//        Map<Node, Color> nodeColors = new HashMap<Node, Color>();
//        Map<Link, Pair<Color, Boolean>> linkColors = new HashMap<Link, Pair<Color, Boolean>>();
//        for (Node n : nodeIds_thisSRG) nodeColors.put(n, Color.ORANGE);
//        for (Link e : linkIds_thisSRG_thisLayer) linkColors.put(e, Pair.of(Color.ORANGE, false));
//
//        topologyPanel.getCanvas().showAndPickNodesAndLinks(nodeColors, linkColors);
//        topologyPanel.getCanvas().refresh();
//    }

//    @Override
//    public synchronized void updateNetPlanView() {
//        updateWarnings();
//        viewEditTopTables.updateView();
//    }

//    @Override
//    public void updateWarnings() {
//        Map<String, String> net2planParameters = Configuration.getNet2PlanOptions();
//        List<String> warnings = NetworkPerformanceMetrics.checkNetworkState(getDesign(), net2planParameters);
//        String warningMsg = warnings.isEmpty() ? "Design is successfully completed!" : StringUtils.join(warnings, StringUtils.getLineSeparator());
//        updateLog(warningMsg);
//    }

//    /**
//     * Adds a new tab in the right panel at the last position.
//     *
//     * @param name Tab name
//     * @param tab  Tab component
//     * @return Tab position
//     * @since 0.3.0
//     */
//    protected final int addTab(String name, JComponent tab) {
//        return addTab(name, tab, -1);
//    }
//
//    /**
//     * Adds a new tab in the right panel at the given position.
//     *
//     * @param name     Tab name
//     * @param tab      Tab component
//     * @param tabIndex Tab position (-1 means last position)
//     * @return Tab position
//     * @since 0.3.0
//     */
//    protected final int addTab(String name, JComponent tab, int tabIndex) {
//        int numTabs = rightPane.getTabCount();
//        if (numTabs == 9) throw new RuntimeException("A maximum of 9 tabs are allowed");
//
//        if (tabIndex == -1) tabIndex = numTabs;
//        rightPane.insertTab(name, null, tab, null, tabIndex);
//
//        if (tabIndex <= viewNetPlanTabIndex) viewNetPlanTabIndex++;
//        return tabIndex;
//    }

//    /**
//     * Indicates whether or not traffic demands can be added to the current design
//     * from an external file.
//     *
//     * @return {@code true} if it is allowed to load traffic demands. Otherwise, {@code false}.
//     * @since 0.3.0
//     */
//    public boolean allowLoadTrafficDemands() {
//        return false;
//    }

//    /**
//     * Indicates whether or not the initial {@code NetPlan} object is stored to be
//     * compared with the current one (i.e. after some simulation steps).
//     *
//     * @return {@code true} if the initial {@code NetPlan} object is stored. Otherwise, {@code false}.
//     * @since 0.3.0
//     */
//    public boolean inOnlineSimulationMode() {
//        return false;
//    }

//    /**
//     * Asks user to confirm plugin reset.
//     *
//     * @return {@code true} if user confirms to reset the plugin, or {@code false} otherwise
//     * @since 0.2.3
//     */
//    protected static boolean askForReset() {
//        int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to reset? This will remove all unsaved data", "Reset", JOptionPane.YES_NO_OPTION);
//
//        return result == JOptionPane.YES_OPTION;
//    }

//    /**
//     * Clears the current demand selection in the network state view.
//     * <p>
//     * <p><b>Important</b>: Always call the parent method.</p>
//     *
//     * @since 0.3.1
//     */
//    protected void clearDemandSelection() {
//        JTable table = viewEditTopTables.getNetPlanViewTable().get(NetworkElementType.DEMAND);
//        table.clearSelection();
//    }
//
//    /**
//     * Clears the current multicast demand selection in the network state view.
//     * <p>
//     * <p><b>Important</b>: Always call the parent method.</p>
//     *
//     * @since 0.3.1
//     */
//    protected void clearMulticastDemandSelection() {
//        JTable table = viewEditTopTables.getNetPlanViewTable().get(NetworkElementType.MULTICAST_DEMAND);
//        table.clearSelection();
//    }
//
//    /**
//     * Clears the current forwarding rule selection in the network state view.
//     * <p>
//     * <p><b>Important</b>: Always call the parent method.</p>
//     *
//     * @since 0.3.1
//     */
//    protected void clearForwardingRuleSelection() {
//        JTable table = viewEditTopTables.getNetPlanViewTable().get(NetworkElementType.FORWARDING_RULE);
//        table.clearSelection();
//    }
//
//    /**
//     * Clears the current link selection in the network state view.
//     * <p>
//     * <p><b>Important</b>: Always call the parent method.</p>
//     *
//     * @since 0.3.1
//     */
//    protected void clearLinkSelection() {
//        JTable table = viewEditTopTables.getNetPlanViewTable().get(NetworkElementType.LINK);
//        table.clearSelection();
//    }
//
//    /**
//     * Clears the current node selection in the network state view.
//     * <p>
//     * <p><b>Important</b>: Always call the parent method.</p>
//     *
//     * @since 0.3.1
//     */
//    protected void clearNodeSelection() {
//        JTable table = viewEditTopTables.getNetPlanViewTable().get(NetworkElementType.NODE);
//        table.clearSelection();
//    }
//
//    /**
//     * Clears the current proctection segment selection in the network state view.
//     * <p>
//     * <p><b>Important</b>: Always call the parent method.</p>
//     *
//     * @since 0.3.1
//     */
//    protected void clearProtectionSegmentSelection() {
//        JTable table = viewEditTopTables.getNetPlanViewTable().get(NetworkElementType.PROTECTION_SEGMENT);
//        table.clearSelection();
//    }
//
//    /**
//     * Clears the current route selection in the network state view.
//     * <p>
//     * <p><b>Important</b>: Always call the parent method.</p>
//     *
//     * @since 0.3.1
//     */
//    protected void clearRouteSelection() {
//        JTable table = viewEditTopTables.getNetPlanViewTable().get(NetworkElementType.ROUTE);
//        table.clearSelection();
//    }
//
//    /**
//     * Clears the current multicast tree selection in the network state view.
//     * <p>
//     * <p><b>Important</b>: Always call the parent method.</p>
//     *
//     * @since 0.3.1
//     */
//    protected void clearMulticastTreeSelection() {
//        JTable table = viewEditTopTables.getNetPlanViewTable().get(NetworkElementType.MULTICAST_TREE);
//        table.clearSelection();
//    }
//
//    /**
//     * Clears the current SRG selection in the network state view.
//     * <p>
//     * <p><b>Important</b>: Always call the parent method.</p>
//     *
//     * @since 0.3.1
//     */
//    protected void clearSRGSelection() {
//        JTable table = viewEditTopTables.getNetPlanViewTable().get(NetworkElementType.SRG);
//        table.clearSelection();
//    }

    /**
     * Allows customizing the 'demand' tab in the network state viewer.
     *
     * @param demandTableView Table with per-demand information
     * @return Component to be included in the 'demand' tab
     * @since 0.3.1
     */
    protected JComponent configureDemandTabView(JScrollPane demandTableView) {
        return demandTableView;
    }

    /**
     * Allows customizing the 'multicast demand' tab in the network state viewer.
     *
     * @param demandTableView Table with per-multicast demand information
     * @return Component to be included in the 'multicast demand' tab
     * @since 0.3.1
     */
    protected JComponent configureMulticastDemandTabView(JScrollPane multicastDemandTableView) {
        return multicastDemandTableView;
    }

    /**
     * Allows customizing the 'forwarding rule' tab in the network state viewer.
     *
     * @param forwadingRuleTableView Table with per-forwarding rule information
     * @return Component to be included in the 'forwarding rule' tab
     * @since 0.3.1
     */
    protected JComponent configureForwardingRuleTabView(JScrollPane forwadingRuleTableView) {
        return forwadingRuleTableView;
    }

//    /**
//     * Allows to include a custom panel in the left-bottom corner of the window,
//     * just below the topologyPanel panel.
//     *
//     * @return A panel to be included in the left-bottom corner of the window
//     * @since 0.3.0
//     */
//    protected JPanel configureLeftBottomPanel() {
//        return null;
//    }

    /**
     * Allows customizing the 'link' tab in the network state viewer.
     *
     * @param linkTableView Table with per-link information
     * @return Component to be included in the 'link' tab
     * @since 0.3.1
     */
    protected JComponent configureLinkTabView(JScrollPane linkTableView) {
        return linkTableView;
    }

    /**
     * Allows customizing the 'node' tab in the network state viewer.
     *
     * @param nodeTableView Table with per-node information
     * @return Component to be included in the 'node' tab
     * @since 0.3.1
     */
    protected JComponent configureNodeTabView(JScrollPane nodeTableView) {
        return nodeTableView;
    }

    /**
     * Allows customizing the 'protection segment' tab in the network state viewer.
     *
     * @param segmentTableView Table with per-protection-segment information
     * @return Component to be included in the 'protection segment' tab
     * @since 0.3.1
     */
    protected JComponent configureProtectionSegmentTabView(JScrollPane segmentTableView) {
        return segmentTableView;
    }

    /**
     * Allows customizing the 'route' tab in the network state viewer.
     *
     * @param routeTableView Table with per-route information
     * @return Component to be included in the 'route' tab
     * @since 0.3.1
     */
    protected JComponent configureRouteTabView(JScrollPane routeTableView) {
        return routeTableView;
    }

    /**
     * Allows customizing the 'multicast tree' tab in the network state viewer.
     *
     * @param multicastTreeTableView Table with per-route information
     * @return Component to be included in the 'multicast tree' tab
     * @since 0.3.1
     */
    protected JComponent configureMulticastTreeTabView(JScrollPane multicastTreeTableView) {
        return multicastTreeTableView;
    }

    /**
     * Allows customizing the 'srg' tab in the network state viewer.
     *
     * @param srgTableView Table with per-SRG information
     * @return Component to be included in the 'srg' tab
     * @since 0.3.1
     */
    protected JComponent configureSRGTabView(JScrollPane srgTableView) {
        return srgTableView;
    }

//    /**
//     * Allows to include custom code after initializing the topologyPanel panel (i.e. add new plugins).
//     *
//     * @since 0.3.0
//     */
//    private void configureTopologyPanel() {
//        popupPlugin = new PopupMenuPlugin(this);
//
//        getTopologyPanel().addPlugin(new PanGraphPlugin(this, MouseEvent.BUTTON1_MASK));
//        if (isEditable() && getTopologyPanel().getCanvas() instanceof JUNGCanvas)
//            getTopologyPanel().addPlugin(new AddLinkGraphPlugin(this, MouseEvent.BUTTON1_MASK, MouseEvent.BUTTON1_MASK | MouseEvent.SHIFT_MASK));
//        getTopologyPanel().addPlugin(popupPlugin);
//        if (isEditable())
//            getTopologyPanel().addPlugin(new MoveNodePlugin(this, MouseEvent.BUTTON1_MASK | MouseEvent.CTRL_MASK));
//    }

//    /**
//     * Returns a reference to the topologyPanel panel.
//     *
//     * @return Reference to the topologyPanel panel
//     * @since 0.3.0
//     */
//    public final TopologyPanel getTopologyPanel() {
//        return topologyPanel;
//    }

//    /**
//     * Allows to include custom code after resetting the topologyPanel panel.
//     *
//     * @since 0.3.0
//     */
//    protected void reset_internal() {
//        loadDesign(new NetPlan());
//    }

//    /**
//     * Shows the tab corresponding associated to a network element.
//     *
//     * @param type   Network element type
//     * @param itemId Item identifier (if null, it will just show the tab)
//     * @since 0.3.0
//     */
//    protected void selectNetPlanViewItem(NetworkElementType type, Object itemId) {
//        selectNetPlanViewItem(getDesign().getNetworkLayerDefault().getId(), type, itemId);
//    }
//
//    /**
//     * Shows the tab corresponding associated to a network element.
//     *
//     * @param layerId Layer identifier
//     * @param type    Network element type
//     * @param itemId  Item identifier (if null, it will just show the tab)
//     * @since 0.3.0
//     */
//    private void selectNetPlanViewItem(long layer, NetworkElementType type, Object itemId) {
//        topologyPanel.selectLayer(layer);
//        showTab(viewNetPlanTabIndex);
//        viewEditTopTables.selectViewItem (type, itemId);
//    }

//    /**
//     * Allows to include actions when a {@code NetPlan} object is loaded.
//     *
//     * @param netPlan {@code NetPlan} object
//     * @since 0.3.0
//     */
//    protected void setNetPlan(NetPlan netPlan) {
//        currentNetPlan = netPlan;
//        if (inOnlineSimulationMode()) initialNetPlan = currentNetPlan.copy();
//    }

//    /**
//     * Shows the {@code NetPlan} view, moving to the corresponding tab.
//     *
//     * @since 0.3.0
//     */
//    public final void showNetPlanView() {
//    	viewEditTopTables.getNetPlanView ().setSelectedIndex(0);
//        showTab(viewNetPlanTabIndex);
//    }

//    /**
//     * Shows the desired tab in {@code NetPlan} view.
//     *
//     * @param tabIndex Tab index
//     * @since 0.3.0
//     */
//    public final void showTab(int tabIndex) {
//        if (tabIndex < rightPane.getTabCount() && rightPane.getSelectedIndex() != tabIndex) {
//            rightPane.setSelectedIndex(tabIndex);
//            rightPane.requestFocusInWindow();
//        }
//    }

//
//    /**
//     * Allows to show some custom log messages.
//     *
//     * @param text Log message
//     * @since 0.3.0
//     */
//    protected void updateLog(String text) {
//    }

//    private class AddLinkAction extends AbstractAction {
//        private final long layer;
//        private final long originNode;
//        private final long destinationNode;
//
//        public AddLinkAction(String name, long layer, long originNode, long destinationNode) {
//            super(name);
//            this.layer = layer;
//            this.originNode = originNode;
//            this.destinationNode = destinationNode;
//        }
//
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            addLink(layer, originNode, destinationNode);
//        }
//    }
//
//    private class AddLinkBidirectionalAction extends AbstractAction {
//        private final long layer;
//        private final long originNode;
//        private final long destinationNode;
//
//        public AddLinkBidirectionalAction(String name, long layer, long originNode, long destinationNode) {
//            super(name);
//            this.layer = layer;
//            this.originNode = originNode;
//            this.destinationNode = destinationNode;
//        }
//
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            addLinkBidirectional(layer, originNode, destinationNode);
//        }
//    }

//    private class AddNodeAction extends AbstractAction {
//        private final Point2D pos;
//
//        public AddNodeAction(String name, Point2D pos) {
//            super(name);
//            this.pos = pos;
//        }
//
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            addNode(pos);
//        }
//    }

//    public static class ColumnComparator implements Comparator<String> {
//        @Override
//        public int compare(String o1, String o2) {
//            String oo1 = o1;
//            String oo2 = o2;
//
//            int pos1 = oo1.indexOf(" (");
//            if (pos1 != -1) oo1 = oo1.substring(0, pos1);
//
//            int pos2 = oo2.indexOf(" (");
//            if (pos2 != -1) oo2 = oo2.substring(0, pos2);
//
//            double d1 = Double.MAX_VALUE;
//            try {
//                d1 = Double.parseDouble(oo1);
//            } catch (Throwable e) {
//            }
//
//            double d2 = Double.MAX_VALUE;
//            try {
//                d2 = Double.parseDouble(oo2);
//            } catch (Throwable e) {
//            }
//
//            if (d1 != Double.MAX_VALUE && d2 != Double.MAX_VALUE) {
//                int out = Double.compare(d1, d2);
//                if (out != 0) return out;
//            }
//
//            return o1.compareTo(o2);
//        }
//    }

//    public abstract class DocumentAdapter implements DocumentListener {
//        @Override
//        public void changedUpdate(DocumentEvent e) {
//            processEvent(e);
//        }
//
//        @Override
//        public void insertUpdate(DocumentEvent e) {
//            processEvent(e);
//        }
//
//        @Override
//        public void removeUpdate(DocumentEvent e) {
//            processEvent(e);
//        }
//
//        private void processEvent(DocumentEvent e) {
//            if (!allowDocumentUpdate) return;
//
//            Document doc = e.getDocument();
//            try {
//                updateInfo(doc.getText(0, doc.getLength()));
//            } catch (BadLocationException ex) {
//            }
//        }
//
//        protected abstract void updateInfo(String text);
//    }

//    private class RemoveLinkAction extends AbstractAction {
//        private final long link;
//
//        public RemoveLinkAction(String name, long link) {
//            super(name);
//            this.link = link;
//        }
//
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            removeLink(link);
//        }
//    }
//
//    private class RemoveNodeAction extends AbstractAction {
//        private final long node;
//
//        public RemoveNodeAction(String name, long node) {
//            super(name);
//            this.node = node;
//        }
//
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            removeNode(node);
//        }
//    }
//
//    public static class SingleElementAttributeEditor extends MouseAdapter {
//        private final INetworkCallback callback;
//        private final NetworkElementType type;
//
//        public SingleElementAttributeEditor(final INetworkCallback callback, final NetworkElementType type) {
//            this.callback = callback;
//            this.type = type;
//        }
//
//        @Override
//        public void mouseClicked(MouseEvent e) {
//            if (SwingUtilities.isRightMouseButton(e)) {
//                final JTable table = (JTable) e.getSource();
//                final NetPlan netPlan = callback.getDesign();
//
//                JPopupMenu popup = new JPopupMenu();
//
//                JMenuItem addAttribute = new JMenuItem("Add/edit attribute");
//                addAttribute.addActionListener(new ActionListener() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        JTextField txt_key = new JTextField(20);
//                        JTextField txt_value = new JTextField(20);
//
//                        JPanel pane = new JPanel();
//                        pane.add(new JLabel("Attribute: "));
//                        pane.add(txt_key);
//                        pane.add(Box.createHorizontalStrut(15));
//                        pane.add(new JLabel("Value: "));
//                        pane.add(txt_value);
//
//                        while (true) {
//                            int result = JOptionPane.showConfirmDialog(null, pane, "Please enter an attribute name and its value", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
//                            if (result != JOptionPane.OK_OPTION) return;
//
//                            try {
//                                if (txt_key.getText().isEmpty())
//                                    throw new Exception("Please, insert an attribute name");
//
//                                switch (type) {
//                                    case NETWORK:
//                                        netPlan.setAttribute(txt_key.getText(), txt_value.getText());
//                                        break;
//
//                                    case LAYER:
//                                        netPlan.getNetworkLayerDefault().setAttribute(txt_key.getText(), txt_value.getText());
//                                        break;
//
//                                    default:
//                                        ErrorHandling.showErrorDialog("Bad", "Internal error");
//                                        return;
//                                }
//
//                                callback.updateNetPlanView();
//                                return;
//                            } catch (Exception ex) {
//                                ErrorHandling.showErrorDialog(ex.getMessage(), "Error adding/editing attribute");
//                            }
//                        }
//                    }
//                });
//
//                popup.add(addAttribute);
//
//                int numAttributes;
//                switch (type) {
//                    case NETWORK:
//                        numAttributes = netPlan.getAttributes().size();
//                        break;
//
//                    case LAYER:
//                        numAttributes = netPlan.getNetworkLayerDefault().getAttributes().size();
//                        break;
//
//                    default:
//                        ErrorHandling.showErrorDialog("Bad", "Internal error");
//                        return;
//                }
//
//                if (numAttributes > 0) {
//                    JMenuItem removeAttribute = new JMenuItem("Remove attribute");
//
//                    removeAttribute.addActionListener(new ActionListener() {
//                        @Override
//                        public void actionPerformed(ActionEvent e) {
//                            try {
//                                String[] attributeList;
//
//                                switch (type) {
//                                    case NETWORK:
//                                        attributeList = StringUtils.toArray(netPlan.getAttributes().keySet());
//                                        break;
//
//                                    case LAYER:
//                                        attributeList = StringUtils.toArray(netPlan.getNetworkLayerDefault().getAttributes().keySet());
//                                        break;
//
//                                    default:
//                                        ErrorHandling.showErrorDialog("Bad", "Internal error");
//                                        return;
//                                }
//
//                                if (attributeList.length == 0) throw new Exception("No attribute to remove");
//
//                                Object out = JOptionPane.showInputDialog(null, "Please, select an attribute to remove", "Remove attribute", JOptionPane.QUESTION_MESSAGE, null, attributeList, attributeList[0]);
//                                if (out == null) return;
//
//                                String attributeToRemove = out.toString();
//
//                                switch (type) {
//                                    case NETWORK:
//                                        netPlan.removeAttribute(attributeToRemove);
//                                        break;
//
//                                    case LAYER:
//                                        netPlan.getNetworkLayerDefault().removeAttribute(attributeToRemove);
//                                        break;
//
//                                    default:
//                                        ErrorHandling.showErrorDialog("Bad", "Internal error");
//                                        return;
//                                }
//
//                                callback.updateNetPlanView();
//                            } catch (Exception ex) {
//                                ErrorHandling.showErrorDialog(ex.getMessage(), "Error removing attribute");
//                            }
//                        }
//                    });
//
//                    popup.add(removeAttribute);
//
//                    JMenuItem removeAttributes = new JMenuItem("Remove all attributes");
//
//                    removeAttributes.addActionListener(new ActionListener() {
//                        @Override
//                        public void actionPerformed(ActionEvent e) {
//                            try {
//                                switch (type) {
//                                    case NETWORK:
//                                        netPlan.setAttributeMap(new HashMap<String, String>());
//                                        break;
//
//                                    case LAYER:
//                                        netPlan.getNetworkLayerDefault().setAttributeMap(new HashMap<String, String>());
//                                        break;
//
//                                    default:
//                                        ErrorHandling.showErrorDialog("Bad", "Internal error");
//                                        return;
//                                }
//
//                                callback.updateNetPlanView();
//                            } catch (Exception ex) {
//                                ErrorHandling.showErrorDialog(ex.getMessage(), "Error removing attributes");
//                            }
//                        }
//                    });
//
//                    popup.add(removeAttributes);
//                }
//
//                popup.show(e.getComponent(), e.getX(), e.getY());
//            }
//        }
//    }

//    private class SwitchTabAction extends AbstractAction {
//        private final int tabId;
//
//        public SwitchTabAction(int tabId) {
//            this.tabId = tabId;
//        }
//
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            showTab(tabId);
//        }
//    }
}
