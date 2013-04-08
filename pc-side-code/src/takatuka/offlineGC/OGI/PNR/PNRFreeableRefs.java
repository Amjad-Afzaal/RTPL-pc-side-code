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
package takatuka.offlineGC.OGI.PNR;

import takatuka.offlineGC.OGI.DAGUtils.DAGNodeRefsCalc;
import takatuka.offlineGC.OGI.GraphUtils.InstrGraphNode;
import takatuka.offlineGC.OGI.DAGUtils.DAGNode;
import java.util.*;
import takatuka.offlineGC.DFA.dataObjs.*;
import takatuka.offlineGC.OGI.FTT.FTTAlgo;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class PNRFreeableRefs extends DAGNodeRefsCalc {

    private static final PNRFreeableRefs myObj = new PNRFreeableRefs();
    private static HashSet<TTReference> referenceFreeDuringIntraMethodAlgo = null;

    /**
     * 
     * @return
     */
    public static DAGNodeRefsCalc getInstanceOf() {
        return myObj;
    }

    @Override
    public boolean validReference(TTReference ref, DAGNode dNode) {
        if (referenceFreeDuringIntraMethodAlgo == null) {
            referenceFreeDuringIntraMethodAlgo = new HashSet<TTReference>();//(HashSet<TTReference>) IntraMethodAlgorithm.getInstanceOf().getReferencesFreed().clone();
        }
        if (referenceFreeDuringIntraMethodAlgo.contains(ref) ||
                FTTAlgo.getInstanceOf().getRefFreeInsideMethodTheyAreUsed().contains(ref)) {
            return false;
        }
        return true;
    }

    @Override
    public HashSet<TTReference> getReferences(DAGNode node) {
            HashSet<TTReference> references = new HashSet<TTReference>();
        HashSet<InstrGraphNode> graphNodes = node.getGraphNodes();
        Iterator<InstrGraphNode> it = graphNodes.iterator();
        while (it.hasNext()) {
            InstrGraphNode gNode = it.next();
            HashSet<TTReference> nodeRef = gNode.getReferences();
            Iterator<TTReference> nodeRefIt = nodeRef.iterator();
            while (nodeRefIt.hasNext()) {
                TTReference ref = nodeRefIt.next();
                if (validReference(ref, node)) {
                    references.add(ref);
                }
            }
        }
        return references;
    }
}
