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

import java.util.*;
import org.apache.commons.lang.builder.*;
import takatuka.classreader.dataObjs.attribute.*;
import takatuka.classreader.dataObjs.*;
import takatuka.optimizer.bytecode.replacer.dataObjs.attributes.*;
import takatuka.vm.autoGenerated.vmSwitch.*;
import takatuka.classreader.logic.util.*;

/**
 * 
 * Description:
 * <p>
 *
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public abstract class InstructionsCombineBase extends IRInstruction {

    //private String className = null;
    protected Vector instVec = new Vector();
    //private static final HashMap newInstructionMap = new HashMap();
    protected int savings = -1;
    private static final String INSTRUCTIONS_SEPERATOR = "___";
    private RegisterCustomInstruction regCustInst = RegisterCustomInstruction.getInstanceOf();
    //whenever add something then dirty become true otherwise false
    protected boolean isDirty = false;
    protected int numberOfBranchSourcesInMe = 0;

    public InstructionsCombineBase() {
    }

    public InstructionsCombineBase(int opcode, Un operands, CodeAtt parentCodeAtt) {
        Instruction inst = new Instruction(opcode, operands, parentCodeAtt);
        addInstruction(inst);
    }

    public void addInstructionRevOrder(Instruction inst) {
        isDirty = true;
        instVec.add(0, inst);
        if (inst.isBranchSourceInstruction()) {
            numberOfBranchSourcesInMe++;
        }
    }

    @Override
    public Object clone() {
        InstructionsCombined instComb = new InstructionsCombined();
        instComb.addInstructions((Vector) instVec.clone());
        //instComb.setClassName(className);
        return instComb;
    }

    @Override
    public int length() {
        int length = getOperandsData().size() + 1;
        return length;
    }

    @Override
    public int getOpCode() {
        int ret = Instruction.getOpcode(getMnemonic());
        if (ret != -1) {
            return ret;
        }
        ret = super.getOpCode();
        if (ret == -1) {
            ret = regCustInst.getOpCode(getMnemonic());
        }
        return ret;
    }

    @Override
    public Un getOperandsData() {
        Un operand = null;
        try {
            operand = new Un();
            Un tempOperand = null;
            for (int loop = 0; loop < instVec.size(); loop++) {
                Instruction inst = (Instruction) instVec.get(loop);
                tempOperand = (Un) inst.getOperandsData().clone();
                if (inst.getMnemonic().contains("SWITCH")) { //a simple instruction
                    int padding = inst.getOperandsData().size() % 4;
                    Un.cutBytes(padding, tempOperand);
                }
                operand.conCat(tempOperand);

            }
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return operand;
    }

    @Override
    public void setOpCode(int opcode) {
        super.setOpCode(opcode);
    }

    @Override
    public void setOperandsData(Un data) {
        throw new UnsupportedOperationException();
    }

    public void addInstructions(Vector insts) {
        for (int loop = 0; loop < insts.size(); loop++) {
            addInstruction((Instruction) insts.elementAt(loop));
        }
    }

    public void addInstruction(Instruction inst) {
        isDirty = true;
        instVec.add(inst);
        if (inst.isBranchSourceInstruction()) {
            numberOfBranchSourcesInMe++;
        }

    }

    public String combineMnemoniators(String mnem) {
        if (true) {
            return mnem; //do not combine any nemonics
        }
        if (mnem.startsWith("aload_")) {
            return "aload_<n>";
        } else if (mnem.startsWith("iload_")) {
            return "iload_<n>";
        } else if (mnem.startsWith("istore_")) {
            return "istore_<n>";
        } else if (mnem.startsWith("astore_")) {
            return "astore_<n>";
        } else if (mnem.startsWith("iconst") || mnem.startsWith("bipush")) {
            return "bipush/iconst";
        }
        return mnem;
    }

    public Instruction getFirstInstruction() {
        return (Instruction) instVec.elementAt(0);
    }

    public Vector getInstructions() {
        return instVec;
    }

    public int size() {
        return instVec.size();
    }

    public void setSavings(int savings) {
        this.savings = savings;
    }

    @Override
    public boolean equals(Object obj) {
        InstructionsCombineBase inputKey = (InstructionsCombineBase) obj;
        if (instVec.equals(inputKey.instVec)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(instVec).toHashCode();
    }

    @Override
    public String getMnemonic() {
        if (this.cacheMne != null) {
            return cacheMne;
        }
        cacheMne = "";
        Instruction inst = null;
        for (int loop = 0; loop < instVec.size(); loop++) {
            inst = (Instruction) instVec.elementAt(loop);
            cacheMne = cacheMne + inst.getMnemonic();
            if (loop + 1 != instVec.size()) {
                cacheMne = cacheMne + INSTRUCTIONS_SEPERATOR;
            }
        }
        return cacheMne;
    }
}
