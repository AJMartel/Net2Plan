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


package com.net2plan.gui.utils.viewEditTopolTables.specificTables;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultRowSorter;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.table.TableModel;

import com.google.common.collect.Sets;
import com.net2plan.gui.utils.CellRenderers;
import com.net2plan.gui.utils.CellRenderers.NumberCellRenderer;
import com.net2plan.gui.utils.viewEditTopolTables.specificTables.AdvancedJTable_NetworkElement.LastRowAggregatedValue;
import com.net2plan.gui.utils.ClassAwareTableModel;
import com.net2plan.gui.utils.IVisualizationCallback;
import com.net2plan.gui.utils.WiderJComboBox;
import com.net2plan.interfaces.networkDesign.Link;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.interfaces.networkDesign.Node;
import com.net2plan.internal.Constants.NetworkElementType;
import com.net2plan.internal.ErrorHandling;
import com.net2plan.utils.CollectionUtils;
import com.net2plan.utils.StringUtils;

import net.miginfocom.swing.MigLayout;

/**
 */
@SuppressWarnings("unchecked")
public class AdvancedJTable_node extends AdvancedJTable_NetworkElement
{
    public static final int COLUMN_ID = 0;
    public static final int COLUMN_INDEX = 1;
    public static final int COLUMN_SHOWHIDE = 2;
    public static final int COLUMN_NAME = 3;
    public static final int COLUMN_STATE = 4;
    public static final int COLUMN_XCOORD = 5;
    public static final int COLUMN_YCOORD = 6;
    public static final int COLUMN_OUTLINKS = 7;
    public static final int COLUMN_INLINKS = 8;
    public static final int COLUMN_INGRESSTRAFFIC = 9;
    public static final int COLUMN_EGRESSTRAFFIC = 10;
    public static final int COLUMN_INGRESSMULTICASTTRAFFIC = 11;
    public static final int COLUMN_EGRESSMULTICASTTRAFFIC = 12;
    public static final int COLUMN_INCOMINGLINKTRAFFIC = 13;
    public static final int COLUMN_OUTGOINGLINKTRAFFIC = 14;
    public static final int COLUMN_SRGS = 15;
    public static final int COLUMN_ATTRIBUTES = 16;
    private static final String netPlanViewTabName = "Nodes";
    private static final String[] netPlanViewTableHeader = StringUtils.arrayOf("Unique identifier", "Index", "Show/Hide", "Name", 
    		"State", "xCoord / Longitude", "yCoord / Latitude", "Outgoing links", "Incoming links", 
    		"Ingress traffic", "Egress traffic", "Incoming traffic", "Outgoing traffic" , "Ingress traffic (multicast)", "Egress traffic (multicast)", "SRGs", "Attributes");
    private static final String[] netPlanViewTableTips = StringUtils.arrayOf("Unique identifier (never repeated in the same netPlan object, never changes, long)", 
    		"Index (consecutive integer starting in zero)", 
    		"Indicates whether or not the node is visible in the topology canvas", 
    		"Node name", "Indicates whether the node is in up/down state", "Coordinate along x-axis (i.e. longitude)", 
    		"Coordinate along y-axis (i.e. latitude)", "Outgoing links", "Incoming links", 
    		"Total UNICAST traffic entering to the network from this node (offered / carried)", 
    		"Total UNICAST traffic of demands ending in this node (offered / carried)", 
    		"Total MULTICAST traffic entering to the network from this node", 
    		"Total MULTICAST traffic leaving the network from this node", 
    		"Total traffic (unicast and multicast) in the node input links", 
    		"Total traffic (unicast and multicast) in the node output links", 
    		"SRGs including this node", "Node-specific attributes");

