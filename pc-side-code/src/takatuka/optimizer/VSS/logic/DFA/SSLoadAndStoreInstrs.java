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

import takatuka.verifier.logic.DFA.LoadAndStoreInstrs;
import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.verifier.dataObjs.*;
import takatuka.verifier.dataObjs.attribute.*;
import takatuka.verifier.logic.*;
import takatuka.verifier.logic.exception.*;
import takatuka.verifier.logic.factory.*;

/**
 * <p>Title: </p>
 * <p>Description:
 * To verifiy load and store related instructions.
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class SSLoadAndStoreInstrs extends LoadAndStoreInstrs {

    private static final SSLoadAndStoreInstrs myObj = new SSLoadAndStoreInstrs();

    /**
     * constructor is private
     */
    protected SSLoadAndStoreInstrs() {
    }

    public static LoadAndStoreInstrs getInstanceOf(Frame frame,
            MethodInfo currentMethod,
            Vector methodCallingParameters) {
        init(frame, currentMethod, methodCallingParameters);
        return myObj;
    }

    @Override
    protected void loadVarInstruction(VerificationInstruction inst, int type,
            boolean isReference, int index) {
        Type loadType = null;
        if (index == -1) {
            index = inst.getOperandsData().intValueUnsigned();
        }
        int blockSize = Type.getBlocks(type, isReference);
        loadType = localVar.get(index);
        //The following check are the reason to overwrite this function.
        if (isReference && !loadType.isReference() || (blockSize != loadType.getBlocks())) {
            throw new VerifyErrorExt(Messages.STACK_INVALID + " " + inst);
        }
        stack.push((Type) loadType.clone());
    }

    /**
     *   blockSize, int (i) ==> blockSize
     */
    @Override
    protected void loadArrayInstruction(VerificationInstruction inst, int type,
            boolean isReference, VerificationInstruction currentInstr) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        Type index = stack.pop();
        Type arrayref = stack.pop();
        ConvertTypeUtil ct = ConvertTypeUtil.getInstanceOf(currentMethod);

        if (index.isIntOrShortOrByteOrBooleanOrCharType()) {
            ct.convertTypes(index, 0, inst);
        }
        if (!ConvertTypeUtil.passIntegerTest(index) /*|| !arrayref.isArrayReference() todo */) {
            throw new VerifyErrorExt(Messages.STACK_INVALID);
        }


        stack.push(frameFactory.createType(type, isReference, 0));
    }

    @Override
    protected void storeVarInstruction(VerificationInstruction inst, int type,
            boolean isReference, int index) {

        if (index == -1) {
            index = inst.getOperandsData().intValueUnsigned();
        }

        Type storeType = stack.pop();
        int blockSize = Type.getBlocks(type, isReference);
        if (isReference && !storeType.isReference() || (blockSize != storeType.getBlocks())) {
            if (!storeType.isReference() && storeType.isIntOrShortOrByteOrBooleanOrCharType()) {
                ConvertTypeUtil ct = ConvertTypeUtil.getInstanceOf(currentMethod);
                //Miscellaneous.println("stop here");
                ct.convertTypeBasedOnLVIndex(storeType, index, inst);
            } else {
                throw new VerifyErrorExt(Messages.STACK_INVALID + ", " + inst + ", " + storeType + ", " + storeType.getBlocks() + " , " + blockSize);
            }
        }
        localVar.set(index, storeType);
    }

    /**
     *    ... ,arrayref, index, value ==> ...
     *    VSet(value (index)
     */
    @Override
    protected void storeArrayInstruction(VerificationInstruction inst, int type,
            boolean isReference, VerificationInstruction instr) {

        //pop values
        Type value = stack.pop();
        Type index = stack.pop();
        Type arrayref = stack.pop();

        if (/*!arrayref.isArrayReference() todo later
                || */!index.isIntOrShortOrByteOrBooleanOrCharType()
                || (value.isReference() != isReference)
                || (!value.isIntOrShortOrByteOrBooleanOrCharType()
                && value.getBlocks() != Type.getBlocks(type, isReference))) {
            throw new VerifyErrorExt(Messages.STACK_INVALID);
        }
        ConvertTypeUtil ct = ConvertTypeUtil.getInstanceOf(currentMethod);
        //convert type of array index to integer
        if (index.getType() != Type.INTEGER) {
            ct.convertTypes(index, 1, inst);
        }
        //convert type of array value.
        if (Type.getBlocks(type, isReference) != value.getBlocks()) {
            if (!value.isIntOrShortOrByteOrBooleanOrCharType()) {
                throw new VerifyErrorExt(Messages.STACK_INVALID);
            } else {
                ct.convertTypes(value, type, 0, inst);
            }
        }
    }
}
