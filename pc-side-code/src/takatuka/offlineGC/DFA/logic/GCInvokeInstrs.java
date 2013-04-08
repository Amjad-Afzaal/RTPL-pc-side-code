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
package takatuka.offlineGC.DFA.logic;

import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.constantPool.*;
import takatuka.classreader.logic.constants.*;
import takatuka.offlineGC.DFA.dataObjs.flowRecord.*;
import takatuka.verifier.dataObjs.*;
import takatuka.verifier.dataObjs.attribute.*;
import takatuka.verifier.logic.DFA.*;
import takatuka.classreader.logic.util.*;
import takatuka.offlineGC.DFA.dataObjs.*;
import takatuka.offlineGC.DFA.dataObjs.functionState.FunctionStateKey;
import takatuka.offlineGC.DFA.dataObjs.virtualThreading.*;
import takatuka.optimizer.VSS.logic.DFA.*;
import takatuka.verifier.logic.factory.*;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * To verify invokestatic, invokespecial, invokevirtual, invokeinterface....
 * 
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class GCInvokeInstrs extends SSInvokeInstrs {

    private static final GCInvokeInstrs myObj = new GCInvokeInstrs();

    protected GCInvokeInstrs() {
    }

    public static GCInvokeInstrs getInstanceOf(Frame frame,
            MethodInfo currentMethod,
            Vector methodCallingParameters,
            Vector nextPossibleInstructionsIds) {
        init(frame, currentMethod,
                methodCallingParameters, nextPossibleInstructionsIds);
        return myObj;
    }

    /**
     *
     * It returns the classes on which method has to be called.
     * In case of static or speical it is always one class.
     * However, in case of virtual it could be many classes because we combine many
     * references together during virtual interpreter.
     *
     * @param parms
     * @param ref
     * @param isStatic
     * @param isSpecial
     * @param currentInstr
     * @return
     * @throws Exception
     */
    protected TreeSet<ClassFile> getMethodClasses(Vector parms, ReferenceInfo ref,
            boolean isStatic, boolean isSpecial, VerificationInstruction currentInstr) throws Exception {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        Un methodClassIndex = ref.getIndex();
        NameAndTypeInfo nAt = (NameAndTypeInfo) pOne.get(ref.getNameAndTypeIndex().
                intValueUnsigned(), TagValues.CONSTANT_NameAndType);

        ClassFile methodClass = oracle.getClass(methodClassIndex, pOne);
        String key = MethodInfo.createKey(nAt.getDescriptorIndex(), nAt.getIndex());
        GCType objectRefType = null;
        HashSet<TTReference> classC = new HashSet<TTReference>();

        /*
         * first get the right classC (See description at
         * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc6.html
         */
        if (!isStatic && !isSpecial) {
            objectRefType = (GCType) parms.elementAt(0);
            classC = objectRefType.getReferences();
        } else if (isSpecial) {
            int classCIndex = getTargetFunctionIfInvokeSpecial(ClassFile.currentClassToWorkOn,
                    oracle.getUTF8(nAt.getIndex().intValueUnsigned(), pOne),
                    methodClass);
            GCType temp = (GCType) frameFactory.createType(classCIndex, true, -1);
            classC = temp.getReferences();
        } else if (isStatic) {
            GCType temp = (GCType) frameFactory.createType(
                    methodClass.getThisClass().intValueUnsigned(),
                    true, -1);
            classC = temp.getReferences();
        }

        isCallingPamaeterUsedInMethodLookup(classC);
        /**
         * Now based on classC get the classses on which method has to be called.
         */
        return getMethodClasses(key, classC, methodClass, currentInstr);
    }

    private void isCallingPamaeterUsedInMethodLookup(HashSet<TTReference> classC) {
        Iterator<TTReference> it = classC.iterator();
        HashSet<TTReference> refInPara = getAllReferencesFromCallingParameters();
        while (it.hasNext()) {
            TTReference ref = it.next();
            if (refInPara.contains(ref)) {
                //Miscellaneous.println("----------- method ="+oracle.getMethodString(currentMethod)+" , "+
                //        ref.getNewId());

                FunctionsFlowRecorder.getInstanceOf().setCallingParaUsedInMethodLookups(new FunctionStateKey(currentMethod, methodCallingParameters));
                break;
            }
        }
    }

    private HashSet<TTReference> getAllReferencesFromCallingParameters() {
        Iterator<GCType> it = methodCallingParameters.iterator();
        HashSet<TTReference> ret = new HashSet<TTReference>();
        while (it.hasNext()) {
            GCType gcType = it.next();
            ret.addAll(gcType.getReferences());
        }
        return ret;
    }

    /**
     * Given classC references it returns classes on which method
     * has to be invoked
     *
     * @param methodKey
     * @param classCs
     * @param methodRefClass
     * @param currentInstr
     * @return
     * @throws Exception
     */
    protected TreeSet<ClassFile> getMethodClasses(String methodKey, HashSet<TTReference> classCs,
            ClassFile methodRefClass, VerificationInstruction currentInstr) throws Exception {
        Iterator<TTReference> it = classCs.iterator();
        TreeSet methodClasses = new TreeSet();
        boolean nullPointerCall = false;
        String fullyQualifiedName = ClassFile.currentClassToWorkOn.getFullyQualifiedClassName();
        while (it.hasNext()) {
            TTReference reference = it.next();
            if (reference.getClassThisPointer() == Type.NULL) {
                DataFlowAnalyzer.Debug_print("WARNING: Cannot call a function with a NULL pointer\n",
                         "class=" + fullyQualifiedName , ", method="
                        , oracle.methodOrFieldName(currentMethod, pOne)
                        , ", curr-Instr=" + currentInstr);
                DataFlowAnalyzer.Debug_print1("WARNING: it could produce NullPointerException during run time");
                nullPointerCall = true;
                continue;
            }
            ClassFile temp = methodClassLookup(reference.getClassThisPointer(), methodKey);
            if (temp != null) {
                methodClasses.add(temp);
            }
        }
        if (methodClasses.size() == 0 && classCs.size() != 0 && !nullPointerCall) {
            String str = "Cannot find method (invoke error):  current Instr =" + currentInstr + ", method="
                    + oracle.methodOrFieldName(currentMethod, pOne)
                    + ", " + oracle.methodOrFieldDescription(currentMethod, pOne) + ", "
                    + "current class =" + fullyQualifiedName;
            Miscellaneous.printlnErr(str);
            new Exception().printStackTrace();
            Miscellaneous.exit();
        }
        return methodClasses;

    }

    @Override
    protected void jumpToNxtFun(Vector parms, ReferenceInfo ref,
            boolean isStatic, boolean isSpecial,
            String methodDesc,
            VerificationInstruction currentInstr,
            String methodNameForDebug) throws Exception {
        if (methodNameForDebug.equals("start")) {
            //DataFlowAnalyzer.shouldDebugPrint = true;
            //Miscellaneous.println("here there here");
        }
        //save previous information
        /*1*/        ClassFile currentClass = ClassFile.currentClassToWorkOn;
        /*2*/        BytecodeVerifier bcLast = DataFlowAnalyzer.getLastSavedBytecodeInterpreter();

        NameAndTypeInfo nAt = (NameAndTypeInfo) pOne.get(ref.getNameAndTypeIndex().
                intValueUnsigned(), TagValues.CONSTANT_NameAndType);
        String key = MethodInfo.createKey(nAt.getDescriptorIndex(), nAt.getIndex());

        TreeSet<ClassFile> methodClasses = getMethodClasses(parms, ref, isStatic, isSpecial, currentInstr);
        if (methodClasses.size() == 0) {
            Type retType = DummyReturnTypeManager.getMethodDummyReturnType(methodDesc);
            if (!retType.isReference() && retType.getType() == Type.VOID) {
                //no push.
            } else {
                stack.push(retType);
            }
        } else {
            callMethod(methodClasses, key, parms, (GCOperandStack) stack, currentInstr);
        }

        //restore previous information
        /*1*/        bcLast.initilizeHelperClasses();
        /*2*/        ClassFile.currentClassToWorkOn = currentClass;
    }

    protected void callMethod(TreeSet<ClassFile> methodClasses, String methodKey,
            Vector parms, GCOperandStack stack,
            VerificationInstruction currentInstr) throws Exception {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        ClassFile methodClass = null;
        Iterator<ClassFile> it = methodClasses.iterator();
        Vector<Type> multiReturnValues = new Vector();
        while (it.hasNext()) {
            methodClass = it.next();
            ClassFile.currentClassToWorkOn = methodClass;
            if (ClassFile.currentClassToWorkOn == null) {
                //Miscellaneous.println("Stop here");
            }
            int size = stack.getCurrentSize();
            BytecodeVerifier bcLast = DataFlowAnalyzer.getLastSavedBytecodeInterpreter();

            DataFlowAnalyzer.Debug_print("\n\n",
                     ClassFile.currentClassToWorkOn.getFullyQualifiedClassName());
            DataFlowAnalyzer.Debug_print(" current method ",
                     oracle.getMethodOrFieldString(currentMethod));

            MethodInfo methodToBeCalled = methodClass.hasMethod(methodKey);
            DataFlowAnalyzer.Debug_print("see me 121a= ",
                     methodClass.getFullyQualifiedClassName(),
                     "-->" + oracle.methodOrFieldName(methodToBeCalled, pOne),
                     ", " + oracle.methodOrFieldDescription(methodToBeCalled, pOne),
                     ", " + parms + " size=" + parms.size());
            if (oracle.methodOrFieldName(methodToBeCalled, pOne).equals("getChars")) {
                // DataFlowAnalyzer.Debug_print("function state " + FunctionStateRecorder.getInstanceOf());
            }
            
            FunctionsFlowRecorder flowRecorder = FunctionsFlowRecorder.getInstanceOf();
            flowRecorder.addInvokeCall(currentMethod,
                    methodCallingParameters,
                    currentInstr, methodToBeCalled, parms);

            frameFactory.createDataFlowAnalyzer().execute(methodToBeCalled,
                    parms, stack);

            bcLast.initilizeHelperClasses();

            DataFlowAnalyzer.Debug_print("back in the function after a call = ",
                     oracle.methodOrFieldName(currentMethod, pOne)+
                     ", " , methodCallingParameters , " with stack " , stack);
            if (size < stack.getCurrentSize()) {
                //a value is returned
                multiReturnValues.addElement(stack.pop());
            }
        }
        mergeStackValue(multiReturnValues, stack);

    }

    private void mergeStackValue(Vector multipleReturns, GCOperandStack stack) throws Exception {
        Iterator<GCType> it = multipleReturns.iterator();
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        GCType reference = (GCType) frameFactory.createType(true);
        if (!it.hasNext()) {
            return;
        }
        while (it.hasNext()) {
            GCType type = it.next();
            if (!type.isReference()) {
                // If not reference than all return types will be same.
                // Hence no need to go in loop.
                if (it.hasNext()) {
                    //if more than one return then cannot merge their values
                    type.value = null;
                }
                stack.push(type);
                return;
            } else {
                reference = GCType.addReferences(type, reference);
                if (type.isArrayReference()) {
                    reference.setIsArray();
                }
            }
        }
        stack.push(reference);
    }
}
