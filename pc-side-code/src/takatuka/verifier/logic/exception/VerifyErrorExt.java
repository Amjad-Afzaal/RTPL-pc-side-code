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
package takatuka.verifier.logic.exception;

import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.attribute.*;
import takatuka.optimizer.cpGlobalization.logic.util.Oracle;
import takatuka.verifier.logic.factory.*;
import takatuka.verifier.logic.DFA.DataFlowAnalyzer;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class VerifyErrorExt extends VerifyError {


    public VerifyErrorExt() {
        super();
    }

    public VerifyErrorExt(String s) {
        super(s + ", at class: "
                + ClassFile.currentClassToWorkOn.getFullyQualifiedClassName()
                + ", method="
                + Oracle.getInstanceOf().getMethodOrFieldString(DataFlowAnalyzer.
                getLastSavedBytecodeInterpreter().getMethodInfo())+
                ", offSet="+DataFlowAnalyzer.
                getLastSavedBytecodeInterpreter().getCurrentPC()+""
                + ", instruction="+DataFlowAnalyzer.
                getLastSavedBytecodeInterpreter().getCurrentInstr().getInstructionId()+
                ":"+DataFlowAnalyzer.
                getLastSavedBytecodeInterpreter().getCurrentInstr().getMnemonic());
    }

    public VerifyErrorExt(String s, boolean addClassName) {
        super(s + (addClassName ? (", at class: "
                + ClassFile.currentClassToWorkOn.getFullyQualifiedClassName())
                : ""));
    }

    public VerifyErrorExt(String s, String methodName, Instruction inst) {
        super(s + ", at method name=" + methodName + ", instruction: " + inst
                + ", class: " + ClassFile.currentClassToWorkOn.getFullyQualifiedClassName());

    }

    public VerifyErrorExt(String s, int methodNo, Instruction inst) {
        super(s + ", at method #: " + methodNo + ", instruction: " + inst
                + ", class: " + ClassFile.currentClassToWorkOn.getFullyQualifiedClassName());
    }
}
