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
package takatuka.offlineGC.DFA.dataObjs.fields;

import takatuka.offlineGC.DFA.dataObjs.*;
import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.util.Miscellaneous;
import takatuka.offlineGC.DFA.dataObjs.attribute.GCInstruction;

/**
 * 
 * Description:
 * <p>
 * Heap is corresponds to each newtype (Each new will create a heap)
 * Heap will have collections of CGFields
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class GCHeap implements GCHeapInterface {

    private int newInstrId = 0;
    private int classId = 0;
    /**
     * Key of the map is NameAndTypeIndex and value is the GCField
     */
    private HashMap<Integer, GCField> fields = new HashMap<Integer, GCField>();

    /**
     * 
     * @param newInstrId
     * @param classId
     */
    public GCHeap(int newInstrId, int classId) {
        this.newInstrId = newInstrId;
        this.classId = classId;
    }

    /**
     * If the newInstrId is equals to -1 then return. Otherwise,
     * first find field in the current class. If field does not exist then find it
     * in the superclasses and set the newId.
     *
     * @param nATIndex
     * @param value
     * @param method
     * @param callingParams
     * @param instr
     * @throws java.lang.Exception
     */
    @Override
    public void putField(int nATIndex, GCType value, MethodInfo method,
            Vector callingParams, GCInstruction instr) throws Exception {
        if (!value.isReference()) {
            Miscellaneous.printlnErr("Error # 2344");
            System.exit(1);
        }
        GCField field = fields.get(nATIndex);
        if (field == null) {
            field = new GCField(nATIndex);
            fields.put(nATIndex, field);
        }
        field.add(value.getReferences());
        //gpRec.record(field);
    }

    /**
     * 
     * @return
     */
    public int getNewInstrId() {
        return newInstrId;
    }

    /**
     * 
     * @param nameAndTypeValue
     * @param method
     * @param callingParams
     * @param instr
     * @return
     */
    @Override
    public GCField getField(int nameAndTypeValue, MethodInfo method,
            Vector callingParams, GCInstruction instr) {
        GCField field = fields.get(nameAndTypeValue);
        gpRec.recordRefIsGetFromAField(field);
        return field;

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof GCHeap)) {
            return false;
        }
        GCHeap input = (GCHeap) obj;
        if (input.newInstrId == newInstrId) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + this.newInstrId;
        return hash;
    }

    @Override
    public String toString() {
        String ret = "GCHeap =";
        ret = ret + newInstrId + ", Fields = **{" + this.fields + "}**";
        return ret;
    }
}
