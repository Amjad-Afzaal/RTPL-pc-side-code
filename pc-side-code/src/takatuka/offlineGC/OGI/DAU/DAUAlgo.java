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
package takatuka.offlineGC.OGI.DAU;

import takatuka.offlineGC.OGI.GraphUtils.InstrGraphNode;
import takatuka.offlineGC.OGI.DAGUtils.GraphLabelsController;
import takatuka.offlineGC.OGI.DAGUtils.DAGNode;
import takatuka.offlineGC.OGI.GraphUtils.CreateIMGraph;
import takatuka.offlineGC.OGI.PNR.*;
import java.util.*;
import takatuka.classreader.dataObjs.MethodInfo;
import takatuka.classreader.logic.logAndStats.*;
import takatuka.classreader.logic.util.Miscellaneous;
import takatuka.offlineGC.DFA.dataObjs.virtualThreading.*;
import takatuka.offlineGC.OGI.DAGUtils.CreateDAG;
import takatuka.offlineGC.OGI.DAGUtils.DAGNodeRefsCalc;
import takatuka.offlineGC.OGI.factory.*;
import takatuka.offlineGC.DFA.dataObjs.*;
import takatuka.offlineGC.DFA.dataObjs.attribute.*;
import takatuka.offlineGC.DFA.dataObjs.fields.*;
import takatuka.offlineGC.DFA.logic.factory.*;
import takatuka.optimizer.cpGlobalization.logic.util.Oracle;

