package com.net2plan.gui.utils.topologyPane;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Shape;
import java.awt.Stroke;
import java.util.*;
import java.util.Map.Entry;

import com.net2plan.interfaces.networkDesign.Link;
import com.net2plan.interfaces.networkDesign.MulticastDemand;
import com.net2plan.interfaces.networkDesign.MulticastTree;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.interfaces.networkDesign.NetworkLayer;
import com.net2plan.interfaces.networkDesign.Node;
import com.net2plan.utils.Pair;
import org.apache.commons.collections15.MapUtils;


public class VisualizationState
{
    private boolean showNodeNames;
    private boolean showLinkLabels;
    private boolean showLinksInNonActiveLayer;
    private boolean showInterLayerLinks;
    private boolean showLowerLayerPropagation;
    private boolean showUpperLayerPropagation;
    private boolean showNonConnectedNodes;
    private double interLayerSpaceInNetPlanCoordinates;
    private boolean isNetPlanEditable;
    private NetPlan currentNp;
    private Set<Node> nonVisibleNodes;
    private Set<Link> nonVisibleLinks;

    /* This only can be changed calling to rebuild */
    private Map<NetworkLayer, Integer> mapLayer2VisualizationOrder; // as many entries as layers
    private Map<NetworkLayer, Boolean> mapLayer2Visibility;
    private Map<NetworkLayer, Boolean> mapShowLayerLinks;

    /* These need is recomputed inside a rebuild */
    private Map<Node, Set<GUILink>> cache_intraNodeGUILinks;
    private Map<Link, GUILink> cache_regularLinkMap;
    private Map<NetworkLayer, Integer> cache_mapVisibleLayer2VisualizationOrder; // as many elements as visible layers
    private Map<Node, Map<Pair<Integer, Integer>, GUILink>> cache_mapNode2IntraNodeGUILinkMap; // integers are orders of REAL VISIBLE LAYERS
    private Map<Node, List<GUINode>> cache_mapNode2ListVerticallyStackedGUINodes;


    public NetPlan getNetPlan()
    {
        return currentNp;
    }

    public VisualizationState(NetPlan currentNp, Map<NetworkLayer, Integer> mapLayer2VisualizationOrder, Map<NetworkLayer, Boolean> layerVisibilityMap)
    {
        this.currentNp = currentNp;
        this.showNodeNames = false;
        this.showLinkLabels = false;
        this.showLinksInNonActiveLayer = true;
        this.showInterLayerLinks = true;
        this.showNonConnectedNodes = true;
        this.isNetPlanEditable = true;
        this.showLowerLayerPropagation = false;
        this.showUpperLayerPropagation = false;
        this.nonVisibleNodes = new HashSet<>();
        this.nonVisibleLinks = new HashSet<>();
        this.mapShowLayerLinks = new HashMap<>();
        updateLayerVisualizationState(currentNp, mapLayer2VisualizationOrder, layerVisibilityMap);
    }

    public boolean isVisible(GUINode gn)
    {
        final Node n = gn.getAssociatedNetPlanNode();
        if (nonVisibleNodes.contains(n)) return false;
        if (showNonConnectedNodes) return true;
        if (showInterLayerLinks && (getNumberOfVisibleLayers() > 1)) return true;
        if (n.getOutgoingLinks(gn.getLayer()).size() > 0) return true;
        if (n.getIncomingLinks(gn.getLayer()).size() > 0) return true;
        return false;
    }

    public boolean isVisible(GUILink gl)
    {
        if (gl.isIntraNodeLink()) return showInterLayerLinks;
        final Link e = gl.getAssociatedNetPlanLink();

        if (!mapShowLayerLinks.get(e.getLayer())) return false;
        if (nonVisibleLinks.contains(e)) return false;
        final boolean inActiveLayer = e.getLayer() == currentNp.getNetworkLayerDefault();
        if (!showLinksInNonActiveLayer && !inActiveLayer) return false;
        return true;
    }

    /**
     * @return the interLayerSpaceInNetPlanCoordinates
     */
    public double getInterLayerSpaceInNetPlanCoordinates()
    {
        return interLayerSpaceInNetPlanCoordinates;
    }

