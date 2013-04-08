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
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.constants.*;
import takatuka.verifier.dataObjs.*;
import takatuka.verifier.dataObjs.attribute.*;
import takatuka.verifier.logic.*;
import takatuka.verifier.logic.exception.*;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;
import takatuka.classreader.logic.util.*;
import takatuka.optimizer.cpGlobalization.logic.util.Oracle;
import takatuka.verifier.logic.factory.*;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class MiscInstrs {

    private static final MiscInstrs myObj = new MiscInstrs();
    protected static Vector<Long> nextPossibleInstructionsIds = null;
    protected static OperandStack stack = null;
    protected static LocalVariables localVar = null;
    protected static MethodInfo currentMethodStatic = null;
    protected static final GlobalConstantPool pOne = GlobalConstantPool.getInstanceOf();
    protected static int currentPC = -1;
    protected static HashSet<Type> returnTypes = null;

    protected MiscInstrs() {
    }

    protected static void init(Vector<Long> nextPossibleInstructionsIds_,
            OperandStack stack_, MethodInfo currentMethodStatic_, LocalVariables localVar_,
            int currentPC_, HashSet<Type> returnTypes_) {
        nextPossibleInstructionsIds = nextPossibleInstructionsIds_;
        currentMethodStatic = currentMethodStatic_;
        localVar = localVar_;
        stack = stack_;
        currentPC = currentPC_;
        returnTypes = returnTypes_;

    }

    public static MiscInstrs getInstanceOf(Vector<Long> nextPossibleInstructionsIds,
            OperandStack stack, MethodInfo currentMethodStatic,
            LocalVariables localVar, int currentPC, HashSet<Type> returnTypes) {
        init(nextPossibleInstructionsIds, stack, currentMethodStatic,
                localVar, currentPC, returnTypes);
        return myObj;
    }

    protected void A2BExecute(VerificationInstruction inst) {
        int typeA = -1;
        int typeB = -1;
        if ((inst.getOpCode() >= JavaInstructionsOpcodes.I2L
                && inst.getOpCode() <= JavaInstructionsOpcodes.I2D)
                || (inst.getOpCode() >= JavaInstructionsOpcodes.I2B
                && inst.getOpCode() <= JavaInstructionsOpcodes.I2S)) {
            typeA = Type.INTEGER;
            if (inst.getOpCode() == JavaInstructionsOpcodes.I2L) {
                typeB = Type.LONG;
            } else if (inst.getOpCode() == JavaInstructionsOpcodes.I2F) {
                typeB = Type.FLOAT;
            } else if (inst.getOpCode() == JavaInstructionsOpcodes.I2D) {
                typeB = Type.DOUBLE;
            } else if (inst.getOpCode() == JavaInstructionsOpcodes.I2B) {
                typeB = Type.BYTE;
            } else if (inst.getOpCode() == JavaInstructionsOpcodes.I2S) {
                typeB = Type.SHORT;
            } else if (inst.getOpCode() == JavaInstructionsOpcodes.I2C) {
                typeB = Type.CHAR;
            }
        } else if (inst.getOpCode() >= JavaInstructionsOpcodes.F2I
                && inst.getOpCode() <= JavaInstructionsOpcodes.F2D) {
            typeA = Type.FLOAT;
            if (inst.getOpCode() == JavaInstructionsOpcodes.F2I) {
                typeB = Type.INTEGER;
            } else if (inst.getOpCode() == JavaInstructionsOpcodes.F2L) {
                typeB = Type.LONG;
            } else if (inst.getOpCode() == JavaInstructionsOpcodes.F2D) {
                typeB = Type.DOUBLE;
            }
        } else if (inst.getOpCode() >= JavaInstructionsOpcodes.D2I
                && inst.getOpCode() <= JavaInstructionsOpcodes.D2F) {
            typeA = Type.DOUBLE;
            if (inst.getOpCode() == JavaInstructionsOpcodes.D2I) {
                typeB = Type.INTEGER;
            } else if (inst.getOpCode() == JavaInstructionsOpcodes.D2L) {
                typeB = Type.LONG;
            } else if (inst.getOpCode() == JavaInstructionsOpcodes.D2F) {
                typeB = Type.FLOAT;
            }
        } else if (inst.getOpCode() >= JavaInstructionsOpcodes.L2I
                && inst.getOpCode() <= JavaInstructionsOpcodes.L2D) {
            typeA = Type.LONG;
            if (inst.getOpCode() == JavaInstructionsOpcodes.L2I) {
                typeB = Type.INTEGER;
            } else if (inst.getOpCode() == JavaInstructionsOpcodes.L2D) {
                typeB = Type.DOUBLE;
            } else if (inst.getOpCode() == JavaInstructionsOpcodes.L2F) {
                typeB = Type.FLOAT;
            }
        } else {
            throw new VerifyErrorExt(Messages.INVALID_BYTECODE + inst);
        }
        AtoBInstruction(inst, typeA, typeB);
    }

    protected Type methodReturnType(VerificationInstruction instr) {
        int opcode = instr.getOpCode();
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        boolean isRef = false;
        int type = -1;
        if (opcode == JavaInstructionsOpcodes.IRETURN) {
            type = Type.INTEGER;
        } else if (opcode == JavaInstructionsOpcodes.FRETURN) {
            type = Type.FLOAT;
        } else if (opcode == JavaInstructionsOpcodes.LRETURN) {
            type = Type.LONG;
        } else if (opcode == JavaInstructionsOpcodes.DRETURN) {
            type = Type.DOUBLE;
        } else if (opcode == JavaInstructionsOpcodes.ARETURN) {
            isRef = true;
        } else if (opcode == JavaInstructionsOpcodes.RETURN) {
            type = Type.VOID;
        } else {
            Miscellaneous.printlnErr("error #7923");
            Miscellaneous.exit();
        }
        return frameFactory.createType(type, isRef, -1);
    }

    protected void returnExecute(VerificationInstruction inst) {
        Oracle oracle = Oracle.getInstanceOf();
        String desc = oracle.methodOrFieldDescription(currentMethodStatic, pOne);
        Type type = methodReturnType(inst);
        if (type.isReference()) {
            returnInstruction(-1, stack, true, inst);
        } else {
            returnInstruction(type.getType(), stack, false, inst);
        }
        //that should be the last instruction of the method
        nextPossibleInstructionsIds.remove(0);
    }

    protected void constExecute(VerificationInstruction inst) {
        int opcode = inst.getOpCode();
        int type = -1;
        boolean isReference = false;
        Object value = null;
        if (opcode >= JavaInstructionsOpcodes.ICONST_M1
                && opcode <= JavaInstructionsOpcodes.ICONST_5) {
            type = Type.INTEGER;
            if (opcode == JavaInstructionsOpcodes.ICONST_M1) {
                value = new Integer(-1);
            } else {
                value = new Integer(opcode - JavaInstructionsOpcodes.ICONST_0);
            }
        } else if (opcode == JavaInstructionsOpcodes.LCONST_0
                || opcode == JavaInstructionsOpcodes.LCONST_1) {
            type = Type.LONG;
            value = new Integer(opcode - JavaInstructionsOpcodes.LCONST_0);
        } else if (opcode == JavaInstructionsOpcodes.FCONST_0
                || opcode == JavaInstructionsOpcodes.FCONST_1
                || opcode == JavaInstructionsOpcodes.FCONST_2) {
            type = Type.FLOAT;
            value = new Float(opcode - JavaInstructionsOpcodes.FCONST_0);
        } else if (opcode == JavaInstructionsOpcodes.DCONST_0
                || opcode == JavaInstructionsOpcodes.DCONST_1) {
            type = Type.DOUBLE;
            value = new Double(opcode - JavaInstructionsOpcodes.DCONST_0);
        } else if (opcode == JavaInstructionsOpcodes.ACONST_NULL) {
            isReference = true;
            type = Type.NULL;
        } else {
            throw new VerifyErrorExt(Messages.INVALID_BYTECODE + inst);
        }
        constInstruction(stack, type, isReference, value);
    }

    protected void negExecute(VerificationInstruction inst) {
        int opcode = inst.getOpCode();
        int type = -1;
        if (opcode == JavaInstructionsOpcodes.DNEG) {
            type = Type.DOUBLE;
        } else if (opcode == JavaInstructionsOpcodes.FNEG) {
            type = Type.FLOAT;
        } else if (opcode == JavaInstructionsOpcodes.INEG) {
            type = Type.INTEGER;
        } else if (opcode == JavaInstructionsOpcodes.LNEG) {
            type = Type.LONG;
        } else {
            throw new VerifyErrorExt(Messages.INVALID_BYTECODE + inst);
        }
        negInstruction(stack, type, inst);
    }

    /**
     *  ..., objectref  ==> ..., objectref
     */
    protected void checkCastInstruction(VerificationInstruction inst) {
        Type type = stack.peep();
        if (!type.isReference()) {
            throw new VerifyError(Messages.STACK_INVALID + " " + inst);
        }
        //do nothing more for the time being
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
    protected void AtoBInstruction(VerificationInstruction inst, int firstType,
            int secondType) {
        Type typeObj = stack.pop();

        if (typeObj.getBlocks() != Type.getBlocks(firstType, false)) {
//todo            throw new VerifyErrorExt(Messages.STACK_INVALID);
        }

        Object value = typeObj.value;
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        Type secondTypeObj = frameFactory.createType(secondType);
        if (value != null) {
            //could convert here :)
            //could set here too
            //secondTypeObj.value = value;
        }
        stack.push(secondTypeObj);
    }

    /**
     * ==> B
     * Where the B could be a Type
     */
    protected void constInstruction(OperandStack stack, int type,
            boolean isReference, Object value) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        //if it is a reference than it will always be null
        Type typeObj = frameFactory.createType(type, isReference, 0);
        typeObj.value = value;
        stack.push(typeObj);
    }

    /**
     * type ==> [empty]
     */
    protected void returnInstruction(int typeToReturn, OperandStack stack,
            boolean isReference, VerificationInstruction instr) {
        Type returnValue = null;
        if (typeToReturn != Type.VOID) {
            returnTypes.add(stack.pop());
        }
        stack.clear();
    }

    /**
     * type ==> type
     */
    protected void negInstruction(OperandStack stack, int type,
            VerificationInstruction instr) {
        Type val = stack.pop();
        if (val.getBlocks() != Type.getBlocks(type, false)
                && !val.isIntOrShortOrByteOrBooleanOrCharType()) {
            throw new VerifyErrorExt(Messages.STACK_INVALID + ", val1= "
                    + val + ", val2=" + Type.typeToString(type));
        }
        //negating could be done here also peep could be used here (instead of push/pop)
        stack.push(val);
    }

    /**
     * implements both monitorenter and monitorexit instructions.
     * .., ref  ==>...
     */
    protected void monitorInstruction() {
        if (!stack.pop().isReference()) {
            throw new VerifyErrorExt(Messages.STACK_INVALID);
        }

    }

    /**
     * ref ==> int
     * Get length of array
     */
    protected void arraylengthInstruction() {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        Type ref = stack.pop();
        if (!ref.isArrayReference()) {
            //todo throw new VerifyErrorExt(Messages.STACK_INVALID + " not array typed reference");
        }

        stack.push(frameFactory.createType(Type.INTEGER));
    }

    

    /**
     * operands: index (signed byte), const (signed byte)
     * value = VGet(index)
     * value should be of type int
     * increment that value by const (ignore this step in validation).
     */
    protected void iincInstruction(VerificationInstruction inst, boolean isWide) {
        try {
            Un data = (Un) inst.getOperandsData().clone();
            int index = Un.cutBytes(1 + (isWide ? 1 : 0), data).intValueUnsigned();
            Type value = localVar.get(index);
            //no need for convertion here. As currently all local variables store
            //instructions has the type convertions.
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }

    }

    /**
     * operand: index
     * jump =: VGet(index) must be of type returnAddress (a)
     *
     */
    protected void retInstruction(VerificationInstruction inst) {
        int index = 0;
        index = inst.getOperandsData().intValueUnsigned();
        Type retAdd = (Type) localVar.get(index);
        if (retAdd.getType() != Type.RETURN_ADDRESS) {
            throw new VerifyErrorExt(Messages.LOCALVAR_INVALID);
        }
        nextPossibleInstructionsIds.clear();
        nextPossibleInstructionsIds.add((Long)retAdd.value);
    }

    /**
     *
     * .., ref ==>  ..., int
     * Operand: int index (constantpool index for classinfo)
     */
    protected void instanceofInstruction(VerificationInstruction inst) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        Type ref = stack.pop();
        if (!ref.isReference()) {
            throw new VerifyErrorExt(Messages.STACK_INVALID);
        }

        int index = inst.getOperandsData().intValueUnsigned();
        if (index == 0 || index >= pOne.getCurrentSize(TagValues.CONSTANT_Class)) {
            throw new VerifyErrorExt(Messages.INVALID_CLASS_ACCESS);
        }

        stack.push(frameFactory.createType(Type.INTEGER));
    }
}