    private ArrayList<String> attributesColumnsNames;
    private boolean expandAttributes = false;
    private List<Node> currentNodes = new LinkedList<>();
    private NetPlan currentTopology = null;
    private Map<String,Boolean> hasBeenAddedEachAttColumn = new HashMap<>();
    /**
     * Default constructor.
     *
     * @param callback The network callback
     * @since 0.2.0
     */
    public AdvancedJTable_node(final IVisualizationCallback callback) {
        super(createTableModel(callback), callback, NetworkElementType.NODE, true);
        setDefaultCellRenderers(callback);
        setSpecificCellRenderers();
        setColumnRowSortingFixedAndNonFixedTable();
        fixedTable.setDefaultRenderer(Boolean.class, this.getDefaultRenderer(Boolean.class));
        fixedTable.setDefaultRenderer(Double.class, this.getDefaultRenderer(Double.class));
        fixedTable.setDefaultRenderer(Object.class, this.getDefaultRenderer(Object.class));
        fixedTable.setDefaultRenderer(Float.class, this.getDefaultRenderer(Float.class));
        fixedTable.setDefaultRenderer(Long.class, this.getDefaultRenderer(Long.class));
        fixedTable.setDefaultRenderer(Integer.class, this.getDefaultRenderer(Integer.class));
        fixedTable.setDefaultRenderer(String.class, this.getDefaultRenderer(String.class));
        fixedTable.getTableHeader().setDefaultRenderer(new CellRenderers.FixedTableHeaderRenderer());

    }



    public List<Object[]> getAllData(NetPlan currentState, ArrayList<String> attributesTitles) {
        List<Object[]> allNodeData = new LinkedList<Object[]>();


        for (Node node : currentState.getNodes()) {
            Set<Link> outgoingLinks = node.getOutgoingLinks();
            Set<Link> incomingLinks = node.getIncomingLinks();

            Object[] nodeData = new Object[netPlanViewTableHeader.length + attributesTitles.size()];
            nodeData[COLUMN_ID] = node.getId();
            nodeData[COLUMN_INDEX] = node.getIndex();
            nodeData[COLUMN_SHOWHIDE] = callback.getVisualizationState().isVisible(node);
            nodeData[COLUMN_NAME] = node.getName();
            nodeData[COLUMN_STATE] = node.isUp();
            nodeData[COLUMN_XCOORD] = node.getXYPositionMap().getX();
            nodeData[COLUMN_YCOORD] = node.getXYPositionMap().getY();
            nodeData[COLUMN_OUTLINKS] = outgoingLinks.isEmpty() ? "none" : outgoingLinks.size() + " (" + CollectionUtils.join(outgoingLinks, ", ") + ")";
            nodeData[COLUMN_INLINKS] = incomingLinks.isEmpty() ? "none" : incomingLinks.size() + " (" + CollectionUtils.join(incomingLinks, ", ") + ")";
            nodeData[COLUMN_INGRESSTRAFFIC] = node.getIngressOfferedTraffic() + "(" + node.getIngressCarriedTraffic() + ")";
            nodeData[COLUMN_EGRESSTRAFFIC] = node.getEgressOfferedTraffic()  + "(" + node.getEgressCarriedTraffic() + ")";
            nodeData[COLUMN_INCOMINGLINKTRAFFIC] = node.getIncomingLinksTraffic();
            nodeData[COLUMN_OUTGOINGLINKTRAFFIC] = node.getOutgoingLinksTraffic();
            nodeData[COLUMN_INGRESSMULTICASTTRAFFIC] = node.getIngressOfferedMulticastTraffic() +  "(" + node.getIngressOfferedMulticastTraffic() + ")";
            nodeData[COLUMN_EGRESSMULTICASTTRAFFIC] = node.getEgressOfferedMulticastTraffic() + "(" + node.getEgressOfferedMulticastTraffic() + ")";
            nodeData[COLUMN_SRGS] = node.getSRGs().isEmpty() ? "none" : node.getSRGs().size() + " (" + CollectionUtils.join(currentState.getIndexes(node.getSRGs()), ", ") + ")";
            nodeData[COLUMN_ATTRIBUTES] = StringUtils.mapToString(node.getAttributes());
            for(int i = netPlanViewTableHeader.length; i < netPlanViewTableHeader.length + attributesTitles.size();i++)
            {
                if(node.getAttributes().containsKey(attributesTitles.get(i-netPlanViewTableHeader.length)))
                {
                    nodeData[i] = node.getAttribute(attributesTitles.get(i-netPlanViewTableHeader.length));
                }
            }

            allNodeData.add(nodeData);
        }
        
        /* Add the aggregation row with the aggregated statistics */
        final double aggIngress = currentState.getNodes().stream().mapToDouble(e->e.getIngressOfferedTraffic()).sum();
        final double aggEgress = currentState.getNodes().stream().mapToDouble(e->e.getEgressOfferedTraffic()).sum();
        final double aggIncomingLinksTraffic = currentState.getNodes().stream().mapToDouble(e->e.getIncomingLinksTraffic()).sum();
        final double aggOutgoingLinksTraffic = currentState.getNodes().stream().mapToDouble(e->e.getOutgoingLinksTraffic()).sum();
        final double aggMIngress = currentState.getNodes().stream().mapToDouble(e->e.getIngressOfferedMulticastTraffic()).sum();
        final double aggMEgress = currentState.getNodes().stream().mapToDouble(e->e.getEgressOfferedMulticastTraffic()).sum();
        final LastRowAggregatedValue[] aggregatedData = new LastRowAggregatedValue [netPlanViewTableHeader.length + attributesTitles.size()];
        Arrays.fill(aggregatedData, new LastRowAggregatedValue());
        aggregatedData [COLUMN_INGRESSTRAFFIC] = new LastRowAggregatedValue(aggIngress);
        aggregatedData [COLUMN_EGRESSTRAFFIC] = new LastRowAggregatedValue(aggEgress);
        aggregatedData [COLUMN_INCOMINGLINKTRAFFIC] = new LastRowAggregatedValue(aggIncomingLinksTraffic);
        aggregatedData [COLUMN_OUTGOINGLINKTRAFFIC] = new LastRowAggregatedValue(aggOutgoingLinksTraffic);
        aggregatedData [COLUMN_INGRESSMULTICASTTRAFFIC] = new LastRowAggregatedValue(aggMIngress);
        aggregatedData [COLUMN_EGRESSMULTICASTTRAFFIC] = new LastRowAggregatedValue(aggMEgress);
        allNodeData.add(aggregatedData);
        
        return allNodeData;
    }

