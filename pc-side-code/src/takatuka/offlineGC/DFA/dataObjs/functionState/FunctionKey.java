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

import takatuka.classreader.dataObjs.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;

/**
 * 
 * Description:
 * <p>
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class FunctionKey {

    private MethodInfo method = null;
    private static Oracle oracle = Oracle.getInstanceOf();
    private int methodClass = -1;
    private int methodNameIndex = -1;
    private int methodDescIndex = -1;

    public FunctionKey(MethodInfo method) {
        this.method = method;
        this.methodClass = method.getClassFile().getThisClass().intValueUnsigned();
        this.methodDescIndex = method.getDescriptorIndex().intValueUnsigned();
        this.methodNameIndex = method.getNameIndex().intValueUnsigned();
    }

    /**
     * 
     * @return
     */
    public MethodInfo getMethod() {
        return method;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FunctionKey)) {
            return false;
        }
        FunctionKey input = (FunctionKey) obj;
        if (input.methodClass == methodClass &&
                input.methodDescIndex == methodDescIndex &&
                input.methodNameIndex == methodNameIndex) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.methodClass;
        hash = 29 * hash + this.methodNameIndex;
        hash = 29 * hash + this.methodDescIndex;
        return hash;
    }

    @Override
    public String toString() {
        return oracle.getMethodOrFieldString(method);
    }
}
