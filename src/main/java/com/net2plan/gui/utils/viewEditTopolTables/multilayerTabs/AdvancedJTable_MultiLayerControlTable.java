package com.net2plan.gui.utils.viewEditTopolTables.multilayerTabs;

import com.net2plan.gui.utils.*;
import com.net2plan.gui.utils.topologyPane.VisualizationState;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.interfaces.networkDesign.NetworkLayer;
import com.net2plan.internal.Constants;
import com.net2plan.utils.StringUtils;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Jorge San Emeterio
 * @date 19-Jan-17
 */
public class AdvancedJTable_MultiLayerControlTable extends AdvancedJTable
{
    private final IVisualizationCallback callback;
    private final NetPlan netPlan;

    private final DefaultTableModel tableModel;

    private static final int COLUMN_DOWN = 0;
    private static final int COLUMN_UP = 1;
    private static final int COLUMN_ID = 2;
    private static final int COLUMN_NAME = 3;
    private static final int COLUMN_SHOW_LAYER = 4;
    private static final int COLUMN_SHOW_LINK = 5;
    private static final int COLUMN_DEFAULT = 6;

    private static final String[] tableHeader = StringUtils.arrayOf(
            "Move up",
            "Move down",
            "Index",
            "Name",
            "Layer visibility",
            "Layer's link visibility",
            "Layer default"
    );

    private static final String[] tableTips = StringUtils.arrayOf(
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    );

    public AdvancedJTable_MultiLayerControlTable(final IVisualizationCallback callback)
    {
        super();

        this.callback = callback;
        this.netPlan = callback.getDesign();

        this.tableModel = createTableModel();

        this.setModel(tableModel);
        this.setDefaultCellRenders();

        //Configure tips
        ColumnHeaderToolTips tips = new ColumnHeaderToolTips();
        for (int c = 0; c < tableHeader.length; c++)
        {
            TableColumn col = getColumnModel().getColumn(c);
            tips.setToolTip(col, tableTips[c]);
        }
        this.getTableHeader().addMouseMotionListener(tips);

        this.updateTable();
    }

    public List<Object[]> getAllData()
    {
        final VisualizationState visualizationState = callback.getVisualizationState();

        final LinkedList<Object[]> allLayerData = new LinkedList<>();

        // TODO: Have layer order in mind.

        for (NetworkLayer networkLayer : netPlan.getNetworkLayers())
        {
            final Object[] layerData = new Object[tableHeader.length];

            final boolean isActiveLayer = networkLayer == netPlan.getNetworkLayerDefault();

            layerData[COLUMN_DOWN] = null;
            layerData[COLUMN_UP] = null;
            layerData[COLUMN_ID] = networkLayer.getId();
            layerData[COLUMN_NAME] = networkLayer.getName();
            layerData[COLUMN_SHOW_LAYER] = isActiveLayer || visualizationState.isLayerVisible(networkLayer);
            layerData[COLUMN_SHOW_LINK] = null;
            layerData[COLUMN_DEFAULT] = networkLayer == netPlan.getNetworkLayerDefault(); // NOTE: Should this go in the visualization state?

            allLayerData.add(layerData);
        }
        return allLayerData;
    }

    private DefaultTableModel createTableModel()
    {
        return new ClassAwareTableModel(new Object[1][tableHeader.length], tableHeader)
        {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                switch (columnIndex)
                {
                    case COLUMN_ID:
                    case COLUMN_NAME:
                        return false;
                    default:
                        return true;
                }
            }

            @Override
            public void setValueAt(Object newValue, int row, int column)
            {
                final Object oldValue = getValueAt(row, column);

                final VisualizationState visualizationState = callback.getVisualizationState();

                final NetworkLayer selectedLayer = netPlan.getNetworkLayerFromId(COLUMN_ID);

                switch (column)
                {
                    case COLUMN_SHOW_LAYER:
                        if (selectedLayer != netPlan.getNetworkLayerDefault())
                        {
                            visualizationState.setLayerVisibility(selectedLayer, (Boolean) newValue);
                        }

                        break;
                    case COLUMN_DEFAULT:
                        netPlan.setNetworkLayerDefault(selectedLayer);
                        break;
                }

                updateTable();
                callback.updateVisualizationAfterChanges(Collections.singleton(Constants.NetworkElementType.LAYER));
            }
        };
    }

    private synchronized void updateTable()
    {
        if (netPlan.getNumberOfLayers() > 0)
        {
            final List<Object[]> layerData = this.getAllData();

            // Setting up values
            this.tableModel.setDataVector(layerData.toArray(new Object[layerData.size()][tableHeader.length]), tableHeader);
        }
    }

    private void setDefaultCellRenders()
    {
        setDefaultRenderer(Boolean.class, new CellRenderers.CheckBoxRenderer());
        setDefaultRenderer(Double.class, new CellRenderers.NumberCellRenderer());
        setDefaultRenderer(Object.class, new CellRenderers.NonEditableCellRenderer());
        setDefaultRenderer(Float.class, new CellRenderers.NumberCellRenderer());
        setDefaultRenderer(Long.class, new CellRenderers.NumberCellRenderer());
        setDefaultRenderer(Integer.class, new CellRenderers.NumberCellRenderer());
        setDefaultRenderer(String.class, new CellRenderers.NonEditableCellRenderer());
    }
}
