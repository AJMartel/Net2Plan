package com.net2plan.gui.utils.focusPane;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JPanel;

import com.google.common.collect.Sets;
import com.net2plan.gui.utils.IVisualizationCallback;
import com.net2plan.gui.utils.topologyPane.VisualizationState;
import com.net2plan.interfaces.networkDesign.Demand;
import com.net2plan.interfaces.networkDesign.Link;
import com.net2plan.interfaces.networkDesign.MulticastDemand;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.interfaces.networkDesign.NetworkElement;
import com.net2plan.interfaces.networkDesign.NetworkLayer;
import com.net2plan.interfaces.networkDesign.Node;
import com.net2plan.interfaces.networkDesign.Resource;
import com.net2plan.interfaces.networkDesign.Route;
import com.net2plan.interfaces.networkDesign.SharedRiskGroup;
import com.net2plan.internal.Constants.NetworkElementType;
import com.net2plan.utils.Pair;

public class FigureNodeSequencePanel extends FigureSequencePanel
{
    private DrawNode dn;
    private List<String> generalMessage;
    private Node node;
    private NetworkLayer layer;
    private Dimension preferredSize;

    public FigureNodeSequencePanel(IVisualizationCallback callback, Node node, NetworkLayer layer, String titleMessage)
    {
        super(callback);
        this.layer = layer;
        this.node = node;
        this.generalMessage = Arrays.asList(titleMessage);
        this.preferredSize = null;
    }

    @Override
    protected void paintComponent(Graphics grphcs)
    {
        super.paintComponent(grphcs);
        final Graphics2D g2d = (Graphics2D) grphcs;
        g2d.setColor(Color.black);

        int currentXYStartOfText = 40;
        final int maxHeightOrSizeIcon = 60;
        final int maxNumberOfTagsPerNodeNorResource = 1;

    	/* Initial messages */
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        final int fontHeightTitle = g2d.getFontMetrics().getHeight();
        for (int indexMessage = 0; indexMessage < generalMessage.size(); indexMessage++)
        {
            final String m = generalMessage.get(indexMessage);
            g2d.drawString(m, currentXYStartOfText, currentXYStartOfText);
            currentXYStartOfText += fontHeightTitle;
        }

        currentXYStartOfText += 15; // separation main message to first info nodes

        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        final FontMetrics fontMetrics = g2d.getFontMetrics();
        final int regularInterlineSpacePixels = fontMetrics.getHeight();

        final int topCoordinateLineNodes = currentXYStartOfText + (maxNumberOfTagsPerNodeNorResource * regularInterlineSpacePixels);
        final Point initialDnTopLeftPosition = new Point(maxHeightOrSizeIcon, topCoordinateLineNodes);

    	/* Initial dn */
        dn = new DrawNode(node, layer, maxHeightOrSizeIcon);
        DrawNode.addNodeToGraphics(g2d, dn, initialDnTopLeftPosition, fontMetrics, regularInterlineSpacePixels);
        drawnNodes.add(dn);
    }
}

