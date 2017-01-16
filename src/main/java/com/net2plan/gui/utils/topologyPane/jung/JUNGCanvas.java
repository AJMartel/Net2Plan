/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Contributors:
 * Pablo Pavon Mariño - initial API and implementation
 ******************************************************************************/


package com.net2plan.gui.utils.topologyPane.jung;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;

import com.net2plan.gui.utils.topologyPane.GUILink;
import com.net2plan.gui.utils.topologyPane.GUINode;
import com.net2plan.gui.utils.topologyPane.ITopologyCanvasPlugin;
import com.net2plan.gui.utils.topologyPane.VisualizationState;
import com.net2plan.gui.utils.topologyPane.VisualizationState.VisualizationLayer;
import com.net2plan.gui.utils.topologyPane.mapControl.osm.state.OSMMapStateBuilder;
import com.net2plan.gui.utils.topologyPane.mapControl.osm.state.OSMRunningState;
import com.net2plan.interfaces.networkDesign.Configuration;
import com.net2plan.interfaces.networkDesign.Link;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.interfaces.networkDesign.Node;
import com.net2plan.internal.CommandLineParser;
import com.net2plan.internal.plugins.ITopologyCanvas;
import com.net2plan.utils.Pair;
import com.net2plan.utils.Triple;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.DefaultParallelEdgeIndexFunction;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.GraphMousePlugin;
import edu.uci.ics.jung.visualization.control.LayoutScalingControl;
import edu.uci.ics.jung.visualization.control.PluggableGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin;
import edu.uci.ics.jung.visualization.decorators.ConstantDirectionalEdgeValueTransformer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.renderers.BasicEdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.BasicVertexLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.DefaultEdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.transform.BidirectionalTransformer;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;
import edu.uci.ics.jung.visualization.transform.shape.ShapeTransformer;
import edu.uci.ics.jung.visualization.transform.shape.TransformingGraphics;
import edu.uci.ics.jung.visualization.util.ArrowFactory;

/**
 * Topology canvas using JUNG library [<a href='#jung'>JUNG</a>].
 *
 * @author Pablo Pavon-Marino, Jose-Luis Izquierdo-Zaragoza
 * @see <a name='jung'></a><a href='http://jung.sourceforge.net/'>Java Universal Network/Graph Framework (JUNG) website</a>
 * @since 0.2.3
 */
@SuppressWarnings("unchecked")
public final class JUNGCanvas implements ITopologyCanvas
{
	private final VisualizationState vs;
    private final static Transformer<GUINode, Point2D> FLIP_VERTICAL_COORDINATES;

    private final Graph<GUINode, GUILink> g;
    private final Layout<GUINode, GUILink> l;
    private final VisualizationViewer<GUINode, GUILink> vv;
    private final PluggableGraphMouse gm;
    private final ScalingControl scalingControl;
    private final Transformer<Context<Graph<GUINode, GUILink>, GUILink>, Shape> originalEdgeShapeTransformer;
    private VisualizationServer.Paintable paintableAssociatedToBackgroundImage;

    static
    {
        FLIP_VERTICAL_COORDINATES = vertex ->
        {
            Point2D pos = vertex.getAssociatedNetPlanNode().getXYPositionMap();
            return new Point2D.Double(pos.getX(), -pos.getY());
        };
    }

