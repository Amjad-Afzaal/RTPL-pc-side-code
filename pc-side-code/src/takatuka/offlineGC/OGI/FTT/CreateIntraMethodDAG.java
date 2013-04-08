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
package takatuka.offlineGC.OGI.FTT;

import takatuka.offlineGC.OGI.DAGUtils.CreateDAG;
import takatuka.offlineGC.OGI.DAGUtils.DAGNodeRefsCalc;
import takatuka.offlineGC.OGI.GraphUtils.ReferenceFilter;
import takatuka.offlineGC.OGI.PNR.PNRAlgo;
import takatuka.offlineGC.OGI.GraphUtils.InstrGraphNode;
import takatuka.offlineGC.OGI.DAGUtils.DAGNode;
import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.attribute.*;
import takatuka.offlineGC.OGI.factory.BaseFactory;
import takatuka.offlineGC.OGI.superInstruction.*;
import takatuka.offlineGC.DFA.dataObjs.attribute.*;
import takatuka.offlineGC.DFA.dataObjs.functionState.FunctionStateValueElement;

/**
 * <p>Title: </p>
 * <p>Description:
 * 
 * Create a DAG for a given method.
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class CreateIntraMethodDAG {

    private static final CreateIntraMethodDAG myObj = new CreateIntraMethodDAG();
    private static final boolean shouldDebug = false;

    private CreateIntraMethodDAG() {
    }

    private static void debugPrint(Object obj) {
        if (shouldDebug) {
            System.out.println(obj);
        }
    }

    private static void debugPrint(Object obj1, Object obj2) {
        if (shouldDebug) {
            System.out.println(obj1 + "" + obj2);
        }
    }

    /**
     * 
     * @return
     */
    public static CreateIntraMethodDAG getInstanceOf() {
        return myObj;
    }

    private void printDAGNodes(Vector dagNodes) {
        Iterator<DAGNode> it = dagNodes.iterator();
        while (it.hasNext()) {
            DAGNode dagNode = it.next();
            debugPrint("\ndagNode =", dagNode);
        }
    }

    public DAGNode createDAG(MethodInfo method,
            HashMap<Long, FunctionStateValueElement> functionState,
            DAGNodeRefsCalc refCalc,
            ReferenceFilter instrRefFilter) {
        InstrGraphNode instrGraph = createInstrGraph(method, functionState, instrRefFilter);
        DAGNode ret = createDAG(instrGraph, refCalc);
        return ret;
    }

    public Vector<DAGNode> createDAGNodes(MethodInfo method,
            HashMap<Long, FunctionStateValueElement> functionState,
            DAGNodeRefsCalc refCalc,
            ReferenceFilter instrRefFilter) {
        CreateDAG createDAG = CreateDAG.getInstanceOf();
        InstrGraphNode instrGraph = createInstrGraph(method, functionState, instrRefFilter);
        Vector<DAGNode> dagNodeVec = createDAG.createDAGNodes(instrGraph, refCalc);
        return dagNodeVec;
    }

    public DAGNode createDAG(InstrGraphNode instrGraph, DAGNodeRefsCalc refCalc) {
        CreateDAG createDAG = CreateDAG.getInstanceOf();
        Vector<DAGNode> dagNodeVec = createDAG.createDAGNodes(instrGraph, refCalc);
        DAGNode ret = createDAG.createLinkBetweenConnectedComponents(dagNodeVec, instrGraph);
        if (shouldDebug) {
            printDAGNodes(dagNodeVec);
        }
        return ret;

    }

    private static class GraphNodeAndParent {

        InstrGraphNode gNode = null;
        InstrGraphNode parent = null;

        public GraphNodeAndParent(InstrGraphNode gNode, InstrGraphNode parent) {
            this.gNode = gNode;
            this.parent = parent;
        }
    }

    public InstrGraphNode createInstrGraph(MethodInfo method,
            HashMap<Long, FunctionStateValueElement> functionState, ReferenceFilter refFilter) {
        Vector<Instruction> instrVec = method.getInstructions();
        Stack<GraphNodeAndParent> stack = new Stack<GraphNodeAndParent>();
        /**
         * To make sure that one node is created exactly once.
         */
        HashMap<Long, InstrGraphNode> alreadyCreatedRecord = new HashMap<Long, InstrGraphNode>();
        Instruction startInstr = instrVec.firstElement();
        createGraphNode(startInstr, method, functionState,
                alreadyCreatedRecord, refFilter);
        InstrGraphNode ret = alreadyCreatedRecord.get(startInstr.getInstructionId());
        stack.push(new GraphNodeAndParent(ret, null));
        boolean temp = false;
        InstrGraphNode.lastAlreadyVisitedNumberUsed ++;
        if (shouldDebug) {
            PNRAlgo.debugOn();
        }
        while (!stack.empty()) {
            GraphNodeAndParent gNodeAndParent = stack.pop();
            InstrGraphNode currentNode = gNodeAndParent.gNode;
            InstrGraphNode parent = gNodeAndParent.parent;
            if (parent != null) {
                parent.addChild(currentNode);
            }
            debugPrint("\n\ngraph so far = ", ret);
            GCInstruction instr = currentNode.getInstruction().getNormalInstrs().firstElement();
            if (currentNode.alreadyVisited == InstrGraphNode.lastAlreadyVisitedNumberUsed) {
                continue;
            }
            currentNode.alreadyVisited = InstrGraphNode.lastAlreadyVisitedNumberUsed;
            Vector nextInstrVec = instr.getNextInstrsToBeExecutedRecord();
            Iterator it = nextInstrVec.iterator();
            while (it.hasNext()) {
                GCInstruction nextInstr = (GCInstruction) it.next();
                InstrGraphNode nextGraphNode = alreadyCreatedRecord.get(nextInstr.getInstructionId());
                if (nextGraphNode == null) {
                    createGraphNode(nextInstr, method,
                            functionState, alreadyCreatedRecord, refFilter);
                    nextGraphNode = alreadyCreatedRecord.get(nextInstr.getInstructionId());
                }
                stack.push(new GraphNodeAndParent(nextGraphNode, currentNode));
            }
        }
        if (shouldDebug) {
            PNRAlgo.debugBackToOriginal();
        }
        return ret;
    }

    private void createGraphNode(Instruction instr, MethodInfo method,
            HashMap<Long, FunctionStateValueElement> functionState,
            HashMap<Long, InstrGraphNode> alreadyCreatedRecord,
            ReferenceFilter filter) {
        SuperInstruction superInstr = new SuperInstruction(method);
        superInstr.addNormalInstrs((GCInstruction) instr);
        InstrGraphNode gNode = BaseFactory.getInstanceOf().createGraphNode(superInstr, method, 1, null);
        //gNode.setReferenceFilter(IntraBlockRefFilter.getInstanceOf());
        gNode.setReferenceFilter(filter);
        gNode.setReferencesUsed(functionState);
        alreadyCreatedRecord.put(instr.getInstructionId(), gNode);
    }
}
