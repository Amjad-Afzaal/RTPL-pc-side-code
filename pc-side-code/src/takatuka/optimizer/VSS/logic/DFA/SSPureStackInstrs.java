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
import takatuka.classreader.logic.constants.JavaInstructionsOpcodes;
import takatuka.verifier.dataObjs.*;
import takatuka.verifier.dataObjs.attribute.VerificationInstruction;
import takatuka.verifier.logic.*;
import takatuka.verifier.logic.exception.*;
import takatuka.verifier.logic.DFA.PureStackInstrs;

/**
 *
 * @author Faisal Aslam
 */
public class SSPureStackInstrs extends PureStackInstrs {

    private static final SSPureStackInstrs myObj = new SSPureStackInstrs();

    protected SSPureStackInstrs() {
    }

    public static SSPureStackInstrs getInstanceOf(OperandStack stack,
            MethodInfo currentMethod) {
        init(stack, currentMethod);
        return myObj;
    }

    /**
     * implements pop and pop2
     * type ==> ...
     * or
     * ..., type1, type2 ==> ...
     */
    @Override
    protected void popInstruction(OperandStack stack,
            VerificationInstruction currentInstr) {
        Type type = stack.pop();
        ConvertTypeUtil ct = ConvertTypeUtil.getInstanceOf(currentMethod);
        if (type.isIntOrShortOrByteOrBooleanOrCharType()) {
            ct.convertTypes(type, 0, currentInstr);
        } else {
            ct.convertTypesToIntegerSize(type, 0, currentInstr);
        }

    }

    @Override
    protected void pop2Instruction(OperandStack stack,
            VerificationInstruction currentInstr) {
        Type type = stack.pop();
        ConvertTypeUtil ct = ConvertTypeUtil.getInstanceOf(currentMethod);

        if (type.isIntOrShortOrByteOrBooleanOrCharType()) {
            ct.convertTypes(type, 0, currentInstr);
        } else if (!type.isDoubleOrLong()) {
            ct.convertTypesToIntegerSize(type, 0, currentInstr);
        }

        if (!type.isDoubleOrLong()) {
            type = stack.pop();

            if (type.isIntOrShortOrByteOrBooleanOrCharType()) {
                ct.convertTypes(type, 1, currentInstr);
            } else {
                ct.convertTypesToIntegerSize(type, 1, currentInstr);
            }
        }

    }

    private void dupInstrConverts(Vector<Type> values,
            VerificationInstruction currentInstr) {
        //check all the vales if any value is integer subtype then convert it into integer.
        ConvertTypeUtil ct = ConvertTypeUtil.getInstanceOf(currentMethod);
        for (int loop = 0; loop < values.size(); loop++) {
            Type type = values.elementAt(loop);

            if (type.getBlocks() < Type.getBlocks(Type.INTEGER, false)) {
                int tobeChanged = Integer.MIN_VALUE;
                if (!type.isReference()) {
                    tobeChanged = type.getType();
                }
                ct.convertTypesSpecial(tobeChanged, loop, currentInstr);
            }
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
    @Override
    protected void dupInstr(OperandStack stack, int dup, int x, VerificationInstruction currentInstr) {
        Vector values = new Vector();
        Type value = null;
        for (int loop = 0; loop < x + dup; loop++) {
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
        dupInstrConverts(values, currentInstr);
        //duplicating part
        for (int loop = howManyToDuplicate(values, dup)-1; loop >= 0; loop--) {
            value = (Type) values.elementAt(loop);
            stack.push(value); //value1 was pop first hence will be at index 0
        }
        //now push back all values including duplicated values set
        for (int loop = values.size() - 1; loop >= 0; loop--) {
            stack.push((Type) values.elementAt(loop));
        }

    }
    
    private int howManyToDuplicate(Vector<Type> values, int dup) {
        int toDup = 0;
        for (int loop = 0; loop < dup; loop ++) {
            Type value = values.elementAt(loop);
            if (value.isDoubleOrLong()) {
                loop++;
            }
            toDup++;
        }
        return toDup;
    }
}
