package com.net2plan.gui.utils.topologyPane.mapControl.osm;

import com.net2plan.gui.tools.GUINetworkDesign;
import com.net2plan.gui.utils.INetworkCallback;
import com.net2plan.gui.utils.topologyPane.GUILink;
import com.net2plan.gui.utils.topologyPane.GUINode;
import com.net2plan.gui.utils.topologyPane.TopologyPanel;
import com.net2plan.gui.utils.topologyPane.components.mapPanel.OSMMapPanel;
import com.net2plan.gui.utils.topologyPane.jung.JUNGCanvas;
import com.net2plan.gui.utils.topologyPane.mapControl.osm.state.OSMRunningState;
import com.net2plan.interfaces.networkDesign.Net2PlanException;
import com.net2plan.interfaces.networkDesign.Node;
import com.net2plan.internal.ErrorHandling;
import com.net2plan.internal.plugins.ITopologyCanvas;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;
import org.jxmapviewer.viewer.*;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;

/**
 * Created by Jorge San Emeterio on 03/11/2016.
 */
public class OSMMapController
{
    private static OSMMapPanel mapViewer;

    private static TopologyPanel topologyPanel;
    private static ITopologyCanvas canvas;
    private static INetworkCallback callback;

    private static final double zoomRatio = 0.6;

    // Previous OSM map state
    private static Rectangle previousOSMViewportBounds;
    private static int previousZoomLevel;

    // Non-instanciable
    private OSMMapController()
    {
    }

    /**
     * Starts and runs the OSM map to its original state.
     * This method should be executed when the OSM map is not yet loaded.
     *
     * @param topologyPanel The topology panel.
     * @param canvas        The JUNG canvas.
     * @param callback      The interface to the NetPlan.
     */
    public static void startMap(final TopologyPanel topologyPanel, final ITopologyCanvas canvas, final INetworkCallback callback)
    {
        // Checking if the nodes are valid for this operation.
        // They may not go outside the bounds: x: -180, 180: y: -90, 90
        for (Node node : callback.getDesign().getNodes())
        {
            final Point2D nodeXY = node.getXYPositionMap();

            final double x = nodeXY.getX();
            final double y = nodeXY.getY();

            if (!OSMMapUtils.isInsideBounds(x, y))
            {
                final StringBuilder builder = new StringBuilder();
                builder.append("Node: " + node.getName() + " is out of the accepted bounds.\n");
                builder.append("All nodes must have their coordinates between the ranges: \n");
                builder.append("x = [-180, 180]\n");
                builder.append("y = [-90, 90]\n");

                GUINetworkDesign.getStateManager().setStoppedState();

                throw new OSMMapException(builder.toString());
            }
        }

        // Check screen resolution
        final GraphicsDevice graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        final Dimension screenSize = new Dimension(graphicsDevice.getDisplayMode().getWidth(), graphicsDevice.getDisplayMode().getHeight());

        if (screenSize.getWidth() > 1920 || screenSize.getHeight() > 1080)
        {
            throw new OSMMapException("Screen resolutions above 1080p are currently not supported.");
        }

        OSMMapController.topologyPanel = topologyPanel;
        OSMMapController.canvas = canvas;
        OSMMapController.callback = callback;
        OSMMapController.mapViewer = new OSMMapPanel();

        // Activating maps on the canvas
        loadMapOntoTopologyPanel();

        // Making the relation between the OSM map and the topology
        restartMapState();
    }

    /**
     * Sets the swing component structure.
     */
    private static void loadMapOntoTopologyPanel()
    {
        // Making some swing adjustments.
        // Canvas on top of the OSM map panel.
        final LayoutManager layout = new OverlayLayout(mapViewer);
        mapViewer.setLayout(layout);

        topologyPanel.remove(canvas.getComponent());

        mapViewer.removeAll();
        mapViewer.add(canvas.getComponent());

        topologyPanel.add(mapViewer, BorderLayout.CENTER);

        mapViewer.validate();
        mapViewer.repaint();

        topologyPanel.validate();
        topologyPanel.repaint();
    }

    /**
     * Creates the starting state of the OSM map.
     * This state is the one where all nodes are seen and they all fit their corresponding position on the OSM map.
     * This method should only be executed when the OSM map is first run. From then on use {@link #zoomAll()}
     */
    private static void restartMapState()
    {
        // Canvas components.
        final VisualizationViewer<GUINode, GUILink> vv = (VisualizationViewer<GUINode, GUILink>) OSMMapController.canvas.getComponent();
        final MutableTransformer layoutTransformer = vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT);

