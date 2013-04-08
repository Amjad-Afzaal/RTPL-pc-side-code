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
package takatuka.offlineGC.DFA.dataObjs;


import takatuka.classreader.dataObjs.*;
import takatuka.offlineGC.DFA.dataObjs.functionState.*;
import java.util.*;
import takatuka.offlineGC.DFA.dataObjs.flowRecord.MethodCallInfo;

/**
 * 
 * Description:
 * <p>
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class TTReferenceSpecial extends TTReference {

    private static short specialTTRefCounter = Short.MAX_VALUE - 1;
    private HashSet<FunctionStateKey> stateKeySet = new HashSet<FunctionStateKey>();

 

/*    public TTReferenceSpecial(MethodInfo method, Vector params) {
        super(0, specialTTRefCounter--);
        this.stateKey = new FunctionStateKey(method, params);
    }
*/
    public TTReferenceSpecial(Set<MethodCallInfo> nodes) {
        super(0, specialTTRefCounter--);
        Iterator<MethodCallInfo> nodeIt = nodes.iterator();
        while (nodeIt.hasNext()) {
            MethodCallInfo node = nodeIt.next();
            FunctionStateKey stateKey = new FunctionStateKey(node.getMethod(), node.getCallingParameters());
            stateKeySet.add(stateKey);
        }
    }

    public static int getSmallestFakeNewId() {
        return specialTTRefCounter - 1;
    }

    public HashSet<FunctionStateKey> getFunctionStateKey() {
        return stateKeySet;
    }
}
