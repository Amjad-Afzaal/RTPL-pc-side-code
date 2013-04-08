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

import takatuka.classreader.dataObjs.MethodInfo;
import takatuka.classreader.logic.constants.*;
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
public class MathInstrs {

    protected static OperandStack stack = null;
    protected static MethodInfo currentMethod = null;
    protected static int currentPC = -1;
    private static final MathInstrs myObj = new MathInstrs();

    /**
     * Constructor is private
     */
    protected MathInstrs() {
    }

    protected static void init(OperandStack stack_,
            MethodInfo currentMethod_,
            int currentPC_) {
        stack = stack_;
        currentMethod = currentMethod_;
        currentPC = currentPC_;
    }

    public static MathInstrs getInstanceOf(OperandStack stack_,
            MethodInfo currentMethod_, int currentPC) {
        init(stack_, currentMethod_, currentPC);
        return myObj;
    }

    protected void mathExecute(VerificationInstruction inst) {
        int typeStack1 = -1;
        int typeStack2 = -1;
        int typeResult = -1;
        int opcode = inst.getOpCode();

        switch (opcode) {
            case JavaInstructionsOpcodes.IADD:
            case JavaInstructionsOpcodes.IDIV:
            case JavaInstructionsOpcodes.IMUL:
            case JavaInstructionsOpcodes.ISUB:
            case JavaInstructionsOpcodes.IREM:
            case JavaInstructionsOpcodes.ISHL:
            case JavaInstructionsOpcodes.ISHR:
            case JavaInstructionsOpcodes.IUSHR:
            case JavaInstructionsOpcodes.IXOR:
            case JavaInstructionsOpcodes.IOR:
            case JavaInstructionsOpcodes.IAND:
                typeStack1 = Type.INTEGER;
                typeStack2 = Type.INTEGER;
                typeResult = Type.INTEGER;
                break;
            case JavaInstructionsOpcodes.FADD:
            case JavaInstructionsOpcodes.FDIV:
            case JavaInstructionsOpcodes.FMUL:
            case JavaInstructionsOpcodes.FSUB:
            case JavaInstructionsOpcodes.FREM:
                typeStack1 = Type.FLOAT;
                typeStack2 = Type.FLOAT;
                typeResult = Type.FLOAT;
                break;
            case JavaInstructionsOpcodes.DADD:
            case JavaInstructionsOpcodes.DDIV:
            case JavaInstructionsOpcodes.DMUL:
            case JavaInstructionsOpcodes.DSUB:
            case JavaInstructionsOpcodes.DREM:
                typeStack1 = Type.DOUBLE;
                typeStack2 = Type.DOUBLE;
                typeResult = Type.DOUBLE;
                break;
            case JavaInstructionsOpcodes.LADD:
            case JavaInstructionsOpcodes.LDIV:
            case JavaInstructionsOpcodes.LMUL:
            case JavaInstructionsOpcodes.LSUB:
            case JavaInstructionsOpcodes.LREM:
            case JavaInstructionsOpcodes.LXOR:
            case JavaInstructionsOpcodes.LAND:
            case JavaInstructionsOpcodes.LOR:
                typeStack1 = Type.LONG;
                typeStack2 = Type.LONG;
                typeResult = Type.LONG;
                break;
            case JavaInstructionsOpcodes.LSHL:
            case JavaInstructionsOpcodes.LSHR:
            case JavaInstructionsOpcodes.LUSHR:
                typeStack1 = Type.INTEGER;
                typeStack2 = Type.LONG;
                typeResult = Type.LONG;
                break;
            default:
                throw new VerifyErrorExt(Messages.INVALID_BYTECODE + inst);
        }
        mathInstruction(stack, typeStack1, typeStack2, typeResult, opcode, inst);
    }

    /**
     * type, type ==> type
     * It implements mul, div, xor, or, add, Shift, REM, Shift instruction
     * in total this function implements around 30 instructions.
     */
    protected void mathInstruction(OperandStack stack, int val1Type, int val2Type,
            int resultType, int opcode, VerificationInstruction currentInstr) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        Type val1 = stack.pop();
        Type val2 = stack.pop();
        Type val1ShouldBe = frameFactory.createType(val1Type);
        Type val2ShouldBe = frameFactory.createType(val2Type);

        if (!Type.isCompatableTypes(val1, val1ShouldBe) ||
                !Type.isCompatableTypes(val2, val2ShouldBe)) {
            /**
             * We check here the block sizes instead of exact type as TakaTuka specific load and store
             * instructions do not differentiate between them.
             */
            throw new VerifyErrorExt(Messages.STACK_INVALID);
        }
        Object value = null;
        Type toSet = frameFactory.createType(resultType);
        stack.push(toSet);
    }
}
