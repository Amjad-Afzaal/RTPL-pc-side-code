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
package takatuka.offlineGC.OGI.superInstruction;

import takatuka.offlineGC.OGI.GraphUtils.InstrGraphNode;
import java.util.*;
import takatuka.offlineGC.DFA.dataObjs.*;
import takatuka.offlineGC.DFA.dataObjs.attribute.*;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * Given an inter-method graph it creates super instructions.
 * Unlike intra-method super instructions in this case invokes are
 * also combined.
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class CreateInterMethodSuperInstrs {

    private static final CreateInterMethodSuperInstrs myObj = new CreateInterMethodSuperInstrs();

    private CreateInterMethodSuperInstrs() {
    }

    public static CreateInterMethodSuperInstrs getInstanceOf() {
        return myObj;
    }

    private class GraphNodeAndItParent {

        public InstrGraphNode gNode = null;
        public InstrGraphNode parent = null;

        public GraphNodeAndItParent(InstrGraphNode gNode, InstrGraphNode parent) {
            this.gNode = gNode;
            this.parent = parent;
        }
    }

    public void createSuperInstrs(InstrGraphNode startingNode) {
        Stack<InstrGraphNode> stack = new Stack<InstrGraphNode>();
        HashSet<Long> alreadyVisited = new HashSet<Long>();
        stack.push(startingNode);
        while (!stack.empty()) {
            InstrGraphNode currentNode = stack.pop();
            if (alreadyVisited.contains(currentNode.getId())) {
                continue;
            }
            alreadyVisited.add(currentNode.getId());
            stack.addAll(currentNode.getChildren());
        }
    }

    private boolean shouldCreateNewSuperInstr(InstrGraphNode gNode, SuperInstruction parentSuperInstr) {
        if (parentSuperInstr == null || gNode.getParents().size() > 1) {
            return true;
        }
        HashSet<TTReference> lastInstrRef = parentSuperInstr.tempRefForComputation;

        HashSet<TTReference> curretInstrRef = gNode.getReferences();

        GCInstruction parentNormalInstr = parentSuperInstr.getNormalInstrs().firstElement();
        if (parentNormalInstr != null
                && (parentNormalInstr.isBranchSource()
                || parentNormalInstr.isBranchTarget()
                || parentNormalInstr.isBranchSourceInstruction()
                || parentNormalInstr.getMnemonic().contains("STATIC"))) {
            return true;
        }
        SuperInstruction currentInstr = gNode.getInstruction();
        if (!currentInstr.isBranchInstr()
                && (curretInstrRef.equals(lastInstrRef)
                || curretInstrRef.size() == 0
                || lastInstrRef.size() == 0)) {
            return false;
        }
        return true;
    }
}
