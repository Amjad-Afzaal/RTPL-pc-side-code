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
package takatuka.optimizer.VSS.logic.DFA;

import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.verifier.dataObjs.*;
import takatuka.verifier.dataObjs.attribute.*;
import takatuka.verifier.logic.*;
import takatuka.verifier.logic.exception.*;
import takatuka.classreader.logic.util.*;
import takatuka.verifier.logic.factory.*;
import takatuka.verifier.logic.DFA.MiscInstrs;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class SSMiscInstrs extends MiscInstrs {

    private static final SSMiscInstrs myObj = new SSMiscInstrs();

    protected SSMiscInstrs() {
    }

    public static SSMiscInstrs getInstanceOf(Vector<Long> nextPossibleInstructionsIds,
            OperandStack stack, MethodInfo currentMethodStatic,
            LocalVariables localVar, int currentPC, HashSet<Type> returnTypes) {
        init(nextPossibleInstructionsIds, stack, currentMethodStatic,
                localVar, currentPC, returnTypes);
        return myObj;
    }

    /**
     * This function implements all instructions of type A2B. That means,
     * convert type A to B. Below is generic description of it.
     *
     * A ==> B
     *
     * It pops type A from the stack, Verify that it is correct type.
     * It pushes type B on the stack
     */
    @Override
    protected void AtoBInstruction(VerificationInstruction inst, int firstType,
            int secondType) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        Type typeObj = stack.pop();
        ConvertTypeUtil ct = ConvertTypeUtil.getInstanceOf(currentMethodStatic);
        if (typeObj.getBlocks() != Type.getBlocks(firstType, false)) {
            if (typeObj.isIntOrShortOrByteOrBooleanOrCharType()) {
                ct.convertTypes(typeObj, firstType, 0, inst);
            } else {
                throw new VerifyErrorExt(Messages.STACK_INVALID);
            }
        }
        Object value = typeObj.value;
        Type secondTypeObj = frameFactory.createType(secondType);
        if (value != null) {
            //could convert here :)
            //could set here too
            //secondTypeObj.value = value;
        }
        stack.push(secondTypeObj);
    }

    /**
     * type ==> [empty]
     */
    @Override
    protected void returnInstruction(int typeToReturn, OperandStack stack,
            boolean isReference, VerificationInstruction currentInstr) {
        Type returnValue = null;
        if (typeToReturn != Type.VOID) {
            returnValue = stack.pop();

            ConvertTypeUtil ct = ConvertTypeUtil.getInstanceOf(currentMethodStatic);

            if ((isReference && !returnValue.isReference())
                    || (!isReference
                    && returnValue.getBlocks() != Type.getBlocks(typeToReturn, isReference))) {
                if (returnValue.isIntOrShortOrByteOrBooleanOrCharType()) {
                    //convert
                    ct.convertTypes(returnValue, typeToReturn, 0, currentInstr);
                } else {
                    throw new VerifyErrorExt(Messages.STACK_INVALID);
                }
            }
        }
        if (typeToReturn != Type.VOID) {
            returnTypes.add(returnValue);
        }

        stack.clear();
    }

    /**
     * type ==> type
     */
    @Override
    protected void negInstruction(OperandStack stack, int type,
            VerificationInstruction currentInstr) {
        Type val = stack.pop();
        if (val.getBlocks() != Type.getBlocks(type, false)) {
            ConvertTypeUtil ct = ConvertTypeUtil.getInstanceOf(currentMethodStatic);

            if (val.isIntOrShortOrByteOrBooleanOrCharType()) {
                ct.convertTypes(val, 0, currentInstr);
            } else {
                throw new VerifyErrorExt(Messages.STACK_INVALID + ", val1= " + val + ", val2=" + Type.typeToString(type));
            }
        }
        //negating could be done here also peep could be used here (instead of push/pop)
        stack.push(val);
    }

    /**
     * operands: index (signed byte), const (signed byte)
     * value = VGet(index)
     * value should be of type int
     * increment that value by const (ignore this step in validation).
     */
    @Override
    protected void iincInstruction(VerificationInstruction inst, boolean isWide) {
        try {
            Un data = (Un) inst.getOperandsData().clone();
            int index = Un.cutBytes(1 + (isWide ? 1 : 0), data).intValueUnsigned();
            Type value = localVar.get(index);
            ConvertTypeUtil ct = ConvertTypeUtil.getInstanceOf(currentMethodStatic);
            //no need for convertion here. As currently all local variables store
            //instructions has the type convertions.
            if (!ConvertTypeUtil.passIntegerTest(value)) {
                throw new VerifyErrorExt(Messages.LOCALVAR_INVALID + "value =" + value);
            }

        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }

    }
}
