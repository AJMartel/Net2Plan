package com.net2plan.gui.utils.viewEditTopolTables.specificTables;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultRowSorter;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import com.google.common.collect.Sets;
import com.net2plan.gui.utils.AdvancedJTable;
import com.net2plan.gui.utils.CellRenderers;
import com.net2plan.gui.utils.ClassAwareTableModel;
import com.net2plan.gui.utils.IVisualizationCallback;
import com.net2plan.gui.utils.StringLabeller;
import com.net2plan.gui.utils.WiderJComboBox;
import com.net2plan.gui.utils.viewEditTopolTables.ITableRowFilter;
import com.net2plan.gui.utils.viewEditTopolTables.specificTables.AdvancedJTable_NetworkElement.LastRowAggregatedValue;
import com.net2plan.gui.utils.viewEditTopolTables.tableVisualizationFilters.TBFToFromCarriedTraffic;
import com.net2plan.interfaces.networkDesign.Demand;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.interfaces.networkDesign.NetworkLayer;
import com.net2plan.interfaces.networkDesign.Node;
import com.net2plan.interfaces.networkDesign.Resource;
import com.net2plan.interfaces.networkDesign.Route;
import com.net2plan.internal.Constants;
import com.net2plan.internal.Constants.NetworkElementType;
import com.net2plan.internal.ErrorHandling;
import com.net2plan.utils.StringUtils;

/**
 * Created by César on 13/12/2016.
 */
@SuppressWarnings("unchecked")
public class AdvancedJTable_resource extends AdvancedJTable_NetworkElement
{

    public static final int COLUMN_ID = 0;
    public static final int COLUMN_INDEX = 1;
    public static final int COLUMN_NAME = 2;
    public static final int COLUMN_TYPE = 3;
    public static final int COLUMN_HOSTNODE = 4;
    public static final int COLUMN_CAPACITY = 5;
    public static final int COLUMN_CAPACITYMUNITS= 6;
    public static final int COLUMN_OCCUPIEDCAPACITY = 7;
    public static final int COLUMN_TRAVERSINGROUTES = 8;
    public static final int COLUMN_UPPERRESOURCES = 9;
    public static final int COLUMN_BASERESOURCES = 10;
    public static final int COLUMN_PROCESSINGTIME = 11;
    public static final int COLUMN_ATTRIBUTES = 12;
    private static final String netPlanViewTabName = "Resources";
    private static final String[] netPlanViewTableHeader = StringUtils.arrayOf("Unique Identifier","Index","Name","Type","Host Node","Capacity","Cap. Units","Ocuppied capacity","Traversing Routes","Upper Resources","Base Resources","Processing Time","Attributes");
    private static final String[] netPlanViewTableTips = StringUtils.arrayOf("Unique Identifier","Index","Name","Type","Host Node","Capacity","Cap. Units","Ocuppied capacity","Traversing Routes","Upper Resources","Base Resources","Processing Time","Attributes");
//    private final String[] resourceTypes = StringUtils.arrayOf("Firewall","NAT","CPU","RAM");

    public AdvancedJTable_resource(final IVisualizationCallback callback)
    {
        super(createTableModel(callback), callback, Constants.NetworkElementType.RESOURCE, true);
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
        setEnabled(true);
    }


