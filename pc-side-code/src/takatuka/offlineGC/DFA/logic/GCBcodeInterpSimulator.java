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
import takatuka.verifier.dataObjs.*;
import takatuka.verifier.dataObjs.attribute.*;
import takatuka.optimizer.VSS.logic.DFA.*;
import takatuka.verifier.logic.DFA.DataFlowAnalyzer;

/**
 * 
 * Description:
 * <p>
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class GCBcodeInterpSimulator extends SSBytecodeVerifier {

    public GCBcodeInterpSimulator(Frame frame, MethodInfo method, Vector callingParamters) {
        super(frame, method, callingParamters);
    }


    /**
     * 
     * @param inst
     * @param currentPC
     * @param callerStack
     * @return
     * @throws Exception
     */
    @Override
    public Vector execute(VerificationInstruction inst, int currentPC,
            OperandStack callerStack) throws Exception {
        //DataFlowAnalyzer.shouldDebugPrint = true;
        return super.execute(inst, currentPC, callerStack);
    }
}

