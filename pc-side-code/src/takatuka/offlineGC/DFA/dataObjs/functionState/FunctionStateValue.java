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
package takatuka.offlineGC.DFA.dataObjs.functionState;

import java.util.*;
import takatuka.offlineGC.DFA.dataObjs.*;

/**
 * 
 * Description:
 * <p>
 *
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class FunctionStateValue {

    private HashMap<Long, FunctionStateValueElement> functionStateValueElms = new HashMap<Long, FunctionStateValueElement>();
    private GCType returnValue = null;
    private FunctionStateKey stateKey = null;


    /**
     * 
     * @param functionStateValueElms
     * @param returnValue
     * @param key
     */
    public FunctionStateValue(HashMap<Long, FunctionStateValueElement> functionStateValueElms,
            GCType returnValue, FunctionStateKey key) {
        this.functionStateValueElms = functionStateValueElms;
        this.returnValue = returnValue;
        this.stateKey = key;
    }

    /**
     * 
     * @param stateKey
     */
    public void setFunctionStateKey(FunctionStateKey stateKey) {
        this.stateKey = stateKey;
    }

    /**
     *
     * @return
     */
    public FunctionStateKey getFunctionStateKey() {
        return stateKey;
    }
    /**
     * 
     * @param refToBeDeleted
     */
    public void delete(HashSet<TTReference> refToBeDeleted) {
        Collection<FunctionStateValueElement> fsvCollection = functionStateValueElms.values();
        Iterator<FunctionStateValueElement> it = fsvCollection.iterator();
        while (it.hasNext()) {
            FunctionStateValueElement fSE = it.next();
            fSE.delete(refToBeDeleted);
        }
    }

    /**
     * 
     * @return
     */
    public Collection<FunctionStateValueElement> getAllStateElements() {
        return functionStateValueElms.values();
    }

    /**
     * 
     * @return
     */
    public HashMap<Long, FunctionStateValueElement> getFunctionStateValues() {
        return (HashMap<Long, FunctionStateValueElement>) functionStateValueElms.clone();
    }

    /**
     *
     * @return
     */
    public GCType getReturnType() {
        return returnValue;
    }

    @Override
    public String toString() {
        return "return Value =" + returnValue + ", function State ="
                + functionStateValueElms;
    }
}
