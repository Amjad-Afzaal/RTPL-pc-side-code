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
import takatuka.verifier.logic.factory.*;

/**
 * <p>Title: </p>
 * <p>Description:
 * To verifiy load and store related instructions.
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class LoadAndStoreInstrs {

    protected static OperandStack stack = null;
    protected static LocalVariables localVar = null;
    protected static MethodInfo currentMethod = null;
    protected static Vector methodCallingParameters = null;
    private static final LoadAndStoreInstrs myObj = new LoadAndStoreInstrs();

    /**
     * constructor is private
     */
    protected LoadAndStoreInstrs() {
    }

    protected static void init(Frame frame, MethodInfo currentMethod_,
            Vector methodCallingParameters_) {
        stack = frame.getOperandStack();
        localVar = frame.getLocalVariables();
        currentMethod = currentMethod_;
        methodCallingParameters = methodCallingParameters_;
    }

    public static LoadAndStoreInstrs getInstanceOf(Frame frame,
            MethodInfo currentMethod,
            Vector methodCallingParameters) {
        init(frame, currentMethod, methodCallingParameters);
        return myObj;
    }

    protected void loadArrayExecute(VerificationInstruction inst) {
        boolean isReference = false;
        int opcode = inst.getOpCode();
        int type = -1;
        if (opcode == JavaInstructionsOpcodes.AALOAD) {
            isReference = true;
        } else if (opcode == JavaInstructionsOpcodes.BALOAD) {
            type = Type.BYTE;
        } else if (opcode == JavaInstructionsOpcodes.SALOAD) {
            type = Type.SHORT;
        } else if (opcode == JavaInstructionsOpcodes.CALOAD) {
            type = Type.CHAR;
        } else if (opcode == JavaInstructionsOpcodes.IALOAD) {
            type = Type.INTEGER;
        } else if (opcode == JavaInstructionsOpcodes.DALOAD) {
            type = Type.DOUBLE;
        } else if (opcode == JavaInstructionsOpcodes.FALOAD) {
            type = Type.FLOAT;
        } else if (opcode == JavaInstructionsOpcodes.LALOAD) {
            type = Type.LONG;
        } else {
            throw new VerifyErrorExt(Messages.INVALID_BYTECODE + inst);
        }
        loadArrayInstruction(inst, type, isReference, inst);
    }

    protected void loadVarExecute(VerificationInstruction inst) {
        int opcode = inst.getOpCode();
        boolean isReference = false;
        int index = -1;
        int type = 1;
        if (opcode == JavaInstructionsOpcodes.LOAD_REFERENCE) { //reference load
            isReference = true;
        } else if (opcode == JavaInstructionsOpcodes.LOAD_LONG_DOUBLE) {
            type = Type.LONG;
        } else if (opcode == JavaInstructionsOpcodes.LOAD_BYTE_BOOLEAN) {
            type = Type.BOOLEAN;
        } else if (opcode == JavaInstructionsOpcodes.LOAD_INT_FLOAT) {
            type = Type.INTEGER;
        } else if (opcode == JavaInstructionsOpcodes.LOAD_SHORT_CHAR) {
            type = Type.SHORT;
        } else {
            throw new VerifyErrorExt(Messages.INVALID_BYTECODE + inst);
        }
        loadVarInstruction(inst, type, isReference, index);

    }

    protected void storeArrayExecute(VerificationInstruction inst) {
        boolean isReference = false;
        int type = -1;
        int opcode = inst.getOpCode();
        if (opcode == JavaInstructionsOpcodes.AASTORE) {
            isReference = true;
        } else if (opcode == JavaInstructionsOpcodes.BASTORE) {
            type = Type.BYTE;
        } else if (opcode == JavaInstructionsOpcodes.CASTORE) {
            type = Type.CHAR;
        } else if (opcode == JavaInstructionsOpcodes.SALOAD) {
            type = Type.SHORT;
        } else if (opcode == JavaInstructionsOpcodes.IASTORE) {
            type = Type.INTEGER;
        } else if (opcode == JavaInstructionsOpcodes.DASTORE) {
            type = Type.DOUBLE;
        } else if (opcode == JavaInstructionsOpcodes.FASTORE) {
            type = Type.FLOAT;
        } else if (opcode == JavaInstructionsOpcodes.LASTORE) {
            type = Type.LONG;
        } else if (opcode == JavaInstructionsOpcodes.SASTORE) {
            type = Type.SHORT;
        } else {
            throw new VerifyErrorExt(Messages.INVALID_BYTECODE + inst);
        }
        storeArrayInstruction(inst, type, isReference, inst);
    }

    protected void storeVarExecute(VerificationInstruction inst) {
        int opcode = inst.getOpCode();
        boolean isReference = false;
        int index = -1;
        int type = 1;
        if (opcode == JavaInstructionsOpcodes.STORE_REFERENCE) { //reference store
            isReference = true;
        } else if (opcode == JavaInstructionsOpcodes.STORE_LONG_DOUBLE) {
            type = Type.LONG;
        } else if (opcode == JavaInstructionsOpcodes.STORE_BYTE_BOOLEAN) {
            type = Type.BOOLEAN;
        } else if (opcode == JavaInstructionsOpcodes.STORE_INT_FLOAT) {
            type = Type.INTEGER;
        } else if (opcode == JavaInstructionsOpcodes.STORE_SHORT_CHAR) {
            type = Type.SHORT;
        } else {
            throw new VerifyErrorExt(Messages.INVALID_BYTECODE + inst);
        }
        storeVarInstruction(inst, type, isReference, index);
    }

    /**
     *   blockSize, int (i) ==> blockSize
     */
    protected void loadArrayInstruction(VerificationInstruction inst, int type,
            boolean isReference, VerificationInstruction instr) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        Type index = stack.pop();
        Type arrayref = stack.pop();
        stack.push(frameFactory.createType(type, isReference, 0));
    }

    /**
     * blockSize =: VGet(operand)
     *     ==> blockSize
     */
    protected void loadVarInstruction(VerificationInstruction inst, int type,
            boolean isReference, int index) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        Type loadType = null;
        if (index == -1) {
            index = inst.getOperandsData().intValueUnsigned();
        }
        Type typeShouldBe = frameFactory.createType(type, isReference, -1);
        loadType = localVar.get(index);
        if (!Type.isCompatableTypes(typeShouldBe, loadType)) {
            throw new VerifyErrorExt(Messages.STACK_INVALID + " " + inst);
        }
        stack.push((Type) loadType.clone());
    }

    protected void storeVarInstruction(VerificationInstruction inst, int type,
            boolean isReference, int index) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        if (index == -1) {
            index = inst.getOperandsData().intValueUnsigned();
        }

        Type storeType = stack.pop();
        Type shouldBeType = frameFactory.createType(type, isReference, -1);
        if (!Type.isCompatableTypes(storeType, shouldBeType)) {
            throw new VerifyErrorExt(Messages.STACK_INVALID + ", " + inst + ", "
                    + storeType + ", " + shouldBeType);
        }
        localVar.set(index, storeType);
    }

    /**
     *    ... ,arrayref, index, value ==> ...
     *    VSet(value (index)
     */
    protected void storeArrayInstruction(VerificationInstruction inst, int type,
            boolean isReference, VerificationInstruction instr) {

        //pop values
        Type value = stack.pop();
        //should be int compatable
        Type index = stack.pop();
        Type arrayref = stack.pop();

        if (!index.isIntOrShortOrByteOrBooleanOrCharType()
                || (value.isReference() != isReference)
                || (!value.isIntOrShortOrByteOrBooleanOrCharType()
                && value.getBlocks() != Type.getBlocks(type, isReference))) {
            throw new VerifyErrorExt(Messages.STACK_INVALID);
        }
    }
}