    /**
     * @param interLayerSpaceInNetPlanCoordinates the interLayerSpaceInNetPlanCoordinates to set
     */
    public void setInterLayerSpaceInNetPlanCoordinates(double interLayerSpaceInNetPlanCoordinates)
    {
        this.interLayerSpaceInNetPlanCoordinates = interLayerSpaceInNetPlanCoordinates;
    }

    /**
     * @return the showInterLayerLinks
     */
    public boolean isShowInterLayerLinks()
    {
        return showInterLayerLinks;
    }

    /**
     * @param showInterLayerLinks the showInterLayerLinks to set
     */
    public void setShowInterLayerLinks(boolean showInterLayerLinks)
    {
        this.showInterLayerLinks = showInterLayerLinks;
    }

    /**
     * @return the isNetPlanEditable
     */
    public boolean isNetPlanEditable()
    {
        return isNetPlanEditable;
    }

    /**
     * @param isNetPlanEditable the isNetPlanEditable to set
     */
    public void setNetPlanEditable(boolean isNetPlanEditable)
    {
        this.isNetPlanEditable = isNetPlanEditable;
    }

    public List<GUINode> getVerticallyStackedGUINodes(Node n)
    {
        return cache_mapNode2ListVerticallyStackedGUINodes.get(n);
    }

    public GUINode getAssociatedGUINode(Node n, NetworkLayer layer)
    {
        final Integer trueVisualizationIndex = cache_mapVisibleLayer2VisualizationOrder.get(layer);
        if (trueVisualizationIndex == null) return null;
        return getVerticallyStackedGUINodes(n).get(trueVisualizationIndex);
    }

    public GUILink getAssociatedGUILink(Link e)
    {
        return cache_regularLinkMap.get(e);
    }

    public Pair<Set<GUILink>, Set<GUILink>> getAssociatedGUILinksIncludingCoupling(Link e, boolean regularLinkIsPrimary)
    {
        Set<GUILink> resPrimary = new HashSet<>();
        Set<GUILink> resBackup = new HashSet<>();
        if (regularLinkIsPrimary) resPrimary.add(getAssociatedGUILink(e));
        else resBackup.add(getAssociatedGUILink(e));
        if (!e.isCoupled()) return Pair.of(resPrimary, resBackup);
        if (e.getCoupledDemand() != null)
        {
            /* add the intranode links */
            final NetworkLayer upperLayer = e.getLayer();
            final NetworkLayer downLayer = e.getCoupledDemand().getLayer();
            if (regularLinkIsPrimary)
            {
                resPrimary.addAll(getIntraNodeGUILinkSequence(e.getOriginNode(), upperLayer, downLayer));
                resPrimary.addAll(getIntraNodeGUILinkSequence(e.getDestinationNode(), downLayer, upperLayer));
            } else
            {
                resBackup.addAll(getIntraNodeGUILinkSequence(e.getOriginNode(), upperLayer, downLayer));
                resBackup.addAll(getIntraNodeGUILinkSequence(e.getDestinationNode(), downLayer, upperLayer));
            }

			/* add the regular links */
            Pair<Set<Link>, Set<Link>> traversedLinks = e.getCoupledDemand().getLinksThisLayerPotentiallyCarryingTraffic(true);
            for (Link ee : traversedLinks.getFirst())
            {
                Pair<Set<GUILink>, Set<GUILink>> pairGuiLinks = getAssociatedGUILinksIncludingCoupling(ee, true);
                if (regularLinkIsPrimary) resPrimary.addAll(pairGuiLinks.getFirst());
                else resBackup.addAll(pairGuiLinks.getFirst());
                resBackup.addAll(pairGuiLinks.getSecond());
            }
            for (Link ee : traversedLinks.getSecond())
            {
                Pair<Set<GUILink>, Set<GUILink>> pairGuiLinks = getAssociatedGUILinksIncludingCoupling(ee, false);
                resPrimary.addAll(pairGuiLinks.getFirst());
                resBackup.addAll(pairGuiLinks.getSecond());
            }
        } else if (e.getCoupledMulticastDemand() != null)
        {
            /* add the intranode links */
            final NetworkLayer upperLayer = e.getLayer();
            final MulticastDemand lowerLayerDemand = e.getCoupledMulticastDemand();
            final NetworkLayer downLayer = lowerLayerDemand.getLayer();
            if (regularLinkIsPrimary)
            {
                resPrimary.addAll(getIntraNodeGUILinkSequence(lowerLayerDemand.getIngressNode(), upperLayer, downLayer));
                resPrimary.addAll(getIntraNodeGUILinkSequence(lowerLayerDemand.getIngressNode(), downLayer, upperLayer));
                for (Node n : lowerLayerDemand.getEgressNodes())
                {
                    resPrimary.addAll(getIntraNodeGUILinkSequence(n, upperLayer, downLayer));
                    resPrimary.addAll(getIntraNodeGUILinkSequence(n, downLayer, upperLayer));
                }
            } else
            {
                resBackup.addAll(getIntraNodeGUILinkSequence(lowerLayerDemand.getIngressNode(), upperLayer, downLayer));
                resBackup.addAll(getIntraNodeGUILinkSequence(lowerLayerDemand.getIngressNode(), downLayer, upperLayer));
                for (Node n : lowerLayerDemand.getEgressNodes())
                {
                    resBackup.addAll(getIntraNodeGUILinkSequence(n, upperLayer, downLayer));
                    resBackup.addAll(getIntraNodeGUILinkSequence(n, downLayer, upperLayer));
                }
            }

            for (MulticastTree t : lowerLayerDemand.getMulticastTrees())
                for (Link ee : t.getLinkSet())
                {
                    Pair<Set<GUILink>, Set<GUILink>> pairGuiLinks = getAssociatedGUILinksIncludingCoupling(ee, true);
                    resPrimary.addAll(pairGuiLinks.getFirst());
                    resBackup.addAll(pairGuiLinks.getSecond());
                }
        }
        return Pair.of(resPrimary, resBackup);
    }

