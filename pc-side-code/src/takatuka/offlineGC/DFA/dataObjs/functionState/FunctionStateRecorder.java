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
import takatuka.classreader.dataObjs.attribute.*;
import takatuka.offlineGC.DFA.dataObjs.*;
import takatuka.offlineGC.DFA.dataObjs.attribute.*;
import takatuka.offlineGC.DFA.logic.*;
import takatuka.offlineGC.DFA.logic.partialInstrOrder.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;
import takatuka.verifier.dataObjs.attribute.*;
import takatuka.verifier.logic.*;
import takatuka.verifier.logic.exception.*;

/**
 * 
 * Description:
 * <p>
 * Given a function calling local-variables (parameters) it saves the stack and 
 * local varaibles state at each instruction of that function.
 *
 *
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class FunctionStateRecorder implements FSKAsHashKeyInterface {

    /**
     * value = FunctionStateValue
     */
    private HashMap<FunctionStateKey, FunctionStateKeyMapValue> statePerMethodCall = new HashMap<FunctionStateKey, FunctionStateKeyMapValue>();
    private HashMap<String, HashMap<FunctionStateKey, FunctionStateKeyMapValue>> statePerMethod = new HashMap<String, HashMap<FunctionStateKey, FunctionStateKeyMapValue>>();
    private static final FunctionStateRecorder myObj = new FunctionStateRecorder();

    private FunctionStateRecorder() {
    }

    @Override
    public String toString() {
        return statePerMethodCall.keySet().toString();
    }

    public static final FunctionStateRecorder getInstanceOf() {
        return myObj;
    }

    /**
     * Take a key from a value and then
     */
    public void updateHashMapUsingFSK() {
        FunctionStateKeyMapValue.update(statePerMethodCall);
        reCreateStatePerMethod();
    }

    private void reCreateStatePerMethod() {
        statePerMethod.clear();
        Set keys = statePerMethodCall.keySet();
        Oracle oracle = Oracle.getInstanceOf();
        Iterator<FunctionStateKey> it = keys.iterator();
        while (it.hasNext()) {
            FunctionStateKey key = it.next();
            MethodInfo method = key.getMethod();
            String methodStr = oracle.getMethodOrFieldString(method);
            HashMap<FunctionStateKey, FunctionStateKeyMapValue> value = statePerMethod.get(methodStr);
            if (value == null) {
                value = new HashMap<FunctionStateKey, FunctionStateKeyMapValue>();
                statePerMethod.put(methodStr, value);
            }
            value.put(key, statePerMethodCall.get(key));
        }
    }

    public HashMap<FunctionStateKey, FunctionStateKeyMapValue> getFunctionStatePerMethod(MethodInfo method) {
        if (statePerMethod.size() == 0) {
            reCreateStatePerMethod();
        }
        Oracle oracle = Oracle.getInstanceOf();
        String methodStr = oracle.getMethodOrFieldString(method);
        HashMap map = statePerMethod.get(methodStr);
        if (map == null) {
            map = new HashMap();
        }
        return map;
    }

    /**
     * 
     * @param refToBeDeleted
     */
    public void delete(HashSet<TTReference> refToBeDeleted) {
        Collection<FunctionStateKeyMapValue> collection = statePerMethodCall.values();
        Iterator<FunctionStateKeyMapValue> it = collection.iterator();
        while (it.hasNext()) {
            FunctionStateKeyMapValue specialValue = it.next();
            FunctionStateValue value = (FunctionStateValue) specialValue.getValue();
            value.delete(refToBeDeleted);
        }
    }

    /**
     * 
     * @return
     */
    public Collection<FunctionStateValueElement> getStatesOfAllMethods() {
        Collection<FunctionStateKeyMapValue> collection = statePerMethodCall.values();
        Iterator<FunctionStateKeyMapValue> it = collection.iterator();
        Vector ret = new Vector();
        while (it.hasNext()) {
            FunctionStateKeyMapValue specialValue = it.next();

            FunctionStateValue value = (FunctionStateValue) specialValue.getValue();
            ret.addAll(value.getAllStateElements());
        }
        return ret;
    }

    /**
     * Records the method calling parameters and corresponding frame of each instruction.
     *
     *
     * @param method : Used to save information and also to create key.
     * @param callingParams : Used to create the key
     * @param callerStack : Used to store the return value of the function
     */
    public void addFunctionState(MethodInfo method, Vector callingParams, GCOperandStack callerStack) {
        callingParams = TransformCallingParameters.transformCallingParameters(method, callingParams);
        //This function call other addFunctionState function but takes lesser number of arguments.
        addFunctionState(method,
                callingParams, method.getInstructions(),
                getReturnValue(callerStack, method));
    }

    private GCType getReturnValue(GCOperandStack callerStack, MethodInfo method) {
        try {
            Vector<VerificationInstruction> instr = method.getCodeAtt().getInstructions();
            if (instr != null) {
                Oracle oracle = Oracle.getInstanceOf();
                String methodString = oracle.getMethodOrFieldString(method);

                if (!methodString.endsWith(")V")) {
                    if (callerStack != null) {
                        return (GCType) callerStack.peep();
                    }
                }
            }
        } catch (Exception d) {
            throw new VerifyErrorExt(Messages.STACK_INVALID);
        }
        return null;
    }

    /**
     * Records the method calling parameters and corresponding frame of each instruction.
     *
     *
     * @param method
     * @param callingParams : The paramerts used to call a method
     * @param methodInstrs : instruction Vector of the methods. It should have VerificationInstruction in it.
     * @param returnValue
     */
    private void addFunctionState(MethodInfo method,
            Vector callingParams,
            Vector<Instruction> methodInstrs, GCType returnValue) {

        FunctionStateKey key = new FunctionStateKey(method,
                callingParams);

        HashMap<Long, FunctionStateValueElement> values = createValue(methodInstrs);
        FunctionStateValue value = new FunctionStateValue(values, returnValue, key);
        FunctionStateKeyMapValue specialValue = new FunctionStateKeyMapValue(key, value);
        statePerMethodCall.put(key, specialValue);
        //Miscellaneous.println("\n\n **** addeded with key = " + key);
        //Miscellaneous.println("added with values = " + values);

    }

    private HashMap<Long, FunctionStateValueElement> createValue(Vector<Instruction> methodInstrs) {
        HashMap<Long, FunctionStateValueElement> ret = new HashMap<Long, FunctionStateValueElement>();
        for (int loop = 0; loop < methodInstrs.size(); loop++) {
            GCInstruction instr = (GCInstruction) methodInstrs.elementAt(loop);
            FunctionStateValueElement fsvElm = new FunctionStateValueElement(instr,
                    instr.getLeavingOperandStack(),
                    (GCOperandStack) instr.getOperandStack(),
                    instr.getLeavingLocalVariables());
            ret.put(instr.getInstructionId(), fsvElm);
        }
        return ret;
    }

    public Set<FunctionStateKey> getAllRecordedFunctionStateKeys() {
        return statePerMethodCall.keySet();
    }

    /**
     * 
     * @param node
     * @return
     */
    public HashMap<Long, FunctionStateValueElement> getFunctionState(FunctionStateKey node) {
        HashMap map = getFunctionState(node.getMethod(), node.getCallingParameters());
        if (map == null) {
            map = new HashMap();
        }
        return map;
    }

    /**
     * returns the Vector whose elements are frame of the function
     * @param method
     * @param callingParams
     * @return
     */
    public HashMap<Long, FunctionStateValueElement> getFunctionState(MethodInfo method,
            Vector callingParams) {
        callingParams = TransformCallingParameters.transformCallingParameters(method, callingParams);
        FunctionStateKeyMapValue specialValue = statePerMethodCall.get(new FunctionStateKey(method, callingParams));
        if (specialValue == null) {
            return null;
        }
        FunctionStateValue value = (FunctionStateValue) specialValue.getValue();
        return value.getFunctionStateValues();
    }

    /**
     * returns the return type of a function.
     *
     * @param method
     * @param callingParams
     * @return
     */
    public GCType getFunctionReturnType(MethodInfo method, Vector callingParams) {
        callingParams = TransformCallingParameters.transformCallingParameters(method, callingParams);
        FunctionStateKeyMapValue specialValue = statePerMethodCall.get(new FunctionStateKey(method,
                callingParams));
        if (specialValue == null) {
            return null;
        }
        FunctionStateValue value = (FunctionStateValue) specialValue.getValue();
        return value.getReturnType();
    }

    public boolean isStateExist(MethodInfo method, Vector callingParams) {
        callingParams = TransformCallingParameters.transformCallingParameters(method, callingParams);
        //Miscellaneous.println("\n\n del me --->\n"+ map.keySet());
        return statePerMethodCall.get(new FunctionStateKey(method,
                callingParams)) != null;
    }
}
