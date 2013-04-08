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
import takatuka.classreader.logic.util.*;
import takatuka.offlineGC.DFA.dataObjs.*;
import takatuka.optimizer.VSS.logic.DFA.*;
import takatuka.verifier.dataObjs.*;
import takatuka.verifier.dataObjs.attribute.*;
import takatuka.verifier.logic.*;
import takatuka.verifier.logic.exception.*;

/**
 * <p>Title: </p>
 * <p>Description:
 * To verifiy load and store related instructions.
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class GCLoadAndStoreInstrs extends SSLoadAndStoreInstrs {

    private static final GCLoadAndStoreInstrs myObj = new GCLoadAndStoreInstrs();

    /**
     * constructor is private
     */
    protected GCLoadAndStoreInstrs() {
    }

    public static GCLoadAndStoreInstrs getInstanceOf(Frame frame,
            MethodInfo currentMethod,
            Vector methodCallingParameters) {
        init(frame, currentMethod, methodCallingParameters);
        return myObj;
    }

    /**
     *   fieldType, int (i) ==> fieldType
     */
    @Override
    protected void loadArrayInstruction(VerificationInstruction inst, int fieldType,
            boolean isReference, VerificationInstruction currentInstr) {
        if (!isReference) {
            super.loadArrayInstruction(inst, fieldType, isReference, currentInstr);
            return;
        }
        HeapUtil heapUtil = HeapUtil.getInstanceOf(methodCallingParameters, currentMethod, currentInstr);
        GCType index = (GCType) stack.pop();
        GCType arrayref = (GCType) stack.pop();
        ConvertTypeUtil ct = ConvertTypeUtil.getInstanceOf(currentMethod);
        if (index.getType() != Type.INTEGER) {
            ct.convertTypes(index, 0, inst);
        }
        if (!index.isIntOrShortOrByteOrBooleanOrCharType() /*|| !arrayref.isArrayReference()todo */) {
            throw new VerifyErrorExt(Messages.STACK_INVALID);
        }
        heapUtil.getFieldAndPushOnStack(-1, arrayref, true, stack);
    }

    /**
     *    ... ,arrayref, index, value ==> ...
     *    VSet(value (index)
     * The function works without index. It means it ignore the array index.
     */
    @Override
    protected void storeArrayInstruction(VerificationInstruction inst, int type,
            boolean isReference, VerificationInstruction currentInstr) {
        try {
            if (!isReference) {
                super.storeArrayInstruction(inst, type, isReference, currentInstr);
                return;
            }
            //pop values
            GCType value = (GCType) stack.pop();
            GCType index = (GCType) stack.pop();
            GCType arrayref = (GCType) stack.pop();
            ConvertTypeUtil ct = ConvertTypeUtil.getInstanceOf(currentMethod);
            if (index.getType() != Type.INTEGER) {
                ct.convertTypes(index, 1, inst);
            }
            if (/*todo later !arrayref.isArrayReference() ||*/ !index.isIntOrShortOrByteOrBooleanOrCharType()) {
                throw new VerifyErrorExt(Messages.STACK_INVALID + ", "+GCType.typeToString(type));
            }
            HeapUtil heapUtil = HeapUtil.getInstanceOf(methodCallingParameters, currentMethod, currentInstr);
            heapUtil.saveInHeap(value, arrayref, -1);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
    }
}