    public String[] getCurrentTableHeaders(){

        ArrayList<String> attColumnsHeaders = getAttributesColumnsHeaders();
        String[] headers = new String[netPlanViewTableHeader.length + attColumnsHeaders.size()];
        for(int i = 0; i < headers.length ;i++)
        {
            if(i<netPlanViewTableHeader.length)
            {
                headers[i] = netPlanViewTableHeader[i];
            }
            else{
                headers[i] = "Att: "+attColumnsHeaders.get(i - netPlanViewTableHeader.length);
            }
        }


        return headers;
    }


    public String getTabName() {
        return netPlanViewTabName;
    }

    public String[] getTableHeaders() {
        return netPlanViewTableHeader;
    }

    public String[] getTableTips() {
        return netPlanViewTableTips;
    }

    public boolean hasElements(NetPlan np) {
        return np.hasNodes();
    }

    @Override
    public int getAttributesColumnIndex()
    {
        return COLUMN_ATTRIBUTES;
    }

    @Override
    public ArrayList<String> getAttributesColumnsHeaders()
    {
        ArrayList<String> attColumnsHeaders = new ArrayList<>();
        currentTopology = callback.getDesign();
        currentNodes = currentTopology.getNodes();
        for(Node node : currentNodes)
        {

            for (Map.Entry<String, String> entry : node.getAttributes().entrySet())
            {
                if(attColumnsHeaders.contains(entry.getKey()) == false)
                {
                    attColumnsHeaders.add(entry.getKey());
                }

            }

        }

        return attColumnsHeaders;
    }

//    public int[] getColumnsOfSpecialComparatorForSorting() {
//        return new int[]{7, 8};
//    }


    private static TableModel createTableModel(final IVisualizationCallback callback)
    {
//    	final TopologyPanel topologyPanel = callback.getTopologyPanel();
        TableModel nodeTableModel = new ClassAwareTableModel(new Object[1][netPlanViewTableHeader.length], netPlanViewTableHeader) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                if (!callback.getVisualizationState().isNetPlanEditable()) return false;
                if( columnIndex >= netPlanViewTableHeader.length) return true;
                if (getValueAt(rowIndex,columnIndex) == null) return false;

                return columnIndex == COLUMN_SHOWHIDE || columnIndex == COLUMN_NAME || columnIndex == COLUMN_STATE || columnIndex == COLUMN_XCOORD
                        || columnIndex == COLUMN_YCOORD;
            }

