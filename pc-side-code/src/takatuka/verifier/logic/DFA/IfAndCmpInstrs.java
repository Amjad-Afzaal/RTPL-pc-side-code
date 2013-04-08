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
import takatuka.optimizer.bytecode.branchSetter.dataObjs.attributes.*;
import takatuka.verifier.logic.factory.*;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class IfAndCmpInstrs {

    private static final IfAndCmpInstrs myObj = new IfAndCmpInstrs();
    protected static Vector<Long> nextPossibleInstructionsIds = new Vector<Long>();
    protected static OperandStack stack = null;
    protected static MethodInfo currentMethod = null;

    protected IfAndCmpInstrs() {
    }

    protected static void init(Vector<Long> nextPossibleInstructionsIds_,
            OperandStack stack_, MethodInfo currentMethod_) {
        nextPossibleInstructionsIds = nextPossibleInstructionsIds_;
        stack = stack_;
        currentMethod = currentMethod_;
    }

    public static IfAndCmpInstrs getInstanceOf(Vector<Long> nextPossibleInstructionsIds_,
            OperandStack stack_, MethodInfo currentMethod_) {
        init(nextPossibleInstructionsIds_, stack_, currentMethod_);
        return myObj;
    }

    protected void ifExecute(VerificationInstruction inst) {
        int opcode = inst.getOpCode();
        long jumpLocation = -1;
        if (opcode == JavaInstructionsOpcodes.IFEQ
                || opcode == JavaInstructionsOpcodes.IFNE
                || opcode == JavaInstructionsOpcodes.IFLT
                || opcode == JavaInstructionsOpcodes.IFGE
                || opcode == JavaInstructionsOpcodes.IFGT
                || opcode == JavaInstructionsOpcodes.IFLE) {
            jumpLocation = ifCmdInstruction(inst, false, false);
        } else if (opcode == JavaInstructionsOpcodes.IFNONNULL
                || opcode == JavaInstructionsOpcodes.IFNULL) {
            jumpLocation = ifCmdInstruction(inst, true, false);
        } else {
            throw new VerifyErrorExt(Messages.INVALID_BYTECODE + inst);
        }
        nextPossibleInstructionsIds.addElement(jumpLocation);
    }

    protected void ifCmdExecute(VerificationInstruction inst) {
        int opcode = inst.getOpCode();
        long jumpLocation = -1;
        if (opcode >= JavaInstructionsOpcodes.IF_ICMPEQ
                && opcode <= JavaInstructionsOpcodes.IF_ICMPLE) {
            jumpLocation = ifCmdInstruction(inst, false, true);
        } else if (opcode == JavaInstructionsOpcodes.IF_ACMPEQ
                || opcode == JavaInstructionsOpcodes.IF_ACMPNE) {
            jumpLocation = ifCmdInstruction(inst, true, true);
        } else {
            throw new VerifyErrorExt(Messages.INVALID_BYTECODE + inst);
        }
        nextPossibleInstructionsIds.addElement(jumpLocation);
    }

    /**
     * ..., type, type ==> ...
     */
    protected long ifCmdInstruction(VerificationInstruction inst,
            boolean isReference, boolean ifCmd) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        Type val1 = stack.pop();
        Type val2 = frameFactory.createType(Type.INTEGER, isReference, -1);
        if (ifCmd) {
            val2 = stack.pop();
        }
        //if both are references.
        if (isReference && (!val1.isReference() || !val2.isReference())) {
            throw new VerifyErrorExt(Messages.STACK_INVALID + ", value 1=" + val1 + ", value 2=" + val2 + ", " + inst);
        }
        return this.getTargetInstrIds(inst).elementAt(0);
    }

    /**
     * for a source instruction it returns its targets.
     * @param instr
     * @return
     */
    protected Vector<Long> getTargetInstrIds(VerificationInstruction instr) {
        Vector<Long> ret = new Vector<Long>();
        Vector<BranchTarget> bTargetVec = ((BHInstruction) instr).getMyTargets();
        Iterator<BranchTarget> it = bTargetVec.iterator();
        while (it.hasNext()) {
            BranchTarget bTarget = it.next();
            ret.add(bTarget.targetId);
        }
        return ret;
    }

    /**
     * type (v1), type (v2) ==> int
     */
    protected void cmpInstruction(OperandStack stack, int type,
            VerificationInstruction instr) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        Type val2 = stack.pop();
        Type val1 = stack.pop();
        //the values pop will never be integer hence no need for convertion here.
        if (val1.getBlocks() != val2.getBlocks() && val1.getBlocks() != frameFactory.createType(type).getBlocks()) {
            throw new VerifyErrorExt(Messages.STACK_INVALID + " at instr=" + instr);
        }
        if (!Type.isCompatableTypes(val1, val2)) {
            //TODO TODO TODO need casting
            /*
             * || val1.getBlocks() != Type.getBlocks(type, false)
             */
            throw new VerifyErrorExt(Messages.STACK_INVALID + " at instr=" + instr);
        }

        stack.push(frameFactory.createType(Type.INTEGER)); //push result
    }

    protected void cmpExecute(VerificationInstruction inst) {
        int opcode = inst.getOpCode();
        int type = -1;
        if (opcode == JavaInstructionsOpcodes.DCMPG
                || opcode == JavaInstructionsOpcodes.DCMPL) {
            type = Type.DOUBLE;
        } else if (opcode == JavaInstructionsOpcodes.FCMPG
                || opcode == JavaInstructionsOpcodes.FCMPL) {
            type = Type.FLOAT;
        } else if (opcode == JavaInstructionsOpcodes.LCMP) {
            type = Type.LONG;
        } else {
            throw new VerifyErrorExt(Messages.INVALID_BYTECODE + inst);
        }
        cmpInstruction(stack, type, inst);
    }

    protected void jumpExecute(VerificationInstruction inst) {
        boolean jsrInst = false;
        int opcode = inst.getOpCode();
        if (opcode == JavaInstructionsOpcodes.GOTO
                || opcode == JavaInstructionsOpcodes.GOTO_W) {
            //have fun :)
        } else if (opcode == JavaInstructionsOpcodes.JSR
                || opcode == JavaInstructionsOpcodes.JSR_W) {
            jsrInst = true;
        } else {
            throw new VerifyErrorExt(Messages.INVALID_BYTECODE + inst);
        }

        nextPossibleInstructionsIds.remove(0);
        //remove previous one and set this one.
        nextPossibleInstructionsIds.add(jumpInstruction(inst, jsrInst));
    }

    /**
     * saves address of next instruction in stack as returnAddress
     * ... ==> ..., returnAddress
     * returns the address of next instruction to
     * based on operands
     */
    protected long jumpInstruction(VerificationInstruction inst, boolean ifJSR) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        if (ifJSR) {
            Type retAddress = frameFactory.createType(Type.RETURN_ADDRESS);
            //address of next instruction

            retAddress.value = (Long) nextPossibleInstructionsIds.elementAt(0);
            stack.push(retAddress);
        }

        return this.getTargetInstrIds(inst).elementAt(0);
    }

    /**
     * ..., int (index) ==> ...
     */
    protected void switchInstruction(VerificationInstruction inst,
            boolean isLookupSwitch) {
        Type index = stack.pop();
        nextPossibleInstructionsIds.clear();
        nextPossibleInstructionsIds.addAll(this.getTargetInstrIds(inst));
    }
}
