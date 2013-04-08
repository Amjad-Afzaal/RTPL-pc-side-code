/*
 * Copyright 2010 Christian Schindelhauer, Peter Thiemann, Faisal Aslam, Luminous Fennell and Gidon Ernst.
 * All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 *
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * only, as published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 3 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 *
 * You should have received a copy of the GNU General Public License
 * version 3 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 *
 * Please contact Faisal Aslam
 * (aslam AT informatik.uni-freibug.de or studentresearcher AT gmail.com)
 * if you need additional information or have any questions.
 */
package takatuka.offlineGC.OGI.DAGUtils;

import takatuka.offlineGC.OGI.GraphUtils.InstrGraphNode;
import takatuka.offlineGC.OGI.DAGUtils.DAGNode;
import java.util.*;
import takatuka.offlineGC.DFA.dataObjs.*;

/**
 * <p>Title: </p>
 * <p>Description:
 *  The class decides what references should appear on a DAG Node
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class DAGNodeRefsCalc {

    private static final DAGNodeRefsCalc myObj = new DAGNodeRefsCalc();
    protected static HashSet<TTReference> interFunctionReferences = null;

    /**
     *
     */
    protected DAGNodeRefsCalc() {
    }

    /**
     * 
     * @return
     */
    public static DAGNodeRefsCalc getInstanceOf() {
        return myObj;
    }

    public boolean init() {
        return true;
    }

    /**
     * @param ref
     * @param node
     * @return : if the given ref is valid for the DAGNode then return true.
     */
    public boolean validReference(TTReference ref, DAGNode node) {
        return true;
    }

    public HashSet<TTReference> getReferences(DAGNode node) {
        HashSet<TTReference> references = new HashSet<TTReference>();
        HashSet<InstrGraphNode> graphNodes = node.getGraphNodes();
        Iterator<InstrGraphNode> it = graphNodes.iterator();
        while (it.hasNext()) {
            InstrGraphNode gNode = it.next();
            references.addAll(gNode.getReferences());
        }
        return references;
    }
}
