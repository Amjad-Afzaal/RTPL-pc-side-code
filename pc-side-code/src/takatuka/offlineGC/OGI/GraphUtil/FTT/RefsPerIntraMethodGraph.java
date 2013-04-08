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
package takatuka.offlineGC.OGI.GraphUtil.FTT;

import takatuka.offlineGC.OGI.GraphUtils.InstrGraphNode;
import takatuka.offlineGC.DFA.dataObjs.*;
import java.util.*;

/**
 * <p>Title: </p>
 * <p>Description:
 * Given a set of DAGNodes we want to find references that appears in more than one
 * DAGNode.
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class RefsPerIntraMethodGraph {

    private static final RefsPerIntraMethodGraph myObj = new RefsPerIntraMethodGraph();
    private HashMap<Integer, HashSet<TTReference>> refPerIntraMethodGraph = new HashMap<Integer, HashSet<TTReference>>();

    private RefsPerIntraMethodGraph() {
    }

    /**
     * 
     * @return
     */
    public static RefsPerIntraMethodGraph getInstanceOf() {
        return myObj;
    }

    public HashSet<TTReference> getRefUseInAMethodGraph(int intraMethodId) {
        HashSet<TTReference> ret = (HashSet<TTReference>) refPerIntraMethodGraph.get(intraMethodId).clone();
        if (ret == null) {
            return new HashSet();
        }
        return ret;
    }
    /**
     * 
     * @param dagNodes
     */
    public void recordRefUsage(InstrGraphNode gNode, HashSet<TTReference> refUsedSet) {
        int intraMethodId = gNode.getIntraMethodGraphID();
        HashSet<TTReference> refForIntraMethodGraph = refPerIntraMethodGraph.get(intraMethodId);
        if (refForIntraMethodGraph == null) {
            refForIntraMethodGraph = new HashSet<TTReference>();
            refPerIntraMethodGraph.put(intraMethodId, refForIntraMethodGraph);
        }
        refForIntraMethodGraph.addAll(refUsedSet);
    }

}
