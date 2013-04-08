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
 */package takatuka.offlineGC.DFA.dataObjs.attribute;

import takatuka.offlineGC.DFA.dataObjs.TTReference;

/**
 *
 * @author Faisal Aslam
 */
public class FreeInstrRecord {

    GCInstruction instr = null;
    TTReference ref = null;

    public FreeInstrRecord(GCInstruction instr, TTReference ref) {
        this.instr = instr;
        this.ref = ref;
    }

    public GCInstruction getInstr() {
        return instr;
    }

    public TTReference getRef() {
        return ref;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null && !(obj instanceof FreeInstrRecord)) {
            return false;
        }
        FreeInstrRecord input = (FreeInstrRecord) obj;
        if (input.instr.getInstructionId() == instr.getInstructionId()
                && ref.equals(input.ref)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        long instrId = instr.getInstructionId();
        hash = 89 * hash + (this.ref != null ? this.ref.hashCode() : 0);
        hash = 89 * hash + (int) (instrId ^ (instrId >>> 32));
        return hash;
    }
    
    public String toString() {
        return ref.getNewId()+", "+instr;
    }
}
