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
package takatuka.verifier.logic.DFA;

import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.constantPool.*;
import takatuka.classreader.logic.constants.*;
import takatuka.classreader.logic.factory.*;
import takatuka.verifier.dataObjs.*;
import takatuka.verifier.dataObjs.attribute.*;
import takatuka.verifier.logic.*;
import takatuka.verifier.logic.exception.*;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;
import takatuka.classreader.logic.util.*;
import takatuka.optimizer.cpGlobalization.logic.util.Oracle;
import takatuka.verifier.logic.factory.*;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class FieldInstrs {

    private static final FieldInstrs myObj = new FieldInstrs();
    protected static final GlobalConstantPool pOne = GlobalConstantPool.getInstanceOf();
    protected static final Oracle oracle = Oracle.getInstanceOf();
    protected static final HashSet<Integer> clinitClassesAlreadyInit = new HashSet<Integer>();
    protected static final String CLINIT_METHOD_NAME = "<clinit>";
    protected static final String VOID_METHOD_DESC = "()V";
    protected static final FactoryFacade factory = FactoryPlaceholder.getInstanceOf().
            getFactory();
    protected static InvokeInstrs invokeInstrVerifier = null;
    protected static OperandStack stack = null;
    protected static MethodInfo currentMethod = null;

    protected FieldInstrs() {
    }

    public static void init(OperandStack stack_, MethodInfo currentMethod_,
            InvokeInstrs invokeInstrVerifier_) {
        stack = stack_;
        currentMethod = currentMethod_;
        invokeInstrVerifier = invokeInstrVerifier_;
    }

    public static FieldInstrs getInstanceOf(OperandStack stack,
            MethodInfo currentMethod, InvokeInstrs invokeInstrVerifier) {
        init(stack, currentMethod, invokeInstrVerifier);
        return myObj;
    }


    /**
     * This function get called when the bytecode instructions
     * PUTSTATIC or GETSTATIC are used.
     *
     * @param instr
     */
    protected void dealWithClinit(VerificationInstruction instr) {
        try {
            int cpIndexOfFieldInfo = instr.getOperandsData().intValueUnsigned();
            FieldRefInfo staticFieldRef = (FieldRefInfo) pOne.get(cpIndexOfFieldInfo,
                    TagValues.CONSTANT_Fieldref);
            int classThis = staticFieldRef.getIndex().intValueUnsigned();
            ClassFile methodClass = oracle.getClass(classThis, pOne);
            DataFlowAnalyzer.Debug_print(" method class =",methodClass.getFullyQualifiedClassName());
            if (clinitClassesAlreadyInit.contains(classThis)) {
                return; //already inside the same clinit.
            }

            clinitClassesAlreadyInit.add(classThis);
            int nATIndex = oracle.findNameAndType_GCP(oracle.getUTF8InfoIndex(CLINIT_METHOD_NAME),
                    oracle.getUTF8InfoIndex(VOID_METHOD_DESC));
            ReferenceInfo methodRef = factory.createMethodRefInfo(methodClass.getThisClass(),
                    factory.createUn(nATIndex).trim(2));
            ClassFile oldClassFile = ClassFile.currentClassToWorkOn;
            invokeInstrVerifier.jumpToNxtFun(new Vector(), methodRef,
                    true, false, VOID_METHOD_DESC, instr, CLINIT_METHOD_NAME);
            //Miscellaneous.println("abc +" +oracle.getMethodString(currentMethod));
            currentMethod = invokeInstrVerifier.getCurrentMethod();
            //Miscellaneous.println("xyz +" +oracle.getMethodString(currentMethod));
            ClassFile.currentClassToWorkOn = oldClassFile;
            clinitClassesAlreadyInit.remove(classThis);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }

    }

    protected void fieldGetExecute(VerificationInstruction inst) {
        int opcode = inst.getOpCode();
        if (opcode == JavaInstructionsOpcodes.GETSTATIC) {
            dealWithClinit(inst);
        }

        if (opcode == JavaInstructionsOpcodes.GETFIELD) {
            getFieldInstruction(inst, false);
        } else if (opcode == JavaInstructionsOpcodes.GETSTATIC) {
            getFieldInstruction(inst, true);
        } else {
            throw new VerifyErrorExt(Messages.INVALID_BYTECODE + inst);
        }
    }

    protected void fieldPutExecute(VerificationInstruction inst) throws Exception {
        int opcode = inst.getOpCode();
        if (opcode == JavaInstructionsOpcodes.PUTSTATIC) {
            dealWithClinit(inst);
        }

        if (opcode == JavaInstructionsOpcodes.PUTFIELD) {
            putFieldInstruction(inst, false);
        } else if (opcode == JavaInstructionsOpcodes.PUTSTATIC) {
            putFieldInstruction(inst, true);
        } else {
            throw new VerifyErrorExt(Messages.INVALID_BYTECODE + inst);
        }
    }

    /**
     * operand: index to constant pool which should point to fieldrefinfo object
     * ..., objectref  ==> ..., value
     * pop ref from stack. That put back value related to that ref.
     *
     */
    protected void getFieldInstruction(VerificationInstruction inst, boolean isStatic) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        if (!isStatic) {
            Type ref = stack.pop();
            if (!ref.isReference()) {
                throw new VerifyErrorExt(Messages.STACK_INVALID);
            }

        }
        //cp index
        int index = inst.getOperandsData().intValueUnsigned();
        FieldRefInfo fInfo = (FieldRefInfo) pOne.get(index,
                TagValues.CONSTANT_Fieldref);
        int nameAndTypeIndex = fInfo.getNameAndTypeIndex().intValueUnsigned();
        NameAndTypeInfo nAtInfo = (NameAndTypeInfo) pOne.get(nameAndTypeIndex,
                TagValues.CONSTANT_NameAndType);
        String description = ((UTF8Info) pOne.get(nAtInfo.getDescriptorIndex().
                intValueUnsigned(),
                TagValues.CONSTANT_Utf8)).convertBytes();
        Type type = frameFactory.createType();
        InitializeFirstInstruction.getType(description, 0, type);
        stack.push(type);
    }

    protected void putFieldConvertion(Type fieldTypeBasedOnStack,
            VerificationInstruction inst, boolean isStatic) throws Exception {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        GlobalConstantPool gcp = GlobalConstantPool.getInstanceOf();
        Un operand = inst.getOperandsData();
        ReferenceInfo refInfo = (ReferenceInfo) gcp.get(operand.intValueUnsigned(), TagValues.CONSTANT_Fieldref);
        String description = oracle.methodOrFieldDescription(refInfo, gcp);
        Type fieldTypeBasedOnDescription = frameFactory.createType();
        InitializeFirstInstruction.getType(description, 0, fieldTypeBasedOnDescription);
        if (fieldTypeBasedOnDescription.getBlocks() != fieldTypeBasedOnStack.getBlocks()) {
            if (!fieldTypeBasedOnStack.isIntOrShortOrByteOrBooleanOrCharType()
                    || !fieldTypeBasedOnDescription.isIntOrShortOrByteOrBooleanOrCharType()) {
                throw new VerifyErrorExt(Messages.STACK_INVALID);
            }
        }
    }

    /**
     * ..., objectref, value ==>  ...
     *
     * @param inst
     * @param isStatic
     * @throws java.lang.Exception
     */
    protected void putFieldInstruction(VerificationInstruction inst, boolean isStatic) throws Exception {
        Type fieldValue = (Type) stack.pop().clone(); //pop value
        putFieldConvertion(fieldValue, inst, isStatic);
        if (!isStatic) {
            //pop reference
            Type value1 = stack.pop();
            if (!value1.isReference() || value1.isArrayReference()) {
                throw new VerifyErrorExt(Messages.STACK_INVALID);
            }
        }
    }
}
