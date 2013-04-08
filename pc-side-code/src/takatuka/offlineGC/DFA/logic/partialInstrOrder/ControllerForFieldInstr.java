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
import takatuka.classreader.logic.constants.JavaInstructionsOpcodes;
import takatuka.offlineGC.DFA.dataObjs.attribute.*;
import takatuka.offlineGC.DFA.dataObjs.functionState.*;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * This controller keeps record of getfield and getstatic instructions 
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
class ControllerForFieldInstr {

    private static final ControllerForFieldInstr myObj = new ControllerForFieldInstr();
    /**
     * used for getstatic and getfield
     * Key: is cp index.
     * value: is the set of field's data corresponding to that CP index.
     */
    private HashMap<Integer, HashSet<FieldRecord>> fieldRecordMap = new HashMap<Integer, HashSet<FieldRecord>>();
    /**
     * used for AALOAD
     * key: is new id
     * value: is the set of field's data corresponding to that key.
     */
    private HashMap<Integer, HashSet<FieldRecord>> arrayBasedFieldRecordMap = new HashMap<Integer, HashSet<FieldRecord>>();

    private ControllerForFieldInstr() {
    }

    public static ControllerForFieldInstr getInstanceOf() {
        return myObj;
    }

    private Collection getFromMap(Object key, HashMap map) {
        Collection record = (Collection) map.get(key);
        if (record == null) {
            record = new HashSet();
            map.put(key, record);
        }
        return record;

    }

    private FieldRecord getOldValue(Collection record, FieldRecord fieldRec) {
        Iterator<FieldRecord> it = record.iterator();
        while (it.hasNext()) {
            FieldRecord oldFieldRec = it.next();
            if (oldFieldRec.equals(fieldRec)) {
                return oldFieldRec;
            }
        }
        return fieldRec;
    }

    public void addRecord(FunctionStateKey stateKey,
            GCInstruction instr, int cpIndex) {
        Collection record = null;
        if (instr.getOpCode() != JavaInstructionsOpcodes.AALOAD) {
            record = getFromMap(cpIndex, fieldRecordMap);
        } else {
            record = getFromMap(cpIndex, arrayBasedFieldRecordMap);
        }
        record.add(getOldValue(record, new FieldRecord(stateKey,
                instr, cpIndex)));
    }

    private HashSet<FieldRecord> getRecords(int fieldIndentifier, boolean isForArray) {
        HashSet ret = null;
        if (!isForArray) {
            ret = fieldRecordMap.get(fieldIndentifier);
        } else {
            ret = arrayBasedFieldRecordMap.get(fieldIndentifier);
        }
        if (ret == null) {
            ret = new HashSet();
        }
        return ret;
    }

    public HashSet<FieldRecord> getRecords(HashSet<Integer> uniqueFieldIdentifiers, boolean isForArray) {
        HashSet<FieldRecord> ret = new HashSet<FieldRecord>();
        Iterator<Integer> it = uniqueFieldIdentifiers.iterator();
        while (it.hasNext()) {
            ret.addAll(getRecords(it.next(), isForArray));
        }
        return ret;
    }
}
