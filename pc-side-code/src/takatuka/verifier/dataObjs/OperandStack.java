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
package takatuka.verifier.dataObjs;

import java.util.*;
import takatuka.classreader.logic.util.Miscellaneous;
import takatuka.optimizer.VSS.logic.preCodeTravers.ReduceTheSizeOfLocalVariables;
import takatuka.verifier.logic.*;
import takatuka.verifier.logic.exception.*;
import takatuka.verifier.logic.factory.*;

/**
 * <p>Title: </p>
 *
 * <p>Description:
 * Adding in operand stack
 * </p>
 *
 * @author Faisal Aslam
 * @version 1.0
 */
public class OperandStack extends FrameElement {


    public OperandStack(int maxSize) {
        super(maxSize);
        elements = new Stack<Vector<Type>>();
    }

    public int getNumberOfTypesInStack() {
        return elements.size();
    }
    @Override
    public int getCurrentSize() {
        int size = 0;
        for (int index = 0; index < elements.size(); index++) {
            Vector typeVec = (Vector) elements.get(index);
            size += typeVec.size();
        }
        return size;
    }

    private void checkSizeConstraint() {
        if (getCurrentSize() > maxSize) {
            throw new VerifyErrorExt(Messages.STACK_SIZE_EXCEEDS_MAX);
        }
    }

    public Object clone() {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        OperandStack cloneVariables = frameFactory.createOperandStack(maxSize);
        cloneVariables.elements = (Vector) elements.clone();
        return cloneVariables;
    }

    /**
     * Works same as pop but does not remove the top element.
     * @return
     */
    public Type peep() {
        try {
            return (Type) ((Vector) ((Stack) elements).peek()).elementAt(0);
        } catch (Exception d) {
            throw new VerifyErrorExt(Messages.EMPTY_STACK_POPPED);
        }
    }

    public Type get(int index) {
        return (Type) ((Vector) elements.get(index)).elementAt(0);
    }

    public Type set(int index, Type type) {
        Vector<Type> oldVec = (Vector) elements.set(index, createEntry(type));
        checkSizeConstraint();
        return oldVec.elementAt(0);
    }

    /**
     * Merge two stack and returns true if there is change after a merge.
     * It throws a VerifyError exception if stacks are not mergeable
     *
     * As per documentation from (http://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html)
     *
     * "To merge two operand stacks, the number of values on each stack must be identical.
     * The types of values on the stacks must also be identical, except that differently
     * typed reference values may appear at corresponding places on the two stacks.
     * In this case, the merged operand stack contains a reference to an instance of the
     * first common superclass of the two types. Such a reference type always exists
     * because the type Object is a superclass of all class and interface types.
     * If the operand stacks cannot be merged, verification of the method fails."
     *
     * @param stackElm
     * @return
     */
    public boolean merge(FrameElement stackElm) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        OperandStack stack = (OperandStack) stackElm;
        boolean changed = false;
//        Miscellaneous.println("merging 1 " + this);
//        Miscellaneous.println("merging 2 " + stack);
        /**
         * Number of elements should be same, even though element sizes might not
         * be same.
         */
        if (elements.size() != stack.elements.size()) {
            Miscellaneous.printlnErr("merging 1 " + this);
            Miscellaneous.printlnErr("merging 2 " + stack);
            throw new VerifyErrorExt(Messages.STACK_MERGE_ERROR);
        }
        try {
            Type inputType = null;
            Type myType = null;
            for (int loop = 0; loop < stack.elements.size(); loop++) {
                inputType = (Type) stack.get(loop);
                myType = (Type) get(loop);
                //both types will always be equal unless they are of reference type
                if (!myType.isReference
                        && !Type.isCompatableTypes(myType, inputType)) {
                    throw new VerifyErrorExt(Messages.STACK_MERGE_ERROR);
                } else if (!myType.isReference) {
                    if (myType.getBlocks() < inputType.getBlocks()) {
                        if (ReduceTheSizeOfLocalVariables.BLOCK_SIZE_IN_BYTES < 4) {
                            set(loop, inputType);
                            changed = true;
                        } else {
                            throw new VerifyErrorExt(Messages.STACK_MERGE_ERROR);

                        }
                    }
                    continue;
                }
                Type newType = mergeReferences(myType, inputType);
                if (newType != null) {
                    changed = true;
                    set(loop, newType);
                }
            }
        } catch (Exception d) {
            throw new VerifyErrorExt(Messages.STACK_INVALID);
        }

        return changed;
    }

    public boolean empty() {
        return ((Stack) elements).empty();
    }

    /**
     *
     * @return Type
     */
    public Type pop() {
        if (empty()) {
            throw new VerifyErrorExt(Messages.EMPTY_STACK_POPPED);
        }
        Type ret = (Type) ((Vector) ((Stack) elements).pop()).elementAt(0);
        return ret;
    }

    /**
     *
     * @param type Type
     * @return Type
     */
    public Type push(Type type) {
        Vector entry = createEntry((Type) type.clone());
        Vector<Type> ret = (Vector) ((Stack) elements).push(entry);
        checkSizeConstraint();
        return ret.elementAt(0);
    }
}
