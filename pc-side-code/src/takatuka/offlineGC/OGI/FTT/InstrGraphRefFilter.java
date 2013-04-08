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

import takatuka.offlineGC.OGI.GraphUtils.ReferenceFilter;
import takatuka.offlineGC.OGI.GraphUtils.InstrGraphNode;
import takatuka.offlineGC.DFA.dataObjs.fields.RecordRefUsedByFields;
import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.offlineGC.OGI.GraphUtil.FTT.*;
import takatuka.offlineGC.DFA.dataObjs.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;

/**
 * <p>Title: </p>
 * <p>Description:
 *  - Filter all the references that are used on any of my parents.
 *  - Filter all the references that are assign to any field.
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class InstrGraphRefFilter extends ReferenceFilter {

    private static final InstrGraphRefFilter refFilter = new InstrGraphRefFilter();
    private HashMap<String, HashSet<MethodLevelGraphNode>> methodToMethodNode = new HashMap<String, HashSet<MethodLevelGraphNode>>();

    protected InstrGraphRefFilter() {
    }

    public static InstrGraphRefFilter getInstanceOf() {
        return refFilter;
    }

    @Override
    public void referencesFilter(HashSet<TTReference> refSet, InstrGraphNode graphNode) {
        findReferencesUsedByEachMethodParent();
        /**
         * First remove all the reference that are ever assigned to a field.
         */
        HashSet<TTReference> refAssignedToField = RecordRefUsedByFields.getInstanceOf().getAllSavedRecord();
        refSet.removeAll(refAssignedToField);
        if (refSet.size() == 0) {
            return;
        }
        /**
         * Now has to remove all the references that are used on the parents of this method.
         */
        MethodInfo method = graphNode.getMethod();
        String methodStr = Oracle.getInstanceOf().getMethodOrFieldString(method);
        HashSet<MethodLevelGraphNode> methodNodeForAMethod = methodToMethodNode.get(methodStr);
        Iterator<MethodLevelGraphNode> it = methodNodeForAMethod.iterator();

        while (it.hasNext() && refSet.size() > 0) {
            MethodLevelGraphNode node = it.next();
            /*if (FreeRefsAlgo.debugMe) {
            System.out.println(" # of methods ="+methodNodeForAMethod.size());
            printAncestors(node);
            }*/
            refSet.removeAll(node.getRefersUsedByAnscestors());
            //refSet.removeAll(getRefUsedByImmeidateParents(node));
            /*if (FreeRefsAlgo.debugMe) {
            System.out.println(refSet);
            }*/
        }
    }

    private void printAncestors(MethodLevelGraphNode graphNode) {
        System.out.println("\n\n for graph Node " + graphNode.getLabel());
        HashSet<MethodLevelGraphNode> ancestors = graphNode.getAllAncestors();
        Iterator<MethodLevelGraphNode> it = ancestors.iterator();
        while (it.hasNext()) {
            MethodLevelGraphNode parent = it.next();
            System.out.println(parent.getLabel());
        }
    }

    private HashSet<TTReference> getRefUsedByImmeidateParents(MethodLevelGraphNode node) {
        HashSet<MethodLevelGraphNode> parents = node.getParents();
        Iterator<MethodLevelGraphNode> itParents = parents.iterator();
        HashSet<TTReference> retRef = new HashSet<TTReference>();
        while (itParents.hasNext()) {
            MethodLevelGraphNode gNode = itParents.next();
            retRef.addAll(gNode.getReferences());
        }
        return retRef;
    }
    
    private void findReferencesUsedByEachMethodParent() {
        if (!methodToMethodNode.isEmpty()) {
            return;
        }
        HashSet<MethodLevelGraphNode> alreadyVisited = new HashSet<MethodLevelGraphNode>();

        MethodLevelGraphController methodLevelContr = MethodLevelGraphController.getInstanceOf();

        Collection<MethodLevelGraphNode> startingNodeOfAThread = methodLevelContr.getFirstNodeRecorded();
        Iterator<MethodLevelGraphNode> it = startingNodeOfAThread.iterator();
        while (it.hasNext()) {
            MethodLevelGraphNode startingPoint = it.next();
            Stack<MethodLevelChildInfo> stack = new Stack<MethodLevelChildInfo>();
            stack.push(new MethodLevelChildInfo(startingPoint, null));
            while (!stack.empty()) {
                MethodLevelChildInfo currentNode = stack.pop();
                String methodString = currentNode.getNode().getMethodStr();
                HashSet<MethodLevelGraphNode> existingRecord = methodToMethodNode.get(methodString);
                if (existingRecord == null) {
                    existingRecord = new HashSet<MethodLevelGraphNode>();
                    methodToMethodNode.put(methodString, existingRecord);
                }
                existingRecord.add(currentNode.getNode());
                if (alreadyVisited.contains(currentNode.getNode())) {
                    continue;
                }
                alreadyVisited.add(currentNode.getNode());

                stack.addAll(currentNode.getNode().getChildren());
            }
        }
    }
}

