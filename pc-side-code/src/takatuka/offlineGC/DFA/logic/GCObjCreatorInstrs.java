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
package takatuka.offlineGC.DFA.logic;

import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.constants.*;
import takatuka.verifier.dataObjs.*;
import takatuka.verifier.dataObjs.attribute.*;
import takatuka.verifier.logic.*;
import takatuka.verifier.logic.exception.*;
import takatuka.classreader.logic.util.*;
import takatuka.offlineGC.DFA.dataObjs.*;
import takatuka.offlineGC.DFA.dataObjs.fields.*;
import takatuka.offlineGC.DFA.logic.factory.*;
import takatuka.optimizer.VSS.logic.DFA.*;
import takatuka.verifier.logic.factory.*;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class GCObjCreatorInstrs extends SSObjCreatorInstrs {

    private static final NewInstrIdFactory newInstrIdFact = NewInstrIdFactory.getInstanceOf();
    private static final GCObjCreatorInstrs myObj = new GCObjCreatorInstrs();

    protected GCObjCreatorInstrs() {
    }

    public static GCObjCreatorInstrs getInstanceOf(OperandStack stack, MethodInfo currentMethod) {
        init(stack, currentMethod);
        return myObj;
    }

    @Override
    protected void ldcInstruction(VerificationInstruction inst,
            boolean isCategoryOne) {
        try {
            //get the index and element at that index
            //int index = inst.getOperandsData().intValueUnsigned();
            //String cName = ClassFile.currentClassToWorkOn.getSourceFileNameWithPath();
            Type type = getLDCTagType(inst);

            //check the element type.
            if ((!isCategoryOne && type.isReference())
                    || (!isCategoryOne
                    && type.getType() != TagValues.CONSTANT_Double
                    && type.getType() != TagValues.CONSTANT_Long)) {
                throw new VerifyErrorExt(Messages.CP_INDEX_INVALID);
            }

            if (type.isReference()) {
                type = newInstrIdFact.createNewIdForString(inst, currentMethod);
            }

            stack.push(type);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }

    }

    /**
     * ..., count (int)  ==> ..., arrayref
     *
     */
    @Override
    protected void newArrayInstruction(VerificationInstruction inst) {
        Un operand = inst.getOperandsData();
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        Type count = stack.pop();
        ConvertTypeUtil cf = ConvertTypeUtil.getInstanceOf(currentMethod);
        if (count.getType() != Type.INTEGER) {
            cf.convertTypes(count, 0, inst);
        }
        if (count.getBlocks() != Type.getBlocks(Type.INTEGER, false)
                || !count.isIntOrShortOrByteOrBooleanOrCharType()) {
            throw new VerifyErrorExt(Messages.STACK_INVALID);
        }
        int arrayType = operand.intValueUnsigned();
        int newInstrId = newInstrIdFact.createNewInstId(currentMethod, inst, true, false);
        GCType aType = (GCType) frameFactory.createType(arrayType, true, newInstrId);

        aType.setIsArray();
        stack.push(aType);
        //Note: GCHeapController does NOT add any object. As the array
        //will **NOT** has references in it.
    }

    /**
     * int (count) ==> ref
     * operand: constant pool index i (classInfo)
     * ref should be of fieldType i and array should have size count.
     */
    @Override
    protected void anewarrayInstruction(VerificationInstruction inst) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        Type count = stack.pop();
        ConvertTypeUtil cf = ConvertTypeUtil.getInstanceOf(currentMethod);
        if (count.getType() != Type.INTEGER) {
            cf.convertTypes(count, 0, inst);
        }
        if (!ConvertTypeUtil.passIntegerTest(count)) {
            throw new VerifyErrorExt(Messages.STACK_INVALID);
        }
        int index = inst.getOperandsData().intValueUnsigned();
        int newInstrId = newInstrIdFact.createNewInstId(currentMethod,
                inst, false, true);
        GCType type = (GCType) frameFactory.createType(index, true, newInstrId);
        type.setIsArray();
        stack.push(type);
        // Ask controller to add a new GCHeap object
        GCHeapController.getInstanceOf().addGCHeapArray(newInstrId, index);
    }

    /**
     * operand:
     *       1) index (16bytes) for runtime constantpool telling arrayType
     *       2) dimentions (tell number of int to read from operand stack)
     * .., int (count1), [int (count2), ...]  ==> ..., arrayref (based on operand #1)
     *
     */
    @Override
    protected void multianewarrayInstruction(VerificationInstruction inst) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        if (true) {
            throw new UnsupportedOperationException("offline GC does not support multianewarray instruction");
        }
        try {
            Un operand = (Un) inst.getOperandsData().clone();
            Un cpIndex = Un.cutBytes(2, operand);
            int dimentions = operand.intValueUnsigned();
            int arrayType = cpIndex.intValueUnsigned();
            if (dimentions == 0) {
                throw new VerifyErrorExt(Messages.INVALID_BYTECODE);
            }
            Type countType = null;
            //popping all the counts
            for (int loop = 0; loop < dimentions; loop++) {
                countType = stack.pop();
                if (!countType.equals(frameFactory.createType(Type.INTEGER))) {
                    throw new VerifyErrorExt(Messages.STACK_INVALID);
                }
            }
            //pushing array-reference
            int newInstrId = newInstrIdFact.createNewInstId(currentMethod,
                    inst, false, true);
            GCType arrayRef = (GCType) frameFactory.createType(arrayType, true, newInstrId);
            arrayRef.setIsArray();
            stack.push(arrayRef);
            // Ask controller to add a new GCHeap object
            GCHeapController.getInstanceOf().addGCHeap(newInstrId, arrayType);

        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
    }

    /**
     * With the fieldType used set the new Instr Id too. The new Instruction Id should be
     * same for each same new Instruction.
     * An GCHeap object is also created.
     * ..
     * operand: index to the constant pool.
     * Get it and save it on stack as reference.
     * ... ==>..., objectref
     *
     * @param instr
     */
    @Override
    protected void newInstruction(VerificationInstruction instr) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        int index = instr.getOperandsData().intValueUnsigned();
        int newInstrId = newInstrIdFact.createNewInstId(currentMethod,
                instr, false, false);
        GCType type = (GCType) frameFactory.createType(index, true, newInstrId);
        stack.push(type);
        // Ask controller to add a new GCHeap object
        GCHeapController.getInstanceOf().addGCHeap(newInstrId, index);
    }
}
