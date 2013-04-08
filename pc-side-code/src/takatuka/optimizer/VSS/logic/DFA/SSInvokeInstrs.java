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
package takatuka.optimizer.VSS.logic.DFA;

import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.constantPool.*;
import takatuka.classreader.logic.constants.*;
import takatuka.verifier.dataObjs.*;
import takatuka.verifier.dataObjs.attribute.*;
import takatuka.verifier.logic.*;
import takatuka.verifier.logic.exception.*;
import takatuka.verifier.logic.DFA.*;

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
public class SSInvokeInstrs extends InvokeInstrs {

    private static final SSInvokeInstrs myObj = new SSInvokeInstrs();

    protected SSInvokeInstrs() {
    }

    public static SSInvokeInstrs getInstanceOf(Frame frame,
            MethodInfo currentMethod,
            Vector methodCallingParameters,
            Vector nextPossibleInstructionsIds) {
        init(frame, currentMethod,
                methodCallingParameters, nextPossibleInstructionsIds);
        return myObj;
    }

    @Override
    protected void invokeInstruction(String methodDesc, int opCode,
            ReferenceInfo ref, VerificationInstruction instr,
            String methodNameForDebug) {
        if (methodNameForDebug.contains("getMoteID")) {
            //Miscellaneous.println("stop here 232");
            boolean print = false;
            if (print) {
                DataFlowAnalyzer.shouldDebugPrint = true;
            }
        }
        Vector parms = verifyDescription(methodDesc, stack);
        Type classPointer = null;
        if (opCode != JavaInstructionsOpcodes.INVOKESTATIC) {
            classPointer = stack.pop();
            parms.add(0, classPointer);
            if (!classPointer.isReference()) {
                throw new VerifyError(Messages.STACK_INVALID);
            }
        }
        toConvertCallingParameters(parms, methodDesc,
                opCode == JavaInstructionsOpcodes.INVOKESTATIC, instr);
        //now call method to actually excute/verify.
        ClassFile oldClass = ClassFile.currentClassToWorkOn; //save the current class
        try {
            jumpToNxtFun(parms, ref,
                    opCode == JavaInstructionsOpcodes.INVOKESTATIC,
                    opCode == JavaInstructionsOpcodes.INVOKESPECIAL,
                    methodDesc, instr, methodNameForDebug);
        } catch (Exception d) {
            throw new VerifyErrorExt(""+d);
        }
        //set the class back to original class
        ClassFile.currentClassToWorkOn = oldClass;
        //DataFlowAnalyzer.Debug_print("back to class =" + ClassFile.currentClassToWorkOn.getFullyQualifiedClassName());
    }

    protected void toConvertCallingParameters(Vector<Type> methodPara,
            String methodDesc, boolean isStatic,
            VerificationInstruction currentInstr) {
        Vector<Type> typeVec = InitializeFirstInstruction.getMethodParametersTypes(methodDesc, isStatic);
        int methodParaIndex = 0;
        int stackIndex = 0;
        for (int loop = 0; loop < typeVec.size(); loop++) {
            Type basedOnMethodDesc = typeVec.elementAt(loop);
            if (!basedOnMethodDesc.isReference() && basedOnMethodDesc.getType() == Type.SPECIAL_TAIL) {
                continue;
            }
            Type basedOnCallerStack = methodPara.elementAt(methodParaIndex++);
            if (basedOnCallerStack.isReference() != basedOnMethodDesc.isReference()/* ||
                    basedOnCallerStack.isArrayReference() != basedOnMethodDesc.isArrayReference()*/) {
                throw new VerifyError(Messages.INVALID_FUNCTION_ARGUMENTS);

            } else if (!basedOnCallerStack.isReference() && basedOnCallerStack.getType() != basedOnMethodDesc.getType()) {
                if (basedOnCallerStack.isIntOrShortOrByteOrBooleanOrCharType() && basedOnMethodDesc.isIntOrShortOrByteOrBooleanOrCharType()) {
                    ConvertTypeUtil ct = ConvertTypeUtil.getInstanceOf(currentMethod);
                    //converstion is needed.
                    //Convert the types to the one described in the description.
                    ct.convertTypes(basedOnCallerStack, basedOnMethodDesc, stackIndex, currentInstr, true);
                } else if (basedOnMethodDesc.getBlocks() < basedOnCallerStack.getBlocks()) {
                    throw new VerifyError(Messages.INVALID_FUNCTION_ARGUMENTS);
                }
            }
            stackIndex += basedOnMethodDesc.getBlocks();
        }
    }
}