    @Override
    public List<Object[]> getAllData(NetPlan currentState, ArrayList<String> attributesTitles) 
    {
        final List<Object[]> allResourceData = new LinkedList<Object[]>();
    	final List<Resource> rowVisibleResources = getVisibleElementsInTable ();
        for (Resource res : rowVisibleResources) 
        {
            Object[] resData = new Object[netPlanViewTableHeader.length + attributesTitles.size()];
            resData[COLUMN_ID] = res.getId();
            resData[COLUMN_INDEX] = res.getIndex();
            resData[COLUMN_NAME] = res.getName();
            resData[COLUMN_TYPE] = res.getType();
            resData[COLUMN_HOSTNODE] = res.getHostNode();
            resData[COLUMN_CAPACITY] = res.getCapacity();
            resData[COLUMN_CAPACITYMUNITS] = res.getCapacityMeasurementUnits();
            resData[COLUMN_OCCUPIEDCAPACITY] = res.getOccupiedCapacity();
            resData[COLUMN_TRAVERSINGROUTES] = joinTraversingRoutesWithTheirCapacities(res);
            resData[COLUMN_UPPERRESOURCES] = joinUpperResourcesWithTheirCapacities(res);
            resData[COLUMN_BASERESOURCES] = joinBaseResourcesWithTheirCapacities(res);
            resData[COLUMN_PROCESSINGTIME] = res.getProcessingTimeToTraversingTrafficInMs();
            resData[COLUMN_ATTRIBUTES] = StringUtils.mapToString(res.getAttributes());

            for(int i = netPlanViewTableHeader.length; i < netPlanViewTableHeader.length + attributesTitles.size();i++)
            {
                if(res.getAttributes().containsKey(attributesTitles.get(i-netPlanViewTableHeader.length)))
                {
                    resData[i] = res.getAttribute(attributesTitles.get(i-netPlanViewTableHeader.length));
                }
            }

            allResourceData.add(resData);

        }

        /* Add the aggregation row with the aggregated statistics */
        final double aggCapacity = rowVisibleResources.stream().mapToDouble(e->e.getCapacity()).sum();
        final double aggOccupiedCapacity = rowVisibleResources.stream().mapToDouble(e->e.getOccupiedCapacity()).sum();
        final int aggTravSCs = rowVisibleResources.stream().mapToInt(e->e.getTraversingRoutes().size()).sum();
        final double aggMaxProcTime = rowVisibleResources.stream().mapToDouble(e->e.getProcessingTimeToTraversingTrafficInMs()).max().orElse(0);
        final LastRowAggregatedValue[] aggregatedData = new LastRowAggregatedValue [netPlanViewTableHeader.length + attributesTitles.size()];
        Arrays.fill(aggregatedData, new LastRowAggregatedValue());
        aggregatedData [COLUMN_CAPACITY] = new LastRowAggregatedValue(aggCapacity);
        aggregatedData [COLUMN_OCCUPIEDCAPACITY] = new LastRowAggregatedValue(aggOccupiedCapacity);
        aggregatedData [COLUMN_TRAVERSINGROUTES] = new LastRowAggregatedValue(aggTravSCs);
        aggregatedData [COLUMN_PROCESSINGTIME] = new LastRowAggregatedValue(aggMaxProcTime);
        allResourceData.add(aggregatedData);

        return allResourceData;
    }

    @Override
    public String getTabName() {
        return netPlanViewTabName;
    }

    @Override
    public String[] getTableHeaders() {
        return netPlanViewTableHeader;
    }