    public GUILink getIntraNodeGUILink(Node n, NetworkLayer from, NetworkLayer to)
    {
        final Integer fromRealVIndex = cache_mapVisibleLayer2VisualizationOrder.get(from);
        final Integer toRealVIndex = cache_mapVisibleLayer2VisualizationOrder.get(to);
        if ((fromRealVIndex == null) || (toRealVIndex == null)) return null;
        return cache_mapNode2IntraNodeGUILinkMap.get(n).get(Pair.of(fromRealVIndex, toRealVIndex));
    }

    public Set<GUILink> getIntraNodeGUILinks(Node n)
    {
        return cache_intraNodeGUILinks.get(n);
    }

    public List<GUILink> getIntraNodeGUILinkSequence(Node n, NetworkLayer from, NetworkLayer to)
    {
        if (from.getNetPlan() != currentNp) throw new RuntimeException("Bad");
        if (to.getNetPlan() != currentNp) throw new RuntimeException("Bad");
        final Integer fromRealVIndex = cache_mapVisibleLayer2VisualizationOrder.get(from);
        final Integer toRealVIndex = cache_mapVisibleLayer2VisualizationOrder.get(to);

        final List<GUILink> res = new LinkedList<>();
        if ((fromRealVIndex == null) || (toRealVIndex == null)) return res;
        if (fromRealVIndex == toRealVIndex) return res;
        final int increment = toRealVIndex > fromRealVIndex ? 1 : -1;
        int vLayerIndex = fromRealVIndex;
        do
        {
            final int origin = vLayerIndex;
            final int destination = vLayerIndex + increment;
            res.add(cache_mapNode2IntraNodeGUILinkMap.get(n).get(Pair.of(origin, destination)));
            vLayerIndex += increment;
        } while (vLayerIndex != toRealVIndex);

        return res;
    }

