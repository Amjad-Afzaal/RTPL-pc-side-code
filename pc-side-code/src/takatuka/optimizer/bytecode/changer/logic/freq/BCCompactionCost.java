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
package takatuka.optimizer.bytecode.changer.logic.freq;

import java.util.*;
import takatuka.classreader.dataObjs.attribute.*;
import takatuka.classreader.logic.file.*;
import takatuka.optimizer.bytecode.changer.logic.*;
import takatuka.optimizer.bytecode.changer.logic.comb.*;
import takatuka.classreader.logic.util.*;

/**
 * 
 * Description:
 * <p>
 * The cost function of compaction simply returns total reduction in bytecode 
 * minus total increment in machine code. Hence a customized instruction with 
 * hight value of cost is term as good.
 * </p>
 * 
 * @author Faisal Aslam
 * @version 1.0
 */
public class BCCompactionCost {

    private static final BCCompactionCost compCost = new BCCompactionCost();
    private static final String MAX_OVERHEAD_COMB_INST = "MAX_OVERHEAD_COMB_INST";
    private static final String MAX_OVERHEAD_OF_CASE = "MAX_OVERHEAD_OF_CASE";
    private static final String MAX_OVERHEAD_BRANCH_SOURCE_INST = "MAX_OVERHEAD_BRANCH_SOURCE_INST";
    private int caseOverhead = 0;
    private int custInstOverhead = 0;
    private int custSourceInstOverhead = 0;
    private ConfigPropertyReader cPropReader = ConfigPropertyReader.getInstanceOf();
    private SimpleSingleInstructionFreq sim = SimpleSingleInstructionFreq.getInstanceOf();
    private HashSet<String> alreadyConsideredForSwitchDec = new HashSet<String>();
    private BCCompactionCost() {
        init();
    }

    private void init() {
        String sCaseOverhead = cPropReader.getConfigProperty(MAX_OVERHEAD_OF_CASE);
        String sCustInstOverhead = cPropReader.getConfigProperty(MAX_OVERHEAD_COMB_INST);
        String sCustSourceInstOverhead = cPropReader.getConfigProperty(MAX_OVERHEAD_BRANCH_SOURCE_INST);
        if (sCaseOverhead == null || sCustInstOverhead == null ||
                sCustSourceInstOverhead == null) {
            Miscellaneous.printlnErr("Either  " + MAX_OVERHEAD_COMB_INST + " or " +
                    MAX_OVERHEAD_OF_CASE + " or " + MAX_OVERHEAD_BRANCH_SOURCE_INST +
                    " property not found");
            Miscellaneous.exit();
        }
        caseOverhead = Integer.parseInt(sCaseOverhead);
        custInstOverhead = Integer.parseInt(sCustInstOverhead);
        custSourceInstOverhead = Integer.parseInt(sCustSourceInstOverhead);
    }

    public static BCCompactionCost getInstanceOf() {
        return compCost;
    }

    public void clearAlreadyConsiderSwitchDecInstRecord() {
        alreadyConsideredForSwitchDec.clear();
    }
    
    public int getTotalReduction(FreqMapValue freqValue) {
        int bytecodeReduction = getTotalBytecodeReduction(freqValue);
        int switchInc = getTotalSwitchIncrement(freqValue);
        int switchRed = getTotalSwitchReduction(freqValue);
        HashSet dummy = (HashSet) alreadyConsideredForSwitchDec.clone();
        if (InputOptionsController.isIgnoreJVMSwitchInc) {
            switchInc = 0;
            switchRed = 0;
        }
        int totalRed = bytecodeReduction - (switchInc - switchRed);
        if (totalRed <= 0) {
            alreadyConsideredForSwitchDec = dummy;
            return Integer.MIN_VALUE;
        }
        return totalRed; //bytecodeReduction;
    }

    private int getTotalBytecodeReduction(FreqMapValue freqValue) {
        return getTotalBytecodeReduction(freqValue.freq,
                freqValue.instComb.getSavings());
    }

    private int getTotalBytecodeReduction(int freq, int savingPerOccurance) {
        return freq * savingPerOccurance;
    }

    /**
     * If an instruction is always replaced by a customized instruction. Then
     * the reduction in the switch is equivalent to the size of case of that original
     * instruction. 
     * When to calculate freq of old instructions?
     * After each kind of optimization. Hence that implies we calculate freq of all instruction
     * three times.
     * @param freqValue
     * @return
     */
    private int getTotalSwitchReduction(FreqMapValue freqValue) {
        if (true) {
        //return 0;
        }

        InstructionsCombined comb = freqValue.instComb;
        Vector<Instruction> simpleInstVector = comb.getSimpleInstructions();
        int reduction = 0;
        for (int index = 0; index < simpleInstVector.size(); index++) {
            Instruction inst = simpleInstVector.elementAt(index);
            reduction += getTotalSwitchReduction(freqValue, inst);
        }
        return reduction;
    }

    private int getTotalSwitchReduction(FreqMapValue freqValue, Instruction inst) {
        int totalFreq = sim.getFrequency(inst.getMnemonic());
        int freq = freqValue.freq;
        if (totalFreq == freq && !alreadyConsideredForSwitchDec.contains(inst.getMnemonic())) {
            //Miscellaneous.println(totalFreq+" , "+freq+" , "+inst);
            alreadyConsideredForSwitchDec.add(inst.getMnemonic());
            return caseOverhead + custInstOverhead;
        }
        return 0;
    }

    private int getTotalSwitchIncrement(FreqMapValue freqValue) {
        InstructionsCombined instComb = freqValue.instComb;
        int numberOfBranchSource = instComb.getNumberOfBranchSourceInMe();
        int totalSimpleInstInc = getTotalSwitchIncrementByNonBranchInstr(instComb.getNumberOfSimpleInsturction(), numberOfBranchSource);
        int totalBranchSourcInstInc = custSourceInstOverhead * numberOfBranchSource;
        return caseOverhead + totalSimpleInstInc + totalBranchSourcInstInc;
    }

    private int getTotalSwitchIncrementByNonBranchInstr(int noOfInstCombined, int numberOfBranchSourceInst) {
        return ((noOfInstCombined - numberOfBranchSourceInst) * custInstOverhead);
    }
}