/**
 * <p>Title: </p>
 * <p>Description:
 * It deals with references that are assigned to a static or a non-static field.
 * These references cannot be freed by either methodExit algorithms.
 * Furthermore,these references cannot be free by the intergraph algorithm if the
 * whole program never exit and is in a loop.
 * Because of above two mention problems. This new algorithm is developed. It
 * remove references assigned to the fields inside loops. The algorithm is as follows:
 *
 * -- Make the graph.
 * -- Take a reference that is assigned to a field.
 * --- For such a reference remove its reference creator (i.e. a new statement creating that reference).
 * --- If all the uses of the reference can now are not reachable when the graph is traverse from the root node.
 *     Then such a reference can be freed even inside loop.
 * --- Remove that reference at the new statement.
 * --- Create the DAG of the graph disconnected from main graph due to above step.
 * ---  Apply PNR algorithm on that graph and free the reference after last use.
 *
 * 
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class DAUAlgo extends PNRBase {

    private static final DAUAlgo myObj = new DAUAlgo();
    public static int currentNewIdToFree = -1;

    /**
     *
     */
    protected DAUAlgo() {
    }

    /**
     *
     * @return
     */
    public static DAUAlgo getInstanceOf() {
        return myObj;
    }

    @Override
    public void execute() {
        setFactory();
        IFactory factory = FactoryPlaceHolder.getInstanceOf().getCurrentFactory();
        VirtualThreadController vContr = VirtualThreadController.getInstanceOf();
        Collection<VirtualThread> vThreadCollection = vContr.getAllFinishedThreads();
        Iterator<VirtualThread> it = vThreadCollection.iterator();
        CreateIMGraph createGraph = (CreateIMGraph) factory.createGraph();
        LogHolder.getInstanceOf().addLog(" Total Number of Theads =" + vThreadCollection.size(), false);
        while (it.hasNext()) {
            VirtualThread vThread = it.next();
            anyThingWithVirtualThread(vThread);
            /**
             * Do not make method level graph as this algorithm does not uses
             * it.
             */
            InstrGraphNode interMethodGraph = createGraph.
                    createInterMethodGraph(vThread, false);
            LogHolder.getInstanceOf().addLog(" created inter "
                    + "method graph ", false);

            GraphLabelsController.getInstanceOf().printGraphInFile(interMethodGraph);
            HashSet<TTReference> allRefFreed = new HashSet<TTReference>();
            execute(interMethodGraph, allRefFreed);
            LogHolder.getInstanceOf().addLog("Done with thread # " + vThread, false);
        }

    }

    @Override
    protected void execute(InstrGraphNode sourceGraphNode,
            HashSet<TTReference> refFreed) {
        /**
         *
         * 1- Traverse the graph.
         * - Find all the references which are assigned to any field from the graph.
         * - For each such reference find the node where these references are created inside the graph.
         *   (NewInstrIdFactory's class method getInstrANewIdAssignedTo).
         * - Record all those graphNodes where the reference was created.
         *
         * 2. Now remove all the nodes corresponding to one such new instruction.
         * - see if all the uses of that new instruction are not reachable when
         *   traverse the graph from the root node.
         * - If yes then all remove instruction for the reference on the new instruction deleted.
         *
         * 3. Restore the new instruction for a reference deleted in above step. Now the graph is restored
         * in its original form.
         * Remove the reference from the refAssignedToField as it is now dealt with.
         *
         * 4. Continue to 1 if the refAssignedToField are not empty.
         */
        //DAGBasedFreeAlgo.debugOn();
        /**
         * Step 1:
         */
        HashMap<Integer, HashSet<InstrGraphNode>> allNewInstInGraphMap = findNewInstrNodeForRefsAssignedToField(sourceGraphNode);

        //System.out.println("new ids are = "+allNewInstInGraphMap.size());
        /**
         * Step 2.
         */
        Iterator<Integer> it = allNewInstInGraphMap.keySet().iterator();
        TreeSet<Long> originalNodesIds = traverseGraphToGetAllNodesIDs(sourceGraphNode);
        RecordRefUsedByFields refsUsedByFieldsRecord = RecordRefUsedByFields.getInstanceOf();
        //GraphLabelsController.getInstanceOf().printGraphInFile(sourceGraphNode, "beforeAnyChange.dot");
        while (it.hasNext()) {
            int newId = it.next();
            currentNewIdToFree = newId;
            //System.out.println("done with "+count++);
            HashSet<InstrGraphNode> newInstrGraphNodes = allNewInstInGraphMap.get(newId);
            if (newInstrGraphNodes.size() != 1 && refsUsedByFieldsRecord.containsNewId(newId)) {
                continue; 
                // this is added because foo() called twice then 
                // free second call will free things in the first call. 
                // This could be an error if the id is saved in some field.
            }
            removeGraphNodesTemporarily(sourceGraphNode, newInstrGraphNodes);
            TreeSet<Long> nodesOfReducedGraph = new TreeSet<Long>();
            int lineNumber = NewInstrIdFactory.getInstanceOf().getInstrANewIdAssignedTo(newId).getLineNumber();
            if (lineNumber == 127) {
                //System.out.println("stop here");
            }
            if (!traverseGraphToFindAnyUses(sourceGraphNode, newId, nodesOfReducedGraph)) {
                /**
                 * restore the graph
                 */
                restoredGraphNodesRemovedTemporarily(sourceGraphNode, newInstrGraphNodes);
                /**
                 * Create a DAG started from the newInstrGraphNode and the
                 * DAG should only have nodes that were in the original graph but not in the reduced graphs
                 */
                TreeSet<Long> nodeInOrginalButNotInReduced = (TreeSet<Long>) originalNodesIds.clone();
                nodeInOrginalButNotInReduced.removeAll(nodesOfReducedGraph);
                InstrGraphNode newSubGraph = newGraphMadeOfNodeInOriginalButNotInReducedGraph(nodeInOrginalButNotInReduced,
                        newInstrGraphNodes.iterator().next(), newId);

                DAGNode dagNode = CreateDAG.getInstanceOf().execute(newSubGraph, DAGNodeRefsCalc.getInstanceOf());
                /*if (lineNumber == 127 || true) {
                    GraphLabelsController.printGraphInFile = true;
                    GraphLabelsController.getInstanceOf().printGraphInFile(newSubGraph, "nodeTestGraph.dot");
                    GraphLabelsController.getInstanceOf().printGraphInFile(dagNode, "test.dot");
                }*/
                HashSet<TTReference> refFreedInsideLoop = new HashSet<TTReference>();
                PNRForDAU.getInstanceOf().freeRefBasedOnDAG(refFreedInsideLoop, dagNode,
                        newSubGraph, GCInstruction.DAU_PNR);
                if (refFreedInsideLoop.isEmpty()) {
                    /**
                     * If a reference cannot be freed using DAG algorithm then free it on the new instr
                     */
                    addFreeInstr(sourceGraphNode, newInstrGraphNodes, newId);
                } else if (refFreedInsideLoop.size() > 1) {
                    Miscellaneous.printlnErr(" error # 6823");
                    Miscellaneous.exit();
                } else {
                    LogHolder.getInstanceOf().addLog("It worked for -----"
                            + "------- " + newId + ", line Number="
                            + NewInstrIdFactory.getInstanceOf().
                            getInstrANewIdAssignedTo(newId).getLineNumber());
                }

            } else {
                restoredGraphNodesRemovedTemporarily(sourceGraphNode, newInstrGraphNodes);
            }
            currentNewIdToFree = -1;
            //GraphLabelsController.getInstanceOf().printGraphInFile(sourceGraphNode, "afterRestore.dot");

        }
    }

    private void filterReferences(HashSet<TTReference> refSet, int newId) {
        Iterator<TTReference> it = refSet.iterator();
        TTReference toKeep = null;
        while (it.hasNext()) {
            TTReference ref = it.next();
            if (ref.getNewId() == newId) {
                toKeep = ref;
                break;
            }
        }
        refSet.clear();
        if (toKeep != null) {
            refSet.add(toKeep);
        }
    }

    /**
     * to create a graph consistes of nodes that was part of original graph but not
     * part of graph after new Instruction deleted.
     * 
     * @param nodeInOrginalButNotInReduced
     * @param newInstrGraphNode
     * @param currentNewId
     * @return
     */
    private InstrGraphNode newGraphMadeOfNodeInOriginalButNotInReducedGraph(TreeSet<Long> nodeInOrginalButNotInReduced,
            InstrGraphNode newInstrGraphNode, int currentNewId) {
        Stack<InstrGraphNode> stack = new Stack<InstrGraphNode>();
        InstrGraphNode ret = null;
        stack.push(newInstrGraphNode);
        InstrGraphNode.lastAlreadyVisitedNumberUsed++;
        HashMap<Long, OldAndNewNode> map = new HashMap<Long, OldAndNewNode>();
        /**
         * step 1: create set of new nodes.
         */
        while (!stack.empty()) {
            InstrGraphNode node = stack.pop();
            if (node.alreadyVisited == InstrGraphNode.lastAlreadyVisitedNumberUsed) {
                continue;
            }
            node.alreadyVisited = InstrGraphNode.lastAlreadyVisitedNumberUsed;
            if (!nodeInOrginalButNotInReduced.contains(node.getId())) {
                continue;
            }
            InstrGraphNode newNode = node.partialClone();
            filterReferences(newNode.getReferences(), currentNewId);
            if (ret == null) {
                ret = newNode;
            }
            map.put(node.getId(), new OldAndNewNode(node, newNode));
            stack.addAll(node.getChildren());
        }
        /**
         * now make connections between the newly created nodes.
         */
        Collection<OldAndNewNode> collection = map.values();
        Iterator<OldAndNewNode> collIt = collection.iterator();
        while (collIt.hasNext()) {
            OldAndNewNode ONparent = collIt.next();
            HashSet<InstrGraphNode> childrenSet = ONparent.oldNode.getChildren();
            Iterator<InstrGraphNode> childIt = childrenSet.iterator();
            while (childIt.hasNext()) {
                InstrGraphNode originalChildNode = childIt.next();
                if (nodeInOrginalButNotInReduced.contains(originalChildNode.getId())) {
                    OldAndNewNode ONchild = map.get(originalChildNode.getId());
                    ONparent.newNode.addChild(ONchild.newNode);
                }
            }
        }

        return ret;
    }

    private class OldAndNewNode {

        InstrGraphNode oldNode = null;
        InstrGraphNode newNode = null;

        public OldAndNewNode(InstrGraphNode oldNode, InstrGraphNode newNode) {
            this.oldNode = oldNode;
            this.newNode = newNode;
        }
    }

    private void addFreeInstr(InstrGraphNode sourceGraphNode, HashSet<InstrGraphNode> newInstrGraphNodes, int newInstrId) {
        Iterator<InstrGraphNode> it = newInstrGraphNodes.iterator();
        HashSet<TTReference> refAssignedToField = RecordRefUsedByFields.getInstanceOf().getAllSavedRecord();
        while (it.hasNext()) {
            InstrGraphNode currentNode = it.next();
            GCInstruction currentInstr = currentNode.getInstruction().getNormalInstrs().firstElement();
            HashSet<TTReference> currentNodeRefSet = currentNode.getReferences();
            Iterator<TTReference> currentNodeRefIt = currentNodeRefSet.iterator();
            while (currentNodeRefIt.hasNext()) {
                TTReference ref = currentNodeRefIt.next();
                if (ref.getNewId() == newInstrId) {
                    currentNode.getInstruction().getNormalInstrs().
                            firstElement().addReferenceFreedOnMe(ref, GCInstruction.DAU);
                }
            }
        }
    }

    private TreeSet<Long> traverseGraphToGetAllNodesIDs(InstrGraphNode sourceGraphNode) {
        TreeSet<Long> nodeIds = new TreeSet<Long>();
        Stack<InstrGraphNode> stack = new Stack<InstrGraphNode>();
        stack.push(sourceGraphNode);
        InstrGraphNode.lastAlreadyVisitedNumberUsed++;
        while (!stack.empty()) {
            InstrGraphNode currentNode = stack.pop();
            if (currentNode.alreadyVisited == InstrGraphNode.lastAlreadyVisitedNumberUsed) {
                continue;
            }
            currentNode.alreadyVisited = InstrGraphNode.lastAlreadyVisitedNumberUsed;
            nodeIds.add(currentNode.getId());
            stack.addAll(currentNode.getChildren());
        }
        return nodeIds;
    }

    private boolean traverseGraphToFindAnyUses(InstrGraphNode sourceGraphNode,
            int newIdToFindUses, TreeSet<Long> nodeOfTheReducedGraph) {
        Stack<InstrGraphNode> stack = new Stack<InstrGraphNode>();
        stack.push(sourceGraphNode);
        InstrGraphNode.lastAlreadyVisitedNumberUsed++;
        while (!stack.empty()) {
            InstrGraphNode currentNode = stack.pop();
            if (currentNode.alreadyVisited == InstrGraphNode.lastAlreadyVisitedNumberUsed) {
                continue;
            }
            nodeOfTheReducedGraph.add(currentNode.getId());
            currentNode.alreadyVisited = InstrGraphNode.lastAlreadyVisitedNumberUsed;
            HashSet<Integer> currentNodeNewIds = currentNode.getNewIds();
            if (currentNodeNewIds.contains(newIdToFindUses)) {
                return true;
            }
            stack.addAll(currentNode.getChildren());

        }
        return false;
    }

    private void removeGraphNodesTemporarily(InstrGraphNode sourceGraphNode, HashSet<InstrGraphNode> nodesToRemove) {
        /**
         * To remove graph nodes we go to the nodesToRemove and then remove from
         * its each ONparent itself and also remove from its each child itself as ONparent.
         * It will make this node non-accessable. Note that the nodeRemoved is not changed.
         * Hence later on we can use that node to restore in the graph by reverting above mention process.
         */
        Iterator<InstrGraphNode> it = nodesToRemove.iterator();
        while (it.hasNext()) {
            InstrGraphNode currentNode = it.next();
            /**
             * Go to all of my parents and remove myself as child.
             */
            HashSet<InstrGraphNode> parentsSet = currentNode.getParents();
            Iterator<InstrGraphNode> parentsIt = parentsSet.iterator();
            while (parentsIt.hasNext()) {
                InstrGraphNode parent = parentsIt.next();
                parent.removeChildPartial(currentNode);
            }
            /**
             * Go to all of my children and remove myself as ONparent.
             */
            HashSet<InstrGraphNode> childrenSet = currentNode.getChildren();
            Iterator<InstrGraphNode> childrenIt = childrenSet.iterator();
            while (childrenIt.hasNext()) {
                InstrGraphNode child = childrenIt.next();
                child.removeParentPartial(currentNode);
            }
        }
    }

    private void restoredGraphNodesRemovedTemporarily(InstrGraphNode sourceGraphNode, HashSet<InstrGraphNode> nodesToRestore) {
        Iterator<InstrGraphNode> it = nodesToRestore.iterator();
        while (it.hasNext()) {
            InstrGraphNode currentNode = it.next();
            /**
             * Go to all of my parents and add myself as child.
             */
            HashSet<InstrGraphNode> parentsSet = currentNode.getParents();
            Iterator<InstrGraphNode> parentsIt = parentsSet.iterator();
            while (parentsIt.hasNext()) {
                InstrGraphNode parent = parentsIt.next();
                parent.addChild(currentNode);
            }
            /**
             * Go to all of my children and add myself as ONparent.
             */
            HashSet<InstrGraphNode> childrenSet = currentNode.getChildren();
            Iterator<InstrGraphNode> childrenIt = childrenSet.iterator();
            while (childrenIt.hasNext()) {
                InstrGraphNode child = childrenIt.next();
                currentNode.addChild(child);
            }
        }
    }

    /**
     *
     * @param sourceGraphNode
     * @return
     * The graph nodes of the references assigned to the fields.
     */
    private HashMap<Integer, HashSet<InstrGraphNode>> findNewInstrNodeForRefsAssignedToField(InstrGraphNode sourceGraphNode) {
        NewInstrIdFactory newIdFactory = NewInstrIdFactory.getInstanceOf();
        HashMap<Integer, HashSet<InstrGraphNode>> newIdToGraphNodesMap = new HashMap<Integer, HashSet<InstrGraphNode>>();
        Stack<InstrGraphNode> stack = new Stack<InstrGraphNode>();
        stack.push(sourceGraphNode);
        InstrGraphNode.lastAlreadyVisitedNumberUsed++;
        while (!stack.empty()) {
            InstrGraphNode nodeToEximine = stack.pop();
            if (nodeToEximine.alreadyVisited == InstrGraphNode.lastAlreadyVisitedNumberUsed) {
                continue;
            }
            nodeToEximine.alreadyVisited = InstrGraphNode.lastAlreadyVisitedNumberUsed;
            GCInstruction instrAssignedToTheNode = nodeToEximine.getInstruction().getNormalInstrs().firstElement();
            MethodInfo method = instrAssignedToTheNode.getMethod();
            Oracle oracle = Oracle.getInstanceOf();
            String methodStr = oracle.getMethodOrFieldString(method);
            if (methodStr.contains("main") && instrAssignedToTheNode.getLineNumber() == 78) {
                //System.out.println("------ stop here ------");
            }
            //System.out.println(methodStr+"-------"+instrAssignedToTheNode.getLineNumber() + "-----> " + instrAssignedToTheNode);

            int newId = newIdFactory.getNewIdGivenInstruction(instrAssignedToTheNode);
            /**
             * Todo there is an error:
             * The reference must be assign to at most one variable. That
             * is either to exactly one field or exactly one local variable.
             */
            if (newId != -1 /*&& newIdsForFieldRefs.contains(newId)*/) {
                HashSet<InstrGraphNode> set = newIdToGraphNodesMap.get(newId);
                if (set == null) {
                    set = new HashSet<InstrGraphNode>();
                    newIdToGraphNodesMap.put(newId, set);
                }
                set.add(nodeToEximine);
            }
            stack.addAll(nodeToEximine.getChildren());
        }
        return newIdToGraphNodesMap;
    }

}