    public LinkedList<NetworkLayer> getLayersInVisualizationOrder(boolean includeInvisibleLayers)
    {
        final Map<Integer, NetworkLayer> layerToOrderMap = MapUtils.invertMap(includeInvisibleLayers ? mapLayer2VisualizationOrder : cache_mapVisibleLayer2VisualizationOrder);

        final LinkedList<NetworkLayer> orderedNetworkLayers = new LinkedList<>();
        for (int i = 0; i < layerToOrderMap.size(); i++)
        {
            orderedNetworkLayers.add(layerToOrderMap.get(i));
        }

        return orderedNetworkLayers;
    }

    public void updateLayerVisualizationState(NetPlan newCurrentNetPlan)
    {
        updateLayerVisualizationState(newCurrentNetPlan, this.mapLayer2VisualizationOrder, this.mapLayer2Visibility);
    }

    public void updateLayerVisualizationState(NetPlan currentNetPlan, Map<NetworkLayer, Integer> layerVisibilityOrderMap)
    {
        updateLayerVisualizationState(currentNetPlan, layerVisibilityOrderMap, this.mapLayer2Visibility);
    }

    public void updateLayerVisualizationState(NetPlan newCurrentNetPlan, Map<NetworkLayer, Integer> layerVisibilityOrderMap,
                                              Map<NetworkLayer, Boolean> mapLayerVisibility)
    {
        if (newCurrentNetPlan == null) throw new RuntimeException("Trying to update an empty topology");

        final boolean netPlanChanged = this.currentNp != newCurrentNetPlan;

        this.currentNp = newCurrentNetPlan;
        this.mapLayer2VisualizationOrder = layerVisibilityOrderMap;
        this.mapLayer2Visibility = new HashMap<>(mapLayerVisibility);

        if (!mapLayer2VisualizationOrder.keySet().equals(new HashSet<>(currentNp.getNetworkLayers())) || !mapLayerVisibility.keySet().equals(new HashSet<>(currentNp.getNetworkLayers())))
        {
            throw new RuntimeException();
        }

        /* Just in case the layers have changed */
        for (NetworkLayer layer : currentNp.getNetworkLayers())
            if (!mapShowLayerLinks.keySet().contains(layer))
                this.mapShowLayerLinks.put(layer, true);

		/* Update the interlayer space */
        this.interLayerSpaceInNetPlanCoordinates = getDefaultVerticalDistanceForInterLayers();

        if (netPlanChanged)
        {
            nonVisibleNodes = new HashSet<>();
            nonVisibleLinks = new HashSet<>();
        }
        this.cache_intraNodeGUILinks = new HashMap<>();
        this.cache_regularLinkMap = new HashMap<>();
        this.cache_mapVisibleLayer2VisualizationOrder = new HashMap<>(  );
        this.cache_mapNode2IntraNodeGUILinkMap = new HashMap<>();
        this.cache_mapNode2ListVerticallyStackedGUINodes = new HashMap<>();
        for (int layerIndex = 0; layerIndex < currentNp.getNumberOfLayers(); layerIndex++)
        {
            final NetworkLayer layer = currentNp.getNetworkLayer(layerIndex);
            if (isLayerVisible(layer))
                cache_mapVisibleLayer2VisualizationOrder.put(layer, cache_mapVisibleLayer2VisualizationOrder.size());
        }
        for (Node n : currentNp.getNodes())
        {
            List<GUINode> guiNodesThisNode = new ArrayList<>();
            cache_mapNode2ListVerticallyStackedGUINodes.put(n, guiNodesThisNode);
            Set<GUILink> intraNodeGUILinksThisNode = new HashSet<>();
            cache_intraNodeGUILinks.put(n, intraNodeGUILinksThisNode);
            Map<Pair<Integer, Integer>, GUILink> thisNodeInterLayerLinksInfoMap = new HashMap<>();
            cache_mapNode2IntraNodeGUILinkMap.put(n, thisNodeInterLayerLinksInfoMap);
            for (int trueVisualizationOrderIndex = 0; trueVisualizationOrderIndex < cache_mapVisibleLayer2VisualizationOrder.size(); trueVisualizationOrderIndex++)
            {
                final NetworkLayer newLayer = getLayerAtOrderIndex(trueVisualizationOrderIndex, false);
                final GUINode gn = new GUINode(n, newLayer, this);
                guiNodesThisNode.add(gn);
                if (trueVisualizationOrderIndex > 0)
                {
                    final GUINode lowerLayerGNode = guiNodesThisNode.get(trueVisualizationOrderIndex - 1);
                    final GUINode upperLayerGNode = guiNodesThisNode.get(trueVisualizationOrderIndex);
                    if (upperLayerGNode != gn) throw new RuntimeException();
                    final GUILink glLowerToUpper = new GUILink(null, lowerLayerGNode, gn);
                    final GUILink glUpperToLower = new GUILink(null, gn, lowerLayerGNode);
                    intraNodeGUILinksThisNode.add(glLowerToUpper);
                    intraNodeGUILinksThisNode.add(glUpperToLower);
                    thisNodeInterLayerLinksInfoMap.put(Pair.of(trueVisualizationOrderIndex - 1, trueVisualizationOrderIndex), glLowerToUpper);
                    thisNodeInterLayerLinksInfoMap.put(Pair.of(trueVisualizationOrderIndex, trueVisualizationOrderIndex - 1), glUpperToLower);
                }
            }
        }
        for (int trueVisualizationOrderIndex = 0; trueVisualizationOrderIndex < cache_mapVisibleLayer2VisualizationOrder.size(); trueVisualizationOrderIndex++)
        {
            final NetworkLayer layer = getLayerAtOrderIndex(trueVisualizationOrderIndex, false);
            for (Link e : currentNp.getLinks(layer))
            {
                final GUINode gn1 = cache_mapNode2ListVerticallyStackedGUINodes.get(e.getOriginNode()).get(trueVisualizationOrderIndex);
                final GUINode gn2 = cache_mapNode2ListVerticallyStackedGUINodes.get(e.getDestinationNode()).get(trueVisualizationOrderIndex);
                final GUILink gl1 = new GUILink(e, gn1, gn2);
                cache_regularLinkMap.put(e, gl1);
            }
        }

        checkCacheConsistency();
    }

