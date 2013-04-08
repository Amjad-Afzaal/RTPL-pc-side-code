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
import takatuka.classreader.dataObjs.*;
import takatuka.offlineGC.DFA.logic.TransformCallingParameters;
import takatuka.verifier.dataObjs.Frame;
import takatuka.verifier.logic.DFA.InitializeFirstInstruction;

/**
 * 
 * Description:
 * <p>
 *
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class FunctionStateKey extends FunctionKey {

    private Vector callingParm = new Vector();

    /**
     *
     * @param method
     * @param callingParm
     */
    public FunctionStateKey(MethodInfo method, Vector callingParm) {
        super(method);
        if (method.getInstructions().size() == 0) {
            //return;
        }
        try {
            if (callingParm == null) {
                Frame frame = InitializeFirstInstruction.createFrameAndInitFirstInstr(method, null);
                callingParm = frame.getLocalVariables().getAll();
            }
            callingParm = TransformCallingParameters.transformCallingParameters(method, callingParm);
            this.callingParm = (Vector) callingParm.clone();
        } catch (Exception d) {
            d.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * 
     * @return
     */
    public Vector getCallingParameters() {
        return callingParm;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FunctionStateKey)) {
            return false;
        }
        FunctionStateKey input = (FunctionStateKey) obj;
        if (super.equals(obj)
                && input.callingParm.equals(callingParm)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (this.callingParm != null ? this.callingParm.hashCode() : 0);
        hash = 97 * hash + (super.hashCode());
        return hash;
    }

    @Override
    public String toString() {
        //return methodClassThisPointer + ", " + methodKey + ", " + callingParm;
        return "["+super.toString() + ", " + callingParm+"]\n";
    }
}


