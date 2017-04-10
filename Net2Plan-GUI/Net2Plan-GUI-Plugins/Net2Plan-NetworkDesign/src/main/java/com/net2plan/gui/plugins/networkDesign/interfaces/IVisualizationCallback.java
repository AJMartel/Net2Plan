/*******************************************************************************
 * Copyright (c) 2015 Pablo Pavon Mariño.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Contributors:
 * Pablo Pavon Mariño - initial API and implementation
 ******************************************************************************/


package com.net2plan.gui.plugins.networkDesign.interfaces;

import com.net2plan.gui.plugins.networkDesign.visualizationControl.VisualizationState;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.internal.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * Interface to be implemented by any class dealing with network designs.
 */
public interface IVisualizationCallback
{
    void updateVisualizationJustTables();

    void updateVisualizationJustCanvasLinkNodeVisibilityOrColor();

    void updateVisualizationAfterNewTopology();

    void updateVisualizationAfterChanges(Set<Constants.NetworkElementType> modificationsMade);

    void updateVisualizationAfterPick();

    @Nonnull
    NetPlan getDesign();

    @Nullable
    NetPlan getInitialDesign();

    @Nonnull
    VisualizationState getVisualizationState();
}
