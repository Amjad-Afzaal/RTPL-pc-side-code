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
package takatuka.verifier.logic.DFA;

import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.constants.*;
import takatuka.verifier.dataObjs.*;
import takatuka.verifier.dataObjs.attribute.*;
import takatuka.verifier.logic.*;
import takatuka.verifier.logic.exception.*;
import takatuka.classreader.logic.util.*;
import takatuka.verifier.logic.factory.*;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class ObjCreatorInstrs {

    private static final ObjCreatorInstrs myObj = new ObjCreatorInstrs();
    protected static OperandStack stack = null;
    protected static MethodInfo currentMethod = null;

    protected ObjCreatorInstrs() {
    }

    protected static void init(OperandStack stack_, MethodInfo currentMethod_) {
        stack = stack_;
        currentMethod = currentMethod_;
    }

    public static ObjCreatorInstrs getInstanceOf(OperandStack stack,
            MethodInfo currentMethod) {
        init(stack, currentMethod);
        return myObj;
    }

    /**
     * operand: index to the constant pool.
     * Get it and save it on stack as reference.
     * ... ==>..., objectref
     */
    protected void newInstruction(VerificationInstruction inst) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        int index = inst.getOperandsData().intValueUnsigned();
        stack.push(frameFactory.createType(index, true, 0));
    }

    protected void ldcExecute(VerificationInstruction inst) {
        int opcode = inst.getOpCode();
        boolean categoryOne = true;
        if (opcode == JavaInstructionsOpcodes.LDC2_W
                || opcode == JavaInstructionsOpcodes.LDC2_W_LONG) {
            categoryOne = false;
        }
        ldcInstruction(inst, categoryOne);
    }

    /**
     * It implements three instruction ldc, ldc_w and ldc2_w
     * operand: index (int) to the constant pool
     * -- Runtime constant pool get value at index
     * -- check that value is either float/int or otherwise long/double
     * -- push the value on the stack i.e. ... ==> ...,value
     *
     * @param inst
     * @param isCategoryOne
     */
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
            stack.push(type);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }

    }

    protected Type getLDCTagType(VerificationInstruction instr) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        int tag = -1;
        boolean isReference = false;
        int opcode = instr.getOpCode();
        if (opcode == JavaInstructionsOpcodes.LDC
                || opcode == JavaInstructionsOpcodes.LDC_W) {
            tag = TagValues.CONSTANT_String;
            isReference = true;
        } else if (opcode == JavaInstructionsOpcodes.LDC_FLOAT
                || opcode == JavaInstructionsOpcodes.LDC_W_FLOAT) {
            tag = TagValues.CONSTANT_Float;
        } else if (opcode == JavaInstructionsOpcodes.LDC_INT
                || opcode == JavaInstructionsOpcodes.LDC_W_INT) {
            tag = TagValues.CONSTANT_Integer;
        } else if (opcode == JavaInstructionsOpcodes.LDC2_W) {
            tag = TagValues.CONSTANT_Double;
        } else if (opcode == JavaInstructionsOpcodes.LDC2_W_LONG) {
            tag = TagValues.CONSTANT_Long;
        } else {
            Miscellaneous.printlnErr("Error 13r9 ... Existing...");
            Miscellaneous.exit();
        }
        Type type = frameFactory.createType(tag, isReference, -1);

        return type;
    }

    /**
     * operand:
     *       1) index (16bytes) for runtime constantpool telling arrayType
     *       2) dimentions (tell number of int to read from operand stack)
     * .., int (count1), [int (count2), ...]  ==> ..., arrayref (based on operand #1)
     *
     */
    protected void multianewarrayInstruction(VerificationInstruction inst) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
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
                if (!countType.isIntOrShortOrByteOrBooleanOrCharType()) {
                    throw new VerifyErrorExt(Messages.STACK_INVALID);
                }

            }
            //pushing array-reference
            Type arrayRef = frameFactory.createType(arrayType, true, 0);
            arrayRef.setIsArray();
            stack.push(arrayRef);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }

    }

    /**
     * int (count) ==> ref
     * operand: constant pool index i (classInfo)
     * ref should be of type i and array should have size count.
     */
    protected void anewarrayInstruction(VerificationInstruction inst) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        Type count = stack.pop();
        Type countShouldBe = frameFactory.createType(Type.INTEGER);
        if (!Type.isCompatableTypes(count, countShouldBe)) {
            throw new VerifyErrorExt(Messages.STACK_INVALID);
        }

        int index = inst.getOperandsData().intValueUnsigned();
        Type ref = frameFactory.createType(index, true, 0);
        ref.setIsArray();
        stack.push(ref);
    }

    /**
     * ..., count (int)  ==> ..., arrayref
     *
     */
    protected void newArrayInstruction(VerificationInstruction inst) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        Un operand = inst.getOperandsData();
        Type count = stack.pop();
        if (!Type.isCompatableTypes(count, frameFactory.createType(Type.INTEGER))) {
            throw new VerifyErrorExt(Messages.STACK_INVALID);
        }
        int arrayType = 0;
        arrayType = operand.intValueUnsigned();
        Type aType = frameFactory.createType(convertATypeToTagValue(arrayType));
        aType.setIsArray();
        stack.push(aType);
    }

    /**
     *     Array Type 	atype
     *      T_BOOLEAN 	4
     *      T_CHAR 	5
     *      T_FLOAT 	6
     *       T_DOUBLE 	7
     *       T_BYTE 	8
     *       T_SHORT 	9
     *       T_INT 	10
     *       T_LONG 	11
     *
     * @param aType
     * @return
     */
    protected int convertATypeToTagValue(int aType) {
        if (aType == FieldTypes.TYPE_JBOOLEAN || aType == FieldTypes.TYPE_JCHAR || aType == FieldTypes.TYPE_JBYTE
                || aType == FieldTypes.TYPE_JSHORT
                || aType == FieldTypes.TYPE_JINT) {
            return TagValues.CONSTANT_Integer;
        } else if (aType == FieldTypes.TYPE_JFLOAT) {
            return TagValues.CONSTANT_Float;
        } else if (aType == FieldTypes.TYPE_JDOUBLE) {
            return TagValues.CONSTANT_Double;
        } else if (aType == FieldTypes.TYPE_JLONG) {
            return TagValues.CONSTANT_Long;
        } else {
            Miscellaneous.printlnErr("Error # 129k ... ATYPE=" + aType);
            Miscellaneous.exit();
        }

        return 0;
    }
}