    /**
     * Default constructor.
     *
     * @since 0.2.3
     */
    public JUNGCanvas(VisualizationState vs)
    {
    	this.vs = vs;
//        nodeTable = new LinkedHashMap<>();
//        linkTable = new LinkedHashMap<>();
//        intraNodeLinkTable = new LinkedHashMap<> ();
        
        g = new DirectedOrderedSparseMultigraph<>();
        l = new StaticLayout<>(g, FLIP_VERTICAL_COORDINATES);
        vv = new VisualizationViewer<>(l);

        originalEdgeShapeTransformer = new EdgeShape.QuadCurve<>();
        ((EdgeShape.QuadCurve<GUINode, GUILink>) originalEdgeShapeTransformer).setControlOffsetIncrement(10); // how much they separate from the direct line (default is 20)
        ((EdgeShape.QuadCurve<GUINode, GUILink>) originalEdgeShapeTransformer).setEdgeIndexFunction(DefaultParallelEdgeIndexFunction.<GUINode, GUILink>getInstance()); // how much they separate from the direct line (default is 20)

		/* Customize the graph */
        vv.getRenderContext().setVertexDrawPaintTransformer(n -> n.getDrawPaint());
        vv.getRenderContext().setVertexFillPaintTransformer(n -> n.getFillPaint());
        vv.getRenderContext().setVertexFontTransformer(n -> n.getFont());

		
		/* If icons => comment this line */
        vv.getRenderContext().setVertexShapeTransformer(n -> n.getShape());
        /* If shapes, comment this line */
        //vv.getRenderContext().setVertexIconTransformer(new Transformer<GUINode,Icon> () {} ... )

        vv.getRenderContext().setVertexIncludePredicate(new NodeDisplayPredicate<>());
        vv.getRenderer().setVertexLabelRenderer(new NodeLabelRenderer());
        vv.setVertexToolTipTransformer(node -> node.getToolTip());


        vv.getRenderContext().setEdgeIncludePredicate(context -> context.element.isVisible());
        vv.getRenderContext().setEdgeArrowPredicate(context -> context.element.isVisible() && context.element.getHasArrow());
        vv.getRenderContext().setEdgeArrowStrokeTransformer(i -> i.getArrowStroke());
        vv.getRenderContext().setEdgeArrowTransformer(new ConstantTransformer(ArrowFactory.getNotchedArrow(7, 10, 5)));
        vv.getRenderContext().setEdgeLabelClosenessTransformer(new ConstantDirectionalEdgeValueTransformer(.6, .6));
        vv.getRenderContext().setEdgeStrokeTransformer(i -> i.getEdgeStroke());

        vv.getRenderContext().setEdgeDrawPaintTransformer(e -> e.getEdgeDrawPaint());
        vv.getRenderContext().setArrowDrawPaintTransformer(e -> e.getArrowDrawPaint());
        vv.getRenderContext().setArrowFillPaintTransformer(e -> e.getArrowFillPaint());

        vv.getRenderContext().setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.BLUE));
        vv.getRenderer().setEdgeLabelRenderer(new BasicEdgeLabelRenderer<GUINode, GUILink>()
        {
            public void labelEdge(RenderContext<GUINode, GUILink> rc, Layout<GUINode, GUILink> layout, GUILink e, String label)
            {
                if (vs.isShowLinkLabels()) super.labelEdge(rc, layout, e, e.getLabel());
            }
        });
        vv.setEdgeToolTipTransformer(link -> link.getToolTip());
//        vv.getRenderContext().setEdgeShapeTransformer(c ->
//                {
//                    final GUINode origin = c.element.getOriginNode();
//                    final GUINode destination = c.element.getDestinationNode();
//                    boolean separateTheLinks = vv.getPickedVertexState().isPicked(origin) || vv.getPickedVertexState().isPicked(destination);
//                    if (!separateTheLinks)
//                    {
//                        Set<GUILink> linksNodePair = new HashSet<>(c.graph.getIncidentEdges(destination));
//                        linksNodePair.retainAll(c.graph.getIncidentEdges(origin));
//                        for (GUILink e : linksNodePair)
//                            if (vv.getPickedEdgeState().isPicked(e) || !e.getAssociatedNetPlanLink().isUp())
//                            {
//                                separateTheLinks = true;
//                                break;
//                            }
//                    }
//                    return separateTheLinks ? originalEdgeShapeTransformer.transform(c) : new Line2D.Float(0.0f, 0.0f, 1.0f, 0.0f);
//                }
//        );

        vv.getRenderContext().setEdgeShapeTransformer(c -> c.element.isShownSeparated() ? originalEdgeShapeTransformer.transform(c) : new Line2D.Float(0.0f, 0.0f, 1.0f, 0.0f));

        // Background controller
        this.paintableAssociatedToBackgroundImage = null;

        gm = new PluggableGraphMouse();
        vv.setGraphMouse(gm);

        scalingControl = new LayoutScalingControl();
        ITopologyCanvasPlugin scalingPlugin = new ScalingCanvasPlugin(scalingControl, MouseEvent.NOBUTTON);
        addPlugin(scalingPlugin);

        vv.setOpaque(false);
        vv.setBackground(new Color(0, 0, 0, 0));

