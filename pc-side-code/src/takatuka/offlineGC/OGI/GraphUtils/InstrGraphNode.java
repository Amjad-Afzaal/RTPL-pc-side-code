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
package takatuka.offlineGC.OGI.GraphUtils;

import takatuka.offlineGC.OGI.GraphUtil.FTT.RefsPerIntraMethodGraph;
import takatuka.offlineGC.OGI.PNR.PNRAlgo;
import takatuka.offlineGC.OGI.DAGUtils.GraphLabelsController;
import takatuka.offlineGC.OGI.DAGUtils.DAGNode;
import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.constants.*;
import takatuka.offlineGC.OGI.GraphUtil.FTT.MethodLevelGraphNode;
import takatuka.offlineGC.OGI.superInstruction.*;
import takatuka.offlineGC.DFA.dataObjs.*;
import takatuka.offlineGC.DFA.dataObjs.attribute.*;
import takatuka.offlineGC.DFA.dataObjs.flowRecord.MethodCallInfo;
import takatuka.offlineGC.DFA.dataObjs.functionState.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class InstrGraphNode implements INode {

    /**
     * These are the tags
     */
    protected HashSet<TTReference> references = new HashSet<TTReference>();
    private HashSet<Integer> newIdsCache = null;
    /**
     * links to my children
     */
    protected HashSet<InstrGraphNode> children = new HashSet<InstrGraphNode>();
    /**
     * links to my parents
     */
    protected HashSet<InstrGraphNode> parents = new HashSet<InstrGraphNode>();
    protected SuperInstruction instr = null;
    protected DAGNode myParentDAGNode = null;
    private static long count = 0;
    private long myId = 0;
    private String myMethodStr = null;
    private MethodInfo method = null;
    private HashMap<InstrGraphNode, HashSet<InstrGraphNode>> savedReturnInstr = new HashMap<InstrGraphNode, HashSet<InstrGraphNode>>();
    /**
     * Used by the DAG algorithm.
     */
    private int DFSstartTime = 0;
    private int DFSfinishTime = 0;
    private int theWholeGraphId = 0;
    private ReferenceFilter refFilter = ReferenceFilter.getInstanceOf();
    private HashSet<MethodCallInfo> methodCallInfoSet = new HashSet<MethodCallInfo>();
    /**
     * This number is used by many DFSes to track of node already visited.
     * If a node is visited then alreadyVisited is set to lastAlreadyVisitedNumberUsed.
     * After a DFS is finished the lastAlreadyVisitedNumberUsed is increased.
     */
    public int alreadyVisited = 0;
    public static int lastAlreadyVisitedNumberUsed = 0;
    /**
     * The valid node will be either this node itself
     * or many of its children.
     */
    private HashSet<InstrGraphNode> validDAGNode = new HashSet<InstrGraphNode>();

    public InstrGraphNode(int methodGraphId) {
        this.theWholeGraphId = methodGraphId;
    }

    /**
     *
     * @param instrId
     */
    private InstrGraphNode(SuperInstruction instr, MethodInfo method,
            int methodGraphId) {
        this(methodGraphId);
        this.instr = instr;
        this.method = method;
    }

    public InstrGraphNode(SuperInstruction instr, MethodInfo method,
            int methodGraphId, MethodCallInfo callInfo) {
        this(instr, method, methodGraphId);
        myId = count++;
        if (callInfo != null) {
            this.methodCallInfoSet.add(callInfo);
        }
    }

    public void addValidDAGNodes(HashSet<InstrGraphNode> toAdd) {
        this.validDAGNode.addAll(toAdd);
    }

    public void addValidDAGNode(InstrGraphNode toAdd) {
        this.validDAGNode.add(toAdd);
    }

    public void clearValidDAGNode() {
        this.validDAGNode.clear();
    }

    /**
     * The calls the graph represents.
     * @param methodCallInfo
     */
    public void addMethodCallInfo(MethodCallInfo methodCallInfo) {
        methodCallInfoSet.add(methodCallInfo);
    }

    public void addMethodCallInfo(InstrGraphNode fromNode) {
        methodCallInfoSet.addAll(fromNode.methodCallInfoSet);
    }

    public HashSet<MethodCallInfo> getMethodCallInfo() {
        return methodCallInfoSet;
    }

    public void setReferenceFilter(ReferenceFilter refFilter) {
        this.refFilter = refFilter;
        /**
         * In case references are already set.
         */
        refFilter.referencesFilter(references, this);
        newIdsCache = null;
    }

    /**
     * A graphNode has two ids. 1) ID that is unique per GraphNode.
     * 2) The ID that is unique per intra-method graph. 
     * This function return the second ID. This intra-method ID should
     * be same for all the graph nodes in for a method.
     * @return
     */
    public int getIntraMethodGraphID() {
        return theWholeGraphId;
    }

    /**
     * To know that this Node belongs to what DAGNode. A DAGNode
     * represents a strongly connected graph, having one or many graph nodes.
     * @param myDagNode
     */
    public void setParentDAGNode(DAGNode myDagNode) {
        this.myParentDAGNode = myDagNode;
    }

    public DAGNode getParentDAGNode() {
        return this.myParentDAGNode;
    }

    public void setDFSStartTime(int time) {
        DFSstartTime = time;
    }

    public void setDFSFinishTime(int time) {
        DFSfinishTime = time;
    }

    public int getDFSStartTime() {
        return DFSstartTime;
    }

    public int getDFSFinishTime() {
        return DFSfinishTime;
    }

    public long getId() {
        return myId;
    }

    public InstrGraphNode partialClone() {
        InstrGraphNode gNode = new InstrGraphNode(instr, method, theWholeGraphId);
        gNode.myId = myId;
        gNode.references = (HashSet<TTReference>) references.clone();
        return gNode;
    }

    @Override
    public Object clone() {
        InstrGraphNode gNode = new InstrGraphNode(instr, method, theWholeGraphId);
        myId = count++;
        gNode.children = ((HashSet<InstrGraphNode>) children.clone());
        gNode.parents = (HashSet<InstrGraphNode>) parents.clone();
        gNode.references = (HashSet<TTReference>) references.clone();
        return gNode;
    }

    public MethodInfo getMethod() {
        return method;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof InstrGraphNode)) {
            return false;
        }
        InstrGraphNode node = (InstrGraphNode) obj;


        if (node.myId == myId) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (int) (this.myId ^ (this.myId >>> 32));
        return hash;
    }

    public void setReferencesUsed(HashMap<Long, FunctionStateValueElement> functionState) {
        if (functionState == null) {
            return;
        }
        Iterator<GCInstruction> it = instr.getNormalInstrs().iterator();
        while (it.hasNext()) {
            GCInstruction instrLocal = it.next();
            addReferencesUsed(instrLocal.getInstructionId(), functionState);
        }
        RefsPerIntraMethodGraph.getInstanceOf().recordRefUsage(this, references);
        newIdsCache = null;

    }

    public void addReferencesAfterFilter(HashSet<TTReference> ref) {
        this.references.addAll(ref);
        refFilter.referencesFilter(references, this);
        newIdsCache = null;
    }

    private void addReferencesUsed(long instrId, HashMap<Long, FunctionStateValueElement> functionState) {

        FunctionStateValueElement funStateElm = functionState.get(instrId);
        HashSet<TTReference> refUsedOnInstr = funStateElm.getAllReferenceUsedCache();
        this.references.addAll(refUsedOnInstr);
        refFilter.referencesFilter(references, this);

    }

    /**
     * 
     * @return
     */
    public SuperInstruction getInstruction() {
        return instr;
    }

    /**
     * 
     * @return
     */
    public HashSet<TTReference> getReferences() {
        return references;
    }

    public HashSet<Integer> getNewIds() {
        if (newIdsCache != null) {
            return newIdsCache;
        }
        Iterator<TTReference> it = references.iterator();
        newIdsCache = new HashSet<Integer>();
        while (it.hasNext()) {
            TTReference ref = it.next();
            newIdsCache.add(ref.getNewId());
        }
        return newIdsCache;
    }

    /**
     * 
     * @param childNode
     */
    public void removeChildFully(InstrGraphNode childNode) {
        childNode.parents.remove(this);
        this.children.remove(childNode);
    }

    /**
     *
     * @param childNode
     */
    public void removeChildPartial(InstrGraphNode childNode) {
        this.children.remove(childNode);
    }

    /**
     *
     * @param parent
     */
    public void removeParentPartial(InstrGraphNode parent) {
        this.parents.remove(parent);
    }

    /**
     * 
     */
    public void clearChildren() {

        /**
         * First go to all of my children and remove me as parent. Then
         * remove my children.
         */
        Iterator<InstrGraphNode> it = children.iterator();
        while (it.hasNext()) {
            InstrGraphNode child = it.next();
            child.parents.remove(this);
        }
        /**
         * now removing my children is safe.
         */
        this.children.clear();
    }

    /**
     *
     * @param child
     */
    public void addChild(InstrGraphNode child) {
        this.children.add(child);
        child.parents.add(this);
    }

    /**
     * 
     * @return
     */
    public HashSet<InstrGraphNode> getChildren() {
        return (HashSet<InstrGraphNode>) children.clone();
    }

    /**
     *
     * @param refSet
     */
    public void addChildren(HashSet<InstrGraphNode> refSet) {
        this.children.addAll(refSet);
        /**
         * also add me as parent in the children nodes.
         */
        Iterator<InstrGraphNode> it = refSet.iterator();
        while (it.hasNext()) {
            InstrGraphNode gNode = it.next();
            gNode.parents.add(this);
        }
    }

    /**
     * 
     * @return
     */
    public HashSet<InstrGraphNode> getParents() {
        return (HashSet<InstrGraphNode>) this.parents.clone();
    }

    private String getMyMethodStr() {
        if (myMethodStr == null) {
            myMethodStr = Oracle.getInstanceOf().getMethodOrFieldString(getMethod());
        }
        return myMethodStr;
    }

    public HashSet<InstrGraphNode> findMethodReturnInstructions() {

        HashSet<InstrGraphNode> ret = savedReturnInstr.get(this);
        if (ret != null) {
            return ret;
        }
        ret = new HashSet<InstrGraphNode>();
        savedReturnInstr.put(this, ret);
        String methodStr = getMyMethodStr();
        Stack<InstrGraphNode> stack = new Stack<InstrGraphNode>();
        stack.push(this);
        HashSet<Long> alreadyVisited = new HashSet<Long>();
        while (!stack.empty()) {
            InstrGraphNode node = stack.pop();
            if (alreadyVisited.contains(node.getId())) {
                continue;
            }
            alreadyVisited.add(node.getId());
            stack.addAll(node.getChildren());
            GCInstruction instrLocal = node.getInstruction().getNormalInstrs().elementAt(0);
            if (node.getMyMethodStr().equals(methodStr) && instrLocal != null
                    && (instrLocal.getOpCode() >= JavaInstructionsOpcodes.IRETURN
                    && instrLocal.getOpCode() <= JavaInstructionsOpcodes.RETURN)) {
                ret.add(node);
            }
        }
        return ret;
    }

    /**
     * It is assumes that this method is used only to find the branch instruction
     * example: invoke, getstatic, putstatic
     * @param fromInstr
     * @return
     */
    public InstrGraphNode findGraphNodeWithFromInstru(
            GCInstruction fromInstr) {
        HashSet<Long> alreadyVisited = new HashSet<Long>();
        Stack<InstrGraphNode> stack = new Stack<InstrGraphNode>();
        stack.push(this);
        while (!stack.empty()) {
            InstrGraphNode currentNode = stack.pop();
            Vector<GCInstruction> instrSet = currentNode.getInstruction().getNormalInstrs();
            long instrIdLocal = instrSet.firstElement().getInstructionId();
            //Miscellaneous.println(" ---------- "+this.getInstruction());
            if (instrIdLocal == fromInstr.getInstructionId()) {
                return currentNode;
            }
            if (alreadyVisited.contains(currentNode.getId())) {
                continue;
            }
            alreadyVisited.add(currentNode.getId());
            stack.addAll(currentNode.getChildren());
        }
        return null;
    }

    public String myOffSetAndMethod() {
        String ret = "";
        if (method != null) {
            String methodStr = Oracle.getInstanceOf().getMethodOrFieldString(method);
            ret += methodStr.substring(methodStr.lastIndexOf(".") + 1, methodStr.indexOf("("));
        }
        //ret += getIntraMethodGraphID();
        if (instr != null) {
            ret += ", " + instr.getNormalInstrs().iterator().next().getMnemonic();
        }

        return ret;
    }

    protected String getLabel(InstrGraphNode gNode, HashSet<TTReference> refs) {
        String referenceStr = MethodLevelGraphNode.getNewIds(refs, false);
        String ret = gNode.myOffSetAndMethod();
        if (referenceStr != null) {
            ret += ", " + referenceStr;
        }
        return ret;
    }

    public String toStringWithoutLabels(InstrGraphNode gNodeToStartWith, DAGNode parentDAGNode) {
        Stack<InstrGraphNode> stack = new Stack<InstrGraphNode>();
        stack.push(gNodeToStartWith);
        HashSet<InstrGraphNode> alreadyVisited = new HashSet<InstrGraphNode>();
        String stringWithoutLabels = "";
        while (!stack.empty()) {
            InstrGraphNode node = stack.pop();
            if (alreadyVisited.contains(node)) {
                continue;
            }
            alreadyVisited.add(node);
            if (parentDAGNode != null && !parentDAGNode.contains(node)) {
                continue;
            }
            String label = getLabel(node, node.getReferences());
            GraphLabelsController.getInstanceOf().add(node.getId(), label, false);
            Iterator<InstrGraphNode> it = node.getChildren().iterator();

            if (!it.hasNext()) {
                stringWithoutLabels += node.getId() + "-> END;\n";
            }
            while (it.hasNext()) {
                InstrGraphNode graphNode = it.next();
                label = getLabel(graphNode, graphNode.getReferences());
                GraphLabelsController.getInstanceOf().add(graphNode.getId(), label, false);

                stringWithoutLabels += node.getId() + " ->" + graphNode.getId() + ";\n";

            }
            stack.addAll(node.getChildren());
        }
        return stringWithoutLabels;
    }

    public String toString(InstrGraphNode gNodeToStartWith, boolean printLables, DAGNode parentDAGNode) {
        if (printLables) {
            if (PNRAlgo.isDebugOn()) {
                return toStringHelper(gNodeToStartWith, true, parentDAGNode);
            } else {
                return "";
            }
        } else {
            if (GraphLabelsController.printGraphInFile || PNRAlgo.isDebugOn()) {
                return toStringWithoutLabels(gNodeToStartWith, parentDAGNode);
            } else {
                return "";
            }
        }
    }

    @Override
    public String toString() {
        return toString(this, true, null);
    }

    @Override
    public String toStringWithOutLables() {
        return toString(this, false, null);
    }

    private String toStringHelper(InstrGraphNode gNodeToStartWith, boolean printLables, DAGNode parentDAGNode) {
        String ret = "\n";
        Stack<InstrGraphNode> stack = new Stack<InstrGraphNode>();
        HashSet<InstrGraphNode> alreadyVisited = new HashSet<InstrGraphNode>();
        stack.push(gNodeToStartWith);
        while (!stack.empty()) {
            InstrGraphNode node = stack.pop();
            if (alreadyVisited.contains(node)) {
                continue; //already visited
            }
            alreadyVisited.add(node);
            if (parentDAGNode != null && !parentDAGNode.contains(node)) {
                continue;
            }
            String label = getLabel(node, node.getReferences());
            GraphLabelsController.getInstanceOf().add(node.getId(), label, false);
            Iterator<InstrGraphNode> it = node.getChildren().iterator();
            if (!it.hasNext()) {
                if (!printLables) {
                    ret += node.getId() + "-> END;\n";
                } else {
                    ret += getLabel(node, node.getReferences()) + " -> END\n";
                }
            }
            while (it.hasNext()) {
                InstrGraphNode graphNode = it.next();
                label = getLabel(graphNode, graphNode.getReferences());
                GraphLabelsController.getInstanceOf().add(graphNode.getId(), label, false);
                if (!printLables) {
                    ret += node.getId() + " ->" + graphNode.getId() + ";\n";
                } else {
                    ret += "(" + getLabel(node, node.getReferences()) + ") ->" + getLabel(graphNode, graphNode.getReferences()) + "\n";

                }
            }
            stack.addAll(node.getChildren());
        }
        return ret;
    }
}
