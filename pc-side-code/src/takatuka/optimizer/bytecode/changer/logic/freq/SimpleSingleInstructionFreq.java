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
import takatuka.optimizer.bytecode.changer.logic.InputOptionsController;
import takatuka.optimizer.bytecode.changer.logic.StartMeBCC;
import takatuka.optimizer.cpGlobalization.logic.util.*;

/**
 * 
 * Description:
 * <p>
 * This class is only Used by BCCompactionCost 
 * to caculate reduction in the switch 
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
class SimpleSingleInstructionFreq {

    private HashMap<String, Integer> map = new HashMap<String, Integer>();
    private static final SimpleSingleInstructionFreq myObj = new SimpleSingleInstructionFreq();
    private int currentStateOfOptimization = -1;
    
    private SimpleSingleInstructionFreq() {
    }

    public static SimpleSingleInstructionFreq getInstanceOf() {
        return myObj;
    }

    private void populateFrequencies() {
        if (currentStateOfOptimization != InputOptionsController.getCurrentStateOfOptimization()) {
            currentStateOfOptimization = InputOptionsController.getCurrentStateOfOptimization();
        } else {
            return;
        }
        //Miscellaneous.println("---------------- Can you see me ------------- "+currentStateOfOptimization);
        map.clear();
        Oracle oracle = Oracle.getInstanceOf();
        Vector<CodeAttCache> codeAttInfoVec = oracle.getAllCodeAtt();
        CodeAtt codeAtt = null;
        for (int index = 0; index < codeAttInfoVec.size(); index++) {
            CodeAttCache codeAttInfo = codeAttInfoVec.elementAt(index);
            codeAtt = (CodeAtt) codeAttInfo.getAttribute();
            if (codeAtt.getCodeLength().intValueUnsigned() == 0) {
                continue;
            }
            Vector<Instruction> instrVec = codeAtt.getInstructions();
            populateFrequencies(instrVec);
        }
    }

    private void populateFrequencies(Vector<Instruction> instrVec) {
        for (int index = 0; index < instrVec.size(); index++) {
            Instruction inst = instrVec.elementAt(index);
            String key = inst.getMnemonic();
            Integer value = map.get(key);
            if (value == null) {
                value = 0;
            }
            map.put(key, ++value);
        }
    }

    public int getFrequency(String mnemonic) {
        populateFrequencies();
        Integer value = map.get(mnemonic);
        if (value == null) {
            value = 0;
        }
        return value;
    }

    @Override
    public String toString() {
        return map.toString();
    }
}
