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
import java.util.*;
import takatuka.offlineGC.OGI.factory.BaseFactory;
import takatuka.offlineGC.DFA.dataObjs.*;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class MethodLevelChildInfo {

    /**
     * The node that info represents.
     */
    private MethodLevelGraphNode node = null;
    /**
     * The invoke instruction to the node.
     */
    private InstrGraphNode parentInstr = BaseFactory.getInstanceOf().createGraphNode(-1);
    public HashSet<TTReference> alreadyFreedRef =  new HashSet<TTReference>();


    public MethodLevelChildInfo(MethodLevelGraphNode node, InstrGraphNode parentInstr) {
        this.node = node;
        if (parentInstr != null) {
            this.parentInstr = parentInstr;
        }
    }

    public MethodLevelGraphNode getNode() {
        return node;
    }

    public InstrGraphNode getParentInvokeInstr() {
        return parentInstr;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof MethodLevelChildInfo)) {
            return false;
        }
        MethodLevelChildInfo input = (MethodLevelChildInfo) obj;
        if (input.node.equals(node) && input.parentInstr.equals(parentInstr)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.node != null ? this.node.hashCode() : 0);
        hash = 67 * hash + (this.parentInstr != null ? this.parentInstr.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        long parentId = -1;
        if (parentInstr != null) {
            parentId = parentInstr.getInstruction().getNormalInstrs().
                    firstElement().getInstructionId();
        }
        return parentId + ", " + node.getLabel();
    }
}
