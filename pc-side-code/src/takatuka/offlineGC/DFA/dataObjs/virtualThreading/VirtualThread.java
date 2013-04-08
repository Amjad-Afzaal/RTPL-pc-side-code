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
package takatuka.offlineGC.DFA.dataObjs.virtualThreading;

import java.util.Vector;
import takatuka.classreader.dataObjs.*;
import takatuka.offlineGC.OGI.superInstruction.*;
import takatuka.offlineGC.DFA.dataObjs.GCType;
import takatuka.offlineGC.DFA.dataObjs.attribute.*;
import takatuka.offlineGC.DFA.dataObjs.flowRecord.*;
import takatuka.offlineGC.DFA.dataObjs.functionState.*;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;
import takatuka.verifier.dataObjs.*;
import takatuka.verifier.dataObjs.attribute.*;
import takatuka.verifier.logic.DFA.*;

/**
 * <p>Title: </p>
 * <p>Description:
 * It is represents virtual thread class of virtual interperter.
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class VirtualThread {

    /**
     * It is the object
     */
    private GCType myObject = null;
    /**
     * method from where the thread execution starts.
     */
    private MethodInfo myStartingMethod = null;
    /**
     * Instruction that has started the thread (request)
     */
    private GCInstruction instrStartedTheThread = null;
    private boolean threadStartedMultipleTimes = false;

    /**
     *
     * @param myObject
     * @param method is the starting method for the thread.
     * It is main method for the default thread. For rest of the threads it is
     * run method of a class that either extends Thread or implements Runnable
     */
    public VirtualThread(GCType myObject, MethodInfo method,
            GCInstruction instrStartedTheThread) {
        this.myObject = myObject;
        this.myStartingMethod = method;
        this.instrStartedTheThread = instrStartedTheThread;
    }

    public MethodCallInfo getStartingMethodCallInfo() {
        Oracle oracle = Oracle.getInstanceOf();
        Type type = getObjectType();
        FunctionsFlowRecorder flowRecorder = FunctionsFlowRecorder.getInstanceOf();
        MethodInfo startingMethod = getStartingMethod();
        int maxLocalSize = startingMethod.getCodeAtt().getMaxLocals().intValueUnsigned();
        String methodDesc = oracle.methodOrFieldDescription(startingMethod, GlobalConstantPool.getInstanceOf());
        LocalVariables lc = InitializeFirstInstruction.createLocalVariablesOfFirstInstruction(maxLocalSize,
                methodDesc, startingMethod.getAccessFlags().isStatic(), type);
        Vector localVariables = null;
        if (lc != null) {
            localVariables = lc.getAll();
        }
        return flowRecorder.getFunctionFlowNode(startingMethod, localVariables);
    }

    /**
     * 
     * @return
     */
    public GCInstruction getInstrStartedTheThread() {
        return instrStartedTheThread;
    }

    /**
     * If the thread is started multiple times or not.
     * @param threadStartedMulTimes
     */
    public void setThreadStartedMultipleTimes(boolean threadStartedMulTimes) {
        this.threadStartedMultipleTimes = threadStartedMulTimes;
    }

    /**
     * 
     * @return
     */
    public boolean getThreadStartedMultipleTimes() {
        return this.threadStartedMultipleTimes;
    }


    /**
     *
     * @return
     */
    public GCType getObjectType() {
        return myObject;
    }

    /**
     *
     * @return
     */
    public MethodInfo getStartingMethod() {
        return myStartingMethod;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof VirtualThread)) {
            return false;
        }
        VirtualThread input = (VirtualThread) obj;
        if ((input.myObject == null && myObject == null) || input.myObject.equals(myObject)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + (this.myObject != null ? this.myObject.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        Oracle oracle = Oracle.getInstanceOf();
        String ret = "";
        if (myObject != null) {
            ret = myObject.toString() + ", ";
        } else {
            ret = "NULL passing Type, ";
        }
        ret += "starting-Method=" + oracle.getMethodOrFieldString(myStartingMethod);
        return ret;
    }
}
