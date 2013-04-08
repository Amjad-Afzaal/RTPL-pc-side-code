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
package takatuka.optimizer.VSS.logic.factory;

import takatuka.verifier.logic.DFA.*;
import takatuka.classreader.dataObjs.MethodInfo;
import takatuka.verifier.dataObjs.*;
import java.util.*;
import takatuka.optimizer.VSS.dataObjs.*;
import takatuka.optimizer.VSS.logic.DFA.*;
import takatuka.verifier.logic.factory.*;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class SSFrameFactory extends VerificationFrameFactory {

    private static final SSFrameFactory frameFactory = new SSFrameFactory();

    protected SSFrameFactory() {
        super();
    }

    public static VerificationFrameFactory getInstanceOf() {
        return frameFactory;
    }

    @Override
    public OperandStack createOperandStack(int maxStack) {
        return new SSOperandStack(maxStack);
    }

    @Override
    public LocalVariables createLocalVariables(int maxStack) {
        return new SSLocalVariables(maxStack);
    }

    @Override
    public BytecodeVerifier createBytecodeVerifier(Frame frame, MethodInfo currentMethod,
            Vector callingParameters) {
        return new SSBytecodeVerifier(frame, currentMethod, callingParameters);
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
        return SSFieldInstrs.getInstanceOf(stack, currentMethod, invokeInsrsInterpreter);
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
        return SSIfAndCmpInstrs.getInstanceOf(nextPossibleInstructionsIds, stack, currentMethod);
    }

    @Override
    public InvokeInstrs createInvokeInstrsInterpreter(Frame frame,
            MethodInfo currentMethod,
            Vector methodCallingParameters,
            Vector nextPossibleInstructionsIds) {
        return SSInvokeInstrs.getInstanceOf(frame, currentMethod,
                methodCallingParameters,
                nextPossibleInstructionsIds);
    }

    @Override
    public LoadAndStoreInstrs createLoadAndStoreInstrsInterpreter(Frame frame,
            MethodInfo currentMethod,
            Vector callingParams) {
        return SSLoadAndStoreInstrs.getInstanceOf(frame, currentMethod,
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
        return SSMathInstrs.getInstanceOf(stack, currentMethod, currentPC);
    }

    @Override
    public MiscInstrs createMiscInstrsInterpreter(Vector<Long> nextPossibleInstructionsIds,
            OperandStack stack, MethodInfo currentMethodStatic, LocalVariables localVar,
            int currentPC, HashSet<Type> returnTypes) {
        return SSMiscInstrs.getInstanceOf(nextPossibleInstructionsIds,
                stack, currentMethodStatic,
                localVar, currentPC, returnTypes);
    }

    @Override
    public ObjCreatorInstrs createObjCreatorInstrsInterpreter(OperandStack stack,
            MethodInfo currentMethod) {
        return SSObjCreatorInstrs.getInstanceOf(stack, currentMethod);
    }

    @Override
    public PureStackInstrs createPureStackInstrsInterpreter(OperandStack stack,
            MethodInfo currentMethod) {
        return SSPureStackInstrs.getInstanceOf(stack, currentMethod);
    }
}
