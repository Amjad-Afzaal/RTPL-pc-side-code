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

import java.util.*;
import takatuka.classreader.dataObjs.attribute.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;

/**
 * 
 * Description:
 * <p>
 * Just record freq of all CP and OffSet instructions, if those instructions
 * has less than 255 byte operand value.
 * Furthermore, it also record the freq of CAST_METHOD_STACK_LOCATION and
 * see how many time stack location of size less than 255 is used.
 * 
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class FreqOfCPAndOffSetInstructions {

    private static final FreqOfCPAndOffSetInstructions fCpAndOffSetInst =
            new FreqOfCPAndOffSetInstructions();
    
    private HashMap<Integer, Integer> opcodeToFreqMap = new HashMap();

    private FreqOfCPAndOffSetInstructions() {
    //no one creates me but me.
    }

    public static final FreqOfCPAndOffSetInstructions getInstanceOf() {
        return fCpAndOffSetInst;
    }

    /**
     * Create record of frequencies of CP and Offset instructions with operand
     * value less than 256.
     * Here is how it works
     * - Go to all the instruction
     * - if instruction is either CP or Offset then check its operand.
     * - if operand is less than 256 then 
     *              increment the counter corresponding to that instruction by one.
     */
    public void createFreqRecord() {
        Oracle oracle = Oracle.getInstanceOf();
        Vector<CodeAttCache> allCodeAttInfo = oracle.getAllCodeAtt();
        CodeAtt codeAtt = null;
        Vector<Instruction> instrs = null;
        for (int loop = 0; loop < allCodeAttInfo.size(); loop++) {
            CodeAttCache codeAttInfo = allCodeAttInfo.elementAt(loop);
            codeAtt = (CodeAtt) codeAttInfo.getAttribute();
            instrs = codeAtt.getInstructions();
            Instruction inst = null;
            for (int instIndex = 0; instIndex < instrs.size(); instIndex++) {
                inst = instrs.elementAt(instIndex);
                createFreqRecord(inst);
            }
        }
    }

    private void createFreqRecord(Instruction instr) {
        int address = CPAndOffSetChangedInstructions.getConstantPoolAddress(instr);
        if (address == -1) {
            address = CPAndOffSetChangedInstructions.getLocalOffsetTwoBytes(instr);
        }
        if (address == -1) {
            address = CPAndOffSetChangedInstructions.getCastMethodInstrStackAddress(instr);
        }
        if (address != -1 && address < CPAndOffSetChangedInstructions.MAX_ONE_BYTE) {
            record(instr);
        }
    }

    private void record(Instruction inst) {
        Integer freq = opcodeToFreqMap.get(inst.getOpCode());
        if (freq == null) {
            freq = 0;
        }
        opcodeToFreqMap.put(inst.getOpCode(), freq++);
    }

    /**
     * 
     * @param opCode
     * @return
     */
    public int getFreqWhenOperandLessThan256(int opCode) {
        Integer freq = opcodeToFreqMap.get(opCode);
        if (freq != null) {
            return freq;
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        return opcodeToFreqMap.toString();
    }
}
