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
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.constantPool.*;
import takatuka.classreader.logic.constants.*;
import takatuka.classreader.logic.util.*;
import takatuka.offlineGC.DFA.dataObjs.*;
import takatuka.offlineGC.DFA.dataObjs.attribute.*;
import takatuka.offlineGC.DFA.dataObjs.flowRecord.*;
import takatuka.offlineGC.DFA.dataObjs.functionState.*;
import takatuka.offlineGC.DFA.logic.*;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;
import takatuka.verifier.dataObjs.*;
import takatuka.verifier.logic.factory.*;
import takatuka.verifier.logic.DFA.*;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class OrderManagerForFieldInstrs implements FSKAsHashKeyInterface {

    private static final OrderManagerForFieldInstrs myObj = new OrderManagerForFieldInstrs();
    private ControllerForFieldInstr contrForField = ControllerForFieldInstr.getInstanceOf();
    /**
     * value = HashSet<FieldRecord>
     */
    private HashMap<FunctionStateKey, FunctionStateKeyMapValue> fieldsToSetDirtyInUnderCallMethod =
            new HashMap<FunctionStateKey, FunctionStateKeyMapValue>();
    private HashMap<FunctionStateKey, FunctionStateKeyMapValue> fieldsToSetDirtyAfterMethodCalled =
            new HashMap<FunctionStateKey, FunctionStateKeyMapValue>();
    private Oracle oracle = Oracle.getInstanceOf();
    private static boolean shouldDebugPrint = false;

    public static void debugPrint(Object obj1, Object obj2) {
        if (shouldDebugPrint) {
            System.out.println(obj1 + "" + obj2);
        }
    }

    private OrderManagerForFieldInstrs() {
    }

    public void updateHashMapUsingFSK() {
        FunctionStateKeyMapValue.update(fieldsToSetDirtyInUnderCallMethod);
        FunctionStateKeyMapValue.update(fieldsToSetDirtyAfterMethodCalled);
    }

    public static OrderManagerForFieldInstrs getInstanceOf() {
        return myObj;
    }

    /**
     * This method is called when GetStatic or GetField or AALoad.
     * It adds record of those instructions so that
     * they can be made dirty when putfield or putstatic is used for the same
     * field.
     * 
     * @param method
     * @param callingParams
     * @param instr
     * @param stack
     * @param localVar
     */
    public void addRecord(MethodInfo method, Vector callingParams,
            GCInstruction instr, GCOperandStack stack, GCLocalVariables localVar) {
        if (!isFieldIsObjectBased(instr)
                || (instr.getOpCode() != JavaInstructionsOpcodes.GETSTATIC
                && instr.getOpCode() != JavaInstructionsOpcodes.GETFIELD
                && instr.getOpCode() != JavaInstructionsOpcodes.AALOAD)) {
            return;
        }
        addRecord(new FunctionStateKey(method, callingParams), instr, stack, localVar);
    }

    /**
     * Should be called Before virtual execution of instructions
     * putfield and putstatic so that all corresponding getFields are set dirty.
     * Similarly should also be called before virtual execution of instructions
     * AASTORE so that corresponding AALOAD are set dirty.
     *
     * @param method
     * @param callingParms
     * @param instr
     * @param stack
     * @param localVariables
     */
    public void fixOrderRelatedInstrs(MethodInfo method, Vector callingParms,
            GCInstruction instr, GCOperandStack stack, GCLocalVariables localVariables) {
        if (!isFieldIsObjectBased(instr)) {
            return;
        }
        String methodStr = oracle.getMethodOrFieldString(method);
        if (instr.getOpCode() == JavaInstructionsOpcodes.PUTSTATIC
                || instr.getOpCode() == JavaInstructionsOpcodes.PUTFIELD
                || instr.getOpCode() == JavaInstructionsOpcodes.AASTORE) {
            GCType type = (GCType) stack.peep();
            if (!type.isReference()) {
                return;
            }
            HashSet<TTReference> refSet = type.getReferences();
            if (refSet.size() == 1) {
                TTReference ref = refSet.iterator().next();
                if (ref.getClassThisPointer() == Type.NULL || ref.getNewId() < 0) {
                    return;
                }
            }
            debugPrint("\n\n++++++++++ ", "fix-Order");
            debugPrint("method Str =", oracle.getMethodOrFieldString(method));
            debugPrint("instr =", instr);
            handlePutRecalls(new MethodCallInfo(method, callingParms),
                    instr, stack);
        }
    }

    private void putInMap(HashMap<FunctionStateKey, FunctionStateKeyMapValue> map,
            FunctionStateKey callInfo,
            FieldRecord getFieldRecord) {

        FunctionStateKeyMapValue specialValue = map.get(callInfo);
        HashSet<FieldRecord> value = null;
        if (specialValue == null) {
            value = new HashSet<FieldRecord>();
            specialValue = new FunctionStateKeyMapValue(callInfo, value);
            map.put(callInfo, specialValue);
            value.add(getFieldRecord);
        } else {
            value = (HashSet<FieldRecord>) specialValue.getValue();
            value.add(getFieldRecord);
        }
    }

    public void removeForMapForDirtyGetFieldInstr(FunctionStateKey methodCallInfo) {
        fieldsToSetDirtyAfterMethodCalled.remove(methodCallInfo);
    }

    public HashMap<FunctionStateKey, FunctionStateKeyMapValue> getAfterCallMethodWithDirtyGetFieldInstr() {
        return fieldsToSetDirtyAfterMethodCalled;
    }

    public HashSet<FieldRecord> getAndRemoveUnderExecMethodWithDirtyGetFieldInstr(FunctionStateKey methodCallInfo) {
        FunctionStateKeyMapValue value = fieldsToSetDirtyInUnderCallMethod.remove(methodCallInfo);
        if (value == null) {
            return null;
        }
        return (HashSet<FieldRecord>) value.getValue();
    }

    /**
     * In case the method is still under-call then put the record of the method fields where
     * get was used in a seperate map. When a method any next instruction is called again
     * then the record is pop from the map and corresponding instructions are made dirty.
     *
     * This method create that record of method fields.
     *
     * @param methodCallInfo
     * @param instrDataSettingInstr
     */
    private void handlePutRecalls(MethodCallInfo methodCallInfo,
            GCInstruction instrDataSettingInstr,
            OperandStack stack) {
        if (true) return;
        try {
            /**
             * 1. Firstly get from input instructions the uniqueId for corresponding
             * data reterival instructions.
             * 2. Subsequently, add those instructions in the two different maps
             * depending upon if the method is still under call or not.
             */
            ControllerForFieldInstr contr = ControllerForFieldInstr.getInstanceOf();
            HashSet<Integer> uniqueIdForDataRetrivalInstr = new HashSet<Integer>();
            if (instrDataSettingInstr.getOpCode() != JavaInstructionsOpcodes.AASTORE) {
                int cpIndex = instrDataSettingInstr.getOperandsData().intValueUnsigned();
                uniqueIdForDataRetrivalInstr.add(cpIndex);
            } else {
                uniqueIdForDataRetrivalInstr = getAAStoreNewIds(stack);
            }
            /**
             * following are the all getField or getStatic instruction for the given
             * constant pool index.
             */
            HashSet<FieldRecord> getFieldOrStateRecordSet = contr.getRecords(uniqueIdForDataRetrivalInstr,
                    instrDataSettingInstr.getOpCode() == JavaInstructionsOpcodes.AASTORE);
            /**
             * Now we iterate from all of those instructions and put them in two
             * different maps depenending up if their method is still under call or not.
             */
            Iterator<FieldRecord> fieldRectIt = getFieldOrStateRecordSet.iterator();
            while (fieldRectIt.hasNext()) {
                FieldRecord fieldRec = fieldRectIt.next();
                HashSet<FunctionStateKey> allTheMethodCallsForTheField = fieldRec.getStateKeys();
                Iterator<FunctionStateKey> methodCallIt = allTheMethodCallsForTheField.iterator();
                while (methodCallIt.hasNext()) {
                    FunctionStateKey stateKey = methodCallIt.next();
                    GCDataFlowAnalyzer dataFlowAnalyzer = GCDataFlowAnalyzer.getInstanceOf();
                    HashSet<FunctionStateKey> allMethodInExecution = dataFlowAnalyzer.getMethodsInExecution();
                    String methodString = oracle.getMethodOrFieldString(stateKey.getMethod());
                    long instrId = fieldRec.getInstr().getInstructionId();
                    if (allMethodInExecution.contains(stateKey)) {
                        putInMap(fieldsToSetDirtyInUnderCallMethod, stateKey, fieldRec);
                    } else {
                        putInMap(fieldsToSetDirtyAfterMethodCalled, stateKey, fieldRec);
                    }
                }
            }
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }

    }

    /**
     * Call it BEFORE executing an instruction virtually.
     * 
     * @param instr
     * @param stack
     * @param localVar
     */
    private void addRecord(FunctionStateKey stateKey, GCInstruction instr,
            GCOperandStack stack, GCLocalVariables localVar) {

        MethodInfo method = instr.getMethod();
        String methodStr = oracle.getMethodOrFieldString(method);
        if (methodStr.contains("jvmTestCases.Main.fooGetOnly")) {
        }
        debugPrint("\n\n", "***** adding record *****");
        debugPrint("method Str=", methodStr);
        debugPrint("instr =", instr);
        HashSet<Integer> unqiueForField = new HashSet<Integer>();
        if (instr.getOpCode() != JavaInstructionsOpcodes.AALOAD) {
            int cpIndex = instr.getOperandsData().intValueUnsigned();
            unqiueForField.add(cpIndex);
        } else {
            unqiueForField = getAALOADNewIds(stack);
        }
        Iterator<Integer> it = unqiueForField.iterator();
        while (it.hasNext()) {
            contrForField.addRecord(stateKey, instr, it.next());
        }
    }

    private HashSet<Integer> getAAStoreNewIds(OperandStack stack) {
        int currentSizeOfStack = stack.getCurrentSize();
        GCType type = (GCType) stack.get(currentSizeOfStack - 3);
        HashSet<Integer> toRet = type.getAllNewIds();
        toRet.remove(-1);
        return toRet;
    }

    private HashSet<Integer> getAALOADNewIds(OperandStack stack) {
        HashSet<Integer> toRet = new HashSet<Integer>();
        try {
            int currentSizeOfStack = stack.getNumberOfTypesInStack();
            GCType type = (GCType) stack.get(currentSizeOfStack - 2);
            toRet = type.getAllNewIds();
            toRet.remove(-1);
        } catch (Exception d) {
            d.printStackTrace();
            System.exit(1);
        }
        return toRet;
    }

    /**
     * check if the field saves an object.
     * @param instr
     * @return
     */
    private boolean isFieldIsObjectBased(GCInstruction instr) {
        if (instr.getOpCode() == JavaInstructionsOpcodes.AALOAD
                || instr.getOpCode() == JavaInstructionsOpcodes.AASTORE) {
            return true;
        }
        if (instr.getOpCode() != JavaInstructionsOpcodes.PUTFIELD
                && instr.getOpCode() != JavaInstructionsOpcodes.GETFIELD
                && instr.getOpCode() != JavaInstructionsOpcodes.GETSTATIC
                && instr.getOpCode() != JavaInstructionsOpcodes.PUTSTATIC) {
            return false;
        }
        GlobalConstantPool pOne = GlobalConstantPool.getInstanceOf();

        int index = instr.getOperandsData().intValueUnsigned();
        FieldRefInfo fInfo = (FieldRefInfo) pOne.get(index,
                TagValues.CONSTANT_Fieldref);
        int nameAndTypeIndex = fInfo.getNameAndTypeIndex().intValueUnsigned();
        NameAndTypeInfo nAtInfo = (NameAndTypeInfo) pOne.get(nameAndTypeIndex,
                TagValues.CONSTANT_NameAndType);

        String description = ((UTF8Info) pOne.get(nAtInfo.getDescriptorIndex().
                intValueUnsigned(),
                TagValues.CONSTANT_Utf8)).convertBytes();
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        Type fieldType = frameFactory.createType();
        InitializeFirstInstruction.getType(description, 0, fieldType);
        return fieldType.isReference();

    }
}