        final Map<Long, GeoPosition> nodeToGeoPositionMap = new HashMap<>();
        // Read xy coordinates of each node as latitude and longitude coordinates.
        for (Node node : callback.getDesign().getNodes())
        {
            final Point2D nodeXY = node.getXYPositionMap();

            final double latitude = nodeXY.getY();
            final double longitude = nodeXY.getX();

            final GeoPosition geoPosition = new GeoPosition(latitude, longitude);
            nodeToGeoPositionMap.put(node.getId(), geoPosition);
        }

        // Calculating OSM map center and zoom.
        // zoomToBestFit fails to deliver the correct center when the map is too big.
        // To solve this, we will be always calculating the center over a 720p.
        // Resolution at which the map is correctly centered.
        // FIXME: Change this solution?
        final Dimension size = mapViewer.getSize();
        mapViewer.setSize(1280, 720);
        mapViewer.zoomToBestFit(new HashSet<>(nodeToGeoPositionMap.values()), zoomRatio);
        mapViewer.setSize(size);

        // Moving the nodes to the position dictated by their geoposition.
        for (Map.Entry<Long, GeoPosition> entry : nodeToGeoPositionMap.entrySet())
        {
            final Node node = callback.getDesign().getNodeFromId(entry.getKey());
            final GeoPosition geoPosition = entry.getValue();

            // The nodes' xy coordinates are not modified.
            final Point2D realPosition = mapViewer.getTileFactory().geoToPixel(geoPosition, mapViewer.getZoom());
            ((JUNGCanvas) canvas).moveNodeToXYPosition(node, realPosition);
        }

        // The OSM map is now centered, now is time to fit the topology to the OSM map.
        // Center the topology.
        ((JUNGCanvas) canvas).frameTopology();

        // Removing the zoom all scale, so that the relation between the JUNG Canvas and the SWING Canvas is 1:1.
        ((JUNGCanvas) canvas).zoom((float) (1 / layoutTransformer.getScale()));

        // As the topology is centered at the same point as the OSM map, and the relation is 1:1 between their coordinates.
        // The nodes will be placed at the exact place as they are supposed to.

        previousOSMViewportBounds = mapViewer.getViewportBounds();
        previousZoomLevel = mapViewer.getZoom();

