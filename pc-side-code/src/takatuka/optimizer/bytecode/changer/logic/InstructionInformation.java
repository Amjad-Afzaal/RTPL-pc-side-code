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
package takatuka.optimizer.bytecode.changer.logic;

import takatuka.classreader.dataObjs.attribute.*;
import java.util.*;
import takatuka.classreader.logic.constants.*;

/**
 * <p>Title: </p>
 *
 * <p>Description:
 * This files tell the information about instruction.
 * 1: it tells that what is the type of instruction.
 * a) stack only
 * b) local Variable only
 * c) neutral
 * d) stack plus local Variables
 * 2: it tells that if an instruction has some jump addresses
 *    and return those jump addresses
 * 3: it tells that how much increase or decrease an instruction made in stack
 * current size.
 * </p>
 *
 * @author Faisal Aslam
 * @version 1.0
 */
public class InstructionInformation {

    public static final int STACK_ONLY_INST = 1;
    public static final int LOCALVAR_ONLY_INST = 2;
    public static final int LOCVAR_AND_STACK_INST = 3;
    public static final int NEUTRAL_INST = 0;
    private Instruction inst = null;

    private InstructionInformation(Instruction inst) {
        this.inst = inst;
    }

    /*    public static InstructionInformation getInstanceOf() {
    return instInfo;
    }*/
    /**
     * returns STACK_ONLY_INST if inst change/check stack only.
     * return LOCALVAR_ONLY_INST if inst change/check local variables only
     * return LOCVAR_AND_STACK_INST if inst change/check local variables as well as stack
     * return NEUTRAL_INST if inst do not change/check local variable or stack
     * @return int
     */
    public int instructionType() {
        return NEUTRAL_INST;
    }


    /**
     * returns the stack changed by an instruction. The values return could be
     * negative or positive.
     * 
     * In case of athorw where it empty stack and put a reference it return -10 
     * which has special meaning that stack is now of size exactly one
     * @return int
     */
    public int getStackChange() {
        //dup2_x1 and dup2_x2 are missing
        //all invoke function are missing
        //wide is also missing
        //multianewarray

        if (inst.getOpCode() == JavaInstructionsOpcodes.DUP2_X1 ||
                inst.getOpCode() == JavaInstructionsOpcodes.DUP2_X2 ||
                inst.getOpCode() == JavaInstructionsOpcodes.WIDE ||
                (inst.getOpCode() >= JavaInstructionsOpcodes.INVOKEVIRTUAL &&
                inst.getOpCode() <= JavaInstructionsOpcodes.INVOKEINTERFACE) ||
                inst.getOpCode() == JavaInstructionsOpcodes.MULTIANEWARRAY) {
            throw new UnsupportedOperationException();
        }

        if (inst.getOpCode() == JavaInstructionsOpcodes.DUP_X2 ||
                inst.getOpCode() == JavaInstructionsOpcodes.DUP2 ||
                inst.getOpCode() == JavaInstructionsOpcodes.JSR ||
                inst.getOpCode() == JavaInstructionsOpcodes.JSR_W) {
            return 2;
        } else if (inst.getOpCode() >= JavaInstructionsOpcodes.ACONST_NULL &&
                inst.getOpCode() <= JavaInstructionsOpcodes.ALOAD_3 ||
                inst.getOpCode() == JavaInstructionsOpcodes.DUP ||
                inst.getOpCode() == JavaInstructionsOpcodes.DUP_X1 ||
                inst.getOpCode() == JavaInstructionsOpcodes.I2L ||
                inst.getOpCode() == JavaInstructionsOpcodes.I2D ||
                inst.getOpCode() == JavaInstructionsOpcodes.F2L ||
                inst.getOpCode() == JavaInstructionsOpcodes.F2D ||
                inst.getOpCode() == JavaInstructionsOpcodes.GETFIELD ||
                inst.getOpCode() == JavaInstructionsOpcodes.NEW) {
            return 1;
        } else if (inst.getOpCode() >= JavaInstructionsOpcodes.IALOAD &&
                inst.getOpCode() <= JavaInstructionsOpcodes.DSTORE_3 ||
                inst.getOpCode() == JavaInstructionsOpcodes.POP ||
                (inst.getOpCode() >= JavaInstructionsOpcodes.IADD &&
                inst.getOpCode() <= JavaInstructionsOpcodes.DREM) ||
                (inst.getOpCode() >= JavaInstructionsOpcodes.ISHL &&
                inst.getOpCode() <= JavaInstructionsOpcodes.IXOR) ||
                inst.getOpCode() == JavaInstructionsOpcodes.L2I ||
                inst.getOpCode() == JavaInstructionsOpcodes.L2F ||
                inst.getOpCode() == JavaInstructionsOpcodes.D2I ||
                inst.getOpCode() == JavaInstructionsOpcodes.D2F ||
                (inst.getOpCode() >= JavaInstructionsOpcodes.LCMP &&
                inst.getOpCode() <= JavaInstructionsOpcodes.IFLE) ||
                inst.getOpCode() == JavaInstructionsOpcodes.TABLESWITCH ||
                inst.getOpCode() == JavaInstructionsOpcodes.LOOKUPSWITCH ||
                inst.getOpCode() == JavaInstructionsOpcodes.GETSTATIC ||
                inst.getOpCode() == JavaInstructionsOpcodes.PUTSTATIC ||
                inst.getOpCode() == JavaInstructionsOpcodes.MONITORENTER ||
                inst.getOpCode() == JavaInstructionsOpcodes.MONITOREXIT ||
                inst.getOpCode() == JavaInstructionsOpcodes.IFNULL ||
                inst.getOpCode() == JavaInstructionsOpcodes.IFNONNULL) {
            return -1;
        } else if ((inst.getOpCode() >= JavaInstructionsOpcodes.IASTORE &&
                inst.getOpCode() <= JavaInstructionsOpcodes.SASTORE) ||
                inst.getOpCode() == JavaInstructionsOpcodes.PUTFIELD) {
            return -3;
        } else if (inst.getOpCode() == JavaInstructionsOpcodes.POP2 ||
                (inst.getOpCode() >= JavaInstructionsOpcodes.IF_ICMPEQ &&
                inst.getOpCode() <= JavaInstructionsOpcodes.IF_ACMPNE)) {
            return -2;
        } else if (inst.getOpCode() == JavaInstructionsOpcodes.ATHROW) {
            return -10;
        }
        return 0;
    }

    public Vector getJumpAddresses() {
        return null;
    }
}
