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

import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.constantPool.*;
import takatuka.classreader.logic.constants.*;
import takatuka.verifier.dataObjs.*;
import takatuka.verifier.dataObjs.attribute.*;
import takatuka.verifier.logic.*;
import takatuka.verifier.logic.exception.*;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;
import takatuka.verifier.logic.factory.*;
import takatuka.verifier.logic.DFA.*;
import takatuka.verifier.logic.DFA.FieldInstrs;
import takatuka.verifier.logic.DFA.InvokeInstrs;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class SSFieldInstrs extends FieldInstrs {

    private static final SSFieldInstrs myObj = new SSFieldInstrs();

    protected SSFieldInstrs() {
    }

    public static SSFieldInstrs getInstanceOf(OperandStack stack,
            MethodInfo currentMethod, InvokeInstrs invokeInstrVerifier) {
        init(stack, currentMethod, invokeInstrVerifier);
        return myObj;
    }

    @Override
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
                int block1 = fieldTypeBasedOnDescription.getBlocks();
        InitializeFirstInstruction.getType(description, 0, fieldTypeBasedOnDescription);

                block1 = fieldTypeBasedOnStack.getBlocks();

                throw new VerifyErrorExt(Messages.STACK_INVALID);
            } else {
                ConvertTypeUtil ct = ConvertTypeUtil.getInstanceOf(currentMethod);
                ct.convertTypes(fieldTypeBasedOnStack, fieldTypeBasedOnDescription, 0, inst);
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
    @Override
    protected void putFieldInstruction(VerificationInstruction inst, boolean isStatic) throws Exception {
        super.putFieldInstruction(inst, isStatic);
    }
}
