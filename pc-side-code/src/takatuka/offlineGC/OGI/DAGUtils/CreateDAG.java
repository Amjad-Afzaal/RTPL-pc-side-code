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

import takatuka.offlineGC.OGI.DAGUtils.DAGNodeRefsCalc;
import takatuka.offlineGC.OGI.PNR.PNRAlgo;
import takatuka.offlineGC.OGI.GraphUtils.InstrGraphNode;
import takatuka.offlineGC.OGI.DAGUtils.DAGNode;
import java.util.*;
import takatuka.classreader.logic.logAndStats.*;
import takatuka.classreader.logic.util.Miscellaneous;
import takatuka.offlineGC.OGI.factory.*;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 *  Given a graph it creates a DAG.
 *
 * The algorithm used in mentioned on from page 488 of 
 * T. H. Cormen, et al. "Introduction to algorthm", 1994, MIT Press
 * 
 * The algorithm is as follows:
 * Stongly-Connected-Components(G) 
 * 1: call DFSForFinishTimeCalc(G) to compute finishing times f[u] for each vertex u
 * 2: compute G^t 
 * 3: call DFSForFinishTimeCalc(Gt), but in the main loop of DFSForFinishTimeCalc, consider the vertices in order
 * of decreasing f[u] (as computed in line 1)
 * 4: Each tree of the DFSForFinishTimeCalc of step three is a strongly connected component.
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class CreateDAG {

    private static final CreateDAG myObj = new CreateDAG();
    protected Vector<InstrGraphNode> sortedByFinshedTime = new Vector<InstrGraphNode>();

    protected CreateDAG() {
    }

    /**
     *
     * @return
     */
    public static CreateDAG getInstanceOf() {
        return myObj;
    }

    public Vector<DAGNode> createDAGNodes(InstrGraphNode node, DAGNodeRefsCalc refforDAG) {
        DFSForFinishTimeCalc(node);
        LogHolder.getInstanceOf().addLog(" Done with computing finish times ", false);
        //printNodesFinishAndStartTimes(startNode);
        Vector<DAGNode> dagNodeVec = new Vector<DAGNode>();
        while (sortedByFinshedTime.size() != 0) {
            InstrGraphNode temp = sortedByFinshedTime.lastElement();
            if (temp.getDFSFinishTime() == -2) {
                sortedByFinshedTime.removeElementAt(sortedByFinshedTime.size() - 1);
                continue;
            }
            //Miscellaneous.println(temp.DFSfinishTime);
            HashSet<InstrGraphNode> connectedNodes = new HashSet<InstrGraphNode>();
            DFSTranspose(temp, connectedNodes);
            DAGNode dNode = new DAGNode(connectedNodes, refforDAG);
            //System.out.println("references =============== " + dNode.getReferences());
            dagNodeVec.add(dNode);
        }
        LogHolder.getInstanceOf().addLog(" Created all the strongly connected components.", false);
        return dagNodeVec;
    }

    public Vector<DAGNode> createDAGNodes(InstrGraphNode node) {
        IFactory factory = FactoryPlaceHolder.getInstanceOf().getCurrentFactory();
        DAGNodeRefsCalc refforDAG = factory.createReferencesForDAGNode();
        return createDAGNodes(node, refforDAG);
    }

    public DAGNode execute(InstrGraphNode node, DAGNodeRefsCalc refFilterForDAGNode) {
        Vector<DAGNode> dagNodeVec = createDAGNodes(node, refFilterForDAGNode);
        DAGNode ret = createLinkBetweenConnectedComponents(dagNodeVec, node);
        LogHolder.getInstanceOf().addLog(" Created link between DAG Nodes ", false);
        return ret;
    }

    public DAGNode execute(InstrGraphNode node) {
        Vector<DAGNode> dagNodeVec = createDAGNodes(node);
        DAGNode ret = createLinkBetweenConnectedComponents(dagNodeVec, node);
        LogHolder.getInstanceOf().addLog(" Created link between DAG Nodes ", false);
        return ret;

    }

    private void printNodesFinishAndStartTimes(InstrGraphNode node) {
        PNRAlgo.debug("instrId=" /*+ node.getInstructionId()*/
                + ", ft" + node.getDFSFinishTime() + ", st=" + node.getDFSStartTime());
        HashSet<InstrGraphNode> childSet = node.getChildren();
        Iterator<InstrGraphNode> it = childSet.iterator();
        while (it.hasNext()) {
            InstrGraphNode child = it.next();
            printNodesFinishAndStartTimes(child);
        }
    }

    /**
     * It create links between different DAGNodes and return the
     * DAGNode that has the source. This DAGNode is our starting point.
     *
     * @param dagNodeVec
     * @param source
     * @return
     */
    public DAGNode createLinkBetweenConnectedComponents(Vector<DAGNode> dagNodeVec, InstrGraphNode source) {
        /**
         * Here is what it do to create links.
         * go through all the nodes in a DAGNode. For each startNode
         * find its children. If a child startNode is not inside the DAGNode then
         * find it in the other DAGs and create a child and parent link to it.
         */
        Iterator<DAGNode> it = dagNodeVec.iterator();
        DAGNode toRet = source.getParentDAGNode();
        while (it.hasNext()) {
            DAGNode dagNode = it.next();

            HashSet<InstrGraphNode> mySubNodeSet = dagNode.getGraphNodes();
            Iterator<InstrGraphNode> gnodeIt = mySubNodeSet.iterator();
            while (gnodeIt.hasNext()) {
                InstrGraphNode mySubNode = gnodeIt.next();

                HashSet<InstrGraphNode> oldChildren = mySubNode.getChildren();
                setChildrenAndParets(dagNodeVec, oldChildren, dagNode, mySubNode);

            }
        }

        return toRet;
    }

    private void setChildrenAndParets(Vector<DAGNode> dagNodeVec,
            HashSet<InstrGraphNode> graphNodeToFind, DAGNode myself, InstrGraphNode fromNode) {
        Iterator<InstrGraphNode> it = graphNodeToFind.iterator();
        while (it.hasNext()) {
            InstrGraphNode gNode = it.next();
            DAGNode dagNode = gNode.getParentDAGNode();
            if (dagNode.equals(myself)) {
                continue;
            }
            if (dagNode.contains(gNode)) {
                myself.addChild(dagNode, fromNode, gNode);
            }
        }
    }

    /**
     * @param startNode the startNode from which DFSForFinishTimeCalc is supposed to be started
     */
    protected void DFSForFinishTimeCalc(InstrGraphNode startNode) {
        int time = 0;
        //Miscellaneous.println(startNode.getInstructionId() + "," + startNode.DFSstartTime);
        Stack<InstrGraphNode> stack = new Stack<InstrGraphNode>();
        stack.add(startNode);
        InstrGraphNode child = null;
        while (!stack.empty()) {
            InstrGraphNode childInStack = stack.pop();
            child = childInStack;
            if (child.getDFSFinishTime() > time || child.getDFSStartTime() > time) {
                System.err.println("invalid finish or start time.");
                Miscellaneous.exit();
            }
            if (child.getDFSFinishTime() != 0 && child.getDFSStartTime() != 0) {
                continue;
            } else if (child.getDFSStartTime() == 0) {
                child.setDFSStartTime(time++);
                stack.add(child); //added again so that finished time can be assigned.
                stack.addAll(child.getChildren());
            } else if (child.getDFSFinishTime() == 0) {
                child.setDFSFinishTime(time++);
                sortedByFinshedTime.add(child);
                //Miscellaneous.println("9343----- "+child.DFSfinishTime+": "+child.DFSstartTime+": "+child.getInstruction());
            }
        }
    }

    /**
     * Do the reverse DFSForFinishTimeCalc and create a DAG.
     * @param startNode
     */
    protected void DFSTranspose(InstrGraphNode node, HashSet<InstrGraphNode> connectedNodes) {
        Stack<InstrGraphNode> stack = new Stack<InstrGraphNode>();

        stack.push(node);
        while (!stack.empty()) {
            node = stack.pop();
            if (node.getDFSFinishTime() == -2) {
                continue; //already visited.
            }
            //just reusing same variable for already visited.
            node.setDFSFinishTime(-2);
            connectedNodes.add(node);
            stack.addAll(node.getParents());
        }
    }
}
