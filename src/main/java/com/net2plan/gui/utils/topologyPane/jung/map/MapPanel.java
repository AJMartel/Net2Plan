package com.net2plan.gui.utils.topologyPane.jung.map;

import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.interfaces.networkDesign.Node;
import org.jxmapviewer.JXMapKit;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Jorge San Emeterio on 13/10/2016.
 */
public class MapPanel extends JXMapKit
{
    private static int mapID = 0;

    protected final TileFactoryInfo info;
    protected final DefaultTileFactory tileFactory;

    public MapPanel()
    {
        // Create a TileFactoryInfo for OpenStreetMap
        info = new OSMTileFactoryInfo();
        tileFactory = new DefaultTileFactory(info);
        this.setTileFactory(tileFactory);

        // Add interactions
        MouseInputListener mia = new PanMouseInputListener(this.getMainMap());
        this.addMouseListener(mia);
        this.addMouseMotionListener(mia);

        this.addMouseListener(new CenterMapListener(this.getMainMap()));

        this.addMouseWheelListener(new ZoomMouseWheelListenerCursor(this.getMainMap()));

        this.addKeyListener(new PanKeyListener(this.getMainMap()));

        // Use 8 threads in parallel to load the tiles
        tileFactory.setThreadPoolSize(8);

        // Default position
        final GeoPosition europe = new GeoPosition(47.20, 25.2);

        this.setZoom(15);
        this.setCenterPosition(europe);

        // Removing markers
        this.setAddressLocationShown(false);

        this.setDataProviderCreditShown(true);
    }

    public JComponent getMapComponent()
    {
        return this;
    }

    public Point2D getMapCoords()
    {
        return this.getMainMap().getCenter();
    }

    public void setMapZoom(final int zoom)
    {
        this.setZoom(zoom);
    }

    public void centerMap(final NetPlan netPlan)
    {
        centerMap(netPlan.getNodes());
    }

    public void centerMap(final List<Node> nodes)
    {
        final Point2D topologyCenter = getTopologyCenter(nodes);

        this.getMainMap().setCenter(topologyCenter);
        this.repaint();
    }

    public Point2D getTopologyCenter(final List<Node> nodes)
    {
        final List<Point2D> knots = nodes.stream().map(node -> node.getXYPositionMap()).collect(Collectors.toList());

        double centroidX = 0, centroidY = 0;

        for (Point2D knot : knots)
        {
            centroidX += knot.getX();
            centroidY += knot.getY();
        }
        return new Point2D.Double(centroidX / knots.size(), centroidY / knots.size());
    }

    public File saveMap(final int width, final int height)
    {
        // Creating data folder
        final File parent = new File("data/bg_maps");

        if (!parent.exists())
        {
            parent.mkdirs();
        }

        // It is necessary to save each map on their own file.
        // If we only use one, the map is never updated.
        final File f = new File(parent, "background_map_" + (mapID++) + ".png");
        f.deleteOnExit();

        // Saving map
        try
        {
            BufferedImage im = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            // Removing overlay
            this.getMiniMap().setVisible(false);
            this.getZoomSlider().setVisible(false);
            this.getZoomInButton().setVisible(false);
            this.getZoomOutButton().setVisible(false);

            this.paint(im.getGraphics());

            ImageIO.write(im, "PNG", f);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return f;
    }
}
