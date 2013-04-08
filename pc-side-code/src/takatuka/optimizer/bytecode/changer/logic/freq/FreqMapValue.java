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
import takatuka.optimizer.bytecode.changer.logic.comb.*;

/**
 * 
 * Description:
 * <p>
 *
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */

public class FreqMapValue {

    public InstructionsCombined instComb = null;
    public int freq = -1;
    public int arrivalTime = -1;
    public Vector<Instruction> originalInstructions = null;

    public FreqMapValue(InstructionsCombined instComb, int freq, int arrivalTime, Vector<Instruction> originalInst) {
        super();
        this.instComb = instComb;
        this.freq = freq;
        this.arrivalTime = arrivalTime;
        this.originalInstructions = originalInst;
    }
    
    public int totalSavings() {
        return freq * instComb.getSavings();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof FreqMapValue)) {
            return false;
        }
        FreqMapValue input = (FreqMapValue) obj;
        if (input.instComb.equals(instComb)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + (this.instComb != null ? this.instComb.hashCode() : 0);
        return hash;
    }
}
