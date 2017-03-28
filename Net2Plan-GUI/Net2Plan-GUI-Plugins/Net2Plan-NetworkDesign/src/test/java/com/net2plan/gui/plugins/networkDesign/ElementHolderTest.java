/*
 * ******************************************************************************
 *  * Copyright (c) 2017 Pablo Pavon-Marino.
 *  * All rights reserved. This program and the accompanying materials
 *  * are made available under the terms of the GNU Lesser Public License v3.0
 *  * which accompanies this distribution, and is available at
 *  * http://www.gnu.org/licenses/lgpl.html
 *  *
 *  * Contributors:
 *  *     Pablo Pavon-Marino - Jose-Luis Izquierdo-Zaragoza, up to version 0.3.1
 *  *     Pablo Pavon-Marino - from version 0.4.0 onwards
 *  *     Pablo Pavon Marino - Jorge San Emeterio Villalain, from version 0.4.1 onwards
 *  *****************************************************************************
 */

package com.net2plan.gui.plugins.networkDesign;

import com.net2plan.interfaces.networkDesign.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jorge San Emeterio Villalain
 * @date 28/03/17
 */
public class ElementHolderTest
{
    private NetPlan netPlan;
    private List<NetworkElement> networkElements;
    private ElementHolder elementHolder;

    @Before
    public void setUp()
    {
        elementHolder = null;
        networkElements = new ArrayList<>();

        netPlan = new NetPlan();

        netPlan.addNode(0, 0, "Node 1", null);
        netPlan.addNode(0, 0, "Node 2", null);
        netPlan.addNode(0, 0, "Node 3", null);

        netPlan.addLink(netPlan.getNodeByName("Node 1"), netPlan.getNodeByName("Node 2"), 0, 0, 1e3, null);
        netPlan.addLink(netPlan.getNodeByName("Node 1"), netPlan.getNodeByName("Node 2"), 0, 0, 1e3, null);

        netPlan.addDemand(netPlan.getNodeByName("Node 1"), netPlan.getNodeByName("Node 2"), 0, null);
    }

    @Test (expected = RuntimeException.class)
    public void checkNetworkListHomogeneity()
    {
        networkElements.clear();
        networkElements.addAll(netPlan.getNodes());
        elementHolder = new ElementHolder(networkElements, new ArrayList<>());
    }
}