    private void checkCacheConsistency()
    {
        for (Node n : currentNp.getNodes())
        {
            assertTrue(cache_intraNodeGUILinks.get(n) != null);
            assertTrue(cache_mapNode2IntraNodeGUILinkMap.get(n) != null);
            assertTrue(cache_mapNode2ListVerticallyStackedGUINodes.get(n) != null);
            for (Entry<Pair<Integer, Integer>, GUILink> entry : cache_mapNode2IntraNodeGUILinkMap.get(n).entrySet())
            {
                final int fromLayer = entry.getKey().getFirst();
                final int toLayer = entry.getKey().getSecond();
                final GUILink gl = entry.getValue();
                assertTrue(gl.isIntraNodeLink());
                assertTrue(gl.getOriginNode().getAssociatedNetPlanNode() == n);
                assertTrue(gl.getOriginNode().getVisualizationOrderRemovingNonVisibleLayers() == fromLayer);
                assertTrue(gl.getDestinationNode().getVisualizationOrderRemovingNonVisibleLayers() == toLayer);
            }
            assertEquals(new HashSet<>(cache_mapNode2IntraNodeGUILinkMap.get(n).values()), cache_intraNodeGUILinks.get(n));
            for (GUILink gl : cache_intraNodeGUILinks.get(n))
            {
                assertTrue(gl.isIntraNodeLink());
                assertEquals(gl.getOriginNode().getAssociatedNetPlanNode(), n);
                assertEquals(gl.getDestinationNode().getAssociatedNetPlanNode(), n);
            }
            assertEquals(cache_mapNode2ListVerticallyStackedGUINodes.get(n).size(), getNumberOfVisibleLayers());
            int indexLayer = 0;
            for (GUINode gn : cache_mapNode2ListVerticallyStackedGUINodes.get(n))
            {
                assertEquals(gn.getLayer(), getLayerAtOrderIndex(indexLayer, false));
                assertEquals(gn.getVisualizationOrderRemovingNonVisibleLayers(), indexLayer++);
                assertEquals(gn.getAssociatedNetPlanNode(), n);
            }
        }
    }

