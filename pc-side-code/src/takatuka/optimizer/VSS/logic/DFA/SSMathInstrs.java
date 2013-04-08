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

import takatuka.classreader.dataObjs.MethodInfo;
import takatuka.verifier.dataObjs.*;
import takatuka.verifier.dataObjs.attribute.*;
import takatuka.verifier.logic.*;
import takatuka.verifier.logic.exception.*;
import takatuka.verifier.logic.factory.*;
import takatuka.verifier.logic.DFA.MathInstrs;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class SSMathInstrs extends MathInstrs {

    private static final SSMathInstrs myObj = new SSMathInstrs();

    /**
     * Constructor is private
     */
    protected SSMathInstrs() {
    }

    public static SSMathInstrs getInstanceOf(OperandStack stack,
            MethodInfo currentMethod,
            int currentPC) {
        init(stack, currentMethod, currentPC);
        return myObj;
    }

    /**
     * type, type ==> type
     * It implements mul, div, xor, or, add, Shift, REM, Shift instruction
     * in total this function implements around 30 instructions.
     */
    @Override
    protected void mathInstruction(OperandStack stack, int val1Type, int val2Type,
            int resultType, int opcode, VerificationInstruction currentInstr) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        Type val1 = stack.pop();
        Type val2 = stack.pop();
        ConvertTypeUtil cf = ConvertTypeUtil.getInstanceOf(currentMethod);

        if (val1.getBlocks() < Type.getBlocks(val1Type, false) && val1.isIntOrShortOrByteOrBooleanOrCharType()) {
            cf.convertTypes(val1, val1Type, 0, currentInstr);
        }
        if (val2.getBlocks() < Type.getBlocks(val2Type, false) && val2.isIntOrShortOrByteOrBooleanOrCharType()) {
            cf.convertTypes(val2, val2Type, 1, currentInstr);
        }
        if (val1.getBlocks() != Type.getBlocks(val1Type, false)
                || val2.getBlocks() != Type.getBlocks(val2Type, false)) {
            /**
             * We check here the block sizes instead of exact type as TakaTuka specific load and store
             * instructions do not differentiate between them.
             */
            //Miscellaneous.println("---------- "+currentInstr);
            //Miscellaneous.println(Type.typeToString(val1Type)+", "+Type.typeToString(val2Type)+
            //        val1+", "+val2);
            throw new VerifyErrorExt(Messages.STACK_INVALID);
        }
        Object value = null;
        Type toSet = frameFactory.createType(resultType);
        stack.push(toSet);
    }
}