            @Override
            public void setValueAt(Object newValue, int row, int column) {
//				System.out.println ("set Value node, newValue: " + newValue + ", row: " + row + ", col: " + column);
                Object oldValue = getValueAt(row, column);

				/* If value doesn't change, exit from function */
                if (newValue != null && newValue.equals(oldValue)) return;

                NetPlan netPlan = callback.getDesign();

                if (getValueAt(row, 0) == null) row = row - 1;
                final long nodeId = (Long) getValueAt(row, 0);
                final Node node = netPlan.getNodeFromId(nodeId);
                                /* Perform checks, if needed */
//				System.out.println ("set Value node: " + node + ", newValue: " + newValue + ", row: " + row + ", col: " + column);
                try {
                    switch (column) {
                        case COLUMN_SHOWHIDE:
                            if (newValue == null) return;
                        	callback.getVisualizationState().setVisibilityState(node , (Boolean) newValue);
                        	callback.updateVisualizationAfterChanges(Sets.newHashSet(NetworkElementType.NODE));
                        	callback.getVisualizationState ().pickNode(node);
                            callback.updateVisualizationAfterPick();
                            break;

                        case COLUMN_NAME:
                        	node.setName(newValue.toString());
                        	callback.updateVisualizationAfterChanges(Sets.newHashSet(NetworkElementType.NODE));
                        	callback.getVisualizationState ().pickNode(node);
                            callback.updateVisualizationAfterPick();
                            break;

                        case COLUMN_STATE:
                            boolean isNodeUp = (Boolean) newValue;
                        	node.setFailureState(isNodeUp);
                        	callback.updateVisualizationAfterChanges(Sets.newHashSet(NetworkElementType.NODE));
                        	callback.getVisualizationState ().pickNode(node);
                            callback.updateVisualizationAfterPick();
                            break;

                        case COLUMN_XCOORD:
                        case COLUMN_YCOORD:
                            Point2D newPosition = column == COLUMN_XCOORD ? 
                            		new Point2D.Double(Double.parseDouble(newValue.toString()), node.getXYPositionMap().getY()) : 
                            		new Point2D.Double(node.getXYPositionMap().getX(), Double.parseDouble(newValue.toString()));
                            node.setXYPositionMap(newPosition);
                            callback.updateVisualizationAfterChanges(Sets.newHashSet(NetworkElementType.NODE));
                        	callback.getVisualizationState ().pickNode(node);
                            callback.updateVisualizationAfterPick();
                            break;

                        default:
                            break;
                    }
                } catch (Throwable ex) {
                    ex.printStackTrace();
                    ErrorHandling.showErrorDialog(ex.getMessage(), "Error modifying node");
//					ErrorHandling.showErrorDialog(ex.getMessage(), "Error modifying node, Object newValue: " + newValue + ", int row: " + row + ", int column: " + column);
                    return;
                }

				/* Set new value */
                super.setValueAt(newValue, row, column);
            }
        };

        return nodeTableModel;
    }

    private void setDefaultCellRenderers(final IVisualizationCallback callback) {
        setDefaultRenderer(Boolean.class, new CellRenderers.CheckBoxRenderer());
        setDefaultRenderer(Double.class, new NumberCellRenderer());
        setDefaultRenderer(Object.class, new CellRenderers.NonEditableCellRenderer());
        setDefaultRenderer(Float.class, new NumberCellRenderer());
        setDefaultRenderer(Long.class, new CellRenderers.NumberCellRenderer());
        setDefaultRenderer(Integer.class, new CellRenderers.NumberCellRenderer());
        setDefaultRenderer(String.class, new CellRenderers.NonEditableCellRenderer());

        setDefaultRenderer(Boolean.class, new CellRenderers.UpDownRenderer(getDefaultRenderer(Boolean.class), callback, NetworkElementType.NODE));
        setDefaultRenderer(Double.class, new CellRenderers.UpDownRenderer(getDefaultRenderer(Double.class), callback, NetworkElementType.NODE));
        setDefaultRenderer(Object.class, new CellRenderers.UpDownRenderer(getDefaultRenderer(Object.class), callback, NetworkElementType.NODE));
        setDefaultRenderer(Float.class, new CellRenderers.UpDownRenderer(getDefaultRenderer(Float.class), callback, NetworkElementType.NODE));
        setDefaultRenderer(Long.class, new CellRenderers.UpDownRenderer(getDefaultRenderer(Long.class), callback, NetworkElementType.NODE));
        setDefaultRenderer(Integer.class, new CellRenderers.UpDownRenderer(getDefaultRenderer(Integer.class), callback, NetworkElementType.NODE));
        setDefaultRenderer(String.class, new CellRenderers.UpDownRenderer(getDefaultRenderer(String.class), callback, NetworkElementType.NODE));
    }

    private void setSpecificCellRenderers() {
    }

    @Override
    public void setColumnRowSortingFixedAndNonFixedTable() 
    {
        setAutoCreateRowSorter(true);
        final Set<Integer> columnsWithDoubleAndThenParenthesis = Sets.newHashSet(COLUMN_OUTLINKS , COLUMN_INLINKS , COLUMN_INGRESSTRAFFIC , COLUMN_EGRESSTRAFFIC , COLUMN_INGRESSMULTICASTTRAFFIC , COLUMN_EGRESSMULTICASTTRAFFIC);
        DefaultRowSorter rowSorter = ((DefaultRowSorter) getRowSorter());
        for (int col = 0; col <= COLUMN_ATTRIBUTES ; col ++)
        	rowSorter.setComparator(col, new AdvancedJTable_NetworkElement.ColumnComparator(rowSorter , columnsWithDoubleAndThenParenthesis.contains(col)));
        fixedTable.setAutoCreateRowSorter(true);
        fixedTable.setRowSorter(this.getRowSorter());
        rowSorter = ((DefaultRowSorter) fixedTable.getRowSorter());
        for (int col = 0; col <= COLUMN_ATTRIBUTES ; col ++)
        	rowSorter.setComparator(col, new AdvancedJTable_NetworkElement.ColumnComparator(rowSorter , columnsWithDoubleAndThenParenthesis.contains(col)));
    }

    public int getNumFixedLeftColumnsInDecoration() {
        return 2;
    }


    @Override
    public void doPopup(final MouseEvent e, final int row, final Object itemId) {
        JPopupMenu popup = new JPopupMenu();

        if (callback.getVisualizationState().isNetPlanEditable()) {
            popup.add(getAddOption());
            for (JComponent item : getExtraAddOptions())
                popup.add(item);
        }

        if (!isTableEmpty()) {
            if (callback.getVisualizationState().isNetPlanEditable()) {
                if (row != -1) {
                    if (popup.getSubElements().length > 0) popup.addSeparator();

                    JMenuItem removeItem = new JMenuItem("Remove " + networkElementType);
                    removeItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            NetPlan netPlan = callback.getDesign();
                            try
                            {
                            	callback.getDesign().getNodeFromId((long) itemId).remove();
                                callback.getVisualizationState().recomputeTopologyBecauseOfLinkOrNodeAdditionsOrRemovals();
                            	callback.updateVisualizationAfterChanges(Sets.newHashSet(NetworkElementType.NODE));
                            } catch (Throwable ex) {
                                ErrorHandling.addErrorOrException(ex, getClass());
                                ErrorHandling.showErrorDialog("Unable to remove " + networkElementType);
                            }
                        }
                    });

                    popup.add(removeItem);
                    addPopupMenuAttributeOptions(e, row, itemId, popup);
                }
                JMenuItem removeItems = new JMenuItem("Remove all " + networkElementType + "s");

                removeItems.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        NetPlan netPlan = callback.getDesign();

                        try {
                            netPlan.removeAllNodes();
                            callback.getVisualizationState().recomputeTopologyBecauseOfLinkOrNodeAdditionsOrRemovals();
                        	callback.updateVisualizationAfterChanges(Sets.newHashSet(NetworkElementType.NODE));
                        } catch (Throwable ex) {
                            ex.printStackTrace();
                            ErrorHandling.showErrorDialog(ex.getMessage(), "Unable to remove all " + networkElementType + "s");
                        }
                    }
                });

                popup.add(removeItems);

                List<JComponent> extraOptions = getExtraOptions(row, itemId);
                if (!extraOptions.isEmpty()) {
                    if (popup.getSubElements().length > 0) popup.addSeparator();
                    for (JComponent item : extraOptions) popup.add(item);
                }
            }

            List<JComponent> forcedOptions = getForcedOptions();
            if (!forcedOptions.isEmpty()) {
                if (popup.getSubElements().length > 0) popup.addSeparator();
                for (JComponent item : forcedOptions) popup.add(item);
            }
        }

        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    @Override
    public void showInCanvas(MouseEvent e, Object itemId) 
    {
        if (isTableEmpty()) return;
    	callback.getVisualizationState ().pickNode(callback.getDesign().getNodeFromId((long) itemId));
        callback.updateVisualizationAfterPick();
    }

    private boolean isTableEmpty() {
        return !callback.getDesign().hasNodes();
    }

    private JMenuItem getAddOption() 
    {
        JMenuItem addItem = addItem = new JMenuItem("Add " + networkElementType);
        addItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                NetPlan netPlan = callback.getDesign();

                try {
                    Node node = netPlan.addNode(0, 0, "Node " + netPlan.getNumberOfNodes(), null);
                    callback.getVisualizationState().recomputeTopologyBecauseOfLinkOrNodeAdditionsOrRemovals();
                	callback.updateVisualizationAfterChanges(Sets.newHashSet(NetworkElementType.NODE));
                	callback.getVisualizationState ().pickNode(node);
                    callback.updateVisualizationAfterPick();
                } catch (Throwable ex) {
                    ErrorHandling.showErrorDialog(ex.getMessage(), "Unable to add " + networkElementType);
                }
            }
        });
        return addItem;
    }

    private List<JComponent> getExtraAddOptions() {
        return new LinkedList<JComponent>();
    }

    private List<JComponent> getExtraOptions(final int row, final Object itemId) {
        List<JComponent> options = new LinkedList<JComponent>();

        final int numRows = model.getRowCount();
        final NetPlan netPlan = callback.getDesign();

        if (itemId != null) {
            JMenuItem switchCoordinates_thisNode = new JMenuItem("Switch node coordinates from (x,y) to (y,x)");

            switchCoordinates_thisNode.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    NetPlan netPlan = callback.getDesign();
                    Node node = netPlan.getNodeFromId((long) itemId);
                    Point2D currentPosition = node.getXYPositionMap();
                    node.setXYPositionMap(new Point2D.Double(currentPosition.getY() , currentPosition.getX()));
                	callback.updateVisualizationAfterChanges(Sets.newHashSet(NetworkElementType.NODE));
                }
            });

            options.add(switchCoordinates_thisNode);

            JMenuItem xyPositionFromAttributes_thisNode = new JMenuItem("Set node coordinates from attributes");

            xyPositionFromAttributes_thisNode.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    NetPlan netPlan = callback.getDesign();

                    Set<String> attributeSet = new LinkedHashSet<String>();
                    Node node = netPlan.getNodeFromId((long) itemId);
                    attributeSet.addAll(node.getAttributes().keySet());

                    try {
                        if (attributeSet.isEmpty()) throw new Exception("No attribute to select");

                        final JComboBox latSelector = new WiderJComboBox();
                        final JComboBox lonSelector = new WiderJComboBox();
                        for (String attribute : attributeSet) {
                            latSelector.addItem(attribute);
                            lonSelector.addItem(attribute);
                        }

                        JPanel pane = new JPanel(new MigLayout("", "[][grow]", "[][]"));
                        pane.add(new JLabel("X-coordinate / Longitude: "));
                        pane.add(lonSelector, "growx, wrap");
                        pane.add(new JLabel("Y-coordinate / Latitude: "));
                        pane.add(latSelector, "growx, wrap");

                        while (true) {
                            int result = JOptionPane.showConfirmDialog(null, pane, "Please select the attributes for coordinates", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                            if (result != JOptionPane.OK_OPTION) return;

                            try {
                                String latAttribute = latSelector.getSelectedItem().toString();
                                String lonAttribute = lonSelector.getSelectedItem().toString();
                                
                                node.setXYPositionMap(new Point2D.Double(Double.parseDouble(node.getAttribute(lonAttribute)), Double.parseDouble(node.getAttribute(latAttribute))));
                            	callback.updateVisualizationAfterChanges(Sets.newHashSet(NetworkElementType.NODE));
                                break;
                            } catch (Throwable ex) {
                                ErrorHandling.showErrorDialog(ex.getMessage(), "Error retrieving coordinates from attributes");
                                break;
                            }
                        }
                    } catch (Throwable ex) {
                        ErrorHandling.showErrorDialog(ex.getMessage(), "Error retrieving coordinates from attributes");
                    }
                }
            });

            options.add(xyPositionFromAttributes_thisNode);

            JMenuItem nameFromAttribute_thisNode = new JMenuItem("Set node name from attribute");
            nameFromAttribute_thisNode.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    NetPlan netPlan = callback.getDesign();

                    Set<String> attributeSet = new LinkedHashSet<String>();
                    long nodeId = (long) itemId;
                    attributeSet.addAll(netPlan.getNodeFromId(nodeId).getAttributes().keySet());

                    try {
                        if (attributeSet.isEmpty()) throw new Exception("No attribute to select");

                        final JComboBox selector = new WiderJComboBox();
                        for (String attribute : attributeSet)
                            selector.addItem(attribute);

                        JPanel pane = new JPanel(new MigLayout("", "[][grow]", "[]"));
                        pane.add(new JLabel("Name: "));
                        pane.add(selector, "growx, wrap");

                        while (true) {
                            int result = JOptionPane.showConfirmDialog(null, pane, "Please select the attribute for name", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                            if (result != JOptionPane.OK_OPTION) return;

                            try {
                                String name = selector.getSelectedItem().toString();
                                netPlan.getNodeFromId(nodeId).setName(netPlan.getNodeFromId(nodeId).getAttribute(name));
                            	callback.updateVisualizationAfterChanges(Sets.newHashSet(NetworkElementType.NODE));

                                break;
                            } catch (Throwable ex) {
                                ErrorHandling.showErrorDialog(ex.getMessage(), "Error retrieving name from attribute");
                                break;
                            }
                        }
                    } catch (Throwable ex) {
                        ErrorHandling.showErrorDialog(ex.getMessage(), "Error retrieving name from attribute");
                    }
                }
            });

            options.add(nameFromAttribute_thisNode);
        }

        if (numRows > 1) {
            if (!options.isEmpty()) options.add(new JPopupMenu.Separator());

            JMenuItem switchCoordinates_allNodes = new JMenuItem("Switch all node coordinates from (x,y) to (y,x)");

            switchCoordinates_allNodes.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    NetPlan netPlan = callback.getDesign();
                    Collection<Long> nodeIds = netPlan.getNodeIds();
                    for (long nodeId : nodeIds) {
                        Point2D currentPosition = netPlan.getNodeFromId(nodeId).getXYPositionMap();
                        double newX = currentPosition.getY();
                        double newY = currentPosition.getX();
                        Point2D newPosition = new Point2D.Double(newX,newY);
                        netPlan.getNodeFromId(nodeId).setXYPositionMap(newPosition);
                    }
                	callback.updateVisualizationAfterChanges(Sets.newHashSet(NetworkElementType.NODE));
                }
            });

            options.add(switchCoordinates_allNodes);

            JMenuItem xyPositionFromAttributes_allNodes = new JMenuItem("Set all node coordinates from attributes");

            xyPositionFromAttributes_allNodes.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    NetPlan netPlan = callback.getDesign();

                    Set<String> attributeSet = new LinkedHashSet<String>();
                    Collection<Node> nodes = netPlan.getNodes();
                    for (Node node : nodes)
                        attributeSet.addAll(node.getAttributes().keySet());

                    try {
                        if (attributeSet.isEmpty()) throw new Exception("No attribute to select");

                        final JComboBox latSelector = new WiderJComboBox();
                        final JComboBox lonSelector = new WiderJComboBox();
                        for (String attribute : attributeSet) {
                            latSelector.addItem(attribute);
                            lonSelector.addItem(attribute);
                        }

                        JPanel pane = new JPanel(new MigLayout("", "[][grow]", "[][]"));
                        pane.add(new JLabel("X-coordinate / Longitude: "));
                        pane.add(lonSelector, "growx, wrap");
                        pane.add(new JLabel("Y-coordinate / Latitude: "));
                        pane.add(latSelector, "growx, wrap");

                        while (true) {
                            int result = JOptionPane.showConfirmDialog(null, pane, "Please select the attributes for coordinates", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                            if (result != JOptionPane.OK_OPTION) return;

                            try {
                                String latAttribute = latSelector.getSelectedItem().toString();
                                String lonAttribute = lonSelector.getSelectedItem().toString();

                                for (Node node : nodes) 
                                    	node.setXYPositionMap(new Point2D.Double(Double.parseDouble(node.getAttribute(lonAttribute)), Double.parseDouble(node.getAttribute(latAttribute))));
                            	callback.updateVisualizationAfterChanges(Sets.newHashSet(NetworkElementType.NODE));
                                break;
                            } catch (Throwable ex) {
                                ErrorHandling.showErrorDialog(ex.getMessage(), "Error retrieving coordinates from attributes");
                                break;
                            }
                        }
                    } catch (Throwable ex) {
                        ErrorHandling.showErrorDialog(ex.getMessage(), "Error retrieving coordinates from attributes");
                    }
                }
            });

            options.add(xyPositionFromAttributes_allNodes);

            JMenuItem nameFromAttribute_allNodes = new JMenuItem("Set all node names from attribute");
            nameFromAttribute_allNodes.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    NetPlan netPlan = callback.getDesign();

                    Set<String> attributeSet = new LinkedHashSet<String>();
                    for (Node node : netPlan.getNodes())
                        attributeSet.addAll(node.getAttributes().keySet());

                    try {
                        if (attributeSet.isEmpty()) throw new Exception("No attribute to select");

                        final JComboBox selector = new WiderJComboBox();
                        for (String attribute : attributeSet)
                            selector.addItem(attribute);

                        JPanel pane = new JPanel(new MigLayout("", "[][grow]", "[]"));
                        pane.add(new JLabel("Name: "));
                        pane.add(selector, "growx, wrap");

                        while (true) {
                            int result = JOptionPane.showConfirmDialog(null, pane, "Please select the attribute for name", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                            if (result != JOptionPane.OK_OPTION) return;

                            try {
                                String name = selector.getSelectedItem().toString();

                                for (Node node : netPlan.getNodes()) node.setName(node.getAttribute(name) != null? node.getAttribute(name) : "");
                            	callback.updateVisualizationAfterChanges(Sets.newHashSet(NetworkElementType.NODE));
                                break;
                            } catch (Throwable ex) {
                                ErrorHandling.showErrorDialog(ex.getMessage(), "Error retrieving name from attribute");
                                break;
                            }
                        }
                    } catch (Throwable ex) {
                        ErrorHandling.showErrorDialog(ex.getMessage(), "Error retrieving name from attribute");
                    }
                }
            });

            options.add(nameFromAttribute_allNodes);
        }


        return options;
    }

    private List<JComponent> getForcedOptions() {
        List<JComponent> options = new LinkedList<JComponent>();

        final int numRows = model.getRowCount();
        if (numRows > 1) {
            JMenuItem showAllNodes = new JMenuItem("Show all nodes");
            showAllNodes.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for (int row = 0; row < numRows; row++)
                        if (model.getValueAt(row, COLUMN_SHOWHIDE) != null)
                            model.setValueAt(true, row, COLUMN_SHOWHIDE);
                }
            });

            options.add(showAllNodes);

            JMenuItem hideAllNodes = new JMenuItem("Hide all nodes");
            hideAllNodes.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int numRows = model.getRowCount();
                    for (int row = 0; row < numRows; row++)
                        if (model.getValueAt(row, COLUMN_SHOWHIDE) != null)
                            model.setValueAt(false, row, COLUMN_SHOWHIDE);
                }
            });

            options.add(hideAllNodes);
        }

        return options;
    }
}