    public boolean decreaseFontSizeAll()
    {
        boolean changedSize = false;
        for (Node n : currentNp.getNodes())
            for (GUINode gn : cache_mapNode2ListVerticallyStackedGUINodes.get(n))
                changedSize |= gn.decreaseFontSize();
        return changedSize;
    }

    public void increaseFontSizeAll()
    {
        for (Node n : currentNp.getNodes())
            for (GUINode gn : cache_mapNode2ListVerticallyStackedGUINodes.get(n))
                gn.increaseFontSize();
    }

    public void decreaseNodeSizeAll()
    {
        for (Node n : currentNp.getNodes())
            for (GUINode gn : cache_mapNode2ListVerticallyStackedGUINodes.get(n))
                gn.setShapeSize(gn.getShapeSize() * VisualizationConstants.SCALE_OUT);
    }

    public void increaseNodeSizeAll()
    {
        for (GUINode gn : getAllGUINodes())
            gn.setShapeSize(gn.getShapeSize() * VisualizationConstants.SCALE_IN);
    }

    public int getNumberOfVisibleLayers()
    {
        return cache_mapVisibleLayer2VisualizationOrder.size();
    }

//	public void setVisualizationLayers (List<List<NetworkLayer>> listOfLayersPerVL , NetPlan netPlan)
//	{
//		if (listOfLayersPerVL.isEmpty()) throw new Net2PlanException ("At least one visualization layer is needed");
//		Set<NetworkLayer> alreadyAppearingLayer = new HashSet<> ();
//		for (List<NetworkLayer> layers : listOfLayersPerVL)
//		{	
//			if (layers.isEmpty()) throw new Net2PlanException ("A visualization layer cannot be empty");
//			for (NetworkLayer layer : layers)
//			{
//				if (layer.getNetPlan() != netPlan) throw new RuntimeException ("Bad");
//				if (alreadyAppearingLayer.contains(layer)) throw new Net2PlanException ("A layer cannot belong to more than one visualization layer");
//				alreadyAppearingLayer.add(layer);
//			}
//		}
//		this.currentNp = netPlan;
//		this.vLayers.clear();
//		for (List<NetworkLayer> layers : listOfLayersPerVL)
//			vLayers.add(new VisualizationLayer(layers, this , vLayers.size()));
//
//		this.cache_layer2VLayerMap = new HashMap<> (); 
//		for (VisualizationLayer visualizationLayer : vLayers) 
//			for (NetworkLayer layer : visualizationLayer.npLayersToShow) 
//				cache_layer2VLayerMap.put(layer , visualizationLayer);
////		for (VisualizationLayer visualizationLayer : vLayers) 
////			visualizationLayer.updateGUINodeAndGUILinks();
//	}

    /**
     * @return the showNodeNames
     */
    public boolean isShowNodeNames()
    {
        return showNodeNames;
    }

    /**
     * @param showNodeNames the showNodeNames to set
     */
    public void setShowNodeNames(boolean showNodeNames)
    {
        this.showNodeNames = showNodeNames;
    }

    /**
     * @return the showLinkLabels
     */
    public boolean isShowLinkLabels()
    {
        return showLinkLabels;
    }

    /**
     * @param showLinkLabels the showLinkLabels to set
     */
    public void setShowLinkLabels(boolean showLinkLabels)
    {
        this.showLinkLabels = showLinkLabels;
    }

    /**
     * @return the showNonConnectedNodes
     */
    public boolean isShowNonConnectedNodes()
    {
        return showNonConnectedNodes;
    }

    /**
     * @param showNonConnectedNodes the showNonConnectedNodes to set
     */
    public void setShowNonConnectedNodes(boolean showNonConnectedNodes)
    {
        this.showNonConnectedNodes = showNonConnectedNodes;
    }

    public void setVisibilityState(Node n, boolean isVisible)
    {
        if (isVisible) nonVisibleNodes.remove(n);
        else nonVisibleNodes.add(n);
    }

    public boolean isVisible(Node n)
    {
        return !nonVisibleNodes.contains(n);
    }

    public void setVisibilityState(Link e, boolean isVisible)
    {
        if (isVisible) nonVisibleLinks.remove(e);
        else nonVisibleLinks.add(e);
    }

