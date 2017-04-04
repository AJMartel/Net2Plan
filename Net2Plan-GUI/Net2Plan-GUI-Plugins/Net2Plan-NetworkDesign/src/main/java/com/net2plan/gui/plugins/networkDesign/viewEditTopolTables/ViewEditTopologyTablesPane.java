package com.net2plan.gui.plugins.networkDesign.viewEditTopolTables;


import com.net2plan.gui.plugins.GUINetworkDesign;
import com.net2plan.gui.plugins.networkDesign.viewEditTopolTables.rightPanelTabs.NetPlanViewTableComponent_layer;
import com.net2plan.gui.plugins.networkDesign.viewEditTopolTables.rightPanelTabs.NetPlanViewTableComponent_network;
import com.net2plan.gui.plugins.networkDesign.viewEditTopolTables.specificTables.*;
import com.net2plan.gui.utils.FullScrollPaneLayout;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.interfaces.networkDesign.NetworkLayer;
import com.net2plan.internal.Constants;
import com.net2plan.internal.ErrorHandling;
import com.net2plan.utils.Pair;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumMap;
import java.util.Map;

import static com.net2plan.internal.Constants.NetworkElementType;

@SuppressWarnings("unchecked")
public class ViewEditTopologyTablesPane extends JPanel
{
    private final GUINetworkDesign callback;
    private final JTabbedPane netPlanView;
    private final Map<Constants.NetworkElementType, AdvancedJTable_networkElement> netPlanViewTable;
    private final Map<Constants.NetworkElementType, JComponent> netPlanViewTableComponent;
    private final Map<Constants.NetworkElementType, JLabel> netPlanViewTableNumEntriesLabel;
    private long time;

    public ViewEditTopologyTablesPane(GUINetworkDesign callback, LayoutManager layout)
    {
        super(layout);

        this.callback = callback;

        netPlanViewTable = new EnumMap<Constants.NetworkElementType, AdvancedJTable_networkElement>(NetworkElementType.class);
        netPlanViewTableComponent = new EnumMap<Constants.NetworkElementType, JComponent>(NetworkElementType.class);
        netPlanViewTableNumEntriesLabel = new EnumMap<Constants.NetworkElementType, JLabel>(NetworkElementType.class);

//        mainWindow.allowDocumentUpdate = mainWindow.isEditable();
        netPlanViewTable.put(NetworkElementType.NODE, new AdvancedJTable_node(callback));
        netPlanViewTable.put(NetworkElementType.LINK, new AdvancedJTable_link(callback));
        netPlanViewTable.put(NetworkElementType.DEMAND, new AdvancedJTable_demand(callback));
        netPlanViewTable.put(NetworkElementType.ROUTE, new AdvancedJTable_route(callback));
        netPlanViewTable.put(NetworkElementType.FORWARDING_RULE, new AdvancedJTable_forwardingRule(callback));
        netPlanViewTable.put(NetworkElementType.MULTICAST_DEMAND, new AdvancedJTable_multicastDemand(callback));
        netPlanViewTable.put(NetworkElementType.MULTICAST_TREE, new AdvancedJTable_multicastTree(callback));
        netPlanViewTable.put(NetworkElementType.SRG, new AdvancedJTable_srg(callback));
        netPlanViewTable.put(NetworkElementType.RESOURCE, new AdvancedJTable_resource(callback));
        netPlanViewTable.put(NetworkElementType.LAYER, new AdvancedJTable_layer(callback));

        netPlanViewTableNumEntriesLabel.put(NetworkElementType.NODE, new JLabel("Number of entries: "));
        netPlanViewTableNumEntriesLabel.put(NetworkElementType.LINK, new JLabel("Number of entries: "));
        netPlanViewTableNumEntriesLabel.put(NetworkElementType.DEMAND, new JLabel("Number of entries: "));
        netPlanViewTableNumEntriesLabel.put(NetworkElementType.ROUTE, new JLabel("Number of entries: "));
        netPlanViewTableNumEntriesLabel.put(NetworkElementType.FORWARDING_RULE, new JLabel("Number of entries: "));
        netPlanViewTableNumEntriesLabel.put(NetworkElementType.MULTICAST_DEMAND, new JLabel("Number of entries: "));
        netPlanViewTableNumEntriesLabel.put(NetworkElementType.MULTICAST_TREE, new JLabel("Number of entries: "));
        netPlanViewTableNumEntriesLabel.put(NetworkElementType.SRG, new JLabel("Number of entries: "));
        netPlanViewTableNumEntriesLabel.put(NetworkElementType.RESOURCE, new JLabel("Number of entries: "));
        netPlanViewTableNumEntriesLabel.put(NetworkElementType.LAYER, new JLabel("Number of entries: "));

        netPlanView = new JTabbedPane();

        for (NetworkElementType elementType : NetworkElementType.values())
        {
            if (elementType == NetworkElementType.NETWORK)
            {
                netPlanViewTableComponent.put(elementType, new NetPlanViewTableComponent_network(callback, (AdvancedJTable_layer) netPlanViewTable.get(NetworkElementType.LAYER)));
            } else if (elementType == NetworkElementType.LAYER)
            {
                netPlanViewTableComponent.put(elementType, new NetPlanViewTableComponent_layer(callback, (AdvancedJTable_layer) netPlanViewTable.get(NetworkElementType.LAYER)));
            } else
            {
                JScrollPane scrollPane = netPlanViewTable.get(elementType).getScroll();
                scrollPane.setLayout(new FullScrollPaneLayout());
                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
                netPlanViewTable.get(elementType).getFixedTable().getColumnModel().getColumn(0).setMinWidth(50);
                final JPanel panel = new JPanel();
                panel.setLayout(new BorderLayout());
                JPanel labelsPanel = new JPanel();
                labelsPanel.setLayout(new BorderLayout());
                labelsPanel.setBackground(Color.YELLOW);
                labelsPanel.setForeground(Color.BLACK);
                labelsPanel.add(netPlanViewTableNumEntriesLabel.get(elementType), BorderLayout.CENTER);
                {
                    final JPanel buttonsPanel = new JPanel();
                    final JButton resetTableRowFilters = new JButton("Reset VFs");
                    buttonsPanel.add(resetTableRowFilters, BorderLayout.EAST);
                    resetTableRowFilters.addActionListener(new ActionListener()
                    {
                        @Override
                        public void actionPerformed(ActionEvent e)
                        {
                            callback.getVisualizationState().updateTableRowFilter(null , true);
                            callback.updateVisualizationJustTables();
                            callback.resetPickedStateAndUpdateView();
                        }
                    });
                    labelsPanel.add(buttonsPanel, BorderLayout.EAST);
                }

                labelsPanel.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
                panel.add(labelsPanel, BorderLayout.NORTH);
                panel.add(scrollPane, BorderLayout.CENTER);
                netPlanViewTableComponent.put(elementType, panel);
            }
        }

        this.add(netPlanView, BorderLayout.CENTER);

    }

