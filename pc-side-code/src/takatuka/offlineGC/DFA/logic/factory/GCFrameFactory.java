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
package takatuka.offlineGC.DFA.logic.factory;

import takatuka.offlineGC.DFA.logic.GCDataFlowAnalyzer;
import takatuka.offlineGC.DFA.dataObjs.*;
import takatuka.offlineGC.logic.*;
import takatuka.classreader.dataObjs.MethodInfo;
import takatuka.verifier.dataObjs.*;
import takatuka.verifier.logic.DFA.*;
import java.util.*;
import takatuka.offlineGC.DFA.logic.*;
import takatuka.optimizer.VSS.logic.factory.SSFrameFactory;
/**
 * 
 * Description:
 * <p>
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class GCFrameFactory extends SSFrameFactory {

    private static final GCFrameFactory frameFactory = new GCFrameFactory();

    protected GCFrameFactory() {
        super();
    }

    public static GCFrameFactory getInstanceOf() {
        return frameFactory;
    }

    @Override
    public Type createType(int type, boolean isReference, int newId) {
        return new GCType(type, isReference, newId);
    }

    @Override
    public Type createType() {
        return new GCType();
    }

    @Override
    public Type createType(boolean isReference) {
        return new GCType(isReference);
    }

    @Override
    public Type createType(int type) {
        return new GCType(type);
    }

    @Override
    public OperandStack createOperandStack(int maxStack) {
        return new GCOperandStack(maxStack);
    }

    @Override
    public LocalVariables createLocalVariables(int maxStack) {
        return new GCLocalVariables(maxStack);
    }

    @Override
    public BytecodeVerifier createBytecodeVerifier(Frame frame,
            MethodInfo currentMethod,
            Vector callingParametrs) {
        return new GCBcodeInterpSimulator(frame, currentMethod, callingParametrs);
    }

    @Override
    public DataFlowAnalyzer createDataFlowAnalyzer() {
        return GCDataFlowAnalyzer.getInstanceOf();
    }


    /**
     *
     * @param stack
     * @param currentMethod
     * @param invokeInsrsInterpreter
     * @return
     */
    @Override
    public FieldInstrs createFieldInstrsInterpreter(OperandStack stack,
            MethodInfo currentMethod, InvokeInstrs invokeInsrsInterpreter) {
        return GCFieldInstrs.getInstanceOf(stack, currentMethod, invokeInsrsInterpreter);
    }

    /**
     *
     * @param nextPossibleInstructionsIds
     * @param stack
     * @param currentMethod
     * @return
     */
    @Override
    public IfAndCmpInstrs createIfAndCmpInstrsInterpreter(Vector<Long> nextPossibleInstructionsIds,
            OperandStack stack, MethodInfo currentMethod) {
        return GCIfAndCmpInstrs.getInstanceOf(nextPossibleInstructionsIds, stack, currentMethod);
    }


    @Override
    public InvokeInstrs createInvokeInstrsInterpreter(Frame frame,
            MethodInfo currentMethod,
            Vector methodCallingParameters,
            Vector nextPossibleInstructionsIds) {
        return GCInvokeInstrs.getInstanceOf(frame, currentMethod,
                 methodCallingParameters,
                nextPossibleInstructionsIds);
    }

    @Override
    public LoadAndStoreInstrs createLoadAndStoreInstrsInterpreter(Frame frame,
            MethodInfo currentMethod, Vector callingParams) {
        return GCLoadAndStoreInstrs.getInstanceOf(frame, currentMethod,
                 callingParams);
    }

    /**
     *
     * @param stack
     * @param currentMethod
     * @param currentPC
     * @return
     */
    @Override
    public MathInstrs createMathInstrsInterpreter(OperandStack stack,
            MethodInfo currentMethod, int currentPC) {
        return GCMathInstrs.getInstanceOf(stack, currentMethod, currentPC);
    }

 
    @Override
    public MiscInstrs createMiscInstrsInterpreter(Vector<Long> nextPossibleInstructionsIds,
            OperandStack stack, MethodInfo currentMethodStatic, LocalVariables localVar,
            int currentPC, HashSet<Type> returnTypes) {
        return GCMiscInstrs.getInstanceOf(nextPossibleInstructionsIds,
                stack, currentMethodStatic,
                localVar, currentPC, returnTypes);
    }

    @Override
    public ObjCreatorInstrs createObjCreatorInstrsInterpreter(OperandStack stack,
            MethodInfo currentMethod) {
        return GCObjCreatorInstrs.getInstanceOf(stack, currentMethod);
    }

    @Override
    public PureStackInstrs createPureStackInstrsInterpreter(OperandStack stack,
            MethodInfo currentMethod) {
        return GCPureStackInstrs.getInstanceOf(stack, currentMethod);
    }
}
