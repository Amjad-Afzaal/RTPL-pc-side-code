/*
 * Copyright 2009 Christian Schindelhauer, Peter Thiemann, Faisal Aslam, Luminous Fennell and Gidon Ernst.
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
package takatuka.offlineGC.OGI.GraphUtils;

import takatuka.offlineGC.OGI.PNR.PNRAlgo;
import takatuka.offlineGC.OGI.GraphUtils.InstrGraphNode;
import java.util.*;
import takatuka.classreader.dataObjs.MethodInfo;
import takatuka.classreader.logic.util.Miscellaneous;
import takatuka.offlineGC.OGI.superInstruction.SuperInstruction;
import takatuka.offlineGC.DFA.dataObjs.TTReference;
import takatuka.optimizer.cpGlobalization.logic.util.Oracle;

/**
 * <p>Title: </p>
 * <p>Description:
 * Given two exactly same instruction graph of a same method (but different references)
 * this class merge references of node of first graph with a corresponding node
 * of the second graph.
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class MergeInstrGraph {

    private static final MergeInstrGraph myObj = new MergeInstrGraph();

    private MergeInstrGraph() {
    }

    public static final MergeInstrGraph getInstanceOf() {
        return myObj;
    }

    /**
     * given two instruction graph of the same method but different references
     * on some node. This function merge their references.
     *
     * @param mergeInto The graph that is changed
     * @param mergeFrom The graph that is unchanged but get data to merge into another graph.
     */
    public void merge(InstrGraphNode mergeInto, InstrGraphNode mergeFrom) {
        HashMap<SuperInstruction, InstrGraphNode> mapMergeInto = createSuperInstrToInstrGraphNodeMap(mergeInto);
        HashMap<SuperInstruction, InstrGraphNode> mapMergeFrom = createSuperInstrToInstrGraphNodeMap(mergeFrom);
        MethodInfo method = mergeFrom.getMethod();
        Oracle oracle = Oracle.getInstanceOf();
        String methodStr = oracle.getMethodOrFieldString(method);
        System.out.println("---------> "+methodStr);
        if (true) {
            PNRAlgo.debugOn();
            System.out.println("merge into =" + mergeInto);
            System.out.println("merge from =" + mergeFrom);
        }
        Iterator<SuperInstruction> it = mapMergeInto.keySet().iterator();
        while (it.hasNext()) {
            SuperInstruction superInstr = it.next();
            InstrGraphNode graphNodeMergeInto = mapMergeInto.get(superInstr);
            InstrGraphNode graphNodeMergeFrom = mapMergeFrom.get(superInstr);
            if (graphNodeMergeFrom == null) {
                System.err.println("error 82392r");
                PNRAlgo.debugOn();
                System.err.println("super instruction in question= " + superInstr);
                System.err.println(" from graph =" + graphNodeMergeFrom);
                System.err.println(" into graph =" + graphNodeMergeInto);
                Miscellaneous.exit();
            }
            mergeSingleNode(graphNodeMergeInto, graphNodeMergeFrom);
        }


    }

    private HashMap<SuperInstruction, InstrGraphNode> createSuperInstrToInstrGraphNodeMap(InstrGraphNode instrGraphNode) {
        Stack<InstrGraphNode> stack = new Stack<InstrGraphNode>();
        stack.push(instrGraphNode);
        HashMap<SuperInstruction, InstrGraphNode> ret = new HashMap<SuperInstruction, InstrGraphNode>();
        InstrGraphNode.lastAlreadyVisitedNumberUsed ++;
        while (!stack.empty()) {
            InstrGraphNode node = stack.pop();
            if (node.alreadyVisited == InstrGraphNode.lastAlreadyVisitedNumberUsed) {
                continue;
            }
            node.alreadyVisited = InstrGraphNode.lastAlreadyVisitedNumberUsed;
            SuperInstruction superInstr = node.getInstruction();
            InstrGraphNode oldSavedNode = ret.get(superInstr);
            if (oldSavedNode != null) {
                Miscellaneous.printlnErr("error 12492");
                Miscellaneous.exit();
            }
            ret.put(superInstr, node);
            stack.addAll(node.getChildren());
        }
        return ret;
    }

    private void mergeSingleNode(InstrGraphNode mergeInto, InstrGraphNode mergeFrom) {
        HashSet<TTReference> refOfMergeInto = mergeInto.getReferences();
        HashSet<TTReference> refOffMergeFrom = mergeFrom.getReferences();
        refOfMergeInto.addAll(refOffMergeFrom);
        mergeInto.addMethodCallInfo(mergeFrom);
    }
}