    public Map<Constants.NetworkElementType, AdvancedJTable_networkElement> currentTables()
    {

        return netPlanViewTable;
    }

    public JTabbedPane getNetPlanView()
    {
        return netPlanView;
    }

    public Map<Constants.NetworkElementType, AdvancedJTable_networkElement> getNetPlanViewTable()
    {
        return netPlanViewTable;
    }

    public void updateView()
    {
        /* Load current network state */
        final NetPlan currentState = callback.getDesign();
        final NetworkLayer layer = currentState.getNetworkLayerDefault();
        if (ErrorHandling.isDebugEnabled()) currentState.checkCachesConsistency();

        final int selectedTabIndex = netPlanView.getSelectedIndex();
        netPlanView.removeAll();
        for (NetworkElementType elementType : NetworkElementType.values())
        {
            if (layer.isSourceRouting() && elementType == NetworkElementType.FORWARDING_RULE)
                continue;
            if (!layer.isSourceRouting() && (elementType == NetworkElementType.ROUTE))
                continue;
            netPlanView.addTab(elementType == NetworkElementType.NETWORK ? "Network" : netPlanViewTable.get(elementType).getTabName(), netPlanViewTableComponent.get(elementType));
        }
        if ((selectedTabIndex < netPlanView.getTabCount()) && (selectedTabIndex >= 0))
            netPlanView.setSelectedIndex(selectedTabIndex);

        if (ErrorHandling.isDebugEnabled()) currentState.checkCachesConsistency();

        /* update the required tables */
        for (Map.Entry<Constants.NetworkElementType, AdvancedJTable_networkElement> entry : netPlanViewTable.entrySet())
        {
            if (layer.isSourceRouting() && entry.getKey() == NetworkElementType.FORWARDING_RULE) continue;
            if (!layer.isSourceRouting() && (entry.getKey() == NetworkElementType.ROUTE)) continue;
            final AdvancedJTable_networkElement table = entry.getValue();
            table.updateView(currentState);
            final JLabel label = netPlanViewTableNumEntriesLabel.get(entry.getKey());
            if (label != null)
            {
                final int numEntries = table.getModel().getRowCount() - 1; // last colums is for the aggregation
                if (callback.getVisualizationState().getTableRowFilter() != null)
                    label.setText("Number of entries: " + numEntries + ", FILTERED VIEW: " + callback.getVisualizationState().getTableRowFilter().getDescription());
                else
                    label.setText("Number of entries: " + numEntries);
            }
        }
        ((NetPlanViewTableComponent_layer) netPlanViewTableComponent.get(NetworkElementType.LAYER)).updateNetPlanView(currentState);
        ((NetPlanViewTableComponent_network) netPlanViewTableComponent.get(NetworkElementType.NETWORK)).updateNetPlanView(currentState);
    }


