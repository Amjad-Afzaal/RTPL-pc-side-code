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

import takatuka.offlineGC.OGI.GraphUtils.InstrGraphNode;
import takatuka.offlineGC.OGI.DAGUtils.GraphLabelsController;
import takatuka.offlineGC.OGI.DAGUtils.DAGNode;
import takatuka.offlineGC.OGI.GraphUtils.CreateIMGraph;
import java.util.*;
import takatuka.classreader.logic.logAndStats.*;
import takatuka.classreader.logic.util.*;
import takatuka.offlineGC.DFA.dataObjs.*;
import takatuka.offlineGC.DFA.dataObjs.attribute.*;
import takatuka.offlineGC.OGI.DAGUtils.CreateDAG;
import takatuka.offlineGC.OGI.factory.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * ******* The Point of No Return (PNR) algorithm *******
 *
 * It says that all the references used on my immediate parents are freed on me
 * if these references are never used in any of my children and me.
 *
 * The algorithm works in two steps:
 * Step 1: Go through the graph (from bottom to up) record all the references used
 * on a node and its children on the node. Say this reference set for Node i as BMi.
 *
 * Step 2: Now all references used on a node i
 * immediate parents but not in BMi set are free on that node. A node i can be now
 * picked without any order and even randomly.
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class PNRAlgo {

    private static boolean shouldDebugPrint = false;
    private static boolean tempShouldDebugPrint = true;
    private int totalFreed = 0;
    private HashSet<TTReference> allRefFreed = null;

    /**
     *
     */
    protected PNRAlgo() {
    }

    /**
     *
     * @param str
     */
    public static void debug(Object obj) {
        if (shouldDebugPrint) {
            Miscellaneous.println(obj);
        }
    }

    public static void debug(Object obj1, Object obj2) {
        if (shouldDebugPrint) {
            Miscellaneous.println(obj1 + ", " + obj2);
        }
    }

    public static void debugOn() {
        tempShouldDebugPrint = shouldDebugPrint;
        shouldDebugPrint = true;

    }

    public static boolean isDebugOn() {
        return shouldDebugPrint;
    }

    public static void debugBackToOriginal() {
        shouldDebugPrint = tempShouldDebugPrint;
    }

    /**
     *
     */
    protected void setFactory() {
        IFactory factory = BaseFactory.getInstanceOf();
        FactoryPlaceHolder.getInstanceOf().setCurrentFactory(factory);
    }

    /**
     *
     * @param sourceGraphNode
     * @param factory
     */
    protected void execute(InstrGraphNode sourceGraphNode,
            HashSet<TTReference> refFreed) {
        IFactory factory = FactoryPlaceHolder.getInstanceOf().getCurrentFactory();
        CreateDAG createDAG = factory.createDAG();
        //make the DAG
        DAGNode sourceDAGNode = createDAG.execute(sourceGraphNode);
        LogHolder.getInstanceOf().addLog(" created the DAG  ", false);
        //GraphLabelsController.printGraphInFile = true;
        GraphLabelsController.getInstanceOf().printGraphInFile(sourceDAGNode);

        freeRefBasedOnDAG(refFreed, sourceDAGNode, sourceGraphNode, GCInstruction.PNR);
    }

    public void freeRefBasedOnDAG(HashSet<TTReference> refFreed,
            DAGNode sourceDAGNode, InstrGraphNode sourceGraphNode, 
            int algorithm) {
        DAGNode.lastAlreadyVisitedNumberUsed++;
        this.allRefFreed = new HashSet<TTReference>();
        HashSet<DAGNode> leafNodes = findLeafNodes(sourceDAGNode);
        Iterator<DAGNode> it = leafNodes.iterator();
        //LogHolder.getInstanceOf().addLog("Creating BMi, leafnode size=" + leafNodes.size(), true);
        while (it.hasNext()) {
            DAGNode leafNode = it.next();
            //LogHolder.getInstanceOf().addLog("leaf Node ="+leafNode.getLabel(), true);
            if (isReturnDAGNode(leafNode)) {
                this.createBMi(leafNode);
            }
        }
        //LogHolder.getInstanceOf().addLog("End BMi ", true);
        it = leafNodes.iterator();
        //LogHolder.getInstanceOf().addLog("Freeing memory ", true);
        while (it.hasNext()) {
            DAGNode leafNode = it.next();
            if (isReturnDAGNode(leafNode)) {
                this.freeMemeoryFromLeaf(leafNode, algorithm);
            }
        }
        //LogHolder.getInstanceOf().addLog("End Freeing memory ", true);
        refFreed.addAll(allRefFreed);
        //LogHolder.getInstanceOf().addLog("total references freed=" + refFreed.size(), true);
    }

    /**
     *
     * @param graphNode
     * @return true if changed the BMI else false
     */
    private boolean addChildrenBMiInMe(DAGNode graphNode) {
        Iterator<DAGNode> childIt = graphNode.getChildren().iterator();
        int sizeBeforeChange = graphNode.getBMi().size();
        graphNode.addInBMiSet(graphNode.getReferences());
        while (childIt.hasNext()) {
            DAGNode child = childIt.next();
            HashSet<TTReference> childBMi = child.getBMi();
            graphNode.addInBMiSet(childBMi);
        }
        int afterSize = graphNode.getBMi().size();
        if (afterSize == sizeBeforeChange) {
            /**
             * size before and after change are same hence return false;
             */
            return false;
        }
        return true;
    }

    private HashSet<DAGNode> findLeafNodes(DAGNode root) {
        Stack<DAGNode> stack = new Stack<DAGNode>();
        HashSet ret = new HashSet();
        stack.push(root);
        DAGNode.lastAlreadyVisitedNumberUsed++;
        while (!stack.isEmpty()) {
            DAGNode currentNode = stack.pop();
            if (currentNode.alreadyVisited == DAGNode.lastAlreadyVisitedNumberUsed) {
                continue;
            } else {
                currentNode.alreadyVisited = DAGNode.lastAlreadyVisitedNumberUsed;
            }
            if (currentNode.getChildren().isEmpty()) {
                /*
                 * current node is a leaf node as it has no children.
                 */
                ret.add(currentNode);
            }
            stack.addAll(currentNode.getChildren());
        }
        return ret;
    }

    private boolean isReturnDAGNode(DAGNode dagNode) {
        InstrGraphNode gNode = dagNode.getGraphNodes().iterator().next();
        GCInstruction instr = gNode.getInstruction().getNormalInstrs().firstElement();
        if (!instr.getMnemonic().toUpperCase().contains("THROW")) {
            return true;
        }
        return false;
    }

    private GCInstruction getChildInstrToFreeRefAt(DAGNode node) {
        Iterator<DAGNode> it = node.getChildren().iterator();
        GCInstruction childValidNodeToFreeRefAt = null;
        while (it.hasNext()) {
            DAGNode dagNode = it.next();
            childValidNodeToFreeRefAt = dagNode.validNodeToFreeRefAt;
            if (childValidNodeToFreeRefAt != null) {
                return childValidNodeToFreeRefAt;
            }
        }
        return null;
    }

    /**
     * The freememory algorithm free each reference used on the immediate parent
     * node on the current node if that reference is not in currentnode BMi set.
     * 
     * @param node
     * @param algorithm 
     */
    private void freeMemoryPerNode(DAGNode node, int algorithm) {
        /**
         * all references used on my parents BMp minus references
         * in my BMi are freed on me.
         */
        HashSet<TTReference> refToBeFreed = getRefsOnMyParents(node);
        //System.out.println(node.getLabel() + "------ node BMi are =\n\t" + node.getBMi());
        refToBeFreed.removeAll(node.getBMi());
        //System.out.println("ref to be freed =" + refToBeFreed);
        GCInstruction instrToFree = getInstrAllowedToFreeRefAt(node);

        if (instrToFree != null) {
            setLastValidInstrToFreeRefOn(instrToFree, node);
            instrToFree.addReferencesFreedOnMe(refToBeFreed, algorithm);
            allRefFreed.addAll(refToBeFreed);
        } else {
            GCInstruction lastValidInstr = getChildInstrToFreeRefAt(node);
            if (lastValidInstr != null) {
                lastValidInstr.addReferencesFreedOnMe(refToBeFreed, algorithm);
                setLastValidInstrToFreeRefOn(lastValidInstr, node);
                allRefFreed.addAll(refToBeFreed);
            }
        }
    }

    protected void setLastValidInstrToFreeRefOn(GCInstruction lastValidInstr, DAGNode node) {
        node.validNodeToFreeRefAt = lastValidInstr;
    }

    private void createBMi(DAGNode leafNode) {
        Stack<DAGNode> stack = new Stack<DAGNode>();
        stack.push(leafNode);
        DAGNode.lastAlreadyVisitedNumberUsed++;
        while (!stack.isEmpty()) {
            DAGNode currentNode = stack.pop();
            /**
             * if BMi is not changed and the node is already visited then do not go up and save time.
             */
            if (!addChildrenBMiInMe(currentNode)
                    && currentNode.alreadyVisited == DAGNode.lastAlreadyVisitedNumberUsed) {
                continue;
            }
            //LogHolder.getInstanceOf().addLog("done with "+currentNode.getLabel(), true);
            currentNode.alreadyVisited = DAGNode.lastAlreadyVisitedNumberUsed;
            stack.addAll(currentNode.getParents());
        }
    }

    private void freeMemeoryFromLeaf(DAGNode leafNode, int algorithm) {
        Stack<DAGNode> stack = new Stack<DAGNode>();
        stack.push(leafNode);
        DAGNode.lastAlreadyVisitedNumberUsed++;
        while (!stack.isEmpty()) {
            DAGNode currentNode = stack.pop();
            freeMemoryPerNode(currentNode, algorithm);
            if (currentNode.alreadyVisited == DAGNode.lastAlreadyVisitedNumberUsed) {
                continue;
            } else {
                currentNode.alreadyVisited = DAGNode.lastAlreadyVisitedNumberUsed;
            }
            stack.addAll(currentNode.getParents());
        }
    }

    private HashSet<TTReference> getRefsOnMyParents(DAGNode dagNode) {
        Iterator<DAGNode> parentLink = dagNode.getParents().iterator();
        HashSet<TTReference> refUsedOnParents = new HashSet<TTReference>();
        while (parentLink.hasNext()) {
            DAGNode parent = parentLink.next();
            //refUsedOnParents.addAll(parent.getReferences());
            refUsedOnParents.addAll(parent.getBMi());
        }
        return refUsedOnParents;
    }

    protected GCInstruction isValidNodeToFreeRefat(InstrGraphNode gNode) {
        Oracle oracle = Oracle.getInstanceOf();
        CreateIMGraph imGraph = CreateIMGraph.getInstanceOf();
        String methodStr = oracle.getMethodOrFieldString(gNode.getMethod());
        GCInstruction instr = gNode.getInstruction().getNormalInstrs().firstElement();
        if (!imGraph.isGraphCreatedMultipleTimes(methodStr)) {
            //if (!instr.isBranchTarget()) {
            return instr;
            //}
        }
        return null;
    }

    protected GCInstruction getInstrAllowedToFreeRefAt(DAGNode dagNode) {
        GCInstruction instr = null;
        Iterator<DAGNode> it = dagNode.getParents().iterator();
        while (it.hasNext()) {
            DAGNode parent = it.next();
            InstrGraphNode instrGraphNode = parent.getConnectingNode(dagNode);
            instr = isValidNodeToFreeRefat(instrGraphNode);
            if (instr != null) {
                return instr;
            }
        }
        Iterator<InstrGraphNode> dagGraphNodesIt = dagNode.getGraphNodes().iterator();
        while (dagGraphNodesIt.hasNext()) {
            InstrGraphNode gNode = dagGraphNodesIt.next();
            instr = isValidNodeToFreeRefat(gNode);
            if (instr != null) {
                return instr;
            }
        }
        return null;
    }
}
