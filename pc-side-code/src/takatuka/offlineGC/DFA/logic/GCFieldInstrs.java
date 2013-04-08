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
import takatuka.classreader.dataObjs.constantPool.*;
import takatuka.classreader.logic.constants.*;
import takatuka.verifier.dataObjs.*;
import takatuka.verifier.dataObjs.attribute.*;
import takatuka.verifier.logic.*;
import takatuka.verifier.logic.exception.*;
import takatuka.verifier.logic.DFA.*;
import takatuka.offlineGC.DFA.dataObjs.GCType;
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
public class GCFieldInstrs extends SSFieldInstrs {

    private static final GCFieldInstrs myObj = new GCFieldInstrs();

    protected GCFieldInstrs() {
    }

    public static GCFieldInstrs getInstanceOf(OperandStack stack,
            MethodInfo currentMethod, InvokeInstrs invokeInstrVerifier) {
        init(stack, currentMethod, invokeInstrVerifier);
        return myObj;
    }

    /**
     * operand: index to constant pool which should point to fieldrefinfo object
     * ..., objectref  ==> ..., value
     * pop ref from stack. That put back value related to that ref.
     *
     */
    @Override
    protected void getFieldInstruction(VerificationInstruction inst, boolean isStatic) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        HeapUtil heapUtil = HeapUtil.getInstanceOf(invokeInstrVerifier.getCallingPara(), currentMethod, inst);
        //cp index
        int index = inst.getOperandsData().intValueUnsigned();
        FieldRefInfo fInfo = (FieldRefInfo) pOne.get(index,
                TagValues.CONSTANT_Fieldref);
        int nameAndTypeIndex = fInfo.getNameAndTypeIndex().intValueUnsigned();
        NameAndTypeInfo nAtInfo = (NameAndTypeInfo) pOne.get(nameAndTypeIndex,
                TagValues.CONSTANT_NameAndType);

        String description = ((UTF8Info) pOne.get(nAtInfo.getDescriptorIndex().
                intValueUnsigned(),
                TagValues.CONSTANT_Utf8)).convertBytes();
        Type fieldType = frameFactory.createType();
        InitializeFirstInstruction.getType(description, 0, fieldType);
        if (!fieldType.isReference()) {
            super.getFieldInstruction(inst, isStatic);
            return;
        }
        GCType objectRef = null;
        if (!isStatic) {
            objectRef = (GCType) stack.pop();
            if (!objectRef.isReference()) {
                throw new VerifyErrorExt(Messages.STACK_INVALID);
            }
        }
        heapUtil.getFieldAndPushOnStack(nameAndTypeIndex, objectRef,
                fieldType.isArrayReference(), stack);
    }

    /**
     * ..., objectref, value ==>  ...
     *
     * Field will be either in objectRef or in one of its super class.
     * We first have to find right object with the field
     * and then set the newInstrId in that field.
     *
     * @param instr
     * @param isStatic
     * @throws java.lang.Exception
     */
    @Override
    protected void putFieldInstruction(VerificationInstruction instr, boolean isStatic) throws Exception {
        HeapUtil heapUtil = HeapUtil.getInstanceOf(invokeInstrVerifier.getCallingPara(), currentMethod, instr);

        GCType value = (GCType) stack.peep(); //peep value
        if (!value.isReference()) {
            super.putFieldInstruction(instr, isStatic);
            return;
        }
        value = (GCType) stack.pop();
        GCType objectRef = null;
        if (!isStatic) {
            //pop reference
            objectRef = (GCType) stack.pop();
            if (!objectRef.isReference() || objectRef.isArrayReference()) {
                throw new VerifyErrorExt(Messages.STACK_INVALID);
            }
            heapUtil.saveInHeapNonStaticField(value, objectRef, instr);
        } else {
            heapUtil.saveInHeapStaticField(value, instr);
        }

    }
}
