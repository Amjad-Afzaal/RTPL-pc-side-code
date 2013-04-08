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
package takatuka.offlineGC.DFA.dataObjs.functionState;

import java.util.*;
import takatuka.offlineGC.DFA.dataObjs.*;
import takatuka.offlineGC.DFA.dataObjs.attribute.GCInstruction;
import takatuka.verifier.dataObjs.FrameElement;

/**
 * 
 * Description:
 * <p>
 *
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class FunctionStateValueElement {

    private GCOperandStack leavingStack = null;
    private GCOperandStack enteringStack = null;
    private GCLocalVariables localVariable = null;
    private GCInstruction instr = null;
    private HashSet<TTReference> usedOnMe = null;
    private HashSet<TTReference> leavingStackRefUsedOnMe = null;

    /**
     * 
     * @param instr
     * @param leavingOperandStack
     * @param enteringOperandStack
     * @param localVariables
     */
    public FunctionStateValueElement(GCInstruction instr, GCOperandStack leavingOperandStack,
            GCOperandStack enteringOperandStack, GCLocalVariables localVariables) {
        this.instr = instr;
        if (enteringOperandStack != null) {
            this.enteringStack = (GCOperandStack) enteringOperandStack.clone();
        }
        if (leavingOperandStack != null) {
            this.leavingStack = (GCOperandStack) leavingOperandStack.clone();
        }
        this.localVariable = localVariables;
    }

    /**
     * 
     * @return
     */
    public GCLocalVariables getGCLeavingLocalVariables() {
        return localVariable;
    }

    /**
     * 
     * @return
     */
    public GCOperandStack getGCLeavingOperandStack() {
        return leavingStack;
    }

    /**
     * 
     * @return
     */
    public GCOperandStack getGCEnteringOperandStack() {
        return enteringStack;
    }

    /**
     * 
     * @return
     */
    public GCInstruction getInstruction() {
        return instr;
    }

    /**
     * check the leaving and enetering stack and returns get all the references used
     * 
     * 
     * @return
     */
    public HashSet<TTReference> getAllReferenceUsed() {
        //if (this.usedOnMe == null) {
        usedOnMe = getAllReferenceUsed(leavingStack);
        usedOnMe.addAll(getAllReferenceUsed(enteringStack));
        //}
        return usedOnMe;
    }

    private HashSet<TTReference> removeRefWithUnkownNew(HashSet<TTReference> refSet) {
        HashSet<TTReference> ret = new HashSet<TTReference>();
        Iterator<TTReference> it = refSet.iterator();
        while (it.hasNext()) {
            TTReference ref = it.next();
            if (ref.getNewId() != -1) {
                ret.add(ref);
            }
        }
        return ret;
    }

    /**
     * check the leaving and enetering stack and returns get all the references used
     *
     *
     * @return
     */
    public HashSet<TTReference> getAllReferenceUsedCache() {
        if (this.usedOnMe == null) {
            if (enteringStack == null) {
                return new HashSet<TTReference>();
            }
            usedOnMe = getAllReferenceUsed(leavingStack);
            usedOnMe.addAll(getAllReferenceUsed(enteringStack));
            usedOnMe = removeRefWithUnkownNew(usedOnMe);
        }
        return usedOnMe;
    }

    /**
     * 
     * @return
     */
    public HashSet<TTReference> getAllReferenceUsedByLeavingStack() {
        if (leavingStackRefUsedOnMe != null) {
            return leavingStackRefUsedOnMe;
        }
        leavingStackRefUsedOnMe = getAllReferenceUsed(leavingStack);
        return leavingStackRefUsedOnMe;
    }

    public void delete(HashSet<TTReference> refsToBeDeleted) {
        delete(enteringStack, refsToBeDeleted);
        delete(leavingStack, refsToBeDeleted);
        delete(localVariable, refsToBeDeleted);
    }

    private static HashSet delete(FrameElement stack,
            HashSet<TTReference> refsToBeDeleted) {
        int size = stack.getCurrentSize();
        HashSet ret = new HashSet();
        for (int loop = 0; loop < size; loop++) {
            GCType type = (GCType) stack.get(loop);
            type.delete(refsToBeDeleted);
        }
        return ret;
    }

    /**
     * 
     * @param stack
     * @return
     */
    private static HashSet getAllReferenceUsed(GCOperandStack stack) {
        HashSet ret = new HashSet();
        if (stack == null) {
            return ret;
        }
        int size = stack.getNumberOfTypesInStack();
        for (int loop = 0; loop < size; loop++) {
            GCType type = (GCType) stack.get(loop);
            if (type.isReference()) {
                HashSet references = type.getReferences();
                ret.addAll(references);
            }
        }
        return ret;
    }


    @Override
    public String toString() {
        return "\n" + "Stack-leaving=[" + leavingStack
                + ", stack-entering" + enteringStack + "],\t LV=[" + localVariable + "]";
    }
}
