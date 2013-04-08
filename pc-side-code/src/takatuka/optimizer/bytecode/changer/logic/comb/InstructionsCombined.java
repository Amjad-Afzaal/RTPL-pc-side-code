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
package takatuka.optimizer.bytecode.changer.logic.comb;

import takatuka.optimizer.bytecode.changer.logic.freq.*;
import java.util.*;
import takatuka.classreader.dataObjs.attribute.*;
import takatuka.classreader.dataObjs.*;
import takatuka.optimizer.bytecode.branchSetter.dataObjs.attributes.*;
import takatuka.classreader.logic.util.*;
import takatuka.optimizer.bytecode.changer.logic.CPAndOffSetChangedInstructions;

/**
 * 
 * Description:
 * <p>
 *
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class InstructionsCombined extends InstructionsCombineBase {
    //private String className = null;

    private static final String ARGUMENT_SEPERATOR = "_";
    private Vector simpleInstructionsCache = new Vector();

    public InstructionsCombined() {
        super();
    }

    public InstructionsCombined(int opcode, Un operands, CodeAtt parentCodeAtt) {
        super(opcode, operands, parentCodeAtt);
    }

    /**
     *  return -1;
     * @return
     */
    @Override
    public long getInstructionId() {
        Instruction instr = (Instruction) getSimpleInstructions().firstElement();
        return instr.getInstructionId();
    }

    public int getNumberOfBranchSourceInMe() {
        return numberOfBranchSourcesInMe;
    }

    /**
     * @return true if combination has one branch instruction 
     */
    @Override
    public boolean isBranchTarget() {
        return getTargetInstructionsInMe().size() > 0 ? true : false;
    }

    /**
     * if last instruction in the combination is source then return true otherwise false
     * @return
     */
    @Override
    public boolean isBranchSource() {
        return getTargetInstructionsInMe().size() > 0 ? true : false;
    }

    @Override
    public Vector<BranchTarget> getMyTargets() {
        Vector<BHInstruction> sources = getSourcesInstructionsInMe();
        Vector ret = new Vector();
        for (int loop = 0; loop < sources.size(); loop++) {
            ret.addAll(sources.elementAt(loop).getMyTargets());
        }
        return ret;
    }

    @Override
    public Vector<Integer> getBranchAddresses() {
        Vector<BHInstruction> targets = getTargetInstructionsInMe();
        Vector addresses = new Vector();
        for (int loop = 0; loop < targets.size(); loop++) {
            addresses.addElement(targets.elementAt(loop).getBranchAddresses());
        }
        return addresses;
    }

    @Override
    public void setBranchAddresses(Vector addresses) {
        Vector<BHInstruction> sources = getSourcesInstructionsInMe();
        for (int loop = 0; loop < sources.size(); loop++) {
            sources.elementAt(loop).setBranchAddresses(addresses);
        }
    }

    /**
     * make a imaginary instruction with operand reduction
     * @param inst
     * It is assumed the input instruction is a CP or Offset instruction
     * and it has only one byte operand value (> 255)
     *
     * @return
     */
    public static InstructionsCombined createImaginaryInstrctionWithOperandReduction(Instruction inst) {
        InstructionsCombined comb = new InstructionsCombined();
        try {
            comb.addInstruction(CPAndOffSetChangedInstructions.createVirtualInstrForCPAndOffSet(inst));
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return comb;
    }

    /**
     * combine single instruction's operand into the opcode.
     * @param inst
     * @return
     */
    public static InstructionsCombined createInstructionWithOperandsCombined(Instruction inst) {
        if (inst.getOperandsData() == null || inst.getOperandsData().size() == 0) {
            return null;
        }
        InstructionsCombined comb = new InstructionsCombined();
        try {
            if (inst instanceof InstructionsCombined) {
                return (InstructionsCombined) inst;
            }
            String newMne = inst.getMnemonic() + ARGUMENT_SEPERATOR + inst.getOperandsData();
            inst = (Instruction) inst.clone();
            inst.setMnemonic(newMne);
            //inst.setOpCode(InstructionsCombined.getOpCodeOfNewInstruction(newMne));
            inst.setOperandsData(new Un());
            comb.addInstruction(inst);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return comb;
    }

    public boolean overlap(InstructionsCombined inputCom) {
        return InstructionCombinedOverlap.getInstanceOf().isOverlap(this, inputCom);
    }

    /**
     * if combination has two targets, or a target is not at the start
     * and then it is invalid combination. Furthermore, if a
     * source inside a combination 
     * is pointing to a target inside this instruction then it is invalid 
     * instruction
     * 
     * Following is its logic. 
     * 1) Get all sources and targets using getSourcesInstructionsInMe() and getTargetInstructionsInMe()
     * 2) Check that it has only one target in it.
     * 3) Check that no source in it points to target in it.
     * 4) Check the only target (if exist) is at the start (0 index).
     * 5) some instruction are allowed only at the start. Other are allowed only at the end.
     * 
     * @return
     */
    public boolean isValidCombination() {
        BHInstruction inst = null;
        Vector<BHInstruction> targets = getTargetInstructionsInMe();
        if (targets.size() > 1) {
            return false; //can only have one target
        }
        if (targets.size() == 1) {
            inst = (BHInstruction) instVec.elementAt(0);
            if (inst instanceof InstructionsCombined) {
                inst = (BHInstruction) ((InstructionsCombined) inst).getSimpleInstructions().elementAt(0);
            }
            if (!inst.isBranchTarget()) {
                return false;
            }
        }
        if (!isValidStartEndInstr()) {
            return false;
        }
        return true;
    }

    private boolean isValidStartEndInstr() {
        Vector<BHInstruction> simpleInst = getSimpleInstructions();
        for (int loop = 0; loop < simpleInst.size(); loop++) {
            if (simpleInst.get(loop).shouldOnlyComeAtTheBeginning() && loop != 0) {
                return false;
            } else if (simpleInst.get(loop).shouldOnlyComeAtTheEnd() && loop + 1 != simpleInst.size()) {
                return false;
            }
        }
        return true;
    }

    public Vector getSourcesOrTargetsInMe(boolean isSource) {
        Vector<BHInstruction> instrctionLocal = getSimpleInstructions();
        BHInstruction bhInst = null;
        Vector ret = new Vector();
        for (int loop = 0; loop < instrctionLocal.size(); loop++) {
            bhInst = instrctionLocal.get(loop);
            if ((isSource && bhInst.isBranchSource()) || (!isSource &&
                    bhInst.isBranchTarget())) {
                ret.addElement(bhInst);
            }
        }
        return ret;
    }

    @Override
    public Vector getSourcesInstructionsInMe() {
        return getSourcesOrTargetsInMe(true);
    }

    @Override
    public Vector getTargetInstructionsInMe() {
        return getSourcesOrTargetsInMe(false);
    }

    /**
     * In goes through each instrution. 3 
     *
     * if an instruction is instanceof InstructionCombined then call this function again.
     * else find operand savings and opcode savings
     *
     * operand savings : originalOperand size minus currentoperand size per instruction.
     * opcode savings : add in savings number of instruction combined minus 1 
     * @param instCom
     * @return
     */
    private int getSavingInternal() {
        if (savings != -1) {
            return savings;
        }
        int numberOfInst = instVec.size();
        savings = numberOfInst - 1; //opcode savings

        for (int loop = 0; loop < numberOfInst; loop++) {
            Instruction inst = (Instruction) instVec.elementAt(loop);

            if (inst instanceof InstructionsCombined) {
                savings += ((InstructionsCombined) inst).getSavingInternal();
            } else {
                Un originalOperand = inst.getOriginalOperand();
                savings += originalOperand.size() -
                        inst.getOperandsData().size();
            }
        }

        if (savings < 0) {
            savings = 0; //single instruction will have zero savings.
        }
        return savings;
    }

    private boolean isSimpleInstruction() {
        if (instVec.size() > 1) {
            return false;
        }
        return true;
    }

    public Vector getSimpleInstructions() {
        if (isDirty) {
            simpleInstructionsCache = getSimpleInstCombined(instVec).getInstructions();
        } else {
            isDirty = false;
        }
        return simpleInstructionsCache;
    }

    public static InstructionsCombined getSimpleInstCombined(Vector instructions) {
        Vector ret = new Vector();
        for (int loop = 0; loop < instructions.size(); loop++) {
            Instruction inst = (Instruction) instructions.elementAt(loop);
            if (inst instanceof InstructionsCombined) {
                InstructionsCombined temp = (InstructionsCombined) inst;
                ret.addAll(getSimpleInstCombined(temp.instVec).instVec);
            } else {
                ret.addElement(inst);
            }
        }
        InstructionsCombined comb = new InstructionsCombined();
        comb.addInstructions(ret);
        return comb;
    }

    public int getNumberOfSimpleInsturction() {
        return getSimpleInstructions().size();
    }

    /**
     * It returns -ve infinity (min integer value) in case combination was not a
     * valid allowed combination otherwise returns 1;
     * @return
     */
    public int getSavings() {
        int savingsLocal = 0;
        if (FreqMap.getInstanceOf().getFreq(this) == 0 && !isSimpleInstruction()) {
            savingsLocal = Integer.MIN_VALUE;
        } else {
            savingsLocal = getSavingInternal();
        }
        return savingsLocal;
    }
}
