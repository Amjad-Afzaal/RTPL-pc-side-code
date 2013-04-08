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

import java.util.*;
import takatuka.offlineGC.DFA.dataObjs.*;
import takatuka.offlineGC.DFA.dataObjs.attribute.GCInstruction;
import takatuka.offlineGC.OGI.GraphUtils.INode;
import takatuka.offlineGC.OGI.GraphUtils.InstrGraphNode;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class DAGNode implements INode {

    private HashSet<InstrGraphNode> graphNodes = new HashSet<InstrGraphNode>();
    private HashSet<DAGNode> parents = new HashSet<DAGNode>();
    private HashSet<DAGNode> children = new HashSet<DAGNode>();
    private static long uniqueID = 1;
    private long myId = uniqueID++;
    private HashSet<TTReference> refSet = null;
    private DAGNodeRefsCalc refForDAG = null;
    private HashMap<DAGNode, InstrGraphNode> childDagLinks = new HashMap<DAGNode, InstrGraphNode>();
    private HashMap<DAGNode, InstrGraphNode> parentDagLinks = new HashMap<DAGNode, InstrGraphNode>();
    public static int lastAlreadyVisitedNumberUsed = 0;
    public int alreadyVisited = 0;
    public GCInstruction validNodeToFreeRefAt = null;
    /*
     * references used on me and in all of my children.
     */
    private HashSet<TTReference> BMi = new HashSet<TTReference>();

    /**
     * 
     */
    public DAGNode() {
    }

    /**
     * @param graphNodes
     * @param refForDAG
     */
    public DAGNode(HashSet<InstrGraphNode> graphNodes, DAGNodeRefsCalc refForDAG) {
        this.refForDAG = refForDAG;
        addGraphNodes(graphNodes);
    }

    public void setDAGNodeRefCalc(DAGNodeRefsCalc refForDAG) {
        this.refForDAG = refForDAG;
    }

    public void addInBMiSet(HashSet<TTReference> refs) {
        BMi.addAll(refs);
    }

    /*public void clearBMi() {
        BMi.clear();
    }*/

    public HashSet<TTReference> getBMi() {
        return BMi;
    }

    /**
     * 
     * @return
     */
    public long getId() {
        return myId;
    }

    /**
     * 
     * @param gNode
     * @return
     */
    public boolean contains(InstrGraphNode gNode) {
        return graphNodes.contains(gNode);
    }

    /**
     * 
     * @return
     */
    public HashSet<InstrGraphNode> getGraphNodes() {
        return graphNodes;
    }

    private void removeAllReferWithNoKnownNew(HashSet<TTReference> refSet) {
        if (refSet == null) {
            return;
        }
        Iterator<TTReference> it = refSet.iterator();
        HashSet<TTReference> referencesToBeRemoved = new HashSet<TTReference>();
        while (it.hasNext()) {
            TTReference ref = it.next();
            if (ref.getNewId() < 0) {
                referencesToBeRemoved.add(ref);
            }
        }
        refSet.removeAll(referencesToBeRemoved);
    }

    /**
     * 
     * @return
     */
    public HashSet<TTReference> getReferences() {

        if (refForDAG == null) {
            return new HashSet();
        } else if (refSet != null) {
            return refSet;
        }
        if (!refForDAG.init()) {
            return new HashSet<TTReference>();
        }
        refSet = refForDAG.getReferences(this);
        removeAllReferWithNoKnownNew(refSet);
        return refSet;
    }

    /**
     *
     * @param graphNodes
     */
    public void addGraphNodes(HashSet<InstrGraphNode> graphNodes) {
        Iterator<InstrGraphNode> it = graphNodes.iterator();
        while (it.hasNext()) {
            InstrGraphNode gNode = it.next();
            gNode.setParentDAGNode(this);
            this.graphNodes.add(gNode);
        }
    }

    /**
     *
     * @param graphNode
     */
    public void addGraphNode(InstrGraphNode graphNode) {
        HashSet<InstrGraphNode> gNodeSet = new HashSet();
        gNodeSet.add(graphNode);
        addGraphNodes(gNodeSet);
    }

    /**
     * 
     * @return
     */
    public HashSet<DAGNode> getParents() {
        return (HashSet<DAGNode>) parents.clone();
    }

    /**
     * 
     * @param child
     */
    public void addChild(DAGNode child, InstrGraphNode fromChild, InstrGraphNode toChild) {
        childDagLinks.put(child, toChild);
        //parentDagLinks.put(toChild, fromChild);
        children.add(child);
        child.parents.add(this);
    }

    public InstrGraphNode getConnectingNode(DAGNode child) {
        return childDagLinks.get(child);
    }
    /**
     * 
     * @return
     */
    public HashSet<DAGNode> getChildren() {
        return (HashSet<DAGNode>) this.children.clone();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof DAGNode)) {
            return false;
        }
        DAGNode input = (DAGNode) obj;
        if (input.myId == myId) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 61 * hash + (int) (this.myId ^ (this.myId >>> 32));
        return hash;
    }

    public String getLabel() {
        String ret = "" + myId + ": ";
        Iterator<InstrGraphNode> it = graphNodes.iterator();
        while (it.hasNext()) {
            InstrGraphNode gNode = it.next();
            ret += gNode.myOffSetAndMethod();
            if (it.hasNext()) {
                ret += ", ";
            }
        }
        ret += ", ref=" + getReferences();
        return ret;
    }

    private String toString(boolean withLabels) {
        Stack<DAGNode> stack = new Stack<DAGNode>();
        stack.push(this);
        String ret = "";
        HashSet<DAGNode> alreadyPrinted = new HashSet<DAGNode>();
        while (!stack.empty()) {
            DAGNode node = stack.pop();
            if (alreadyPrinted.contains(node)) {
                continue;
            }
            alreadyPrinted.add(node);
            /**
             * first print nodes inside me.
             */
            GraphLabelsController.getInstanceOf().add(node.myId, node.getLabel(), true);
            if (node.children.size() == 0) {
                if (!withLabels) {
                    ret += node.myId + " -> END;\n";
                } else {
                    ret += node.getLabel() + " -> END\n";
                }
            }
            Iterator<DAGNode> childNodes = node.children.iterator();
            while (childNodes.hasNext()) {
                if (!withLabels) {
                    ret += node.myId + " -> " + childNodes.next().myId + ";\n";
                } else {
                    ret += node.getLabel() + " -> " + childNodes.next().getLabel() + ";\n";
                }
            }
            childNodes = node.children.iterator();
            while (childNodes.hasNext()) {
                stack.add(childNodes.next());
            }
        }
        return ret;

    }

    /**
     * 
     * @return
     */
    @Override
    public String toString() {
        return toString(true);
    }

    public String toStringWithOutLables() {
        return toString(false);
    }
}
