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

import java.util.*;
import takatuka.classreader.dataObjs.MethodInfo;
import takatuka.classreader.logic.constants.*;
import takatuka.verifier.dataObjs.*;
import takatuka.verifier.dataObjs.attribute.*;
import takatuka.verifier.logic.*;
import takatuka.verifier.logic.exception.*;
import takatuka.verifier.logic.factory.*;

/**
 *
 * @author Faisal Aslam
 */
public class PureStackInstrs {

    private static final PureStackInstrs myObj = new PureStackInstrs();
    protected static OperandStack stack = null;
    protected static MethodInfo currentMethod = null;
    
    protected static final VerificationFrameFactory frameFactory =
            VerificationPlaceHolder.getInstanceOf().getFactory();

    protected PureStackInstrs() {
    }

    protected static void init(OperandStack stack_,
            MethodInfo currentMethod_) {
        stack = stack_;
        currentMethod = currentMethod_;
    }

    public static PureStackInstrs getInstanceOf(OperandStack stack,
            MethodInfo currentMethod) {
        init(stack, currentMethod);
        return myObj;
    }

    protected void pushExecute(VerificationInstruction inst) {
        int opcode = inst.getOpCode();
        if (opcode == JavaInstructionsOpcodes.SIPUSH) {
            pushInstruction(stack, Type.SHORT,
                    inst.getOperandsData().intValueUnsigned());
        } else if (opcode == JavaInstructionsOpcodes.BIPUSH) {
            pushInstruction(stack, Type.BYTE,
                    inst.getOperandsData().intValueUnsigned());
        } else {
            throw new VerifyErrorExt(Messages.INVALID_BYTECODE + inst);
        }
    }

    protected void popExecute(VerificationInstruction inst) {
        int opcode = inst.getOpCode();
        if (opcode == JavaInstructionsOpcodes.POP) {
            popInstruction(stack, inst);
        } else if (opcode == JavaInstructionsOpcodes.POP2) {
            pop2Instruction(stack, inst);
        } else {
            throw new VerifyErrorExt(Messages.INVALID_BYTECODE + inst);
        }
    }

    /**
     * implements pop and pop2
     * type ==> ...
     * or
     * ..., type1, type2 ==> ...
     */
    protected void popInstruction(OperandStack stack,
            VerificationInstruction currentInstr) {
        Type type = stack.pop();
    }

    protected void pop2Instruction(OperandStack stack,
            VerificationInstruction currentInstr) {
        Type type = stack.pop();
        if (!type.isDoubleOrLong()) {
            type = stack.pop();
        }

    }

    protected void pushInstruction(OperandStack stack, int type, Object value) {
        Type typeObj = frameFactory.createType(type);
        typeObj.value = value;
        stack.push(typeObj);
    }

    protected void dupExecute(VerificationInstruction inst) {
        int opcode = inst.getOpCode();
        if (opcode == JavaInstructionsOpcodes.DUP) {
            dupInstr(stack, 1, 0, inst);
        } else if (opcode == JavaInstructionsOpcodes.DUP_X1) {
            dupInstr(stack, 1, 1, inst);
        } else if (opcode == JavaInstructionsOpcodes.DUP_X2) {
            dupInstr(stack, 1, 2, inst);
        } else if (opcode == JavaInstructionsOpcodes.DUP2) {
            dupInstr(stack, 2, 0, inst);
        } else if (opcode == JavaInstructionsOpcodes.DUP2_X1) {
            dupInstr(stack, 2, 1, inst);
        } else if (opcode == JavaInstructionsOpcodes.DUP2_X2) {
            dupInstr(stack, 2, 2, inst);
        } else {
            throw new VerifyErrorExt(Messages.INVALID_BYTECODE + inst);
        }
    }

    /**
     * dup (when x = 0, dup = 1)
     * ..., value ==> ..., value, value
     *
     * dup2 (when x = 0, dup = 2)
     * ..., value2, value1 ==> ..., value2, value1, value2, value1
     *
     * dup_x1 (when x = 1, dup = 1)
     * .., value2, value1  ==> ..., value1, value2, value1 (no value of category-2)
     *
     * dup_x2 (when x = 2, dup = 1), It takes following two forms automatically
     * ..., value3, value2, value1 ==> ..., value1, value3, value2, value1 (no value of category-2)
     * ..., value2, value1  ==> ..., value1, value2, value1 (value2 is of category-2)
     *
     * dup2_x1 (when x = 1, dup =2) It takes following two forms automatically
     * ..., value3, value2, value1  ==> ..., value2, value1, value3, value2, value1 (no value of category-2)
     * ..., value2, value1  ==> ..., value1, value2, value1 (value 2 is of category-2)
     *
     * dup2_x2 (when x = 2, dup =2) It takes following four forms automatically
     * ..., value4, value3, value2, value1  ==> ..., value2, value1, value4, value3, value2, value1(no value of category-2)
     * ..., value3, value2, value1  ==> ..., value1, value3, value2, value1  (only value1 is of category-2)
     * ..., value3, value2, value1  ==> ..., value2, value1, value3, value2, value1 (only value3 is of category-2)
     * ..., value2, value1  ==> ..., value1, value2, value1 (both value1 and value2 are of category-2)
     */
    protected void dupInstr(OperandStack stack, int dup, int x,
            VerificationInstruction currentInstr) {
        Vector values = new Vector();
        Type value = null;
        for (int loop = 0; loop
                < x + dup; loop++) {
            value = stack.pop();
            //in case of x = 0 and dup = 1, nothing should be of category 2 : DUP
            //in case of x=1 and dup = 1, nothing should be of category 2 : DUP_X1
            //in case of x=2 and dup = 1, value1 should not be of category 2 : DUP_X2
            //in case of x = 0 and dup 2, a value may or may not be of category 2: DUP2
            //in case of x =1 and dup= 2 value2 should not be of category 2: DUP2_X1
            //in case of x =2 and dup = 2 every value could be of category 2: DUP2_X2
            if (((x == 0 && dup == 1) || (x == 1 && dup == 1)
                    || (x == 2 && dup == 1 && loop == 0)
                    || (x == 1 && dup == 2 && loop == 1))
                    && value.isDoubleOrLong()) {
                throw new VerifyErrorExt(Messages.STACK_INVALID);
            }
            //long and double are category 2
            if (value.isDoubleOrLong()) {
                loop++;
            }
            if (loop > x + dup) {
                throw new VerifyErrorExt(Messages.STACK_INVALID);
            }
            values.addElement(value);
        }
        //duplicating part
        for (int loop = dup - 1; loop >= 0; loop--) {
            stack.push((Type) values.elementAt(loop)); //value1 was pop first hence will be at index 0
            //long and double are category 2
            if (value.isDoubleOrLong()) {
                loop--;
            }
        }
        //now push back all values including duplicated values set
        for (int loop = values.size() - 1; loop >= 0; loop--) {
            stack.push((Type) values.elementAt(loop));
        }

    }

    /**
     *  ..., value2, value1 ==> ..., value1, value2
     */
    protected void swapInstruction() {
        Type val1 = stack.pop(); //val1 is on the top of stack
        Type val2 = stack.pop();
        stack.push(val1);
        stack.push(val2); //now val2 is on the top of stack.
    }
}
