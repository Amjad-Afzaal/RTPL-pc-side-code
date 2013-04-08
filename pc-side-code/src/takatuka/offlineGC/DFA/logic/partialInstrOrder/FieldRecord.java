/*
 * Copyright 2009 Christian Schindelhauer, Peter Thiemann, Faisal Aslam, Luminous Fennell and Gidon Ernst.
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
package takatuka.offlineGC.DFA.logic.partialInstrOrder;

import java.util.*;
import takatuka.offlineGC.DFA.dataObjs.attribute.*;
import takatuka.offlineGC.DFA.dataObjs.functionState.*;

/**
 * <p>Title: </p>
 * <p>Description:
 * record for getstatic, getfield and AALoad instructions
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class FieldRecord extends InstrRecordBase {

    private int uniqueForField = -1;
    private HashSet<FunctionStateKey> stateKeySet = new HashSet<FunctionStateKey>();


    /**
     *
     * @param stateKey
     * @param instr
     * @param uniqueForField for getstatic and getfield instructions the unique for
     * field is the constant-pool-index. For aaload instruction it is the newId
     * the instruction corresponds too.
     */
    public FieldRecord(FunctionStateKey stateKey, GCInstruction instr,
            int uniqueForField) {
        super(instr);
        this.stateKeySet.add(stateKey);
        this.uniqueForField = uniqueForField;
    }

    @Override
    public String toString() {
        return "cp-index="+uniqueForField+", fun-state-Key="+stateKeySet;
    }
    
    
    public void addStateKey(FunctionStateKey stateKey) {
        stateKeySet.add(stateKey);
    }

    public HashSet<FunctionStateKey> getStateKeys() {
        return (HashSet<FunctionStateKey>) stateKeySet.clone();
    }

    public int getCPIndex() {
        return uniqueForField;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof InstrRecordBase)) {
            return false;
        }
        FieldRecord input = (FieldRecord) obj;
        if (super.equals(obj) && input.uniqueForField == uniqueForField) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        long instrId = getInstr().getInstructionId();
        hash = 37 * hash + this.uniqueForField;
        hash = 37 * hash + (int) (instrId ^ (instrId >>> 32));
        return hash;
    }

    
}
