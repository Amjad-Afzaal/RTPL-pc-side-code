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
package takatuka.optimizer.bytecode.branchSetter.dataObjs.attributes;

import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.attribute.*;
import java.util.*;
import takatuka.classreader.logic.logAndStats.*;
import takatuka.optimizer.bytecode.branchSetter.logic.factory.*;
import takatuka.optimizer.bytecode.changer.logic.*;
import takatuka.classreader.logic.util.*;

/**
 * 
 * Description:
 * <p>
 *
 * -- Every instruction has an id.
 * -- Every source/destination of a branch has a flag set and hence should not 
 *    get optimized (check the flag always before optimizing an instruction).
 * -- We have source-id, destination-id map. Hence even if the targetOffset are changed 
 *    the map is still valid as the ids are still useful.
 * -- We create that map during instruction creation and then use it to reset the offsets
 * after optimization is completed.
 * 
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class BHInstruction extends Instruction {

    private boolean isBranchTarget = false;
    private boolean isBranchSource = false;
    private Vector<BranchTarget> branchTargetsVec = new Vector();
    private static HashMap<Integer, Vector> missingTargetOffsetAndSource = new HashMap();
    public Vector<Long> sourceIds = new Vector();
    private static boolean firstTime = true;
    private static HashMap<Integer, Long> numberOfTimesAnOpCodeOccurs = new HashMap();
    private static long totalNumberOfInstructions = 0;
    private boolean isBlockStartInst = false;
    private boolean isBlockEndInst = false;

    public BHInstruction() {
        super();
    }

    public BHInstruction(int opcode, Un operands, CodeAtt parentCodeAtt) {
        super(opcode, operands, parentCodeAtt);
        init();
    }

    /**
     * sepcify which instruction should always be at the beggining or
     * end of a block.
     */
    private void specifyStartAndEndBlock() {
        /*
         * All branch targets should come at the beginning of block. However,
         * yet we do not know if current instruction is target of futrue instructions.
         * hence this we have to cater later. We do that in InstructionCombined class.
         */
        if (isBranchSourceInstruction() && !InputOptionsController.isIgnoreJVMSwitchInc) {
            /*
             * there is a macro to support branch source instructions.
             * However, it increases JVM switch size a lot.
             */
            setShouldOnlyComeAtTheEnd();
        }
        if (getMnemonic().contains("STATIC")) {
            setShouldOnlyComeAtTheBeginning();
        }
        /**
         * These instructions should also come at the end of a block.
         *
         */
        if (getMnemonic().contains("RETURN")
                || getMnemonic().contains("INVOKE")
                || getMnemonic().contains("STATIC")
                || getMnemonic().contains("MONITOR")) {
            setShouldOnlyComeAtTheEnd();
        }
    }

    protected void init() {
        specifyStartAndEndBlock();
        BHInstruction prevInst = (BHInstruction) BytecodeProcessor.getPrevInstruction();
        if (BHCodeAtt.currentCodeAtt == null /*&& BHCodeAtt.codeAttCountDebug == 15*/) {
            return;
        }
        Long number = numberOfTimesAnOpCodeOccurs.get(getOpCode());
        if (number == null) {
            numberOfTimesAnOpCodeOccurs.put(getOpCode(), new Long(1));
        } else {
            numberOfTimesAnOpCodeOccurs.put(getOpCode(), number + 1);
        }
        totalNumberOfInstructions++;
        if (firstTime) {
            firstTime = false;
            LogHolder.getInstanceOf().addLog("Saving Branch information ..... ");
        }
        if (prevInst == null) {
            clear(); // first instruction of codeAttribute 
        } else {
            offSet = prevInst.length() + prevInst.getOffSet();
        }
        BHCodeAtt.currentCodeAtt.putInOffSetIdMap(offSet, new IdInstruction(this, instructionId));

        //Miscellaneous.println("ME ME = " + this);
        Vector<Integer> vec = getBranchAddresses();
        populateBranchTargetVector(vec);
        meAsMissingTarget();
        if (isBranchSource()) {
            //Miscellaneous.println("kill me " + this);
            BHCodeAtt.currentCodeAtt.sourceIdVsTargetIds.put(getInstructionId(), branchTargetsVec);
        }
    }

    public Vector<BHInstruction> getSourcesInstructionsInMe() {
        Vector ret = new Vector();
        if (isBranchSource) {
            ret.addElement(this);
        }
        return ret;
    }

    public Vector<BHInstruction> getTargetInstructionsInMe() {
        Vector ret = new Vector();
        if (isBranchTarget) {
            ret.addElement(this);
        }
        return ret;
    }

    public long getNumberOfTimesAnOpcodeOccurs() {
        Long ret = numberOfTimesAnOpCodeOccurs.get(getOpCode());
        if (ret == null) {
            return 0;
        }
        return ret;
    }

    public long getTotalNumberOfInstructions() {
        return totalNumberOfInstructions;
    }

    @Override
    public Object clone() {
        BHInstruction ret = null;
        try {

            ret = (BHInstruction) ((FactoryFacadeBH) factory).createInstruction(getOpCode(),
                    (Un) getOperandsData().clone(), getParentCodeAtt());
            ret.instructionId = getInstructionId();
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return ret;
    }

    public boolean isMyTarget(long targetId) {
        Vector<BranchTarget> targets = getMyTargets();
        for (int loop = 0; loop < targets.size(); loop++) {
            BranchTarget bTarget = targets.elementAt(loop);
            if (bTarget.targetId == targetId) {
                return true;
            }
        }
        return false;
    }

    private void clear() {
        if (missingTargetOffsetAndSource.size() != 0) {
            Miscellaneous.printlnErr(" Some error #201 at ="
                    + ClassFile.currentClassToWorkOn.getFullyQualifiedClassName());
            Miscellaneous.exit();
        }
    }

    private void comingMissingTarget(int targetOffset, long targetId) {
        for (int loop = 0; loop < branchTargetsVec.size(); loop++) {
            BranchTarget bTarget = branchTargetsVec.elementAt(loop);
            if (bTarget.targetOffset == targetOffset) {
                if (bTarget.targetId != -1) {
                    continue;
                }
                bTarget.targetId = targetId;
                return;
            }
        }
    }

    private void meAsMissingTarget() {
        Vector sources = missingTargetOffsetAndSource.get(offSet);
        if (sources == null) {
            return;
        }
        this.isBranchTarget = true;
        for (int loop = 0; loop < sources.size(); loop++) {
            ((BHInstruction) sources.elementAt(loop)).comingMissingTarget(offSet, instructionId);
        }
        missingTargetOffsetAndSource.remove(offSet);
        //Miscellaneous.println(" ---------------< " + missingTargetOffsetAndSource);
    }

    private void populateMissingTargetRecord(int targetOffset) {
        Vector sources = missingTargetOffsetAndSource.get(targetOffset);
        if (sources == null) {
            sources = new Vector();
            missingTargetOffsetAndSource.put(targetOffset, sources);
        }
        sources.addElement(this);
    }

    private void populateBranchTargetVector(Vector<Integer> branchTargetOffsets) {
        int size = branchTargetOffsets.size();
        if (size > 0) {
            sourceIds.addElement(instructionId);
            this.isBranchSource = true;
        }
        for (int loop = 0; loop < size; loop++) {
            int targetOffset = branchTargetOffsets.elementAt(loop);
            long targetId = -1;
            IdInstruction idInst = BHCodeAtt.currentCodeAtt.getFromOffSetIdMap(targetOffset);
            if (idInst != null) {
                targetId = idInst.id;
                idInst.inst.isBranchTarget = true;
            } else {
                populateMissingTargetRecord(targetOffset);
            }
            branchTargetsVec.addElement(new BranchTarget(targetOffset, targetId));
        }
    }

    public boolean isBranchSrcAndDst() {
        if (isBranchSource() && isBranchTarget()) {
            return true;
        }
        return false;
    }

    public void setBranchTarget(boolean value) {
        this.isBranchTarget = value;
    }

    public boolean isBranchTarget() {
        return this.isBranchTarget;
    }

    public boolean isBranchSource() {
        return this.isBranchSource;
    }

    public boolean shouldOnlyComeAtTheBeginning() {
        return isBlockStartInst;
    }

    public boolean shouldOnlyComeAtTheEnd() {
        return isBlockEndInst;
    }

    public void setShouldOnlyComeAtTheBeginning() {
        isBlockStartInst = true;
    }

    public void setShouldOnlyComeAtTheEnd() {
        isBlockEndInst = true;
    }

    /**
     * If the instruction is not a branch source instruction then it return null
     * otherwise, returns addresses (offsets) of the branch targets from this instruction
     * @return
     */
    public Vector<Integer> getBranchAddresses() {
        String mne = getMnemonic();
        MiniBytecodeProcessor miniPro = MiniBytecodeProcessor.getInstanceOf();
        Vector<Integer> ret = new Vector();
        if (mne.startsWith("GOTO") || mne.startsWith("IF")
                || mne.startsWith("JSR")) {

            ret.addElement(getOperandsData().intValueSigned() + offSet);
        } else if (mne.startsWith("TABLESWITCH")) {
            return miniPro.getTableSwitchAddreses(getOperandsData(), getOffSet());
        } else if (mne.startsWith("LOOKUP")) {
            return miniPro.getLookupSwitchAddreses(getOperandsData(), getOffSet());
        }
        return ret;
    }

    public Vector<BranchTarget> getMyTargets() {
        return this.branchTargetsVec;
    }

    
    public void setBranchAddresses(Vector<Integer> addresses) {
        String mne = getMnemonic();
        int address = 0;
        Un unAddress = null;
        MiniBytecodeProcessor miniPro = MiniBytecodeProcessor.getInstanceOf();
        try {
            if (mne.startsWith("GOTO") || mne.startsWith("IF")
                    || mne.startsWith("JSR")) {
                address = (Integer) addresses.remove(0);
                Un operands = (Un) getOperandsData().clone();
                unAddress = factory.createUn(address).trim(operands.size());
                setOperandsData(unAddress);
            } else if (mne.startsWith("TABLESWITCH")) {
                setOperandsData(miniPro.setTableSwitchAddreses(getOperandsData(), addresses));
            } else if (mne.startsWith("LOOKUP")) {
                setOperandsData(miniPro.setLookUpSwitchAddresses(getOperandsData(), addresses));
            }
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
    }

    @Override
    public String toString() {
        return "[" + getInstructionId() + ":" + super.getMnemonic() + ", operandData="
                + super.getOperandsData() + ", isSource=" + isBranchSource()
                + ", isTarget=" + isBranchTarget() + ", Offset=" + getOffSet()
                + ", targets =" + getMyTargets() + "]\n";
    }
}