    @Override
    public String[] getCurrentTableHeaders() {
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

    @Override
    public String[] getTableTips() {
        return netPlanViewTableTips;
    }

    @Override
    public boolean hasElements()
    {
    	final ITableRowFilter rf = callback.getVisualizationState().getTableRowFilter();
    	final NetworkLayer layer = callback.getDesign().getNetworkLayerDefault();
    	return rf == null? callback.getDesign().hasResources() : rf.hasResources(layer);
    }

    @Override
    public int getAttributesColumnIndex() {
        return COLUMN_ATTRIBUTES;
    }

//    @Override
//    public int[] getColumnsOfSpecialComparatorForSorting() {
//        return new int[]{5};
//    }

    private static TableModel createTableModel(final IVisualizationCallback callback)
    {
        TableModel resourceTableModel = new ClassAwareTableModel(new Object[1][netPlanViewTableHeader.length], netPlanViewTableHeader) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                if (!callback.getVisualizationState().isNetPlanEditable()) return false;
                if( columnIndex >= netPlanViewTableHeader.length) return true;
                if (getValueAt(rowIndex,columnIndex) == null) return false;

                return columnIndex == COLUMN_NAME || columnIndex == COLUMN_CAPACITY || columnIndex == COLUMN_PROCESSINGTIME;
            }

            @Override
            public void setValueAt(Object newValue, int row, int column) {
                Object oldValue = getValueAt(row, column);

				/* If value doesn't change, exit from function */
                if (newValue != null && newValue.equals(oldValue)) return;

                NetPlan netPlan = callback.getDesign();

                if (getValueAt(row, 0) == null) row = row - 1;
                final long resId = (Long) getValueAt(row, 0);
                final Resource res = netPlan.getResourceFromId(resId);

                try {
                    switch (column) {
                        case COLUMN_NAME:
                            res.setName(newValue.toString());
                        	callback.updateVisualizationAfterChanges(Sets.newHashSet(NetworkElementType.RESOURCE));
                        	callback.getVisualizationState ().pickResource(res);
                            callback.updateVisualizationAfterPick();
                            callback.getUndoRedoNavigationManager().updateNavigationInformation_newNetPlanChange();
                            break;

                        case COLUMN_CAPACITY:
                            if (newValue == null) return;
                            res.setCapacity((Double)newValue,  netPlan.getResourceFromId(resId).getCapacityOccupiedInBaseResourcesMap());
                        	callback.updateVisualizationAfterChanges(Sets.newHashSet(NetworkElementType.RESOURCE));
                        	callback.getVisualizationState ().pickResource(res);
                            callback.updateVisualizationAfterPick();
                            callback.getUndoRedoNavigationManager().updateNavigationInformation_newNetPlanChange();
                            break;

                        case COLUMN_PROCESSINGTIME:
                            if(newValue == null) return;
                            res.setProcessingTimeToTraversingTrafficInMs((Double)newValue);
                        	callback.updateVisualizationAfterChanges(Sets.newHashSet(NetworkElementType.RESOURCE));
                        	callback.getVisualizationState ().pickResource(res);
                            callback.updateVisualizationAfterPick();
                            callback.getUndoRedoNavigationManager().updateNavigationInformation_newNetPlanChange();
                            break;

                        default:
                            break;
                    }
                } catch (Throwable ex) {
                    ex.printStackTrace();
                    ErrorHandling.showErrorDialog(ex.getMessage(), "Error modifying resource");
                    return;
                }

				/* Set new value */
                super.setValueAt(newValue, row, column);
            }
        };

