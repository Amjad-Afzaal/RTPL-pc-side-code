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
package takatuka.offlineGC.DFA.dataObjs.functionState;

import java.util.*;

/**
 * <p>Title: </p>
 * <p>Description:
 * If function state is put in as a hashmap key then the value must always be
 * this class.
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class FunctionStateKeyMapValue {

    private FunctionStateKey key = null;
    private Object theValue = null;

    public FunctionStateKeyMapValue(FunctionStateKey key, Object theValue) {
        this.key = key;
        this.theValue = theValue;
    }

    public Object getValue() {
        return theValue;
    }

    public FunctionStateKey getFunctionStateKey() {
        return key;
    }

    /**
     * Take a key from a value and then
     */
    public static void update(HashMap map) {
        Collection valuesSet = map.values();
        HashMap temp = new HashMap();
        Iterator<FunctionStateKeyMapValue> it = valuesSet.iterator();
        while (it.hasNext()) {
            FunctionStateKeyMapValue value = it.next();
            temp.put(value.getFunctionStateKey(), value);
        }
        map.clear();
        Iterator tempKeySet = temp.keySet().iterator();
        while (tempKeySet.hasNext()) {
            Object key = tempKeySet.next();
            Object value = temp.get(key);
            map.put(key, value);
        }
    }
}
