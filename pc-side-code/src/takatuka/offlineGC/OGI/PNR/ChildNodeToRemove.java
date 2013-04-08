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
import takatuka.classreader.dataObjs.MethodInfo;
import takatuka.optimizer.cpGlobalization.logic.util.Oracle;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class ChildNodeToRemove {

    public InstrGraphNode nodeToRemoveFrom = null;
    public InstrGraphNode nodeToRemove = null;

    public ChildNodeToRemove(InstrGraphNode nodeToRemoveFrom, InstrGraphNode nodeToRemove) {
        this.nodeToRemove = nodeToRemove;
        this.nodeToRemoveFrom = nodeToRemoveFrom;
    }

    public boolean removeTheChild() {
        MethodInfo methodInfoToRemoveFrom = nodeToRemove.getMethod();
        Oracle oracle = Oracle.getInstanceOf();
        String methodToRemoveStr = oracle.getMethodOrFieldString(methodInfoToRemoveFrom);
        if (!methodToRemoveStr.startsWith("java.lang")) {
            nodeToRemoveFrom.removeChildFully(nodeToRemove);
            // System.out.println("removing link from method " + methodToRemoveStr);
            return true;
        } else {
            //System.out.println("not removing link from method " + methodToRemoveStr);
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ChildNodeToRemove)) {
            return false;
        }
        ChildNodeToRemove input = (ChildNodeToRemove) obj;
        if (input.nodeToRemove.equals(nodeToRemove)
                && input.nodeToRemoveFrom.equals(nodeToRemoveFrom)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + (this.nodeToRemoveFrom != null ? this.nodeToRemoveFrom.hashCode() : 0);
        hash = 43 * hash + (this.nodeToRemove != null ? this.nodeToRemove.hashCode() : 0);
        return hash;
    }
}