    public boolean isVisible(Link e)
    {
        return !nonVisibleLinks.contains(e);
    }

    /* Everything to its default color, shape. Separated nodes, are set together again. Visibility state is unchanged */
    public void resetColorAndShapeState()
    {
        for (GUINode n : getAllGUINodes())
        {
            n.setFont(VisualizationConstants.DEFAULT_GUINODE_FONT);
            n.setDrawPaint(VisualizationConstants.DEFAULT_GUINODE_DRAWCOLOR);
            n.setFillPaint(VisualizationConstants.DEFAULT_GUINODE_FILLCOLOR);
            n.setShape(VisualizationConstants.DEFAULT_GUINODE_SHAPE);
            n.setShapeSize(VisualizationConstants.DEFAULT_GUINODE_SHAPESIZE);
        }
        for (GUILink e : getAllGUILinks(true, false))
        {
            e.setHasArrow(VisualizationConstants.DEFAULT_REGGUILINK_HASARROW);
            e.setArrowStroke(VisualizationConstants.DEFAULT_REGGUILINK_ARROWSTROKE);
            e.setEdgeStroke(VisualizationConstants.DEFAULT_REGGUILINK_EDGETROKE);
            e.setArrowDrawPaint(VisualizationConstants.DEFAULT_REGGUILINK_ARROWDRAWCOLOR);
            e.setArrowFillPaint(VisualizationConstants.DEFAULT_REGGUILINK_ARROWFILLCOLOR);
            e.setEdgeDrawPaint(VisualizationConstants.DEFAULT_REGGUILINK_EDGEDRAWCOLOR);
            e.setShownSeparated(false);
        }
        for (GUILink e : getAllGUILinks(false, true))
        {
            e.setHasArrow(VisualizationConstants.DEFAULT_INTRANODEGUILINK_HASARROW);
            e.setArrowStroke(VisualizationConstants.DEFAULT_INTRANODEGUILINK_ARROWSTROKE);
            e.setEdgeStroke(VisualizationConstants.DEFAULT_INTRANODEGUILINK_EDGETROKE);
            e.setArrowDrawPaint(VisualizationConstants.DEFAULT_INTRANODEGUILINK_ARROWDRAWCOLOR);
            e.setArrowFillPaint(VisualizationConstants.DEFAULT_INTRANODEGUILINK_ARROWFILLCOLOR);
            e.setEdgeDrawPaint(VisualizationConstants.DEFAULT_INTRANODEGUILINK_EDGEDRAWCOLOR);
            e.setShownSeparated(false);
        }
    }

    public Set<GUILink> getAllGUILinks(boolean includeRegularLinks, boolean includeInterLayerLinks)
    {
        Set<GUILink> res = new HashSet<>();
        if (includeRegularLinks) res.addAll(cache_regularLinkMap.values());
        if (includeInterLayerLinks) for (Node n : currentNp.getNodes()) res.addAll(this.cache_intraNodeGUILinks.get(n));
        return res;
    }

    public Set<GUINode> getAllGUINodes()
    {
        Set<GUINode> res = new HashSet<>();
        for (List<GUINode> list : this.cache_mapNode2ListVerticallyStackedGUINodes.values()) res.addAll(list);
        return res;
    }


    public void setNodeProperties(Collection<GUINode> nodes, Color color, Shape shape, double shapeSize)
    {
        for (GUINode n : nodes)
        {
            if (color != null)
            {
                n.setDrawPaint(color);
                n.setFillPaint(color);
            }
            if (shape != null)
            {
                n.setShape(shape);
            }
            if (shapeSize > 0)
            {
                n.setShapeSize(shapeSize);
            }
        }
    }

    public void setLinkProperties(Collection<GUILink> links, Color color, Stroke stroke, Boolean hasArrows, Boolean shownSeparated)
    {
        for (GUILink e : links)
        {
            if (color != null)
            {
                e.setArrowDrawPaint(color);
                e.setArrowFillPaint(color);
                e.setEdgeDrawPaint(color);
            }
            if (stroke != null)
            {
                e.setArrowStroke(stroke);
                e.setEdgeStroke(stroke);
            }
            if (hasArrows != null)
            {
                e.setHasArrow(hasArrows);
            }
            if (shownSeparated != null)
            {
                e.setShownSeparated(shownSeparated);
            }
        }
    }

