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
import takatuka.verifier.dataObjs.*;
import takatuka.verifier.dataObjs.attribute.*;
import takatuka.verifier.logic.*;
import takatuka.verifier.logic.exception.*;
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
public class SSIfAndCmpInstrs extends IfAndCmpInstrs {

    private static final SSIfAndCmpInstrs myObj = new SSIfAndCmpInstrs();

    protected SSIfAndCmpInstrs() {
    }

    public static SSIfAndCmpInstrs getInstanceOf(Vector<Long> nextPossibleInstructionsIds,
            OperandStack stack, MethodInfo currentMethod) {
        init(nextPossibleInstructionsIds, stack, currentMethod);
        return myObj;
    }

    /**
     * ..., type, type ==> ...
     */
    @Override
    protected long ifCmdInstruction(VerificationInstruction inst,
            boolean isReference, boolean ifCmd) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        Type val1 = stack.pop();
        Type val2 = frameFactory.createType(Type.INTEGER, isReference, -1);
        if (ifCmd) {
            val2 = stack.pop();
        }
        //if both are references.
        if (isReference && (!val1.isReference() || !val2.isReference())) {
            throw new VerifyErrorExt(Messages.STACK_INVALID + ", value 1=" + val1 + ", value 2=" + val2 + ", " + inst);
        } else if (!isReference
                && (val1.getType() != Type.INTEGER || val2.getType() != Type.INTEGER)) {
            ConvertTypeUtil ct = ConvertTypeUtil.getInstanceOf(currentMethod);

            if (val1.isIntOrShortOrByteOrBooleanOrCharType() && val1.getType() != Type.INTEGER) {
                ct.convertTypes(val1, 0, inst);
            }
            if (val2.isIntOrShortOrByteOrBooleanOrCharType() && val2.getType() != Type.INTEGER) {
                ct.convertTypes(val2, 1, inst);
            }
            if (!ConvertTypeUtil.passIntegerTest(val2) || !ConvertTypeUtil.passIntegerTest(val1)) {
                throw new VerifyErrorExt(Messages.STACK_INVALID + ", value 1=" + val1 + ", value 2=" + val2);
            }

        }
        return this.getTargetInstrIds(inst).elementAt(0);
    }

    /**
     * ..., int (index) ==> ...
     */
    @Override
    protected void switchInstruction(VerificationInstruction inst,
            boolean isLookupSwitch) {
        Type index = stack.pop();
        ConvertTypeUtil ct = ConvertTypeUtil.getInstanceOf(currentMethod);


        if (index.isIntOrShortOrByteOrBooleanOrCharType()) {
            ct.convertTypes(index, 0, inst);
        }
        if (!ConvertTypeUtil.passIntegerTest(index)) {
            throw new VerifyErrorExt(Messages.STACK_INVALID + " " + inst);
        }
        nextPossibleInstructionsIds.clear();
        nextPossibleInstructionsIds.addAll(this.getTargetInstrIds(inst));
    }
}