        // Refresh swing components.
        canvas.refresh();
        mapViewer.repaint();
    }

    private static void alignZoomJUNGToOSMMap()
    {
        final double zoomChange = mapViewer.getZoom() - previousZoomLevel;

        if (zoomChange != 0)
        {
            ((JUNGCanvas) canvas).zoom((float) Math.pow(2, -zoomChange));
        }

        previousZoomLevel = mapViewer.getZoom();
        previousOSMViewportBounds = mapViewer.getViewportBounds();

        canvas.refresh();
        mapViewer.repaint();
    }

    private static void alignPanJUNGToOSMMap()
    {
        final Rectangle currentOSMViewportBounds = mapViewer.getViewportBounds();

        final double currentCenterX = currentOSMViewportBounds.getCenterX();
        final double currentCenterY = currentOSMViewportBounds.getCenterY();

        final Point2D currentOSMCenterJUNG = ((JUNGCanvas) canvas).convertViewCoordinatesToRealCoordinates(new Point2D.Double(currentCenterX, currentCenterY));

        final double preCenterX = previousOSMViewportBounds.getCenterX();
        final double preCenterY = previousOSMViewportBounds.getCenterY();

        final Point2D previousOSMCenterJUNG = ((JUNGCanvas) canvas).convertViewCoordinatesToRealCoordinates(new Point2D.Double(preCenterX, preCenterY));

        final VisualizationViewer<GUINode, GUILink> vv = (VisualizationViewer<GUINode, GUILink>) OSMMapController.canvas.getComponent();
        final MutableTransformer layoutTransformer = vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT);

        final double dx = (currentOSMCenterJUNG.getX() - previousOSMCenterJUNG.getX());
        final double dy = (currentOSMCenterJUNG.getY() - previousOSMCenterJUNG.getY());

        if (dx != 0 || dy != 0)
            layoutTransformer.translate(-dx, dy);

        previousZoomLevel = mapViewer.getZoom();
        previousOSMViewportBounds = currentOSMViewportBounds;

        canvas.refresh();
        mapViewer.repaint();
    }

    /**
     * Returns the swing component to the state they were before activating the OSM map.
     */
    public static void cleanMap()
    {
        if (mapViewer != null)
        {
            // First, remove any canvas from the top of the OSM map viewer.
            mapViewer.removeAll();

            // Then remove the OSM map from the topology panel.
            topologyPanel.remove(mapViewer);

            // Deleting the map component
            mapViewer = null;

            // Repaint canvas on the topology panel
            topologyPanel.add(canvas.getComponent(), BorderLayout.CENTER);

            // Reset nodes' original position
            for (Node node : callback.getDesign().getNodes())
            {
                canvas.updateNodeXYPosition(node);
                canvas.zoomAll();
            }

            topologyPanel.validate();
            topologyPanel.repaint();
        }
    }

    /**
     * Restores the topology to its original state.
     */
    public static void zoomAll()
    {
        if (isMapActivated())
        {
            restartMapState();
        } else
        {
            throw new OSMMapException("Map is currently deactivated");
        }
    }

    /**
     * Moves the OSM map center a given amount of pixels.
     *
     * @param dx Moves OSM map dx pixels over the X axis.
     * @param dy Moves OSM map dy pixels over the Y axis.
     */
    public static void moveMap(final double dx, final double dy)
    {
        if (isMapActivated())
        {
            final TileFactory tileFactory = mapViewer.getTileFactory();

            final Point2D mapCenter = mapViewer.getCenter();
            final Point2D newMapCenter = new Point2D.Double(mapCenter.getX() + dx, mapCenter.getY() + dy);

            mapViewer.setCenterPosition(tileFactory.pixelToGeo(newMapCenter, mapViewer.getZoom()));

            // Align the topology to the newly change OSM map.
            if (callback.getDesign().hasNodes())
            {
                alignPanJUNGToOSMMap();
            }
        } else
        {
            throw new OSMMapException("Map is currently deactivated");
        }
    }

    /**
     * Zooms the OSM map in and adapts the topology to its new state.
     */
    public static void zoomIn()
    {
        if (isMapActivated())
        {
            mapViewer.setZoom(mapViewer.getZoom() - 1);

            // Align the topology to the newly change OSM map.
            alignZoomJUNGToOSMMap();
        } else
        {
            throw new OSMMapException("Map is currently deactivated");
        }
    }

    /**
     * Zooms the OSM map out and adapts the topology to the new state.
     */
    public static void zoomOut()
    {
        if (isMapActivated())
        {
            final int maximumZoomLevel = mapViewer.getTileFactory().getInfo().getMaximumZoomLevel() - 1;

            if (!(mapViewer.getZoom() == maximumZoomLevel))
            {
                mapViewer.setZoom(mapViewer.getZoom() + 1);
            }

            // Align the topology to the newly change OSM map.
            alignZoomJUNGToOSMMap();
        } else
        {
            throw new OSMMapException("Map is currently deactivated");
        }
    }

    /**
     * Gets whether the OSM map component is activated or not.
     *
     * @return Map activation state.
     */
    private static boolean isMapActivated()
    {
        return GUINetworkDesign.getStateManager().getCurrentState() instanceof OSMRunningState;
    }

    public static JComponent getMapComponent()
    {
        return mapViewer;
    }

    public static class OSMMapException extends Net2PlanException
    {
        public OSMMapException(final String message)
        {
            ErrorHandling.showErrorDialog(message, "Could not display OSM Map");
        }

        public OSMMapException(final String message, final String title)
        {
            ErrorHandling.showErrorDialog(message, title);
        }
    }

    public static class OSMMapUtils
    {
        public static GeoPosition convertPointToGeo(final Point2D point)
        {
            // Pixel to geo must be calculated at the zoom level where canvas and map align.
            // That zoom level is the one given by the restore map method.
            return mapViewer.getTileFactory().pixelToGeo(point, mapViewer.getZoom());
        }

        public static boolean isInsideBounds(final Point2D point)
        {
            return isInsideBounds(point.getX(), point.getY());
        }

        public static boolean isInsideBounds(final double x, final double y)
        {
            if ((x > 180 || x < -180) || (y > 90 || y < -90))
            {
                return false;
            }

            return true;
        }
    }
}
