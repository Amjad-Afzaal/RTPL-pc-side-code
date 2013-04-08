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
import takatuka.offlineGC.OGI.GraphUtils.INode;
import takatuka.offlineGC.OGI.DAGUtils.GraphLabelsController;
import java.util.*;
import takatuka.classreader.dataObjs.MethodInfo;
import takatuka.classreader.logic.util.Miscellaneous;
import takatuka.offlineGC.DFA.dataObjs.*;
import takatuka.offlineGC.DFA.dataObjs.fields.RecordRefUsedByFields;
import takatuka.optimizer.cpGlobalization.logic.util.Oracle;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * Represents a method level graph node.
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class MethodLevelGraphNode implements INode {

    private InstrGraphNode parentInstr = null;
    private HashSet<MethodLevelChildInfo> children = new HashSet<MethodLevelChildInfo>();
    private RefsPerIntraMethodGraph allRefUsed = RefsPerIntraMethodGraph.getInstanceOf();
    private HashSet<MethodLevelGraphNode> parents = new HashSet<MethodLevelGraphNode>();
    private int myId = 0;
    private String methodStr = null;
    private static boolean anscestorCacheCreated = false;
    private HashSet<MethodLevelGraphNode> anscestorsCache = new HashSet<MethodLevelGraphNode>();
    private HashSet<TTReference> refUsedByAnscestorsCache = new HashSet<TTReference>();
    private MethodInfo method = null;

    public MethodLevelGraphNode(int myId, MethodInfo method) {
        this.myId = myId;
        this.method = method;
        this.methodStr = Oracle.getInstanceOf().getMethodOrFieldString(method);
    }

    public String getMethodStr() {
        return methodStr;
    }

    public MethodInfo getMethod() {
        return method;
    }

    public void addParentInstr(InstrGraphNode parent) {
        this.parentInstr = parent;
    }

    public InstrGraphNode getParentInstr() {
        return this.parentInstr;
    }

    public int getId() {
        return myId;
    }

    public void addChild(MethodLevelChildInfo childInfo) {
        if (anscestorCacheCreated) {
            Miscellaneous.printlnErr("cannot create cache when the graph is not yet completed");
            Miscellaneous.exit();
        }
        this.children.add(childInfo);
        childInfo.getNode().parents.add(this);
    }

    public HashSet<MethodLevelGraphNode> getAllAncestors() {
        anscestorCacheCreated = true;
        if (anscestorsCache.size() != 0) {
            return anscestorsCache;
        }
        Stack<MethodLevelGraphNode> stack = new Stack<MethodLevelGraphNode>();
        stack.addAll(parents);
        while (!stack.empty()) {
            MethodLevelGraphNode graphNode = stack.pop();
            if (anscestorsCache.contains(graphNode)
                    || graphNode.equals(this)) {
                continue;
            }
            anscestorsCache.add(graphNode);
            stack.addAll(graphNode.parents);
        }
        return anscestorsCache;
    }

    public HashSet<TTReference> getRefersUsedByAnscestors() {
        if (refUsedByAnscestorsCache.size() != 0) {
            return refUsedByAnscestorsCache;
        }
        HashSet<MethodLevelGraphNode> anscestors = getAllAncestors();
        Iterator<MethodLevelGraphNode> it = anscestors.iterator();
        while (it.hasNext()) {
            MethodLevelGraphNode mlGNode = it.next();
            refUsedByAnscestorsCache.addAll(mlGNode.getRefsByMeButNeverAssignedToAField());
        }
        return refUsedByAnscestorsCache;
    }

    public HashSet<MethodLevelChildInfo> getChildren() {
        return children;
    }

    public HashSet<MethodLevelGraphNode> getParents() {
        return parents;
    }

    public HashSet<TTReference> getReferences() {
        return allRefUsed.getRefUseInAMethodGraph(getId());
    }

    public HashSet<TTReference> getRefsByMeButNeverAssignedToAField() {
        HashSet<TTReference> refAssignedToFields = RecordRefUsedByFields.getInstanceOf().getAllSavedRecord();
        HashSet<TTReference> refUseInMethod = allRefUsed.getRefUseInAMethodGraph(getId());
        refUseInMethod.removeAll(refAssignedToFields);
        return refUseInMethod;
    }

    public String toStringWithOutLables() {
        MethodLevelChildInfo mlcInfo = new MethodLevelChildInfo(this, null);
        return toStringWithoutLabels(mlcInfo);
    }

    public static String getNewIds(HashSet<TTReference> refSet, boolean filterFieldRef) {
        TreeSet ret = new TreeSet();
        Iterator<TTReference> it = refSet.iterator();
        HashSet<TTReference> refAssignedToField = RecordRefUsedByFields.getInstanceOf().getAllSavedRecord();
        if (!filterFieldRef) {
            refAssignedToField.clear();
        }
        while (it.hasNext()) {
            TTReference ref = it.next();
            if (refAssignedToField.contains(ref)) {
                continue;
            }
            ret.add(ref.getNewId());
        }
        if (ret.size() != 0) {
            return ret.toString();
        } else {
            return null;
        }
    }

    public String getLabel() {
        String newIdsUsed = getNewIds(getReferences(), true);
        String ret = methodStr.substring(methodStr.lastIndexOf(".") + 1, methodStr.length());
        if (newIdsUsed != null) {
            ret += ", Tags-Used=" + newIdsUsed + "";
        }
        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof MethodLevelGraphNode)) {
            return false;
        }
        MethodLevelGraphNode input = (MethodLevelGraphNode) obj;
        if (input.myId == myId) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 43 * hash + this.myId;
        return hash;
    }

    public String toStringWithoutLabels(MethodLevelChildInfo gNodeToStartWith) {
        Stack<MethodLevelChildInfo> stack = new Stack<MethodLevelChildInfo>();
        stack.push(gNodeToStartWith);
        HashSet<MethodLevelGraphNode> alreadyVisited = new HashSet<MethodLevelGraphNode>();
        String stringWithoutLabels = "";
        while (!stack.empty()) {
            MethodLevelChildInfo node = stack.pop();
            if (alreadyVisited.contains(node.getNode())) {
                continue;
            }
            alreadyVisited.add(node.getNode());

            GraphLabelsController.getInstanceOf().addForMethodLevelGraph(node.getNode().getId(), node.getNode().getLabel());
            Iterator<MethodLevelChildInfo> it = node.getNode().getChildren().iterator();

            if (!it.hasNext()) {
                //stringWithoutLabels += node.getNode().getId() + "-> END;\n";
            }
            while (it.hasNext()) {
                MethodLevelChildInfo graphNode = it.next();
                GraphLabelsController.getInstanceOf().addForMethodLevelGraph(graphNode.getNode().getId(), graphNode.getNode().getLabel());
                stringWithoutLabels += node.getNode().getId() + " ->" + graphNode.getNode().getId() + ";\n";
            }
            stack.addAll(node.getNode().getChildren());
        }
        return stringWithoutLabels;
    }

    @Override
    public String toString() {
        return toStringWithOutLables();
    }
}
