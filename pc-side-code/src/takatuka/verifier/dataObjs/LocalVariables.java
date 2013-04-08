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

import takatuka.classreader.logic.util.*;
import takatuka.optimizer.VSS.logic.preCodeTravers.ReduceTheSizeOfLocalVariables;
import takatuka.verifier.logic.*;
import takatuka.verifier.logic.exception.*;
import takatuka.verifier.logic.factory.*;

/**
 * <p>Title: </p>
 *
 * <p>Description:
 * It represents an array of local variables of a function.
 *
 * </p>
 *
 * @author Faisal Aslam
 * @version 1.0
 */
public class LocalVariables extends FrameElement {

    public LocalVariables(int maxSize) {
        super(maxSize);
        elements = new Vector();
    }

    /**
     * 
     * @param variables
     */
    public void addVaraibles(Vector<Type> variables) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        for (int loop = 0; loop < variables.size(); loop++) {
            Type typeToAdd = variables.elementAt(loop);
            if (!typeToAdd.isReference && typeToAdd.getType() == Type.SPECIAL_TAIL) {
                continue;
            }
            add(typeToAdd);
        }
        while (elements.size() != maxSize) {
            elements.addElement(frameFactory.createType(Type.UNUSED));
        }
    }

    private void checkSizeConstraint(int category) {
        if (elements.size() + category > maxSize) { //+1 as it is called before add
            throw new VerifyErrorExt(Messages.LOCAL_VAR_SIZE_UPPER_LIMIT);
        }
    }

    @Override
    public Object clone() {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        LocalVariables cloneVariables = frameFactory.createLocalVariables(maxSize);
        cloneVariables.elements = (Vector) elements.clone();
        return cloneVariables;
    }

    /**
     * 
     * @param type
     * @return the category (or the number of elemented added)
     */
    public int add(Type type) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        checkSizeConstraint(type.getBlocks());
        elements.add(type);
        for (int loop = 0; loop < type.getBlocks() - 1; loop++) {
            elements.add(frameFactory.createType(Type.SPECIAL_TAIL));
        }
        return type.getBlocks();
    }

    /**
     * @param index
     * @return
     */
    public Type remove(int index) {
        return (Type) elements.remove(index);
    }

    /**
     * Per documentation (http://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html)
     * "Whenever a value of type long or double is moved into a local variable at index n,
     * index n + 1 is specially marked to indicate that it has been reserved by the value
     * at index n and may not be used as a local variable index.
     * Any value previously at index n + 1 becomes unusable.
     *
     * Whenever a value is moved to a local variable at index n,
     * the index n - 1 is examined to see if it is the index of a value of type long or double.
     * If so, the local variable at index n - 1 is changed to indicate that it now contains an unusable value.
     * Since the local variable at index n has been overwritten, the local variable at index n - 1
     * cannot represent a value of type long or double."
     *
     * Note: In our case it is more general as even an integer or short can take multiple slots of local variables.
     *
     * @param index int
     * @param type Type
     * @return Type
     */
    public Type set(int index, Type type) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        Type ret = null;
        try {
            //check for n-1
            checkForOverlapingLV(index);

            ret = (Type) elements.set(index, type);
            int size = type.getBlocks() - 1;
            //update for n+1
            for (int loop = 0; loop < size; loop++) {
                elements.set(index + 1 + loop, frameFactory.createType(Type.SPECIAL_TAIL));
            }

        } catch (ArrayIndexOutOfBoundsException d) {
            throw new VerifyError(Messages.LOCAL_VAR_SIZE_UPPER_LIMIT);
        }
        return ret;
    }

    private void checkForOverlapingLV(int index) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        if (index == 0) {
            return; //done
        }
        for (int loop = index - 1; loop >= 0; loop--) {
            Type temp = (Type) elements.get(loop);
            if (temp.type != Type.UNUSED && temp.type != Type.SPECIAL_TAIL) {
                //used LV is found.
                int typeSize = temp.getBlocks();
                if (index < loop + typeSize) {
                    for (int innerLoop = loop; innerLoop < index; innerLoop++) {
                        elements.set(index - 1, frameFactory.createType(Type.UNUSED));
                    }
                }
                break;
            }
        }
    }

    private boolean isUnused(Type variable) {
        if (variable.isReference) {
            return false;
        }
        if (variable.type == Type.SPECIAL_TAIL
                || variable.type == Type.UNUSED) {
            return true;
        }
        return false;
    }

    public Type get(int index) {
        Type variable = (Type) elements.get(index);
        if (variable == null || isUnused(variable)) { //uninitalized
            throw new VerifyErrorExt(Messages.LOCAL_VAR_UNUSED);
        }
        return variable;
    }

    public Type getUnverified(int index) {
        return (Type) elements.get(index);
    }

    public boolean mergeWhenLastInstrWasRET(FrameElement localVars) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        LocalVariables vars = (LocalVariables) localVars;
        boolean changed = false;
        try {
            int size = vars.getCurrentSize();
            if (size > this.getCurrentSize()) {
                throw new VerifyError(Messages.LOCAL_VAR_MERGE);
            }
            Type inputType = null;
            Type mineType = null;
            for (int loop = 0; loop < size; loop++) {
                inputType = vars.getUnverified(loop);
                if (!mineType.equals(inputType)) {
                    changed = true;
                }
                set(loop, inputType);
            }
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return changed;
    }

    /**
     * Merge two LocalVariables and returns true if there is change after a merge.
     * Per documentation
     *
     * "To merge two local variable array states, corresponding pairs of local
     * elements are compared. If the two types are not identical, then unless
     * both contain reference values, the verifier records that the local variable
     * contains an unusable value. If both of the pair of local elements contain
     * reference values, the merged state contains a reference to an instance of
     * the first common superclass of the two types."
     *
     * @param vars LocalVariables
     * @return boolean
     * @throws Exception
     */
    public boolean merge(FrameElement localVars) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        LocalVariables vars = (LocalVariables) localVars;
        boolean changed = false;
        try {
            int size = vars.getCurrentSize();
            if (size > this.getCurrentSize()) {
                throw new VerifyError(Messages.LOCAL_VAR_MERGE);
            }
            Type inputType = null;
            Type mineType = null;
            for (int loop = 0; loop < size; loop++) {
                inputType = vars.getUnverified(loop);
                mineType = getUnverified(loop);
                if (mineType.isReference && inputType.isReference) {
                    Type changedType = mergeReferences(mineType, inputType);
                    if (changedType != null) {
                        changed = true;
                        set(loop, changedType);
                    }
                } else if (mineType.type != inputType.type
                        && !Type.isCompatableTypes(mineType, inputType)) {
                    if (mineType.type != Type.UNUSED) {
                        set(loop, frameFactory.createType(Type.UNUSED));
                        changed = true;
                    }
                } else if (mineType.type != inputType.type
                        && Type.isCompatableTypes(mineType, inputType)) {
                    if (mineType.getBlocks() < inputType.getBlocks()) {
                        if (ReduceTheSizeOfLocalVariables.BLOCK_SIZE_IN_BYTES < 4) {
                            changed = true;
                            set(loop, inputType);
                        } else {
                            if (mineType.type != Type.UNUSED) {
                                set(loop, frameFactory.createType(Type.UNUSED));
                                changed = true;
                            }
                        }
                    }
                }
            }
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return changed;
    }
}
