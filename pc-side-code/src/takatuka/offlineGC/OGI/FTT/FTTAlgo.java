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
package takatuka.offlineGC.OGI.FTT;

import takatuka.offlineGC.OGI.DAGUtils.DAGNodeRefsCalc;
import takatuka.offlineGC.OGI.GraphUtils.InstrGraphNode;
import takatuka.offlineGC.OGI.DAGUtils.GraphLabelsController;
import takatuka.offlineGC.OGI.DAGUtils.DAGNode;
import takatuka.offlineGC.OGI.GraphUtils.CreateInterMethodLinks;
import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.offlineGC.DFA.dataObjs.attribute.*;
import takatuka.offlineGC.OGI.GraphUtil.FTT.*;
import takatuka.offlineGC.DFA.dataObjs.*;
import takatuka.offlineGC.DFA.dataObjs.functionState.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;

/**
 * <p>Title: </p>
 * <p>Description:
 * We traverse the MethodLevel graph and free references that are NEVER assigned
 * to a field. We use following algorithm.
 *
 * - Start from the first method and do the DFS.
 * - During DFS we encounter a reference that is used first time on a method.
 * --- Make the method DAG and free the reference so that it will never be used again.
 * --- In case making method DAG and removing the reference is not possible then
 *     freed on it parents node after the invoke instruction to that child node.
 *
 * If a parent node has already freed the references After invoke instruction
 * A, then the subgraph emerging from invoke instruction A never frees the ref
 * again.
 * 
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class FTTAlgo {

    private static final FTTAlgo myObj = new FTTAlgo();
    private static CreateInterMethodLinks intraMethodLinkInfo = CreateInterMethodLinks.getInstanceOf();
    private HashMap<MethodInfo, CacheForSpeed> cacheOfDAGNodes = new HashMap<MethodInfo, CacheForSpeed>();
    private HashSet<TTReference> allRefFreedInsideTheMethodTheyAreUsed = new HashSet<TTReference>();

    private class CacheForSpeed {

        public DAGNode dagNode = null;
        public InstrGraphNode instrGraph = null;

        public CacheForSpeed(DAGNode dagNode, InstrGraphNode instrGraph) {
            this.dagNode = dagNode;
            this.instrGraph = instrGraph;
        }
    }

    private FTTAlgo() {
    }

    public static FTTAlgo getInstanceOf() {
        return myObj;
    }

    public GCInstruction getNextFromInvokeInstr(InstrGraphNode invokeNode) {
        return intraMethodLinkInfo.getInvokeInstrOriginalChild(invokeNode);
    }

    public HashSet<TTReference> getRefFreeInsideMethodTheyAreUsed() {
        return allRefFreedInsideTheMethodTheyAreUsed;
    }

    public void execute() {

        MethodLevelGraphController methodLevelContr = MethodLevelGraphController.getInstanceOf();
        Collection<MethodLevelGraphNode> startingNodeOfAThread = methodLevelContr.getFirstNodeRecorded();
        Iterator<MethodLevelGraphNode> it = startingNodeOfAThread.iterator();
        HashSet<MethodLevelGraphNode> alreadyVisited = new HashSet<MethodLevelGraphNode>();
        while (it.hasNext()) {
            MethodLevelGraphNode startingPoint = it.next();
            freeRefInsideFunctionTheyAreUsed(startingPoint);
            Stack<MethodLevelChildInfo> stack = new Stack<MethodLevelChildInfo>();
            stack.push(new MethodLevelChildInfo(startingPoint, null));
            while (!stack.empty()) {
                MethodLevelChildInfo currentNode = stack.pop();

                String methodStr = currentNode.getNode().getMethodStr();
                if (methodStr.contains("jvmTestCases.Main.testStaticSync")) {
                    //Miscellaneous.println("hellp htere stop ");
                }
                HashSet<TTReference> currentNodeRefs = currentNode.getNode().getRefsByMeButNeverAssignedToAField();
                if (alreadyVisited.contains(currentNode.getNode())) {
                    continue;
                }
                alreadyVisited.add(currentNode.getNode());
                /**
                 * check all the children of currentNode. All the references that are
                 * not already freed
                 * - either on the child node itself.
                 * - or on any of the parents of the current node
                 * are freed on the current node.
                 */
                HashSet<MethodLevelChildInfo> children = currentNode.getNode().getChildren();
                Iterator<MethodLevelChildInfo> childIt = children.iterator();
                while (childIt.hasNext()) {
                    MethodLevelChildInfo childInfo = childIt.next();
                    HashSet<TTReference> refAtChild = (HashSet<TTReference>) childInfo.getNode().getRefsByMeButNeverAssignedToAField().clone();
                    /**
                     * remove reference that are present in the current node.
                     */
                    refAtChild.removeAll(currentNodeRefs);
                    /**
                     * remove references that are already freed on current node or any
                     * of its parent node.
                     */
                    refAtChild.removeAll(currentNode.alreadyFreedRef);

                    GCInstruction toFreeAt = getNextFromInvokeInstr(childInfo.getParentInvokeInstr());
                    refAtChild.removeAll(childInfo.getNode().getRefersUsedByAnscestors());
                    refAtChild.removeAll(allRefFreedInsideTheMethodTheyAreUsed);
                    if (refAtChild.size() != 0) {
                        refAtChild.removeAll(allRefFreedInsideTheMethodTheyAreUsed);
                        toFreeAt.addReferencesFreedOnMe(refAtChild, GCInstruction.FTT);
                    }
                    childInfo.alreadyFreedRef.addAll(currentNode.alreadyFreedRef);
                    childInfo.alreadyFreedRef.addAll(refAtChild);
                    stack.push(childInfo);
                }
            }
        }
    }

    private void freeRefInsideFunctionTheyAreUsed(MethodLevelGraphNode startingPoint) {
        Stack<MethodLevelChildInfo> stack = new Stack<MethodLevelChildInfo>();
        HashSet<MethodLevelGraphNode> alreadyVisited = new HashSet<MethodLevelGraphNode>();
        HashSet<TTReference> ret = new HashSet<TTReference>();
        stack.push(new MethodLevelChildInfo(startingPoint, null));
        while (!stack.empty()) {
            MethodLevelChildInfo currentNode = stack.pop();


            String methodStr = currentNode.getNode().getMethodStr();
            //printAncestors(currentNode.getNode());
            if (methodStr.contains("tryToSend")) {
                //Miscellaneous.println("hello htere stop ");
            }
            HashSet<TTReference> currentNodeRefs = currentNode.getNode().getRefsByMeButNeverAssignedToAField();
            if (alreadyVisited.contains(currentNode.getNode())) {
                continue;
            }
            alreadyVisited.add(currentNode.getNode());
            HashSet<TTReference> refFreedOnTheMethodItself = freeRefOnTheNodeItself(currentNode.getNode(), currentNodeRefs);
            allRefFreedInsideTheMethodTheyAreUsed.addAll(refFreedOnTheMethodItself);
            stack.addAll(currentNode.getNode().getChildren());
        }
    }

    private void printAncestors(MethodLevelGraphNode node) {
        String methodStr = node.getMethodStr();
        System.out.println("\n\n\n*************** " + methodStr + "************* \n");
        HashSet<MethodLevelGraphNode> nodesSet = node.getAllAncestors();
        Iterator<MethodLevelGraphNode> it = nodesSet.iterator();
        while (it.hasNext()) {
            MethodLevelGraphNode child = it.next();
            System.out.println(child.getLabel());
        }
    }

    private CacheForSpeed getDAGNodeAndInstrGraph(MethodInfo method) {
        CacheForSpeed ret = cacheOfDAGNodes.get(method);
        if (ret != null) {
            return ret;
        }
        CreateIntraMethodDAG createIntraMethodDAG = CreateIntraMethodDAG.getInstanceOf();

        FunctionStateRecorder stateRecorder = FunctionStateRecorder.getInstanceOf();
        HashMap<FunctionStateKey, FunctionStateKeyMapValue> stateKeyMap = stateRecorder.getFunctionStatePerMethod(method);
        /* --- for debugging ---
        Oracle oracle = Oracle.getInstanceOf();
        String methodSTring = oracle.getMethodOrFieldString(method);
        if (methodSTring.contains("copiedProg.Node.<init>(LcopiedProg/Node;LcopiedProg/Node;)V")) {
        System.out.println("stop here");
        System.out.println(stateKeyMap);
        }*/
        FunctionStateKeyMapValue specialValue = stateKeyMap.values().iterator().next();
        FunctionStateValue value = (FunctionStateValue) specialValue.getValue();
        HashMap stateMap = value.getFunctionStateValues();
        InstrGraphRefFilter filterForInstrGraphRef = InstrGraphRefFilter.getInstanceOf();
        InstrGraphNode sourceGraphNode = createIntraMethodDAG.createInstrGraph(method, stateMap, filterForInstrGraphRef);

        DAGNode dagNode = createIntraMethodDAG.createDAG(method, stateMap, DAGNodeRefsCalc.getInstanceOf(), filterForInstrGraphRef);
        GraphLabelsController.getInstanceOf().printGraphInFile(dagNode);
        ret = new CacheForSpeed(dagNode, sourceGraphNode);
        cacheOfDAGNodes.put(method, ret);
        return ret;
    }

    /**
     * create the method DAG and remove references using the original DAG Based algorithm.
     * 
     * @param node
     * @param references
     */
    private HashSet<TTReference> freeRefOnTheNodeItself(MethodLevelGraphNode node, HashSet<TTReference> references) {
        HashSet<TTReference> refFreed = new HashSet<TTReference>();
        MethodInfo method = node.getMethod();
        Oracle oracle = Oracle.getInstanceOf();
        HashSet<TTReference> ret = new HashSet<TTReference>();
        if (references.isEmpty()) {
            return ret; //nothing to free.
        }

        CacheForSpeed cacheForSpeed = getDAGNodeAndInstrGraph(method);
        PNRForFTT.getInstanceOf().freeRefBasedOnDAG(refFreed, cacheForSpeed.dagNode,
                cacheForSpeed.instrGraph, GCInstruction.FTT_PNR);
        return refFreed;
    }
}
