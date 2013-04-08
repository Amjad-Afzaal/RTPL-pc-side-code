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

import takatuka.verifier.logic.DFA.ObjCreatorInstrs;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.util.Miscellaneous;
import takatuka.verifier.dataObjs.*;
import takatuka.verifier.dataObjs.attribute.*;
import takatuka.verifier.logic.*;
import takatuka.verifier.logic.exception.*;
import takatuka.verifier.logic.factory.*;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class SSObjCreatorInstrs extends ObjCreatorInstrs {

    private static final SSObjCreatorInstrs myObj = new SSObjCreatorInstrs();

    protected SSObjCreatorInstrs() {
    }

    public static SSObjCreatorInstrs getInstanceOf(OperandStack stack,
            MethodInfo currentMethod) {
        init(stack, currentMethod);
        return myObj;
    }

    @Override
    protected void newArrayInstruction(VerificationInstruction inst) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        Un operand = inst.getOperandsData();
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
        Type aType = frameFactory.createType(arrayType, true, -1);

        aType.setIsArray();
        stack.push(aType);
        //Note: GCHeapController does NOT add any object. As the array will not have references in it.
    }

    @Override
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
            ConvertTypeUtil cf = ConvertTypeUtil.getInstanceOf(currentMethod);
            Type countType = null;
            //popping all the counts
            for (int loop = 0; loop < dimentions; loop++) {
                countType = stack.pop();
                if (!countType.isIntOrShortOrByteOrBooleanOrCharType()) {
                    throw new VerifyErrorExt(Messages.STACK_INVALID);
                } else if (countType.getType() != Type.INTEGER) {
                    cf.convertTypes(countType, loop, inst);
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
        Type type = frameFactory.createType(index, true, -1);
        type.setIsArray();
        stack.push(type);
    }
}