//        reset();
    }

    @Override
    public JComponent getComponent()
    {
        return vv;
    }
//    @Override
//    public void addNode(Node npNode) //long nodeId, Point2D pos, String label)
//    {
//        if (nodeTable.containsKey(npNode)) throw new RuntimeException("Bad - Node " + npNode + " already exists");
//        List<GUINode> associatedGUINodes = new ArrayList<> ();
//        for (VisualizationState.VisualizationLayer vLayer : vs.getVLList())
//        {
//        	GUINode gn = new GUINode(npNode , vLayer);
//            g.addVertex(gn);
//        	associatedGUINodes.add(gn);
//        	if (associatedGUINodes.size() > 1)
//        	{
//        		GUILink gl1 = new GUILink (null , associatedGUINodes.get(associatedGUINodes.size() - 2), gn);
//        		GUILink gl2 = new GUILink (null , gn , associatedGUINodes.get(associatedGUINodes.size() - 2));
//        		List<GUILink> existingList = intraNodeLinkTable.get(npNode);
//        		if (existingList == null) { existingList = new ArrayList<GUILink> (); intraNodeLinkTable.put(npNode , existingList); } 
//        		existingList.add(gl1); existingList.add(gl2);
//        	}
//        }
//        nodeTable.put(npNode, associatedGUINodes);
//    }

    @Override
    public void addPlugin(ITopologyCanvasPlugin plugin)
    {
        gm.add(new GraphMousePluginAdapter(plugin));
    }

    /**
     * Converts a point from the SWING coordinates system into a point from the JUNG coordinates system.
     *
     * @param screenPoint (@code Point2D) on the SWING canvas.
     * @return (@code Point2D) on the JUNG canvas.
     */
    @Override
    public Point2D convertViewCoordinatesToRealCoordinates(Point2D screenPoint)
    {
        Point2D layoutCoordinates = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(Layer.LAYOUT, screenPoint);
        layoutCoordinates.setLocation(layoutCoordinates.getX(), -layoutCoordinates.getY());

        return layoutCoordinates;
    }

    /**
     * Converts a point from the JUNG coordinates system to the SWING coordinates system.
     *
     * @param screenPoint (@code Point2D) on the JUNG canvas.
     * @return (@code Point2D) on the SWING canvas.
     */
    @Override
    public Point2D convertRealCoordinatesToViewCoordinates(Point2D screenPoint)
    {
        screenPoint.setLocation(screenPoint.getX(), -screenPoint.getY());
        return vv.getRenderContext().getMultiLayerTransformer().transform(Layer.LAYOUT, screenPoint);
    }

    @Override
    public String getDescription()
    {
        return null;
    }

    @Override
    public JComponent getInternalVisualizationController()
    {
        return vv;
    }

    @Override
    public GUILink getLink(MouseEvent e)
    {
        final VisualizationViewer<GUINode, GUILink> vv = (VisualizationViewer<GUINode, GUILink>) e.getSource();
        GraphElementAccessor<GUINode, GUILink> pickSupport = vv.getPickSupport();
        if (pickSupport != null)
        {
            final Point p = e.getPoint();
            return pickSupport.getEdge(vv.getModel().getGraphLayout(), p.getX(), p.getY());
        }

        return null;
    }

    @Override
    public String getName()
    {
        return "JUNG Canvas";
    }

    @Override
    public GUINode getNode(MouseEvent e)
    {
        final VisualizationViewer<GUINode, GUILink> vv = (VisualizationViewer<GUINode, GUILink>) e.getSource();
        GraphElementAccessor<GUINode, GUILink> pickSupport = vv.getPickSupport();
        if (pickSupport != null)
        {
            final Point p = e.getPoint();
            final GUINode vertex = pickSupport.getVertex(vv.getModel().getGraphLayout(), p.getX(), p.getY());
            if (vertex != null) return vertex;
        }

        return null;
    }

    @Override
    public List<Triple<String, String, String>> getParameters()
    {
        return null;
    }

    @Override
    public void panTo(Point2D initialPoint, Point2D currentPoint)
    {
        OSMMapStateBuilder.getSingleton().panTo(initialPoint, currentPoint);
    }

    @Override
    public void refresh()
    {
        vv.repaint();
    }


    @Override
    public void removePlugin(ITopologyCanvasPlugin plugin)
    {
        if (plugin instanceof GraphMousePlugin) gm.remove((GraphMousePlugin) plugin);
    }

    
    @Override
    public void resetPickedStateAndRefresh()
    {
        vv.getPickedVertexState().clear();
        vv.getPickedEdgeState().clear();
        refresh();
    }

    @Override
    public void rebuildTopologyAndRefresh()
    {
    	for (GUILink gl : new ArrayList<>(g.getEdges()))
    		g.removeEdge(gl);
    	for (GUINode gn : new ArrayList<>(g.getVertices()))
    		g.removeVertex(gn);
    	for (int layerIndex = 0 ; layerIndex < vs.getNumberOfVisualizationLayers() ; layerIndex ++)
    	{
    		final VisualizationLayer vl = vs.getVLList().get(layerIndex);
    		for (GUINode gn : vl.getGUINodes())
    		{
    			g.addVertex(gn);
    			Point2D basePositionInJungCoord = convertViewCoordinatesToRealCoordinates (gn.getAssociatedNetPlanNode().getXYPositionMap());
    			
    			final double yOfPixelZero = convertViewCoordinatesToRealCoordinates (new Point2D.Double (0 , 0)).getY();
    			final double yOfPixelUp = convertViewCoordinatesToRealCoordinates (new Point2D.Double (0 , ((double) layerIndex) * vs.getInterLayerDistanceInPixels())).getY();
    			final double extraInJungCoordinates =  Math.abs(yOfPixelUp - yOfPixelZero);
    			l.setLocation(gn , new Point2D.Double(basePositionInJungCoord.getX() , basePositionInJungCoord.getY() + extraInJungCoordinates));
    		}
    		for (GUILink gl : vl.getGUIIntraLayerLinks())
    			g.addEdge(gl , gl.getOriginNode() , gl.getDestinationNode());
    	}
    	
    	/* Add inter layer links */
    	for (GUILink gl : vs.getAllGUILinks(false , true))
			g.addEdge(gl , gl.getOriginNode() , gl.getDestinationNode());
    	refresh();
    }

    @Override
    public void zoomAll()
    {
        OSMMapStateBuilder.getSingleton().zoomAll();
    }

    public void frameTopology()
    {
        Set<GUINode> nodes = new LinkedHashSet<>();
        for (GUINode n : g.getVertices()) if (n.isVisible()) nodes.add(n);

        if (nodes.isEmpty()) return;

        double aux_xmax = Double.NEGATIVE_INFINITY;
        double aux_xmin = Double.POSITIVE_INFINITY;
        double aux_ymax = Double.NEGATIVE_INFINITY;
        double aux_ymin = Double.POSITIVE_INFINITY;
        double auxTransf_xmax = Double.NEGATIVE_INFINITY;
        double auxTransf_xmin = Double.POSITIVE_INFINITY;
        double auxTransf_ymax = Double.NEGATIVE_INFINITY;
        double auxTransf_ymin = Double.POSITIVE_INFINITY;
        for (GUINode node : nodes)
        {
            Point2D aux = node.getAssociatedNetPlanNode().getXYPositionMap();
            Point2D auxTransf = l.transform(node);
            if (aux_xmax < aux.getX()) aux_xmax = aux.getX();
            if (aux_xmin > aux.getX()) aux_xmin = aux.getX();
            if (aux_ymax < aux.getY()) aux_ymax = aux.getY();
            if (aux_ymin > aux.getY()) aux_ymin = aux.getY();
            if (auxTransf_xmax < auxTransf.getX()) auxTransf_xmax = auxTransf.getX();
            if (auxTransf_xmin > auxTransf.getX()) auxTransf_xmin = auxTransf.getX();
            if (auxTransf_ymax < auxTransf.getY()) auxTransf_ymax = auxTransf.getY();
            if (auxTransf_ymin > auxTransf.getY()) auxTransf_ymin = auxTransf.getY();
        }

        double PRECISION_FACTOR = 0.00001;

        Rectangle viewInLayoutUnits = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(vv.getBounds()).getBounds();
        float ratio_h = Math.abs(aux_xmax - aux_xmin) < PRECISION_FACTOR ? 1 : (float) (viewInLayoutUnits.getWidth() / (aux_xmax - aux_xmin));
        float ratio_v = Math.abs(aux_ymax - aux_ymin) < PRECISION_FACTOR ? 1 : (float) (viewInLayoutUnits.getHeight() / (aux_ymax - aux_ymin));
        float ratio = (float) (0.8 * Math.min(ratio_h, ratio_v));
        scalingControl.scale(vv, ratio, vv.getCenter());

		/* Generate an auxiliary node at center of the graph */
        Point2D q = new Point2D.Double((auxTransf_xmin + auxTransf_xmax) / 2, (auxTransf_ymin + auxTransf_ymax) / 2);
        Point2D lvc = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(vv.getCenter());
        double dx = (lvc.getX() - q.getX());
        double dy = (lvc.getY() - q.getY());

        vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT).translate(dx, dy);
    }

    @Override
    public void updateNodeXYPosition(Node node)
    {
    	for (GUINode gn : vs.getVerticallyStackedGUINodes(node))
    		l.setLocation(gn, FLIP_VERTICAL_COORDINATES.transform(gn));
    }

    @Override
    public void moveNodeToXYPosition(Node npNode, Point2D point)
    {
		final double yOfPixelZero = convertViewCoordinatesToRealCoordinates (new Point2D.Double (0 , 0)).getY();
		final double yOfPixelUp = convertViewCoordinatesToRealCoordinates (new Point2D.Double (0 , vs.getInterLayerDistanceInPixels())).getY();
		final double extraInJungCoordinates =  Math.abs(yOfPixelUp - yOfPixelZero);

    	for (GUINode node : vs.getVerticallyStackedGUINodes(npNode))
    		l.setLocation(node, new Point2D.Double(point.getX() , point.getY() + node.getVisualizationLayer().getIndex()*extraInJungCoordinates));
    }

    public MutableTransformer getTransformer()
    {
        return vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT);
    }

    @Override
    public void zoomIn()
    {
        OSMMapStateBuilder.getSingleton().zoomIn();
    }

    @Override
    public void zoomOut()
    {
        OSMMapStateBuilder.getSingleton().zoomOut();
    }

    public void zoom(float scale)
    {
        scalingControl.scale(vv, scale, vv.getCenter());
    }

    public void zoomIn(Point2D point)
    {
        scalingControl.scale(vv, VisualizationState.SCALE_IN, point);
    }

    public void zoomOut(Point2D point)
    {
        scalingControl.scale(vv, VisualizationState.SCALE_OUT, point);
    }

    public void setBackgroundImage(final File bgFile, final double x, final double y)
    {
        final Double x1 = x;
        final Double y1 = y;

        setBackgroundImage(bgFile, x1.intValue(), y1.intValue());
    }

    public void setBackgroundImage(final ImageIcon image, final double x, final double y)
    {
        final Double x1 = x;
        final Double y1 = y;

        setBackgroundImage(image, x1.intValue(), y1.intValue());
    }

    public void setBackgroundImage(final ImageIcon image, final int x, final int y)
    {
        updateBackgroundImage(image, x, y);
    }

    public void setBackgroundImage(final File bgFile, final int x, final int y)
    {
        final ImageIcon background = new ImageIcon(bgFile.getAbsolutePath());
        updateBackgroundImage(background, x, y);
    }

    public void updateBackgroundImage(final ImageIcon icon)
    {
        updateBackgroundImage(icon, 0, 0);
    }

    public void updateBackgroundImage(final ImageIcon icon, final int x, final int y)
    {
        if (paintableAssociatedToBackgroundImage != null)
            vv.removePreRenderPaintable(paintableAssociatedToBackgroundImage);
        paintableAssociatedToBackgroundImage = null;
        if (icon != null)
        {
            this.paintableAssociatedToBackgroundImage = new VisualizationViewer.Paintable()
            {
                public void paint(Graphics g)
                {
                    Graphics2D g2d = (Graphics2D) g;
                    AffineTransform oldXform = g2d.getTransform();
                    AffineTransform lat = vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT).getTransform();
                    AffineTransform vat = vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW).getTransform();
                    AffineTransform at = new AffineTransform();
                    at.concatenate(g2d.getTransform());
                    at.concatenate(vat);
                    at.concatenate(lat);
                    g2d.setTransform(at);
                    g.drawImage(icon.getImage(), x, y, icon.getIconWidth(), icon.getIconHeight(), vv);
                    g2d.setTransform(oldXform);
                }

                public boolean useTransform()
                {
                    return false;
                }
            };
            vv.addPreRenderPaintable(paintableAssociatedToBackgroundImage);
        }
    }

    private final class NodeDisplayPredicate<Node, Link> implements Predicate<Context<Graph<Node, Link>, Node>>
    {
        @Override
        public boolean evaluate(Context<Graph<Node, Link>, Node> context)
        {
            com.net2plan.gui.utils.topologyPane.GUINode v = (com.net2plan.gui.utils.topologyPane.GUINode) context.element;
            if (!vs.isShowNonConnectedNodes())
            {
                Collection<GUILink> incidentLinks = g.getIncidentEdges(v);
                if (incidentLinks == null) return false;
                if (incidentLinks.isEmpty()) return false;
            }

            return v.isVisible();
        }
    }


    private class NodeLabelRenderer extends BasicVertexLabelRenderer<GUINode, GUILink>
    {
        @Override
        public void labelVertex(RenderContext<GUINode, GUILink> rc, Layout<GUINode, GUILink> layout, GUINode v, String label)
        {
            Graph<GUINode, GUILink> graph = layout.getGraph();
            if (rc.getVertexIncludePredicate().evaluate(Context.getInstance(graph, v)) == false)
            {
                return;
            }

            Point2D pt = layout.transform(v);
            pt = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, pt);

            float x = (float) pt.getX();
            float y = (float) pt.getY();

            Component component = prepareRenderer(rc, rc.getVertexLabelRenderer(), "<html><font color='white'>" + v.getAssociatedNetPlanNode().getIndex() + "</font></html>", rc.getPickedVertexState().isPicked(v), v);
            GraphicsDecorator g = rc.getGraphicsContext();
            Dimension d = component.getPreferredSize();
            AffineTransform xform = AffineTransform.getTranslateInstance(x, y);

            Shape shape = rc.getVertexShapeTransformer().transform(v);
            shape = xform.createTransformedShape(shape);
            GraphicsDecorator gd = rc.getGraphicsContext();
            if (gd instanceof TransformingGraphics)
            {
                BidirectionalTransformer transformer = ((TransformingGraphics) gd).getTransformer();
                if (transformer instanceof ShapeTransformer)
                {
                    ShapeTransformer shapeTransformer = (ShapeTransformer) transformer;
                    shape = shapeTransformer.transform(shape);
                }
            }

            Rectangle2D bounds = shape.getBounds2D();

            Point p = getAnchorPoint(bounds, d, Renderer.VertexLabel.Position.CNTR);
            g.draw(component, rc.getRendererPane(), p.x, p.y, d.width, d.height, true);

            if (vs.isShowNodeNames())
            {
                component = prepareRenderer(rc, rc.getVertexLabelRenderer(), "<html><font color='black'>" + v.getLabel() + "</font></html>", rc.getPickedVertexState().isPicked(v), v);
                g = rc.getGraphicsContext();
                d = component.getPreferredSize();
                xform = AffineTransform.getTranslateInstance(x, y);

                shape = rc.getVertexShapeTransformer().transform(v);
                shape = xform.createTransformedShape(shape);
                if (rc.getGraphicsContext() instanceof TransformingGraphics)
                {
                    BidirectionalTransformer transformer = ((TransformingGraphics) rc.getGraphicsContext()).getTransformer();
                    if (transformer instanceof ShapeTransformer)
                    {
                        ShapeTransformer shapeTransformer = (ShapeTransformer) transformer;
                        shape = shapeTransformer.transform(shape);
                    }
                }

                bounds = shape.getBounds2D();

                p = getAnchorPoint(bounds, d, Renderer.VertexLabel.Position.NE);
                g.draw(component, rc.getRendererPane(), p.x, p.y, d.width, d.height, true);
            }
        }

        @Override
        protected Point getAnchorPoint(Rectangle2D vertexBounds, Dimension labelSize, Renderer.VertexLabel.Position position)
        {
            double x;
            double y;
            int offset = 5;
            switch (position)
            {
                case NE:
                    x = vertexBounds.getMaxX() - offset;
                    y = vertexBounds.getMinY() + offset - labelSize.height;
                    return new Point((int) x, (int) y);
                case CNTR:
                    x = vertexBounds.getCenterX() - ((double) labelSize.width / 2);
                    y = vertexBounds.getCenterY() - ((double) labelSize.height / 2);
                    return new Point((int) x, (int) y);

                default:
                    return new Point();
            }

        }
    }

    private static class ScalingCanvasPlugin extends ScalingGraphMousePlugin implements ITopologyCanvasPlugin
    {
        public ScalingCanvasPlugin(ScalingControl scaler, int modifiers)
        {
            super(scaler, modifiers, VisualizationState.SCALE_OUT, VisualizationState.SCALE_IN);
            setZoomAtMouse(false);
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e)
        {
            boolean accepted = this.checkModifiers(e);
            if (accepted)
            {
                VisualizationViewer vv = (VisualizationViewer) e.getSource();
                int amount = e.getWheelRotation();
                if (this.zoomAtMouse)
                {
                    if (amount > 0)
                    {
                        OSMMapStateBuilder.getSingleton().zoomOut();
                    } else if (amount < 0)
                    {
                        OSMMapStateBuilder.getSingleton().zoomIn();
                    }
                } else if (amount > 0)
                {
                    OSMMapStateBuilder.getSingleton().zoomOut();
                } else if (amount < 0)
                {
                    OSMMapStateBuilder.getSingleton().zoomIn();
                }

                e.consume();
                vv.repaint();
            }

        }
    }

	@Override
	public void setBackgroundOSMMapsActiveState(boolean activateMap)
	{
		if (activateMap) 
			OSMMapStateBuilder.getSingleton().setRunningState(); 
		else 
			OSMMapStateBuilder.getSingleton().setStoppedState(); 
	}

	@Override
	public boolean getBackgroundOSMMapsActiveState()
	{
		return (OSMMapStateBuilder.getSingleton().getCurrentState() instanceof OSMRunningState);
	}

	@Override
	public final Map<String, String> getCurrentOptions()
	{
		return CommandLineParser.getParameters(getParameters(), Configuration.getOptions());
	}
	
	@Override
	public int getPriority() { return 0; }


	@Override
	public void takeSnapshot()
	{
		OSMMapStateBuilder.getSingleton().takeSnapshot(this);
	}
	
	
}
