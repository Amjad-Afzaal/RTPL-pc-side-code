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
package takatuka.verifier.dataObjs.attribute;

import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.attribute.*;
import takatuka.classreader.logic.constants.JavaInstructionsOpcodes;
import takatuka.classreader.logic.factory.*;
import takatuka.verifier.dataObjs.*;
import takatuka.verifier.logic.factory.*;
import takatuka.classreader.logic.util.*;
import takatuka.optimizer.bytecode.replacer.dataObjs.attributes.*;
import java.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 *
 * Every Instruction will have a OperandStack and localVariables.
 * Furthermore, it als has changebit
 * @author Faisal Aslam
 * @version 1.0
 */
public class VerificationInstruction extends IRInstruction {

    private OperandStack enteringStack = null;
    private LocalVariables enteringLocalVar = null;
    private boolean changeBit = false;
    //per documentation it become true if the instruction is executed once.
    private boolean visited = false;
    //use only in case of wide instruction
    private VerificationInstruction underLineUnWideInstrction = null;
    private FactoryFacade verificationFactory = FactoryPlaceholder.getInstanceOf().getFactory();
    private HashMap<Long, VerificationInstruction> nextInstrSets = new HashMap<Long, VerificationInstruction>();
    private HashMap<Long, VerificationInstruction> prevInstrSets = new HashMap<Long, VerificationInstruction>();

    public VerificationInstruction(int opcode, Un operands, CodeAtt codeAtt) {
        super(opcode, operands, codeAtt);
    }

    public void createStackLVForInstr(CodeAtt codeAtt) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        if (enteringStack == null) {
            enteringStack = frameFactory.createOperandStack(getCurrentCodeAttMaxStack(codeAtt));
            enteringLocalVar = frameFactory.createLocalVariables(getCurrentCodeAttMaxLocal(codeAtt));
        }
    }

    public void clear() {
        visited = false;
        changeBit = false;
        enteringStack = null;
        enteringLocalVar = null;
    }

    /**
     * set if insturction proceeding this instruction. It could be the subsequent
     * appearing in the bytecode array or it could be any other instruction(s) if current
     * instruction is a jumb instruction (e.g. IFNE, GOTO etc).
     *
     * @param nextInstrVec : idss of next instructions.
     * @param allInstrs
     */
    public void addNextInstrsToBeExecutedRecord(Vector<Long> nextInstrVec, Vector allInstrs) {
        try {
            Iterator<Long> it = nextInstrVec.iterator();
            while (it.hasNext()) {
                long id = it.next();
                VerificationInstruction nextInstr = null;
                nextInstr = (VerificationInstruction) MethodInfo.findInstruction(id, allInstrs);
                addNextInstrToBeExecutedRecord(nextInstr);
            }
        } catch (Exception d) {
            d.printStackTrace();
            System.exit(1);
        }
    }

    /**
     *
     * @param nextInstr
     */
    private void addNextInstrToBeExecutedRecord(VerificationInstruction nextInstr) {
        nextInstrSets.put(nextInstr.getInstructionId(), nextInstr);
        nextInstr.prevInstrSets.put(this.getInstructionId(), this);
    }

    /**
     *
     * @return
     */
    public Vector<VerificationInstruction> getNextInstrsToBeExecutedRecord() {
        return new Vector(nextInstrSets.values());
    }

    /**
     * returns the previous immediate instruction(s) that would have been executed before
     * this instruction.
     * @return
     */
    public Vector<VerificationInstruction> getPreviousInstrsToBeExecutedRecord() {
        return new Vector(prevInstrSets.values());
    }

    private int getCurrentCodeAttMaxStack(CodeAtt currentCodeAttr) {
        if (currentCodeAttr == null) {
            return 0;
        } else {
            return currentCodeAttr.getMaxStack().intValueUnsigned();
        }
    }

    private static int getCurrentCodeAttMaxLocal(CodeAtt currentCodeAttr) {
        if (currentCodeAttr == null) {
            return 0;
        } else {
            return currentCodeAttr.getMaxLocals().intValueUnsigned();
        }
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public void setChangeBit(boolean changeBit) {
        this.changeBit = changeBit;
    }

    public boolean getChangeBit() {
        return changeBit;
    }

    public OperandStack getOperandStack() {
        return enteringStack;
    }

    private void setOperandStack(OperandStack stack) {
        this.enteringStack = (OperandStack) stack.clone();
    }

    public LocalVariables getLocalVariables() {
        return enteringLocalVar;
    }

    public void setLocalVariables(LocalVariables localVar) {
        this.enteringLocalVar = (LocalVariables) localVar.clone();
    }

    public void set(LocalVariables localVar, OperandStack stack) {
        setLocalVariables(localVar);
        setOperandStack(stack);
    }

    public boolean merge(VerificationInstruction lastInstr,
            LocalVariables toMergeLoc, OperandStack toMergeStack) {
        boolean mergeStack = merge(toMergeStack);
        boolean mergeLoc = merge(toMergeLoc, lastInstr);
        if (mergeStack || mergeLoc) {
            return true;
        }
        return false;
    }

    public boolean merge(LocalVariables toMergeWith, VerificationInstruction lastInstr) {
        if (lastInstr != null
                && lastInstr.getOpCode() == JavaInstructionsOpcodes.RET) {
            return enteringLocalVar.mergeWhenLastInstrWasRET(toMergeWith);
        } else {
            return enteringLocalVar.merge(toMergeWith);
        }
    }

    public boolean merge(OperandStack toMergeWith) {
        return enteringStack.merge(toMergeWith);
    }

    public VerificationInstruction getWideUnderlineInstruction() {
        if (underLineUnWideInstrction != null) {
            return underLineUnWideInstrction;
        }
        Un operand = getOperandsData();
        int underLineOpCode = 0;
        try {
            Un underLineInst = Un.cutBytes(1, operand);
            underLineOpCode = underLineInst.intValueUnsigned();
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        try {
            underLineUnWideInstrction = (VerificationInstruction) verificationFactory.createInstruction(underLineOpCode,
                    operand, getParentCodeAtt());
        } catch (Exception ex) {
            ex.printStackTrace();
            Miscellaneous.exit();
        }
        return underLineUnWideInstrction;
    }

    public String toStringSpecial() {
        return getOffSet() + ":" + super.toString() + ", stack =" + getOperandStack()
                + ", local-var =" + getLocalVariables();
    }
}