    /**
     * Shows the tab corresponding associated to a network element.
     *
     * @param type   Network element type
     * @param itemId Item identifier (if null, it will just show the tab)
     */
    public void selectViewItem(NetworkElementType type, Object itemId)
    {
        AdvancedJTable_networkElement table = netPlanViewTable.get(type);
        int tabIndex = netPlanView.getSelectedIndex();
        int col = 0;
        if (netPlanView.getTitleAt(tabIndex).equals(type == NetworkElementType.NETWORK ? "Network" : table.getTabName()))
        {
            col = table.getSelectedColumn();
            if (col == -1) col = 0;
        } else
        {
            netPlanView.setSelectedComponent(netPlanViewTableComponent.get(type));
        }

        if (itemId == null)
        {
            table.clearSelection();
            return;
        }

        TableModel model = table.getModel();
        int numRows = model.getRowCount();
        for (int row = 0; row < numRows; row++)
        {
            Object obj = model.getValueAt(row, 0);
            if (obj == null) continue;

            if (type == NetworkElementType.FORWARDING_RULE)
            {
                obj = Pair.of(
                        Integer.parseInt(model.getValueAt(row, AdvancedJTable_forwardingRule.COLUMN_DEMAND).toString().split(" ")[0]),
                        Integer.parseInt(model.getValueAt(row, AdvancedJTable_forwardingRule.COLUMN_OUTGOINGLINK).toString().split(" ")[0]));
                if (!obj.equals(itemId)) continue;
            } else if ((long) obj != (long) itemId)
            {
                continue;
            }

            row = table.convertRowIndexToView(row);
            table.changeSelection(row, col, false, true);
            return;
        }

        throw new RuntimeException(type + " " + itemId + " does not exist");
    }

    public void selectItem(NetworkElementType type, Object itemId)
    {
        AdvancedJTable_networkElement table = netPlanViewTable.get(type);

        final NetPlan netPlan = callback.getDesign();
        int itemIndex = netPlan.getNetworkElement((Long) itemId).getIndex();

        table.addRowSelectionInterval(table.convertRowIndexToModel(itemIndex), table.convertRowIndexToModel(itemIndex));
    }

    public void clearSelection(NetworkElementType type)
    {
        AdvancedJTable_networkElement table = netPlanViewTable.get(type);
        table.clearSelection();
    }

    public void showMainTab()
    {
        getNetPlanView().setSelectedIndex(0);
    }
}


//final JButton applyIntersectFilter = new JButton ("AND filter");
//buttonsPanel.add(applyIntersectFilter , BorderLayout.CENTER);
//applyIntersectFilter.addActionListener(new ActionListener()
//{
//	@Override
//	public void actionPerformed(ActionEvent e)
//	{
//		final AdvancedJTable_networkElement table = netPlanViewTable.get(elementType);
//		final int  [] selectedElements = table.getSelectedRows();
//		if (selectedElements.length == 0) return;
//		if (selectedElements.length > 1) throw new RuntimeException("MULTIPLE SELECTIONS NOT IMPLEMENTED");
//		if (elementType != Constants.NetworkElementType.FORWARDING_RULE)
//		{
//			final List<NetworkElement> selectedNetElements = new LinkedList<NetworkElement> ();
//			for (int row : selectedElements) selectedNetElements.add(callback.getDesign().getNetworkElement((long) table.getValueAt(row , 0)));
//
//
//			PABLO: HACER EL FILTRO GENERICO QUE LO LLAMAS SIN SABER DE DONDE ES LA TABLA, CON EL UP DOWN Y TODA LA HISTORIA
//		}
//		
//		ITableRowFilter filter = callback.getVisualizationState().getTableRowFilter();
//		if (filter == null)
//			filter = new TBFToFromCarriedTraffic(pickedDemand , showInCanvasThisLayerPropagation , showInCanvasLowerLayerPropagation , showInCanvasUpperLayerPropagation);
//		
//		callback.getVisualizationState().setTableRowFilter(null);
//		callback.updateVisualizationJustTables();
//		callback.resetPickedStateAndUpdateView();
//	}
//});