        return resourceTableModel;
    }
    private void setSpecificCellRenderers() {
    }

    private void setDefaultCellRenderers(final IVisualizationCallback callback) {
        setDefaultRenderer(Boolean.class, new CellRenderers.CheckBoxRenderer());
        setDefaultRenderer(Double.class, new CellRenderers.NumberCellRenderer());
        setDefaultRenderer(Object.class, new CellRenderers.NonEditableCellRenderer());
        setDefaultRenderer(Float.class, new CellRenderers.NumberCellRenderer());
        setDefaultRenderer(Long.class, new CellRenderers.NumberCellRenderer());
        setDefaultRenderer(Integer.class, new CellRenderers.NumberCellRenderer());
        setDefaultRenderer(String.class, new CellRenderers.NonEditableCellRenderer());
    }
    
    @Override
    public void setColumnRowSortingFixedAndNonFixedTable() 
    {
        setAutoCreateRowSorter(true);
        final Set<Integer> columnsWithDoubleAndThenParenthesis = Sets.newHashSet(COLUMN_CAPACITY);
        DefaultRowSorter rowSorter = ((DefaultRowSorter) getRowSorter());
        for (int col = 0; col <= COLUMN_ATTRIBUTES ; col ++)
        	rowSorter.setComparator(col, new AdvancedJTable_NetworkElement.ColumnComparator(rowSorter , columnsWithDoubleAndThenParenthesis.contains(col)));
        fixedTable.setAutoCreateRowSorter(true);
        fixedTable.setRowSorter(this.getRowSorter());
        rowSorter = ((DefaultRowSorter) fixedTable.getRowSorter());
        for (int col = 0; col <= COLUMN_ATTRIBUTES ; col ++)
        	rowSorter.setComparator(col, new AdvancedJTable_NetworkElement.ColumnComparator(rowSorter , columnsWithDoubleAndThenParenthesis.contains(col)));
    }

    @Override
    public int getNumFixedLeftColumnsInDecoration() {
        return 2;
    }

    @Override
    public ArrayList<String> getAttributesColumnsHeaders() {
        ArrayList<String> attColumnsHeaders = new ArrayList<>();
        for(Resource res : getVisibleElementsInTable())
        {

            for (Map.Entry<String, String> entry : res.getAttributes().entrySet())
            {
                if(attColumnsHeaders.contains(entry.getKey()) == false)
                {
                    attColumnsHeaders.add(entry.getKey());
                }

            }

        }

        return attColumnsHeaders;
    }

    private String joinTraversingRoutesWithTheirCapacities(Resource res)
    {
        Map<Route, Double> routesCapacities = res.getTraversingRouteOccupiedCapacityMap();
        String t = "";
        int counter = 0;
        for(Map.Entry<Route, Double> entry : routesCapacities.entrySet())
        {
            if(counter == routesCapacities.size() - 1)
                t = t + "R"+entry.getKey().getIndex()+" ("+entry.getValue()+") ";
            else
                t = t + "R"+entry.getKey().getIndex()+" ("+entry.getValue()+"), ";

            counter++;
        }

        return t;
    }

    private String joinUpperResourcesWithTheirCapacities(Resource res)
    {
        Map<Resource, Double> upperResourcesCapacities = res.getCapacityOccupiedByUpperResourcesMap();
        String t = "";
        int counter = 0;
        for(Map.Entry<Resource, Double> entry : upperResourcesCapacities.entrySet())
        {
            if(counter == upperResourcesCapacities.size() - 1)
                t = t + "r"+entry.getKey().getIndex()+" ("+entry.getValue()+") ";
            else
                t = t + "r"+entry.getKey().getIndex()+" ("+entry.getValue()+"), ";

            counter++;
        }

        return t;
    }

    private String joinBaseResourcesWithTheirCapacities(Resource res)
    {
        Map<Resource, Double> baseResourcesCapacities = res.getCapacityOccupiedInBaseResourcesMap();
        String t = "";
        int counter = 0;
        for(Map.Entry<Resource, Double> entry : baseResourcesCapacities.entrySet())
        {
            if(counter == baseResourcesCapacities.size() - 1)
                t = t + "r"+entry.getKey().getIndex()+" ("+entry.getValue()+") ";
            else
                t = t + "r"+entry.getKey().getIndex()+" ("+entry.getValue()+"), ";

            counter++;
        }

        return t;

    }
    @Override
    public void doPopup(MouseEvent e, int row, Object itemId) {

        JPopupMenu popup = new JPopupMenu();
        final ITableRowFilter rf = callback.getVisualizationState().getTableRowFilter();
        final List<Resource> rowsInTheTable = getVisibleElementsInTable();

        /* Add the popup menu option of the filters */
        final List<Resource> selectedResources = (List<Resource>) (List<?>) getSelectedElements().getFirst();
        if (!selectedResources.isEmpty()) 
        {
        	final JMenu submenuFilters = new JMenu ("Filters");
            final JMenuItem filterKeepElementsAffectedThisLayer = new JMenuItem("This layer: Keep elements associated to this resource traffic");
            final JMenuItem filterKeepElementsAffectedAllLayers = new JMenuItem("All layers: Keep elements associated to this resource traffic");
            submenuFilters.add(filterKeepElementsAffectedThisLayer);
            if (callback.getDesign().getNumberOfLayers() > 1) submenuFilters.add(filterKeepElementsAffectedAllLayers);
            filterKeepElementsAffectedThisLayer.addActionListener(new ActionListener() 
            {
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					if (selectedResources.size() > 1) throw new RuntimeException ();
					TBFToFromCarriedTraffic filter = new TBFToFromCarriedTraffic(selectedResources.get(0), callback.getDesign().getNetworkLayerDefault() , true);
					callback.getVisualizationState().updateTableRowFilter(filter);
					callback.updateVisualizationJustTables();
				}
			});
            filterKeepElementsAffectedAllLayers.addActionListener(new ActionListener() 
            {
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					if (selectedResources.size() > 1) throw new RuntimeException ();
					TBFToFromCarriedTraffic filter = new TBFToFromCarriedTraffic(selectedResources.get(0), callback.getDesign().getNetworkLayerDefault() , false);
					callback.getVisualizationState().updateTableRowFilter(filter);
					callback.updateVisualizationJustTables();
				}
			});
            popup.add(submenuFilters);
            popup.addSeparator();
        }

        if (callback.getVisualizationState().isNetPlanEditable()) {
            popup.add(getAddOption());
            for (JComponent item : getExtraAddOptions())
                popup.add(item);
        }

        if (!rowsInTheTable.isEmpty()) 
        {
            if (callback.getVisualizationState().isNetPlanEditable()) {
                if (row != -1) {
                    if (popup.getSubElements().length > 0) popup.addSeparator();

                    JMenuItem removeItem = new JMenuItem("Remove " + networkElementType);
                    removeItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) 
                        {
                            try {
                                callback.getDesign().getResourceFromId((Long)itemId).remove();
                                callback.getVisualizationState().resetPickedState();
                            	callback.updateVisualizationAfterChanges(Sets.newHashSet(NetworkElementType.RESOURCE));
                            	callback.getUndoRedoNavigationManager().updateNavigationInformation_newNetPlanChange();
                            } catch (Throwable ex) {
                                ErrorHandling.addErrorOrException(ex, getClass());
                                ErrorHandling.showErrorDialog("Unable to remove " + networkElementType);
                            }
                        }
                    });

                    popup.add(removeItem);
                    addPopupMenuAttributeOptions(e, row, itemId, popup);
                }
                JMenuItem removeItemsOfAType = new JMenuItem("Remove all table "+networkElementType+"s of a type");
                removeItemsOfAType.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        NetPlan netPlan = callback.getDesign();
                        JComboBox typeSelector = new WiderJComboBox();
                        final Set<String> resourceTypes = rowsInTheTable.stream().map(ee->ee.getType()).collect(Collectors.toSet());
                        for(String type : resourceTypes)
                            typeSelector.addItem(type);
                        try{
                            JPanel pane = new JPanel();
                            pane.add(new JLabel("Resource Type"));
                            pane.add(typeSelector);
                            while (true) {
                                int result = JOptionPane.showConfirmDialog(null, pane, "Please choose the type of "+ networkElementType+" to remove", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                                if (result != JOptionPane.OK_OPTION) return;
                                final String typeToRemove = typeSelector.getSelectedItem().toString();
                                final List<Resource> resourcesToRemove = rowsInTheTable.stream().filter(r->r.getType().equals(typeToRemove)).collect(Collectors.toList());
                                for(Resource res : resourcesToRemove)
                                    res.remove();
                                callback.getVisualizationState().resetPickedState();
                            	callback.updateVisualizationAfterChanges(Sets.newHashSet(NetworkElementType.RESOURCE));
                            	callback.getUndoRedoNavigationManager().updateNavigationInformation_newNetPlanChange();
                                break;
                            }
                        }catch (Throwable ex) {
                            ErrorHandling.addErrorOrException(ex, getClass());
                            ErrorHandling.showErrorDialog("Unable to remove " + networkElementType+"s");
                        }

                    }
                });
                popup.add(removeItemsOfAType);
                JMenuItem removeItems = new JMenuItem("Remove all table " + networkElementType + "s");

                removeItems.addActionListener(new ActionListener() 
                {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        NetPlan netPlan = callback.getDesign();

                        try 
                        {
                        	if (rf == null)
                        		netPlan.removeAllResources();
                        	else
                        		for (Resource r : rowsInTheTable) r.remove();
                            callback.getVisualizationState().resetPickedState();
                        	callback.updateVisualizationAfterChanges(Sets.newHashSet(NetworkElementType.RESOURCE));
                        	callback.getUndoRedoNavigationManager().updateNavigationInformation_newNetPlanChange();
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

    private JMenuItem getAddOption() {
        JMenuItem addItem = addItem = new JMenuItem("Add " + networkElementType);
        addItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                NetPlan netPlan = callback.getDesign();

                try {

                    JComboBox hostNodeSelector = new WiderJComboBox();
                    JTextField capUnitsField = new JTextField(20);
                    JTextField typeSelector = new JTextField(20);

                    for (Node n : netPlan.getNodes()) {
                        final String nodeName = n.getName();
                        String nodeLabel = "Node " + n.getIndex();
                        if (!nodeName.isEmpty()) nodeLabel += " (" + nodeName + ")";
                        hostNodeSelector.addItem(StringLabeller.of(n.getId(), nodeLabel));
                    }
                    Node hostNode = null;
                    String capacityUnits = "";
                    String resType = "";
                    JPanel pane = new JPanel();
                    pane.add(new JLabel("Resource Type"));
                    pane.add(typeSelector);
                    pane.add(Box.createHorizontalStrut(30));
                    pane.add(new JLabel("Host Node"));
                    pane.add(hostNodeSelector);
                    pane.add(Box.createHorizontalStrut(30));
                    pane.add(new JLabel("Capacity Units"));
                    pane.add(capUnitsField);
                    while (true) {
                        int result = JOptionPane.showConfirmDialog(null, pane, "Please enter parameters for the new " + networkElementType, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                        if (result != JOptionPane.OK_OPTION) return;
                        hostNode = netPlan.getNodeFromId((Long) ((StringLabeller) hostNodeSelector.getSelectedItem()).getObject());
                        capacityUnits = capUnitsField.getText();
                        resType = typeSelector.getText();
                        break;
                    }
                    JPanel panel = new JPanel();
                    Object [][] data = {null,null,null};
                    String [] headers = StringUtils.arrayOf("Base Resource","Is Base Resource","Capacity");
                    TableModel tm = new ClassAwareTableModelImpl(data,headers,new HashSet<Integer>(Arrays.asList(1,2)));
                    AdvancedJTable table = new AdvancedJTable(tm);
                    int baseResCounter = 0;
                    for(Resource r : netPlan.getResources())
                    {
                        if(r.getHostNode().toString().equals(hostNode.toString()))
                        {
                            baseResCounter++;
                        }
                    }
                    Object[][] newData = new Object[baseResCounter][headers.length];
                    int counter = 0;
                    for(Resource r : netPlan.getResources())
                    {
                        if(r.getHostNode().toString().equals(hostNode.toString())) {
                            newData[counter][0] = r.getName();
                            newData[counter][1] = false;
                            newData[counter][2] = 0;
                            addCheckboxCellEditor(false, counter, 1, table);
                            counter++;
                        }
                    }
                    panel.setLayout(new BorderLayout());
                    panel.add(new JLabel("Set new resource base resources"),BorderLayout.NORTH);
                    panel.add(new JScrollPane(table),BorderLayout.CENTER);
                    ((DefaultTableModel)table.getModel()).setDataVector(newData, headers);
                    while (true) {
                        int result = JOptionPane.showConfirmDialog(null, panel, "Set base resources", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                        if (result != JOptionPane.OK_OPTION) return;
                        Map<Resource, Double> newBaseResources = new HashMap<>();
                        for(int j = 0; j < table.getRowCount(); j++)
                        {
                            Resource r = netPlan.getResource(j);
                            String capacity = table.getModel().getValueAt(j,2).toString();
                            boolean isBaseResource = (boolean)table.getModel().getValueAt(j,1);
                            if(isBaseResource)
                                newBaseResources.put(r, Double.parseDouble(capacity));

                        }
                        netPlan.addResource(resType, "Resource n_" + netPlan.getResources().size(), hostNode,
                                0, capacityUnits, newBaseResources, 0, null);
                        callback.getVisualizationState().resetPickedState();
                    	callback.updateVisualizationAfterChanges(Sets.newHashSet(NetworkElementType.RESOURCE));
                    	callback.getUndoRedoNavigationManager().updateNavigationInformation_newNetPlanChange();
                        break;
                    }
                }catch (Throwable ex) {
                    ErrorHandling.showErrorDialog(ex.getMessage(), "Unable to add " + networkElementType);
                }
            }
        });
        return addItem;
    }

    private List<JComponent> getExtraAddOptions() {
        List<JComponent> options = new LinkedList<JComponent>();

        return options;
    }

    private List<JComponent> getExtraOptions(final int row, final Object itemId) {
        List<JComponent> options = new LinkedList<JComponent>();
        final List<Resource> rowsInTheTable = getVisibleElementsInTable();

        JMenuItem capacityInBaseResources = new JMenuItem("Set capacity to base resources");
        capacityInBaseResources.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                NetPlan netPlan = callback.getDesign();

                    try 
                    {

                        Resource res = netPlan.getResourceFromId((Long) itemId);
                        List<Resource> baseResources = new LinkedList<>(res.getBaseResources());
                        if(baseResources.size() == 0)
                        {
                            JOptionPane.showMessageDialog(null,"This resource hasn't any base resource");
                            return;
                        }
                        JPanel pane = new JPanel();
                        Object [][] data = {null,null,null};
                        String [] headers = StringUtils.arrayOf("Index","Type" , "Capacity");
                        TableModel tm = new ClassAwareTableModelImpl(data,headers,new HashSet<Integer>(Arrays.asList(2)));
                        AdvancedJTable table = new AdvancedJTable(tm);
                        Object[][] newData = new Object[baseResources.size()][headers.length];
                        int counter = 0;
                        for(Resource r : baseResources)
                        {
                            newData[counter][0] = r.getIndex();
                            newData[counter][1] = r.getType();
                            newData[counter][2] = res.getCapacityOccupiedInBaseResource(r);
                            counter++;
                        }
                        pane.setLayout(new BorderLayout());
                        pane.add(new JLabel("Setting the occupied capacity in base resource of index: " + res.getIndex()) , BorderLayout.NORTH);
                        pane.add(new JScrollPane(table) , BorderLayout.CENTER);
                        ((DefaultTableModel)table.getModel()).setDataVector(newData, headers);
                        int result = JOptionPane.showConfirmDialog(null, pane, "Set capacity to base resources", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                        if (result != JOptionPane.OK_OPTION) return;
                        Map<Resource,Double> newCapMap = new HashMap<> (); 
                        for (int t = 0 ; t < table.getRowCount() ; t ++)
                        	newCapMap.put(baseResources.get(t) , Double.parseDouble((String) table.getModel().getValueAt(t,2)));
                        res.setCapacity(res.getCapacity() , newCapMap);
                        callback.getVisualizationState().resetPickedState();
                        callback.updateVisualizationAfterChanges(Sets.newHashSet(NetworkElementType.RESOURCE));
                        callback.getUndoRedoNavigationManager().updateNavigationInformation_newNetPlanChange();
                    } catch (Throwable ex) {
                        ErrorHandling.showErrorDialog(ex.getMessage(), "Unable to set capacity to base resources");
                    }
                }


        });
        options.add(capacityInBaseResources);

        JMenuItem setCapacityToAll = new JMenuItem("Set capacity to all (base resources occupation unchanged)");
        setCapacityToAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                NetPlan netPlan = callback.getDesign();
                double cap;
                while (true) {
                    String str = JOptionPane.showInputDialog(null, "Capacity value", "Set capacity to all table resources", JOptionPane.QUESTION_MESSAGE);
                    if (str == null) return;

                    try {
                        cap = Double.parseDouble(str);
                        if (cap < 0) throw new RuntimeException();

                        break;
                    } catch (Throwable ex) {
                        ErrorHandling.showErrorDialog("Please, introduce a non-negative number", "Error setting capacity");
                    }
                }

                try {
                    for(Resource r : rowsInTheTable)
                    		r.setCapacity(cap , null);
                    callback.getVisualizationState().resetPickedState();
                	callback.updateVisualizationAfterChanges(Sets.newHashSet(NetworkElementType.RESOURCE));
                	callback.getUndoRedoNavigationManager().updateNavigationInformation_newNetPlanChange();
                } catch (Throwable ex) {
                    ErrorHandling.showErrorDialog(ex.getMessage(), "Unable to set capacity to resources");
                }
            }
        });
        options.add(setCapacityToAll);

        JMenuItem setTraversingTimeToAll = new JMenuItem("Set processing time to all table resources");
        setTraversingTimeToAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                NetPlan netPlan = callback.getDesign();
                double procTime;
                while (true) {
                    String str = JOptionPane.showInputDialog(null, "Processing time in miliseconds value", "Set processing time in miliseconds to all resources", JOptionPane.QUESTION_MESSAGE);
                    if (str == null) return;

                    try {
                        procTime = Double.parseDouble(str);
                        if (procTime < 0) throw new RuntimeException();

                        break;
                    } catch (Throwable ex) {
                        ErrorHandling.showErrorDialog("Please, introduce a non-negative number", "Error setting processing time");
                    }
                }

                try {
                    for(Resource r : rowsInTheTable)
                    		r.setProcessingTimeToTraversingTrafficInMs(procTime);
                    callback.getVisualizationState().resetPickedState();
                	callback.updateVisualizationAfterChanges(Sets.newHashSet(NetworkElementType.RESOURCE));
                	callback.getUndoRedoNavigationManager().updateNavigationInformation_newNetPlanChange();
                } catch (Throwable ex) {
                    ErrorHandling.showErrorDialog(ex.getMessage(), "Unable to set processing time to resources");
                }
            }
        });
        options.add(setTraversingTimeToAll);

        return options;
    }

    private List<JComponent> getForcedOptions() {
        return new LinkedList<JComponent>();
    }


    @Override
    public void showInCanvas(MouseEvent e, Object itemId) 
    {
        if (getVisibleElementsInTable().isEmpty()) return;
    	callback.getVisualizationState ().pickResource(callback.getDesign().getResourceFromId((long) itemId));
        callback.updateVisualizationAfterPick();
        callback.getUndoRedoNavigationManager().updateNavigationInformation_onlyVisualizationChange();
    }

    private class ClassAwareTableModelImpl extends ClassAwareTableModel
    {
    	private final Set<Integer> editableColumns;
        public ClassAwareTableModelImpl(Object[][] dataVector, Object[] columnIdentifiers , Set<Integer> editableColumns)
        {
            super(dataVector, columnIdentifiers);
            this.editableColumns = editableColumns;
        }


        @Override
        public Class getColumnClass(int col)
        {
            return Object.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            return (this.editableColumns.contains(columnIndex));
        }

        @Override
        public void setValueAt(Object value, int row, int column)
        {
            super.setValueAt(value, row, column);

        }
    }

    private void addCheckboxCellEditor(boolean defaultValue, int rowIndex, int columnIndex, AdvancedJTable table)
    {
        JCheckBox checkBox = new JCheckBox();
        checkBox.setHorizontalAlignment(JLabel.CENTER);
        checkBox.setSelected(defaultValue);
        table.setCellEditor(rowIndex, columnIndex, new DefaultCellEditor(checkBox));
        table.setCellRenderer(rowIndex, columnIndex, new CheckBoxRenderer());
    }

    private static class CheckBoxRenderer extends JCheckBox implements TableCellRenderer
    {
        public CheckBoxRenderer()
        {
            setHorizontalAlignment(JLabel.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
        {
            if (isSelected)
            {
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            } else
            {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }

            setSelected(value != null && Boolean.parseBoolean(value.toString()));
            return this;
        }
    }

    private List<Resource> getVisibleElementsInTable ()
    {
    	final ITableRowFilter rf = callback.getVisualizationState().getTableRowFilter();
    	final NetworkLayer layer = callback.getDesign().getNetworkLayerDefault();
    	return rf == null? callback.getDesign().getResources() : rf.getVisibleResources(layer);
    }
}
