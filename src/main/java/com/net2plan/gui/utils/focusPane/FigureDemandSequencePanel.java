package com.net2plan.gui.utils.focusPane;

import com.net2plan.gui.utils.IVisualizationCallback;
import com.net2plan.interfaces.networkDesign.*;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * @author Jorge San Emeterio
 * @date 06-Feb-17
 */
public class FigureDemandSequencePanel extends FigureSequencePanel
{
    private final List<String> generalMessage;
    private final Demand demand;
    private final BasicStroke lineStroke;
    private Dimension preferredSize;

    public FigureDemandSequencePanel(final IVisualizationCallback callback, final Demand demand, final String... titleMessage)
    {
        super(callback);
        this.demand = demand;
        this.generalMessage = Arrays.asList(titleMessage);
        this.lineStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{10.0f}, 0.0f);
        this.preferredSize = null;
    }

    @Override
    public Dimension getPreferredSize()
    {
        return preferredSize == null? new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT) : preferredSize;
    }

    @Override
    protected void paintComponent(Graphics graphics)
    {
        final Graphics2D g2d = (Graphics2D) graphics;
        g2d.setColor(Color.black);

        final int maxIconSize = 40;
        final int maxNumberOfTagsPerNodeNorResource = 1;

    	/* Initial messages */
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        final int fontHeightTitle = g2d.getFontMetrics().getHeight();
        for (int indexMessage = 0; indexMessage < generalMessage.size(); indexMessage++)
        {
            final String m = generalMessage.get(indexMessage);
            g2d.drawString(m, maxIconSize, maxIconSize + (indexMessage * fontHeightTitle));
        }

        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        final FontMetrics fontMetrics = g2d.getFontMetrics();
        final int regularInterlineSpacePixels = fontMetrics.getHeight();

        final DrawNode ingressNode = new DrawNode(demand.getIngressNode(), demand.getLayer(), maxIconSize);
        final DrawNode egressNode = new DrawNode(demand.getEgressNode(), demand.getLayer(), maxIconSize);

        final int topCoordinateLineNodes = maxIconSize + (generalMessage.size() * fontHeightTitle) + (maxNumberOfTagsPerNodeNorResource * regularInterlineSpacePixels);
        final Point initialDnTopLeftPosition = new Point(maxIconSize, topCoordinateLineNodes);
        final int xSeparationDnCenters = maxIconSize * 3;

    	/* Initial dn */
        Point auxPoint;
        Point southEastPoint = new Point (0,0);
        auxPoint = DrawNode.addNodeToGraphics(g2d, ingressNode, initialDnTopLeftPosition, fontMetrics, regularInterlineSpacePixels, null);
        southEastPoint = southEastPoint(southEastPoint , auxPoint);
        auxPoint = DrawNode.addNodeToGraphics(g2d, egressNode, new Point(initialDnTopLeftPosition.x + xSeparationDnCenters, initialDnTopLeftPosition.y), fontMetrics, regularInterlineSpacePixels, null);
        southEastPoint = southEastPoint(southEastPoint , auxPoint);

        drawnNodes.add(ingressNode);
        drawnNodes.add(egressNode);

        final DrawLine link = new DrawLine(ingressNode, egressNode, ingressNode.posEast(), egressNode.posWest());
        auxPoint = DrawLine.addLineToGraphics(g2d, link, fontMetrics, regularInterlineSpacePixels,lineStroke);
        southEastPoint = southEastPoint(southEastPoint , auxPoint);
        preferredSize = new Dimension (southEastPoint.x + XYMARGIN , southEastPoint.y + XYMARGIN);
    }
    
    private static Point southEastPoint (Point a , Point b) { return new Point (Math.max(a.x , b.x), Math.max(a.y,b.y)); }
    
}
