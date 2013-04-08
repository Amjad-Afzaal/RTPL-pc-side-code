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

import takatuka.verifier.logic.DFA.BytecodeVerifier;
import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.verifier.dataObjs.*;
import takatuka.verifier.dataObjs.attribute.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 * This class process an instruction stack and localVariables as described below.
 *
 * 2. Model the effect of the instruction on the operand stack and local variable array by doing the following:
 * (A) If the instruction uses values from the operand stack, ensure that there are a sufficient number of values
on the stack and that the top values on the stack are of an appropriate type. Otherwise, verification fails.
 * (B) If the instruction uses a local variable, ensure that the specified local variable contains a value of the
appropriate type. Otherwise, verification fails.
 * (C) If the instruction pushes values onto the operand stack, ensure that there is sufficient room on the operand
stack for the new values. Add the indicated types to the top of the modeled operand stack.
 * (D) If the instruction modifies a local variable, record that the local variable now contains the new type.
 *
 * TERMINOLOGY: I will use following terminology in function descriptions.
 * A, B, .. ==> C, D means an instruction pop A, B and push C,D. Here A, B will be types.
 * VGet(i, j, ...) means that an instruction get from local variables at location i, j
 * VSet(X (i),Y (j), ...) meaans that an instruction set local variables at index i, j to types X, Y respectively
 *
 * Types are defined in class Type. In case of reference the classInfo index is used, with a
 * special bit telling that it is a reference (so that we do not mix reference with other normal types)
 *
 * @author Faisal Aslam
 * @version 1.0
 */
public class SSBytecodeVerifier extends BytecodeVerifier {

    public SSBytecodeVerifier(Frame frame, MethodInfo currentMethod, Vector methodCallingPara) {
        super(frame, currentMethod, methodCallingPara);
    }

    @Override
    public Vector execute(VerificationInstruction inst,
            int currentPC, OperandStack parentFunctionStack) throws Exception {
        return super.execute(inst, currentPC, parentFunctionStack);
    }
}
