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
package takatuka.offlineGC.DFA.logic;

import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.util.*;
import takatuka.offlineGC.DFA.dataObjs.*;
import takatuka.offlineGC.DFA.dataObjs.flowRecord.*;
import takatuka.offlineGC.DFA.dataObjs.functionState.*;
import takatuka.verifier.dataObjs.*;
import takatuka.offlineGC.DFA.logic.partialInstrOrder.*;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;
import takatuka.verifier.logic.factory.*;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class DummyReturnTypeManager implements FSKAsHashKeyInterface {

    private static final DummyReturnTypeManager myObj = new DummyReturnTypeManager();
    private static Oracle oracle = Oracle.getInstanceOf();
    private static GlobalConstantPool pOne = GlobalConstantPool.getInstanceOf();
    /**
     * value = GCType
     */
    private HashMap<FunctionStateKey, FunctionStateKeyMapValue> record =
            new HashMap<FunctionStateKey, FunctionStateKeyMapValue>();

    private DummyReturnTypeManager() {
    }

    public static final DummyReturnTypeManager getInstanceOf() {
        return myObj;
    }

    public void updateHashMapUsingFSK() {
        FunctionStateKeyMapValue.update(record);
    }

    /**
     * methods which are already executed once are virtually executed by
     * returning a dummy variable.
     * This function checks for previous execution and returns dummy variable.
     * 
     * @param method
     * @param localVariables
     * @param callerStack
     * @return 
     */
    protected boolean executeVirtually(MethodInfo method, Vector localVariables,
            OperandStack callerStack) {
        FunctionsFlowRecorder flowRecorder = FunctionsFlowRecorder.getInstanceOf();
        FunctionStateRecorder stateRecorder = FunctionStateRecorder.getInstanceOf();

        //Check if method has been called before.
        boolean isinvokedBefore = flowRecorder.invokedBefore(method, localVariables);
        String methodStr = oracle.getMethodOrFieldString(method);
        if (method.getAccessFlags().isAbstract()) {
            Miscellaneous.printlnErr("cannot find the implementation of the abstract method Error!");
            Miscellaneous.exit();
        }
        /**
         * If method is not invoked before and it has
         * code in it then return without providing a dummy
         * type. This method will be formally executed.
         */
        if (!isinvokedBefore
                && !method.getInstructions().isEmpty()) {
            return false;
        }
        
        /**
         * If method state already exists then return its dummy type.
         * Method state should always exist unless method is of recursive
         * and is called again from within itself
         */
        if (stateRecorder.isStateExist(method, localVariables)) {
            Type retType = stateRecorder.getFunctionReturnType(method, localVariables);
            if (retType != null) {
                callerStack.push(retType);
            } else if (!methodStr.endsWith(")V")) {
                Miscellaneous.printlnErr("error # 23242");
                Miscellaneous.println("method =\t" + oracle.getMethodOrFieldString(method));
                Miscellaneous.exit();
            }
        } else {
            /**
             * the method is recursive hence return a dummy type to 
             * break the infinite loop.
             */
            setMethodDummyReturnType(method, localVariables, callerStack);
            //return dummy what method supposed to return
        }
        return true;

    }

    /**
     * The function must be called from workAfterMethodCall...
     * 
     * @param method
     * @param params
     * @param callerStack
     */
    public void updateReturnType(MethodInfo method, Vector params, OperandStack callerStack) {
        FunctionStateKey key = new FunctionStateKey(method, params);
        FunctionStateKeyMapValue specialValue = record.get(key);
        if (specialValue == null) {
            return;
        }
        GCType value = (GCType) specialValue.getValue();
        GCType returnType = (GCType) callerStack.peep();
        HashSet<TTReference> returnRefSet = (HashSet<TTReference>) returnType.getReferences().clone();

        value.addReferences(returnRefSet);
        while (value.getChildClone() != null) {
            value = value.getChildClone();
            value.addReferences(returnRefSet);
        }
        ContrOfClassesUsingFSKAsHashKey.getInstanceOf().updateHashMapUsingFSK();


    }

    private void setMethodDummyReturnType(MethodInfo method, Vector params, OperandStack callerStack) {
        //1) Get method description
        String descr = oracle.methodOrFieldDescription(method, pOne);
        //2) Get the method return ret
        GCType type = (GCType) getMethodDummyReturnType(descr);
        if (!type.isReference() && type.getType() == Type.VOID) {
            return;
        }
        callerStack.push(type);
        if (method.getInstructions().size() != 0) {
            FunctionStateKey key = new FunctionStateKey(method, (Vector) params.clone());
            FunctionStateKeyMapValue value = new FunctionStateKeyMapValue(key, type);
            record.put(key, value);
        }
    }

    /**
     * it return 0 is void.
     * return -1 if reference and -2 if arrayreference
     * and return ret otherwise.
     *
     * @param descr
     * @return
     */
    private static int getMethodDummyReturnType(char descr) {
        int type = 0;
        if (descr == 'V') {
            return Type.VOID;
        }
        if (descr == '[') {
            type = -2;  //array reference.
        } else if (descr == 'L') {
            type = -1; //reference
        } else if (descr == 'B') {
            type = Type.BYTE;
        } else if (descr == 'C') {
            type = Type.CHAR;
        } else if (descr == 'I') {
            type = Type.INTEGER;
        } else if (descr == 'S') {
            type = Type.SHORT;
        } else if (descr == 'F') {
            type = Type.FLOAT;
        } else if (descr == 'Z') {
            type = Type.BOOLEAN;
        } else if (descr == 'D') {
            type = Type.DOUBLE;
        } else if (descr == 'J') {
            type = Type.LONG;
        }
        return type;
    }

    public static Type getMethodDummyReturnType(String descr) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        descr = descr.substring(descr.indexOf(")") + 1);
        int index = 0;
        int retType = getMethodDummyReturnType(descr.charAt(index));
        boolean isReference = false;
        boolean isArrayRef = false;
        while (retType == -2) {
            isArrayRef = true;
            isReference = true;
            index++;
            retType = getMethodDummyReturnType(descr.charAt(index));
        }
        if (retType == -1) { // reference
            isReference = true;
            retType = getReferenceType(descr, index);
        }
        Type toReturn = frameFactory.createType(retType, isReference, -1);
        if (isArrayRef) {
            toReturn.setIsArray();
        }
        return toReturn;
    }

    private static int getReferenceType(String name, int refIndex) {
        int cur = refIndex;
        int start = cur + 1;
        String refName = name.substring(start, name.indexOf(";", start));
        return Oracle.getInstanceOf().getClassInfoByName(refName);
    }
}
