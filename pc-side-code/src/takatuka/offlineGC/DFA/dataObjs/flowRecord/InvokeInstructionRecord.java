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
package takatuka.offlineGC.DFA.dataObjs.flowRecord;

import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.offlineGC.DFA.dataObjs.attribute.*;
import takatuka.offlineGC.DFA.dataObjs.functionState.*;
import takatuka.offlineGC.DFA.logic.partialInstrOrder.FSKAsHashKeyInterface;

/**
 * 
 * Description:
 * <p>
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class InvokeInstructionRecord implements FSKAsHashKeyInterface {

    /**
     * value is HashMap<Long, GCInstruction>
     */
    private HashMap<FunctionStateKey, FunctionStateKeyMapValue> invokeInstrMap = new HashMap<FunctionStateKey, FunctionStateKeyMapValue>();
    private static final InvokeInstructionRecord myObj = new InvokeInstructionRecord();

    public void updateHashMapUsingFSK() {
        FunctionStateKeyMapValue.update(invokeInstrMap);
    }

    /**
     * private constructor
     */
    private InvokeInstructionRecord() {
    }

    public static final InvokeInstructionRecord getInstanceOf() {
        return myObj;
    }

    /**
     * which method has which invoke instruction
     * @param method
     * @param params
     * @param invokeInstr
     */
    public void addInvokeInstrRecord(MethodInfo method, Vector params, GCInstruction invokeInstr) {
        FunctionStateKey key = new FunctionStateKey(method, params);
        addInvokeInstrRecord(key, invokeInstr);
    }

    /**
     * 
     * @param key
     * @param invokeInstr
     */
    private void addInvokeInstrRecord(FunctionStateKey key, GCInstruction invokeInstr) {
        if (!invokeInstr.getMnemonic().contains("INVOKE")
                && !invokeInstr.getMnemonic().contains("PUTSTATIC")
                && !invokeInstr.getMnemonic().contains("GETSTATIC")) {
            return;
        }
        FunctionStateKeyMapValue invokeInstrSet = invokeInstrMap.get(key);
        HashMap value = null;
        if (invokeInstrSet == null) {
            value = new HashMap<Long, GCInstruction>();
            invokeInstrSet = new FunctionStateKeyMapValue(key, value);
            invokeInstrMap.put(key, invokeInstrSet);
        } else {
            value = (HashMap) invokeInstrSet.getValue();
        }
        value.put(invokeInstr.getInstructionId(), invokeInstr);
    }

    /**
     * 
     * @param method
     * @param params
     * @return
     */
    public Collection<GCInstruction> getInvokeInstrRecord(MethodInfo method, Vector params) {
        FunctionStateKey stateKey = new FunctionStateKey(method, params);
        return getInvokeInstrRecord(stateKey);
    }

    /**
     * 
     * @param key
     * @return
     */
    public Collection<GCInstruction> getInvokeInstrRecord(FunctionStateKey key) {
        FunctionStateKeyMapValue specialValue = invokeInstrMap.get(key);
        if (specialValue == null) {
            return new Vector();
        }
        HashMap invokeInstrSet = (HashMap) specialValue.getValue();
        return invokeInstrSet.values();
    }

    @Override
    public String toString() {
        String ret = "";
        Set<FunctionStateKey> keys = invokeInstrMap.keySet();
        Iterator<FunctionStateKey> it = keys.iterator();
        while (it.hasNext()) {
            FunctionStateKey key = it.next();
            ret = ret + "\nFunction=[" + key + "], invoke-Indexes " + invokeInstrMap.get(key);
        }
        return ret;
    }
}
