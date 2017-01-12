/*******************************************************************************
 * Copyright (c) 2015 Pablo Pavon Mariño.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * Contributors:
 *     Pablo Pavon Mariño - initial API and implementation
 ******************************************************************************/










package com.net2plan.internal.plugins;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Map;

import javax.swing.JComponent;

import com.net2plan.gui.utils.topologyPane.ITopologyCanvasPlugin;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.interfaces.networkDesign.Node;

/**
 * Base class for topology canvas.
 */
public interface ITopologyCanvas extends Plugin
{
	public Map<String, String> getCurrentOptions();

	public void setBackgroundOSMMapsActiveState (boolean activateMap);
	
	public boolean getBackgroundOSMMapsActiveState ();

	/**
	 * Adds a new plugin to the canvas.
	 *
	 * @param plugin Plugin
	 * @since 0.3.0
	 */
	public void addPlugin(ITopologyCanvasPlugin plugin);

	/**
	 * Returns the real coordinates in the topology for a given screen point.
	 *
	 * @param screenPoint Screen location
	 * @return Coordinates in the topology system for the screen point
	 * @since 0.3.0
	 */
	public Point2D convertViewCoordinatesToRealCoordinates(Point2D screenPoint);

	/**
	 * Returns the view coordinates in the panel for a given screen point.
	 *
	 * @param screenPoint Screen location
	 * @return Coordinates in the view system for the screen point
	 * @since 0.4.2
	 */
	public Point2D convertRealCoordinatesToViewCoordinates(Point2D screenPoint);

	/**
	 * Returns the top-level component of the canvas.
	 *
	 * @return Top-level component of the canvas
	 * @since 0.3.0
	 */
	public JComponent getComponent();

	/**
	 * Returns a reference to the internal component containing the canvas.
	 *
	 * @return Internal component containing the canvas
	 * @since 0.3.0
	 */
	public JComponent getInternalComponent();

	/**
	 * Returns the identifier of a link associated to a mouse event, or -1 otherwise.
	 *
	 * @param e Mouse event
	 * @return Link identifier, or -1 if no link was clicked
	 * @since 0.3.1
	 */
	public long getLink(MouseEvent e);

	/**
	 * Returns the identifier of a link associated to a mouse event, or -1 otherwise.
	 *
	 * @param e Mouse event
	 * @return Link identifier, or -1 if no link was clicked
	 * @since 0.3.1
	 */
	public long getNode(MouseEvent e);

	/**
	 * Pans the graph to the .
	 *
	 * @param initialPoint Initial point where the mouse was pressed
	 * @param currentPoint Current point where the mouse is
	 * @since 0.3.1
	 */
	public void panTo(Point2D initialPoint, Point2D currentPoint);

	/**
	 * Refreshes the canvas.
	 *
	 * @since 0.3.0
	 */
	public void refresh();


	/**
	 * Moves a node to the desired point.
	 * This method does not change the node's xy coordinates.
	 * Have in mind that by using this methos, the xy coordinates from the table do not equal the coordinates from the topology.
	 *
	 * @param npNode Node to move.
	 * @param point  Point to which the node will be moved.
	 */
	public void moveNodeToXYPosition(Node npNode, Point2D point);

	/**
	 * Removes a plugin from the canvas.
	 *
	 * @param plugin Plugin
	 * @since 0.3.0
	 */
	public void removePlugin(ITopologyCanvasPlugin plugin);

	/**
	 * Resets the emphasized elements.
	 *
	 * @since 0.3.0
	 */
	public void resetPickedStateAndRefresh();

	/**
	 * Takes a snapshot of the canvas.
	 *
	 * @since 0.3.0
	 */
	public void takeSnapshot();

	/**
	 * Refresh the canvas with the physical topology from the given network design.
	 *
	 * @param netPlan Network design
	 * @since 0.3.0
	 */
	public void rebuildTopology();

	/**
	 * Makes zoom-all from the center of the view.
	 *
	 * @since 0.3.0
	 */
	public void zoomAll();

	/**
	 * Makes zoom-in from the center of the view.
	 *
	 * @since 0.3.0
	 */
	public void zoomIn();

	/**
	 * Makes zoom-out from the center of the view.
	 *
	 * @since 0.3.0
	 */
	public void zoomOut();
}