    public double getDefaultVerticalDistanceForInterLayers()
    {
        if (currentNp.getNumberOfNodes() == 0) return 1.0;
        final int numVisibleLayers = getNumberOfVisibleLayers() == 0 ? currentNp.getNumberOfLayers() : getNumberOfVisibleLayers();
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        for (Node n : currentNp.getNodes())
        {
            final double y = n.getXYPositionMap().getY();
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }
        if ((maxY - minY < 1e-6)) return Math.abs(maxY) / (30 * numVisibleLayers);
        return (maxY - minY) / (30 * numVisibleLayers);
    }

    public void setLayerVisibility(final NetworkLayer layer, final boolean isVisible)
    {
        if (!this.currentNp.getNetworkLayers().contains(layer)) throw new RuntimeException();
        Map<NetworkLayer, Integer> new_layerVisiblityOrderMap = this.mapLayer2VisualizationOrder;
        Map<NetworkLayer, Boolean> new_layerVisibilityMap = new HashMap<>(this.mapLayer2Visibility);
        new_layerVisibilityMap.put(layer, isVisible);
        updateLayerVisualizationState(this.currentNp, new_layerVisiblityOrderMap, new_layerVisibilityMap);
    }

    public boolean isLayerVisible(final NetworkLayer layer)
    {
        return mapLayer2Visibility.get(layer);
    }

    public void setLayerLinksVisibility(final NetworkLayer layer, final boolean showLinks)
    {
        if (!this.currentNp.getNetworkLayers().contains(layer)) throw new RuntimeException();
        mapShowLayerLinks.put(layer, showLinks);
    }

    public boolean isLayerLinksShown(final NetworkLayer layer)
    {
        return mapShowLayerLinks.get(layer);
    }

    public NetworkLayer getLayerAtOrderIndex(int orderIndex, boolean includeInvisibleLayers)
    {
        if (includeInvisibleLayers)
        {
            if (orderIndex < 0 || orderIndex >= currentNp.getNumberOfLayers()) throw new RuntimeException("Array out of bound for order index: " + orderIndex);
            return MapUtils.invertMap(mapLayer2VisualizationOrder).get(orderIndex);
        } else
        {
            if (orderIndex < 0 || orderIndex >= getNumberOfVisibleLayers())
            {
                throw new RuntimeException("Array out of bound for order index: " + orderIndex);
            }

            return MapUtils.invertMap(cache_mapVisibleLayer2VisualizationOrder).get(orderIndex);
        }
    }

    public Map<Integer, NetworkLayer> getLayerAtOrderIndexMap(boolean includeInvisibleLayers)
    {
        return MapUtils.invertMap(getLayerOrderIndexMap(includeInvisibleLayers));
    }

    public int getLayerOrderIndex(NetworkLayer layer, boolean includeInvisibleLayers)
    {
        Integer res = includeInvisibleLayers ? mapLayer2VisualizationOrder.get(layer) : cache_mapVisibleLayer2VisualizationOrder.get(layer);
        if (res == null) throw new RuntimeException("");
        return res;
    }

    public Map<NetworkLayer, Integer> getLayerOrderIndexMap(boolean includeInvisibleLayers)
    {
        return includeInvisibleLayers ? mapLayer2VisualizationOrder : cache_mapVisibleLayer2VisualizationOrder;
    }

    public static Pair<Map<NetworkLayer, Integer>, Map<NetworkLayer, Boolean>> generateDefaultVisualizationLayerInfo(NetPlan np)
    {
        final int L = np.getNumberOfLayers();
        final Map<NetworkLayer, Integer> res_1 = new HashMap<>();
        final Map<NetworkLayer, Boolean> res_2 = new HashMap<>(L);

        for (NetworkLayer layer : np.getNetworkLayers())
        {
            res_1.put(layer, res_1.size());
            res_2.put(layer, true);
        }
        return Pair.of(res_1, res_2);
    }

}
